package io.crunch.rag;

import io.smallrye.mutiny.Multi;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestQuery;

@Path("/rag")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DocumentRetrieverResource {

    private final RagAssistant ragAssistant;

    public DocumentRetrieverResource(RagAssistant ragAssistant) {
        this.ragAssistant = ragAssistant;
    }

    /**
     * HTTP GET endpoint for retrieving an answer to a given natural language question.
     * <p>
     * The question is passed as a query parameter, and the response contains the generated answer
     * as a plain string.
     *
     * @param question the user's natural language question passed as a query parameter
     * @return a {@link Multi} containing the generated answer as a stream of strings.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Multi<String> retrieve(@RestQuery("question") String question) {
        return ragAssistant.answer(question);
    }
}
