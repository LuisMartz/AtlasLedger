package atlasledger.service;

import atlasledger.model.Orden;
import atlasledger.model.Producto;
import atlasledger.model.Proveedor;
import atlasledger.repository.OrderRepository;
import atlasledger.repository.ProductRepository;
import atlasledger.repository.ProviderRepository;
import atlasledger.utils.DBHelper;
import atlasledger.utils.Logger;
import atlasledger.utils.NetworkUtils;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class SyncService implements AutoCloseable {

    public enum SyncOperation {
        CREATE,
        UPDATE,
        DELETE
    }

    private final ProductRepository productRepository;
    private final ProviderRepository providerRepository;
    private final OrderRepository orderRepository;
    private final HttpClient httpClient;
    private final String apiBaseUrl;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final NetworkUtils networkUtils;

    public SyncService(String apiBaseUrl,
                       ProductRepository productRepository,
                       ProviderRepository providerRepository,
                       OrderRepository orderRepository,
                       NetworkUtils networkUtils) {
        this.apiBaseUrl = apiBaseUrl.endsWith("/") ? apiBaseUrl.substring(0, apiBaseUrl.length() - 1) : apiBaseUrl;
        this.productRepository = productRepository;
        this.providerRepository = providerRepository;
        this.orderRepository = orderRepository;
        this.networkUtils = networkUtils;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(networkUtils.defaultTimeout())
            .executor(executor)
            .build();
    }

    public void enqueueChange(String entity, String reference, String payload, SyncOperation operation) {
        String sql = """
            INSERT INTO sync_queue (entidad, referencia, payload, operacion)
            VALUES (?, ?, ?, ?)
        """;
        try (Connection conn = DBHelper.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity);
            ps.setString(2, reference);
            ps.setString(3, payload);
            ps.setString(4, operation.name());
            ps.executeUpdate();
        } catch (SQLException e) {
            Logger.error(SyncService.class, "Error registrando cambio para sincronizar", e);
        }
    }

    public void pushPendingAsync(Consumer<Boolean> callback) {
        CompletableFuture
            .supplyAsync(this::pushPendingInternal, executor)
            .whenComplete((result, throwable) -> {
                if (callback != null) {
                    callback.accept(throwable == null && Boolean.TRUE.equals(result));
                }
            });
    }

    public boolean pushPendingInternal() {
        if (!networkUtils.isOnline()) {
            Logger.warn(SyncService.class, "Sin conexion para sincronizar cambios pendientes.");
            return false;
        }

        List<QueuedChange> pendientes = obtenerPendientes(25);
        for (QueuedChange cambio : pendientes) {
            if (enviarCambio(cambio)) {
                marcarCompletado(cambio.id());
            } else {
                incrementarIntentos(cambio.id());
            }
        }
        return true;
    }

    public void pullUpdates() {
        if (!networkUtils.isOnline()) {
            Logger.warn(SyncService.class, "Sin conexion. Pull remoto omitido.");
            return;
        }

        Map<String, Consumer<List<Map<String, Object>>>> handlers = Map.of(
            "productos", this::actualizarProductos,
            "proveedores", this::actualizarProveedores,
            "ordenes", this::actualizarOrdenes
        );

        handlers.forEach((entity, handler) -> {
            Optional<List<Map<String, Object>>> response = solicitarEntidad(entity);
            response.ifPresent(handler);
        });
    }

    private List<QueuedChange> obtenerPendientes(int limit) {
        List<QueuedChange> cambios = new ArrayList<>();
        String sql = """
            SELECT id, entidad, referencia, payload, operacion
            FROM sync_queue
            WHERE estado = 'PENDING'
            ORDER BY creado_en ASC
            LIMIT ?
        """;
        try (Connection conn = DBHelper.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cambios.add(new QueuedChange(
                        rs.getInt("id"),
                        rs.getString("entidad"),
                        rs.getString("referencia"),
                        rs.getString("payload"),
                        SyncOperation.valueOf(rs.getString("operacion"))
                    ));
                }
            }
        } catch (SQLException e) {
            Logger.error(SyncService.class, "Error obteniendo cola de sincronizacion", e);
        }
        return cambios;
    }

    private boolean enviarCambio(QueuedChange cambio) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(apiBaseUrl + "/sync/" + cambio.entidad()))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(cambio.payload()))
            .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() >= 200 && response.statusCode() < 300;
        } catch (Exception e) {
            Logger.error(SyncService.class, "Error enviando cambio de sincronizacion", e);
            return false;
        }
    }

    private void marcarCompletado(int id) {
        String sql = "UPDATE sync_queue SET estado = 'DONE', ultimo_intento = ?, intentos = intentos + 1 WHERE id = ?";
        try (Connection conn = DBHelper.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, Instant.now().toString());
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            Logger.error(SyncService.class, "Error marcando sincronizacion como completada", e);
        }
    }

    private void incrementarIntentos(int id) {
        String sql = "UPDATE sync_queue SET intentos = intentos + 1, ultimo_intento = ? WHERE id = ?";
        try (Connection conn = DBHelper.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, Instant.now().toString());
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            Logger.error(SyncService.class, "Error incrementando intentos de sincronizacion", e);
        }
    }

    private Optional<List<Map<String, Object>>> solicitarEntidad(String entity) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(apiBaseUrl + "/sync/" + entity))
            .GET()
            .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return Optional.of(networkUtils.parseJsonArray(response.body()));
            }
        } catch (Exception e) {
            Logger.error(SyncService.class, "Error solicitando datos remotos", e);
        }
        return Optional.empty();
    }

    private void actualizarProductos(List<Map<String, Object>> payloads) {
        payloads.stream()
            .map(networkUtils::mapToProducto)
            .forEach(productRepository::save);
    }

    private void actualizarProveedores(List<Map<String, Object>> payloads) {
        payloads.stream()
            .map(networkUtils::mapToProveedor)
            .forEach(providerRepository::save);
    }

    private void actualizarOrdenes(List<Map<String, Object>> payloads) {
        payloads.stream()
            .map(networkUtils::mapToOrden)
            .forEach(orderRepository::save);
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }

    private record QueuedChange(int id, String entidad, String referencia, String payload, SyncOperation operacion) {
    }
}
