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

    /** The vector-based embedding store backed by PostgreSQL + pgvector extension. */
    private final PgVectorEmbeddingStore store;

    /**
     * Represents a model that can convert a given text into an embedding (vector representation of the text).
     * We use nomic-embed-text model, a high-performing open embedding model with a large token context window.
     */
    private final EmbeddingModel model;

    /** The maximum number of retrieved embeddings when querying for relevant documents. */
    private final int maxResults;

    /** The maximum length (in characters) of a document segment used during ingestion. */
    private final int maxSegmentSize;

    /**
     * The maximum number of characters that can overlap between two segments.
     * Helps preserve semantic continuity between segments while ensuring sentence boundaries.
     */
    private final int maxOverlapSize;

    /**
     * The minimum cosine similarity score for a document to be considered relevant during retrieval.
     * Score ranges between 0 (no similarity) and 1 (identical).
     */
    private final double minScore;

    public EmbeddingProvider(PgVectorEmbeddingStore store, EmbeddingModel model,
                             @ConfigProperty(name = "rag.embedding.max-segment-size", defaultValue = "550") int maxSegmentSize,
                             @ConfigProperty(name = "rag.embedding.max-overlap-size", defaultValue = "25") int maxOverlapSize,
                             @ConfigProperty(name = "rag.retrieval.max-results", defaultValue = "200") int maxResults,
                             @ConfigProperty(name = "rag.retrieval.min-score", defaultValue = "0.8") double minScore) {
        this.store = store;
        this.model = model;
        this.maxResults = maxResults;
        this.minScore = minScore;
        this.maxSegmentSize = maxSegmentSize;
        this.maxOverlapSize = maxOverlapSize;
    }

    /**
     * Produces an {@link EmbeddingStoreIngestor} bean for ingestion of documents into the embedding store.
     * <p>
     * The ingestor splits documents recursively using sentence boundaries,
     * creates embeddings using the configured embedding model, and stores them in the PgVector store.
     *
     * @return an {@link EmbeddingStoreIngestor} instance configured with the current embedding model and store.
     */
    @Produces
    public EmbeddingStoreIngestor createIngestor() {
        return EmbeddingStoreIngestor.builder()
                .embeddingStore(store)
                .embeddingModel(model)
                .documentSplitter(recursive(maxSegmentSize, maxOverlapSize))
                .build();
    }

    /**
     * Produces a {@link DocumentParser} bean that uses Apache Tika to extract text content from various document formats.
     * <p>
     * Tika is configured to extract both main content and metadata, if available.
     *
     * @return a new instance of {@link ApacheTikaDocumentParser}.
     */
    @Produces
    public DocumentParser createDocumentParser() {
        return new ApacheTikaDocumentParser(true);
    }

    /**
     * Produces an {@link EmbeddingStoreContentRetriever} bean for retrieving content from the embedding store
     * based on semantic similarity to a query.
     * <p>
     * This retriever uses the configured embedding model and store, and applies filtering based on
     * maximum number of results and a similarity score threshold.
     *
     * @return a configured {@link EmbeddingStoreContentRetriever} instance.
     */
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
