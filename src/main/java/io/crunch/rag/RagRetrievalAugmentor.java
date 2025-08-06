package io.crunch.rag;

import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.function.Supplier;

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
