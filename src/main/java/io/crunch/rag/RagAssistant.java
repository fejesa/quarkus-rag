package io.crunch.rag;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.smallrye.mutiny.Multi;

/**
 * {@code RagAssistant} defines a Quarkus AI service interface for performing Retrieval-Augmented Generation (RAG) queries.
 * <p>
 * This interface is registered as an AI service via the
 * {@link RegisterAiService @RegisterAiService} annotation and is backed by a
 * {@link RagRetrievalAugmentor} to enhance responses with relevant contextual data retrieved
 * from an external source or knowledge base.
 * </p>
 * Key Characteristics
 * <ul>
 *   <li><strong>RAG Integration:</strong> Uses {@code RagRetrievalAugmentor} to perform
 *       context retrieval before generating answers, enabling more accurate and context-aware responses.</li>
 *   <li><strong>Streaming Output:</strong> The response type is {@link Multi}&lt;{@link String}&gt;,
 *       which allows partial responses (chunks) to be streamed to the client as they are produced by the LLM.</li>
 * </ul>
 * @see RagRetrievalAugmentor
 * @see RegisterAiService
 * @see Multi
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
    Multi<String> answer(@UserMessage String question);
}
