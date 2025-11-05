package atlasledger.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class Orden {
    public enum Estado {
        BORRADOR,
        APROBADA,
        ENVIADA,
        RECIBIDA,
        CANCELADA
    }

    private int id;
    private String codigo;
    private LocalDate fecha;
    private String proveedorCodigo;
    private double total;
    private Estado estado;
    private LocalDateTime actualizadoEn;

    public Orden() {
    }

    public Orden(String codigo, LocalDate fecha, String proveedorCodigo, double total, Estado estado) {
        this.codigo = codigo;
        this.fecha = fecha;
        this.proveedorCodigo = proveedorCodigo;
        this.total = total;
        this.estado = estado;
    }

    public Orden(int id, String codigo, LocalDate fecha, String proveedorCodigo, double total, Estado estado, LocalDateTime actualizadoEn) {
        this.id = id;
        this.codigo = codigo;
        this.fecha = fecha;
        this.proveedorCodigo = proveedorCodigo;
        this.total = total;
        this.estado = estado;
        this.actualizadoEn = actualizadoEn;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getProveedorCodigo() {
        return proveedorCodigo;
    }

    public void setProveedorCodigo(String proveedorCodigo) {
        this.proveedorCodigo = proveedorCodigo;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public LocalDateTime getActualizadoEn() {
        return actualizadoEn;
    }

    public void setActualizadoEn(LocalDateTime actualizadoEn) {
        this.actualizadoEn = actualizadoEn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Orden orden)) {
            return false;
        }
        return Objects.equals(codigo, orden.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(codigo);
    }

    @Override
    public String toString() {
        return codigo + " - " + estado;
    }
}
