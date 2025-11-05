package atlasledger.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Informe {
    public enum Tipo {
        INVENTARIO,
        COMPRAS,
        PROVEEDORES,
        PERSONALIZADO
    }

    private int id;
    private String nombre;
    private Tipo tipo;
    private String definicionJson;
    private LocalDateTime generadoEn;

    public Informe() {
    }

    public Informe(String nombre, Tipo tipo, String definicionJson) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.definicionJson = definicionJson;
    }

    public Informe(int id, String nombre, Tipo tipo, String definicionJson, LocalDateTime generadoEn) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.definicionJson = definicionJson;
        this.generadoEn = generadoEn;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }

    public String getDefinicionJson() {
        return definicionJson;
    }

    public void setDefinicionJson(String definicionJson) {
        this.definicionJson = definicionJson;
    }

    public LocalDateTime getGeneradoEn() {
        return generadoEn;
    }

    public void setGeneradoEn(LocalDateTime generadoEn) {
        this.generadoEn = generadoEn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Informe informe)) {
            return false;
        }
        return Objects.equals(nombre, informe.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(nombre);
    }

    @Override
    public String toString() {
        return nombre + " [" + tipo + "]";
    }
}
