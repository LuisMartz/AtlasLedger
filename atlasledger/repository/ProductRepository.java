package atlasledger.repository;

import atlasledger.dao.ProductoDao;
import atlasledger.model.Producto;
import java.util.List;
import java.util.Optional;

public class ProductRepository {

    public List<Producto> findAll() {
        return ProductoDao.listar();
    }

    public Optional<Producto> findByCode(String codigo) {
        return ProductoDao.buscarPorCodigo(codigo);
    }

    public void save(Producto producto) {
        ProductoDao.guardar(producto);
    }

    public void deleteById(int id) {
        ProductoDao.eliminar(id);
    }

    public void deleteByCode(String codigo) {
        ProductoDao.buscarPorCodigo(codigo).ifPresent(producto -> ProductoDao.eliminar(producto.getId()));
    }
}
