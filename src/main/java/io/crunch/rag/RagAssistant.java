package io.crunch.rag;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

/**
 * {@code RagAssistant} defines a conversational AI interface for answering user queries, powered by Retrieval-Augmented Generation (RAG).
 * <p>
 * This interface is annotated with {@link RegisterAiService}, making it a Quarkus-managed AI service
 * that automatically integrates with the LangChain4j runtime. It is configured to use a custom
 * {@code RetrievalAugmentor} implementation ({@link RagRetrievalAugmentor}) to inject contextually relevant
 * documents during prompt construction.
 *
 * <p>Features
 * <ul>
 *   <li>Leverages document embeddings via RAG to enhance the accuracy and contextuality of answers</li>
 *   <li>Supports declarative prompt configuration using LangChain4j annotations</li>
 * </ul>
 *
 * This interface is used by simply injecting it where needed in your Quarkus application:
 * <pre>{@code
 * @Inject
 * RagAssistant assistant;
 *
 * String answer = assistant.answer("How does DNS work?");
 * }</pre>
 * @apiNote Defining the retrieval augmentor is not required if there is only one registered in the application.
 */
@RegisterAiService(retrievalAugmentor = RagRetrievalAugmentor.class)
public interface RagAssistant {

    @SystemMessage(
            """
    You are an AI assistant specialized in the Information Technology (IT) domain. Follow these instructions when responding to user queries:
    - Act as a knowledgeable IT specialist with expertise across all areas of information technology, including but not limited to:
      - Software development
      - Networking
      - Databases
      - Cybersecurity
      - Cloud computing
      - IT architecture
    - Provide detailed explanations and precise answers to any question that is related to the IT domain or the content of the loaded documents.
    """
    )
    String answer(@UserMessage String question);
}
