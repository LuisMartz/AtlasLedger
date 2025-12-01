package atlasledger.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Proveedor {
    private int id;
    private String codigo;
    private String nombre;
    private String email;
    private String telefono;
    private String direccion;
    private LocalDateTime actualizadoEn;

    public Proveedor() {
    }

    public Proveedor(String codigo, String nombre, String email, String telefono, String direccion) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.direccion = direccion;
    }

    public Proveedor(int id, String codigo, String nombre, String email, String telefono, String direccion, LocalDateTime actualizadoEn) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.direccion = direccion;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
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
        if (!(o instanceof Proveedor proveedor)) {
            return false;
        }
        return Objects.equals(codigo, proveedor.codigo);
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
