package io.crunch.rag;

import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService(retrievalAugmentor = RagRetrievalAugmentor.class)
public interface RagAssistant {

    // TODO: Add system prompt for the assistant
    @UserMessage("{{question}}")
    String answer(String question);
}
