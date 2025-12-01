package atlasledger.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Producto {
    private int id;
    private String codigo;
    private String nombre;
    private String categoria;
    private String proveedorCodigo;
    private int stock;
    private double coste;
    private double precio;
    private LocalDateTime actualizadoEn;

    public Producto() {
    }

    public Producto(String codigo, String nombre, String categoria, String proveedorCodigo, int stock, double coste, double precio) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.categoria = categoria;
        this.proveedorCodigo = proveedorCodigo;
        this.stock = stock;
        this.coste = coste;
        this.precio = precio;
    }

    public Producto(int id, String codigo, String nombre, String categoria, String proveedorCodigo, int stock, double coste, double precio, LocalDateTime actualizadoEn) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.categoria = categoria;
        this.proveedorCodigo = proveedorCodigo;
        this.stock = stock;
        this.coste = coste;
        this.precio = precio;
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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getProveedorCodigo() {
        return proveedorCodigo;
    }

    public void setProveedorCodigo(String proveedorCodigo) {
        this.proveedorCodigo = proveedorCodigo;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public double getCoste() {
        return coste;
    }

    public void setCoste(double coste) {
        this.coste = coste;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public LocalDateTime getActualizadoEn() {
        return actualizadoEn;
    }

    public void setActualizadoEn(LocalDateTime actualizadoEn) {
        this.actualizadoEn = actualizadoEn;
    }

    public double getMargen() {
        return precio - coste;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Producto producto)) {
            return false;
        }
        return Objects.equals(codigo, producto.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(codigo);
    }

    @Override
    public String toString() {
        return nombre + " (" + codigo + ")";
    }
}

