package io.crunch.rag;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

public class RagTestProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
                "quarkus.langchain4j.ollama.chat-model.model-id", "tinyllama");
    }
}
