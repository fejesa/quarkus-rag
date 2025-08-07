package io.crunch.rag;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestQuery;
import org.jboss.resteasy.reactive.RestResponse;

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
     * @return a {@link RestResponse} containing the generated answer
     */
    @GET
    public RestResponse<String> retrieve(@RestQuery String question) {
        var answer = ragAssistant.answer(question);
        return RestResponse.ok(answer);
    }
}
