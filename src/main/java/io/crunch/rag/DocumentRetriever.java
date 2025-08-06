package io.crunch.rag;

import jakarta.inject.Inject;
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
public class DocumentRetriever {

    @Inject
    RagAssistant ragAssistant;

    @GET
    public RestResponse<String> retrieve(@RestQuery String question) {
        var answer = ragAssistant.answer(question);
        return RestResponse.ok(answer);
    }
}
