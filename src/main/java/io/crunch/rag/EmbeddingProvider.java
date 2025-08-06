package io.crunch.rag;

import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import io.quarkiverse.langchain4j.pgvector.PgVectorEmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import static dev.langchain4j.data.document.splitter.DocumentSplitters.recursive;

@ApplicationScoped
public class EmbeddingProvider {

    private final PgVectorEmbeddingStore store;

    private final EmbeddingModel model;

    private final int maxResults;

    private final double minScore;

    public EmbeddingProvider(PgVectorEmbeddingStore store, EmbeddingModel model,
                             @ConfigProperty(name = "rag.retrieval.max-results", defaultValue = "15") int maxResults,
                             @ConfigProperty(name = "rag.retrieval.min-score", defaultValue = "0.75") double minScore) {
        this.store = store;
        this.model = model;
        this.maxResults = maxResults;
        this.minScore = minScore;
    }

    @Produces
    public EmbeddingStoreIngestor createIngestor() {
        return EmbeddingStoreIngestor.builder()
                .embeddingStore(store)
                .embeddingModel(model)
                .documentSplitter(recursive(550, 25))
                .build();
    }

    @Produces
    public DocumentParser createDocumentParser() {
        return new ApacheTikaDocumentParser(true);
    }

    @Produces
    public EmbeddingStoreContentRetriever createContentRetriever() {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingModel(model)
                .embeddingStore(store)
                .maxResults(maxResults)
                .minScore(minScore)
                .build();
    }
}
