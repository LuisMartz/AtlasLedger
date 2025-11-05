package atlasledger.repository;

import atlasledger.dao.OrdenDao;
import atlasledger.model.Orden;
import java.util.List;
import java.util.Optional;

public class OrderRepository {

    public List<Orden> findAll() {
        return OrdenDao.listar();
    }

    public Optional<Orden> findByCode(String codigo) {
        return OrdenDao.buscarPorCodigo(codigo);
    }

    public void save(Orden orden) {
        OrdenDao.guardar(orden);
    }

    public void deleteById(int id) {
        OrdenDao.eliminar(id);
    }

    public void deleteByCode(String codigo) {
        OrdenDao.buscarPorCodigo(codigo).ifPresent(orden -> OrdenDao.eliminar(orden.getId()));
    }
}
