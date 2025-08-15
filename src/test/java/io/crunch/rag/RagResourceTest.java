package io.crunch.rag;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.awaitility.Durations;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@QuarkusTest
@TestProfile(RagTestProfile.class)
class RagResourceTest {

    @Test
    void whenAskLLMAboutQuarkusThenResponseContainsKeywords() {
        // Send a question: "What are the benefits of Quarkus?"
        await()
            .atMost(10, SECONDS)
            .pollInterval(Durations.TWO_SECONDS)
            .untilAsserted(() -> {
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
        });
    }
}
