package io.crunch.rag;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class RagResourceTest {

    @Test
    void whenAskLLMAboutQuarkusThenResponseContainsKeywords() {
        // Send a question: "What are the benefits of Quarkus?"
        var response = given()
            .when()
            .get("/rag?question=%22What+are+the+benefits+of+Quarkus%3F%22")
            .then()
            .statusCode(200)
            .contentType("text/plain").extract().response();
        assertThat(response.body().asString())
            .isNotNull()
            .isNotEmpty()
            .contains("Quarkus", "Kubernetes", "Java");
    }
}
