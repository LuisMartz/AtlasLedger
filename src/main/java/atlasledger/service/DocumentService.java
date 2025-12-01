package atlasledger.service;

import atlasledger.dao.DocumentQueueDao;
import atlasledger.model.DocumentTask;
import atlasledger.utils.Logger;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

public class DocumentService {

    private final Path storageRoot;

    public DocumentService(Path storageRoot) {
        this.storageRoot = storageRoot;
        try {
            Files.createDirectories(storageRoot);
        } catch (IOException e) {
            Logger.error(DocumentService.class, "No se pudo crear el directorio de documentos: " + storageRoot, e);
        }
    }

    public Path getStorageRoot() {
        return storageRoot;
    }

    public Path saveLocalCopy(Path source) throws IOException {
        Files.createDirectories(storageRoot);
        Path target = storageRoot.resolve(source.getFileName());
        int counter = 1;
        while (Files.exists(target)) {
            String fileName = source.getFileName().toString();
            String base = fileName;
            String extension = "";
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0) {
                base = fileName.substring(0, dotIndex);
                extension = fileName.substring(dotIndex);
            }
            target = storageRoot.resolve(base + "_" + counter++ + extension);
        }
        Files.copy(source, target);
        queueForUpload(target.getFileName().toString(), target, null);
        return target;
    }

    public void queueForUpload(String fileName, Path localPath, String metadata) {
        DocumentTask task = new DocumentTask();
        task.setFileName(fileName);
        task.setLocalPath(localPath.toString());
        task.setStatus("PENDING");
        task.setMetadata(metadata);
        DocumentQueueDao.enqueue(task);
    }

    public List<DocumentTask> pendingDocuments() {
        return DocumentQueueDao.pending();
    }

    public void markUploaded(DocumentTask task) {
        DocumentQueueDao.updateStatus(task.getId(), "DONE");
    }

    public void uploadPendingDocuments(Function<Path, Boolean> uploader) {
        List<DocumentTask> pending = pendingDocuments();
        for (DocumentTask task : pending) {
            Path path = Path.of(task.getLocalPath());
            if (!Files.exists(path)) {
                DocumentQueueDao.updateStatus(task.getId(), "MISSING");
                continue;
            }
            boolean ok = uploader.apply(path);
            DocumentQueueDao.updateStatus(task.getId(), ok ? "DONE" : "ERROR");
            if (ok) {
                task.setUploadedAt(LocalDateTime.now());
            }
        }
    }
}
