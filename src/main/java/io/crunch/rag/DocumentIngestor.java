package io.crunch.rag;

import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

import java.nio.file.Path;

@ApplicationScoped
public class DocumentIngestor {

    private final EmbeddingStoreIngestor embeddingStoreIngestor;

    private final DocumentParser documentParser;

    public DocumentIngestor(EmbeddingStoreIngestor embeddingStoreIngestor, DocumentParser documentParser) {
        this.embeddingStoreIngestor = embeddingStoreIngestor;
        this.documentParser = documentParser;
    }

    public void ingest(Path path) {
        var document = FileSystemDocumentLoader.loadDocument(path, documentParser);
        Log.info("Loaded document: " + document.metadata());
        embeddingStoreIngestor.ingest(document);
        Log.info("Document ingested successfully: " + document.metadata());
    }
}
