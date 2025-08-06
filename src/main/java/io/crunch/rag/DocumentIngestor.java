package io.crunch.rag;

import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.nio.file.FileSystems;
import java.nio.file.Path;

@ApplicationScoped
public class DocumentIngestor {

    @Inject
    EmbeddingStoreIngestor embeddingStoreIngestor;

    @Inject
    DocumentParser documentParser;

    public void ingest(Path path) {
        var document = FileSystemDocumentLoader.loadDocument(path, documentParser);
        Log.info("Loaded document: " + document.metadata());
        embeddingStoreIngestor.ingest(document);
        Log.info("Document ingested successfully: " + document.metadata());
    }

    public void ingest(@Observes StartupEvent ev, @ConfigProperty(name = "rag.location") Path path) {
        Log.info("Starting document ingestion from: " + path);

        var pathMatcher = FileSystems.getDefault().getPathMatcher("glob:**.pdf");
        var documents = FileSystemDocumentLoader.loadDocuments(path, pathMatcher, documentParser);
        documents.forEach(doc -> Log.info("Loaded document: " + doc.metadata()));

        embeddingStoreIngestor.ingest(documents);
        Log.info("Documents ingested successfully from path: " + path);
    }
}
