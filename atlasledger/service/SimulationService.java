package atlasledger.service;

import atlasledger.model.Orden;
import atlasledger.model.Producto;
import atlasledger.model.Proveedor;
import atlasledger.repository.OrderRepository;
import atlasledger.repository.ProductRepository;
import atlasledger.repository.ProviderRepository;
import atlasledger.simulation.PackagingEvent;
import atlasledger.simulation.PricePoint;
import atlasledger.simulation.TransportEvent;
import atlasledger.simulation.TransportMode;
import atlasledger.utils.Logger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

public class SimulationService {

    private final ProductRepository productRepository;
    private final ProviderRepository providerRepository;
    private final OrderRepository orderRepository;
    private final Random random = new Random();
    private final List<TransportMode> transportModes = new ArrayList<>();
    private final List<PricePoint> priceHistory = new ArrayList<>();
    private final Map<String, Double> simulatedPrices = new HashMap<>();

    public SimulationService(ProductRepository productRepository,
                             ProviderRepository providerRepository,
                             OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.providerRepository = providerRepository;
        this.orderRepository = orderRepository;
        seedTransportModes();
    }

    public void runDemoSimulation() {
        ensureProviders();
        createSampleProducts();
        createSampleOrders();
        simulatePackagingAndTransport();
        resetSimulatedPrices();
    }

    public List<TransportMode> getTransportModes() {
        return transportModes;
    }

    public void addTransportMode(TransportMode mode) {
        transportModes.add(mode);
        Logger.info(SimulationService.class, "Modo de transporte agregado: " + mode.getName());
    }

    public List<PricePoint> simulatePriceStep() {
        List<Producto> productos = productRepository.findAll();
        List<PricePoint> tick = new ArrayList<>();
        productos.forEach(prod -> {
            double basePrice = simulatedPrices.getOrDefault(prod.getCodigo(), prod.getPrecio());
            double drift = randomDouble(-0.05, 0.05); // +/-5%
            double newPrice = Math.max(0.5, basePrice * (1 + drift));
            simulatedPrices.put(prod.getCodigo(), newPrice);
            PricePoint point = new PricePoint(prod.getCodigo(), LocalDateTime.now(), newPrice);
            priceHistory.add(point);
            tick.add(point);
        });
        return tick;
    }

    public double getSimulatedPrice(String productCode) {
        return simulatedPrices.getOrDefault(
            productCode,
            productRepository.findByCode(productCode).map(Producto::getPrecio).orElse(0.0)
        );
    }

    public List<PricePoint> getPriceHistoryFor(String productCode, int limit) {
        List<PricePoint> filtered = priceHistory.stream()
            .filter(p -> p.getProductCode().equals(productCode))
            .toList();
        if (filtered.size() <= limit) {
            return filtered;
        }
        return filtered.subList(filtered.size() - limit, filtered.size());
    }

    private void ensureProviders() {
        if (!providerRepository.findAll().isEmpty()) {
            return;
        }
        IntStream.range(1, 4).forEach(i -> {
            Proveedor proveedor = new Proveedor(
                "PV-SIM-" + i,
                "Proveedor Sim " + i,
                "sim" + i + "@proveedor.com",
                "+34 600 00 0" + i,
                "Calle Simulada " + i
            );
            providerRepository.save(proveedor);
        });
    }

    private void createSampleProducts() {
        List<Proveedor> proveedores = providerRepository.findAll();
        for (int i = 1; i <= 5; i++) {
            Producto producto = new Producto(
                "SIM-P-" + i,
                "Producto Sim " + i,
                randomCategory(),
                proveedores.isEmpty() ? "" : proveedores.get(random.nextInt(proveedores.size())).getCodigo(),
                random.nextInt(50) + 10,
                randomDouble(5, 20),
                randomDouble(15, 50)
            );
            productRepository.save(producto);
        }
        Logger.info(SimulationService.class, "Productos de simulacion creados.");
    }

    private void createSampleOrders() {
        List<Proveedor> proveedores = providerRepository.findAll();
        for (int i = 1; i <= 3; i++) {
            String proveedorCodigo = proveedores.isEmpty() ? "PV-SIM-1" : proveedores.get(random.nextInt(proveedores.size())).getCodigo();
            Orden orden = new Orden(
                "SIM-OC-" + i,
                LocalDate.now().minusDays(random.nextInt(20)),
                proveedorCodigo,
                randomDouble(100, 500),
                Orden.Estado.APROBADA
            );
            orderRepository.save(orden);
        }
        Logger.info(SimulationService.class, "Ordenes de simulacion creadas.");
    }

    private void simulatePackagingAndTransport() {
        productRepository.findAll().stream().limit(3).forEach(prod -> {
            PackagingEvent packing = new PackagingEvent(
                prod.getCodigo(),
                "Lote-" + random.nextInt(999),
                "Empaquetado completado",
                LocalDateTime.now().minusHours(random.nextInt(24))
            );
            TransportEvent transport = new TransportEvent(
                "ENV-" + prod.getCodigo(),
                "Almacen Central",
                "Cliente Demo",
                "En ruta",
                LocalDateTime.now().plusHours(random.nextInt(48))
            );
            Logger.info(SimulationService.class,
                "Simulacion: " + packing.getStatus() + " para " + packing.getProductCode() + " | " +
                    "Transporte " + transport.getStatus() + " hacia " + transport.getDestination());
        });
    }

    private void seedTransportModes() {
        transportModes.add(new TransportMode("Terrestre", 0.8, 48, 0.05));
        transportModes.add(new TransportMode("Aereo", 2.5, 12, 0.02));
        transportModes.add(new TransportMode("Maritimo", 0.4, 96, 0.08));
    }

    private String randomCategory() {
        String[] categorias = {"General", "Tecnologia", "Oficina", "Consumibles"};
        return categorias[random.nextInt(categorias.length)];
    }

    public ProductRepository getProductRepository() {
        return productRepository;
    }

    public List<TransportEvent> simulateTransportJourney() {
        List<Producto> productos = productRepository.findAll();
        List<TransportEvent> events = new ArrayList<>();
        productos.stream().limit(5).forEach(prod -> {
            events.add(new TransportEvent(
                "ENV-" + prod.getCodigo(),
                "Almacen Central",
                "Cliente Demo",
                "Preparando",
                LocalDateTime.now().plusHours(random.nextInt(72))
            ));
            events.add(new TransportEvent(
                "ENV-" + prod.getCodigo(),
                "Almacen Central",
                "Cliente Demo",
                "En ruta",
                LocalDateTime.now().plusHours(random.nextInt(72))
            ));
            events.add(new TransportEvent(
                "ENV-" + prod.getCodigo(),
                "Almacen Central",
                "Cliente Demo",
                "Entregado",
                LocalDateTime.now().plusHours(random.nextInt(72))
            ));
        });
        if (events.isEmpty()) {
            events.add(new TransportEvent("ENV-DEMO", "Almacen Central", "Cliente Demo", "Preparando", LocalDateTime.now().plusHours(24)));
        }
        return events;
    }

    private double randomDouble(double min, double max) {
        return Math.round((min + (max - min) * random.nextDouble()) * 100.0) / 100.0;
    }

    private void resetSimulatedPrices() {
        simulatedPrices.clear();
        priceHistory.clear();
    }
}
