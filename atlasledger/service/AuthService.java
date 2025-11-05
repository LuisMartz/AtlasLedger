package atlasledger.service;

import atlasledger.dao.WorkerDao;
import atlasledger.model.Worker;
import atlasledger.utils.PasswordUtils;
import java.time.LocalDateTime;
import java.util.Optional;

public class AuthService {

    public Optional<Worker> authenticate(String username, String password) {
        return WorkerDao.findByUsername(username)
            .filter(worker -> PasswordUtils.matches(password, worker.getPasswordHash()))
            .map(worker -> {
                WorkerDao.updateLastLogin(worker.getId());
                worker.setLastLogin(LocalDateTime.now());
                return worker;
            });
    }

    public void ensureDefaultAdmin() {
        if (!WorkerDao.hasAnyWorker()) {
            Worker admin = new Worker();
            admin.setUsername("admin");
            admin.setPasswordHash(PasswordUtils.hash("admin"));
            admin.setNombre("Administrador");
            admin.setRol("ADMIN");
            admin.setLastLogin(LocalDateTime.now());
            WorkerDao.save(admin);
        }
    }
}
