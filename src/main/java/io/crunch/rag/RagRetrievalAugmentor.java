package io.crunch.rag;

import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.function.Supplier;

/**
 * A CDI-managed supplier for producing a configured {@link RetrievalAugmentor} instance
 * used in a Retrieval-Augmented Generation (RAG) pipeline.
 * <p>
 * This class wraps a {@link EmbeddingStoreContentRetriever} and builds a
 * {@link DefaultRetrievalAugmentor} to integrate content retrieval capabilities into
 * the RAG workflow. It can be injected wherever a {@code Supplier<RetrievalAugmentor>}
 * is needed in the application.
 * <p>
 * Typically, the retrieval augmentor is responsible for:
 * <ul>
 *     <li>Retrieving relevant documents from the embedding store based on a user query</li>
 *     <li>Augmenting the original query or prompt with the retrieved content before passing it to a language model</li>
 * </ul>
 */
@ApplicationScoped
public class RagRetrievalAugmentor implements Supplier<RetrievalAugmentor> {

    private final EmbeddingStoreContentRetriever contentRetriever;

    public RagRetrievalAugmentor(EmbeddingStoreContentRetriever contentRetriever) {
        this.contentRetriever = contentRetriever;
    }

    @Override
    public RetrievalAugmentor get() {
        return DefaultRetrievalAugmentor.builder()
                .contentRetriever(contentRetriever)
                .build();
    }
}
