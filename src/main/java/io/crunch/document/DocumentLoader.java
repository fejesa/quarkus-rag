package io.crunch.document;

import io.crunch.rag.DocumentIngestor;
import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * {@code DocumentLoader} is a Quarkus-managed background service that periodically scans a configured
 * folder for new document files and ingests them into a RAG (Retrieval-Augmented Generation) pipeline.
 * <p>
 * It uses a checksum-based mechanism to avoid reprocessing the same files and persists ingestion metadata
 * to a database using a {@link DocumentFile} JPA entity.
 *
 * <p>Responsibilities:
 * <ul>
 *     <li>Watch a folder for new documents</li>
 *     <li>Compute SHA-256 checksum to detect duplicates</li>
 *     <li>Trigger ingestion via {@link DocumentIngestor}</li>
 *     <li>Persist information about processed files</li>
 * </ul>
 *
 * <p>Configuration:
 * <ul>
 *     <li>{@code rag.document.location}: the absolute folder path to monitor for documents</li>
 *     <li>{@code rag.document.loader.scheduler.period}: scheduler interval (default: 60s)</li>
 * </ul>
 */
@ApplicationScoped
public class DocumentLoader {

    private final Path folderPath;

    private final DocumentIngestor documentIngestor;

    public DocumentLoader(@ConfigProperty(name = "rag.document.location") Path folderPath,
                          DocumentIngestor documentIngestor) {
        this.folderPath = Paths.get(".").resolve(folderPath).toAbsolutePath().normalize();
        this.documentIngestor = documentIngestor;
        Log.info("Document folder: " + this.folderPath);
    }

    @Scheduled(every = "{rag.document.loader.scheduler.period:60s}", delay = 2, delayUnit = TimeUnit.SECONDS, concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    public void load() {
        try (var files = Files.list(folderPath)) {
            files.filter(Files::isRegularFile).forEach(this::processFile);
        } catch (IOException e) {
            throw new RuntimeException("Error listing files in folder: " + folderPath, e);
        }
    }

    private void processFile(Path path) {
        try {
            var checksum = checksum(path);
            if (isNotProcessed(checksum)) {
                documentIngestor.ingest(path);
                saveDocumentFile(path, checksum);
                Log.info("Document file has been processed: " + path);
            }
        } catch (IOException e) {
            Log.error("Error processing document file: " + path, e);
        }
    }

    @Transactional
    public void saveDocumentFile(Path path, String checksum) {
        var documentFile = new DocumentFile();
        documentFile.setChecksum(checksum);
        documentFile.setFileName(path.getFileName().toString());
        documentFile.persistAndFlush();
    }

    private boolean isNotProcessed(String checksum) {
        return DocumentFile.findByChecksum(checksum).isEmpty();
    }

    public String checksum(Path path) throws IOException {
        try (var fis = new BufferedInputStream(new FileInputStream(path.toFile()))) {
            return DigestUtils.sha256Hex(fis);
        }
    }
}
