package atlasledger.repository;

import atlasledger.dao.ProveedorDao;
import atlasledger.model.Proveedor;
import java.util.List;
import java.util.Optional;

public class ProviderRepository {

    public List<Proveedor> findAll() {
        return ProveedorDao.listar();
    }

    public Optional<Proveedor> findByCode(String codigo) {
        return ProveedorDao.buscarPorCodigo(codigo);
    }

    public void save(Proveedor proveedor) {
        ProveedorDao.guardar(proveedor);
    }

    public void deleteById(int id) {
        ProveedorDao.eliminar(id);
    }
}
