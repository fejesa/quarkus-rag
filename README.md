# Building My Own Java-Based RAG Assistant with Quarkus, LangChain4j, and Ollama
About a year ago, I attended a talk at our Java User Group. It wasn’t just about Large Language Models (LLMs) — it was also about **Quarkus**, my favorite Java framework.
That presentation made me realize: the Java ecosystem is not just passively observing the AI revolution — it’s actively embracing it. Since then, **LangChain** has been integrated not only into the Spring ecosystem but also into Quarkus. That was the moment I decided: _I’m going to dive into LLMs_.

I had **zero** machine learning background. And here’s the good news — for many use cases, you don’t need it. My philosophy is simple: the best way to learn something new is by **building it**.

This experimental project is my personal journey into **Retrieval-Augmented Generation (RAG)** in Java, powered by **LangChain4j**, **Quarkus**, and **Ollama**.

## The Problem I Wanted to Solve
Over the years, I’ve accumulated a huge collection of PDFs — books, articles, whitepapers — all about IT topics. The collection keeps growing.
But here’s the catch: I rarely have time to read most of them. And when I _do_ need something specific, searching is painful. I often can’t remember the exact keyword or buzzword — only something close to it. Keyword search is tedious, brittle, and often leads to irrelevant results.
So I thought: **why not make an AI assistant that can “understand” my documents and find answers for me?**

### My Constraints
- Only need to support **PDF files** (at least for now).
- Must run locally (privacy and cost reasons).
- Should work on my laptop (32 GB RAM — not huge for LLMs).
- Prefer open-source tools.
- I want to just **type a question** into a console and get the answer.

## Technology Choices
I opted for **LangChain** because it is an open, composable framework that provides a standard interface for models, tools, and databases — perfect for building an LLM workflow. Since I prefer Java, **LangChain4j** simplifies LLM integration into Java applications. For the LLM, I picked **Ollama** — it runs locally, is easy to use, and offers good embedding models. I also chose **Quarkus**, for several reasons:
- Integrating an LLM using LangChain4j is seamless.
- Adding embedded models is straightforward.
- Implementing the RAG pattern is easy.
- Registering an AI service only needs annotations.
- Built-in reactive features (via Mutiny) make streaming LLM responses trivial.
- And — not a small thing — **developing with Quarkus is genuinely fun**.

## LLMs Have Limits — That’s Where RAG Comes In
An LLM’s knowledge is frozen at the time of its training. It may have never “seen” my documents. **Retrieval-Augmented Generation (RAG)** addresses this by:
- Finding relevant pieces of data from your dataset.
- Injecting them into the LLM’s prompt.

This allows the LLM to respond using domain-specific information and helps reduce hallucinations. I didn’t want just keyword search — I wanted **semantic search**.

## Opps, Wait… What Is “Embedding”?
If you’re new to this, you might be wondering: “Embedding? Cosine similarity? Vectors? Where are these vectors stored? How do documents become vectors? And how does the LLM use them?”
I asked those exact same questions. Here’s the short version:
- An **embedding** is a numerical vector representing a piece of text (or image, or other data) in a way that captures its meaning.
- Similar meanings → vectors that are close to each other.
- “Cosine similarity” is one way to measure how close two vectors are.

## Converting Documents into Vectors
To turn a document into embeddings, you need a special **embedding model**. The tricky part:
- Embedding vectors have a **fixed dimension**, depending on the model.
- If your text is long, you must **split it into chunks** (segmentation).
- You need to store **both** the vector and the original text.

Sounds easy? It’s not. For example, a PDF contains more than just text — it has metadata, special characters, separators, even formatting markers. Sometimes you want to strip them; sometimes they carry meaning (e.g., section headings). Splitting the text is also delicate:
- If chunks are too short, they lose context.
- If too long, the embeddings become less precise.

In my setup:
- **Max segment size**: 500 characters.
- **Overlap**: 25 characters (to preserve context between chunks).
- Parser: **Apache Tika** — great at extracting text from PDFs and integrates smoothly with LangChain4j.

## Storing Vectors
Once you have chunks and their embeddings, you need a **vector store**.There are many free/open-source options. I chose **PostgreSQL with the PgVector extension** because:
- I already know PostgreSQL.
- PgVector is easy to install.
- I can store not just embeddings but also file names, so I know which files have been processed.

It’s a simple, reliable setup with minimal extra dependencies.

## Query Flow
Let’s walk through what happens when I ask: _"What are the benefits of Quarkus?"_

### 1. Generate the query embedding
Ollama’s REST API /api/embed:
```json
{
    "model": "nomic-embed-text",
    "input": "What are the benefits of Quarkus?"
}
```

Response (truncated for sanity):
```json
{
    "model": "nomic-embed-text",
    "embeddings": [[0.010825694, 0.084175445, -0.18477407]],
    "total_duration": 62151875,
    "load_duration": 14999250,
    "prompt_eval_count": 21
}
```
### 2. Search the vector store
SQL example using PgVector:
```sql
SELECT(2 - (embedding <=> '[0.010825694, 0.084175445, ...]')) / 2 AS score, embedding_id, embedding, text, metadata
FROM embedding_item
WHERE round(cast(float8 (embedding <=> '[0.010825694, 0.084175445, ...]') as numeric), 8) <= round(2 - 2 * 0.8, 8)
ORDER BY embedding <=> '[0.010825694, 0.084175445, ...]'
LIMIT 200;
```
Here:
- The array is the query embedding returned by the embedding model.
- 0.8 is the similarity threshold (see below).
- LIMIT 200 caps the number of returned segments.

### 3. Send context to the LLM
Ollama’s /api/chat:
```json
{
    "model": "llama3.1",
    "messages": [
        { "role": "system", "content": "You are an AI assistant specialized in ..." },
        { "role": "user", "content": "What are the benefits of Quarkus for the Spring developers?\\n\\nAnswer using the following information:\\nThere are many similarities and differences between Quarkus and Spring. When getting \\nstarted with Quarkus, I found many Quarkus books, guides, and tutorials, ..." }
    ],
    "options": {
        "temperature": 0.2,
        "top_k": 40,
        "top_p": 0.9
    },
    "stream": true
}
```
In this call:
- The **system** role provides global instructions to the model.
- The **user** role contains the original query **plus** the retrieved segments/context.
- stream: true enables streaming responses.

## About Scoring
PgVector (as of now) supports cosine similarity. Values range from 0 to 1:
- **1.0** → identical vectors
- **0.0** → completely unrelated

The **score threshold** controls how strict your retrieval is:
- Higher threshold → fewer, more precise matches.
- Lower threshold → more matches, potentially more noise.

Tune this empirically for your dataset and use case. I also created IVF_FLAT indexes for performance, but that’s outside the scope of this article.

## Bringing It Together in Code
Now comes the fun part — wiring all the pieces together and actually seeing our RAG pipeline in action. The beating heart of my PDF document loader is the ```EmbeddingStoreIngestor```. This is where the magic starts.
```java
@Produces
public EmbeddingStoreIngestor createIngestor() {
    return EmbeddingStoreIngestor.builder()
    .embeddingStore(PgVectorEmbeddingStore)
    .embeddingModel(EmbeddingModel)
    .documentSplitter(recursive(500, 25))
    .build();
}
```
Here’s what’s going on:
- **embeddingModel** → in my case, this is nomic-embed-text from LangChain.
- **PgVectorEmbeddingStore** → also initialized by LangChain and Quarkus, ready to store our vectors.
- **documentSplitter(recursive(500, 25))** → splits text into 500-character chunks with a 25-character overlap to preserve context.

When a PDF is ingested, calling ```EmbeddingStoreIngestor.ingest()``` is when the chain reaction happens:
1. **Apache Tika** parses the PDF — pulling out both text and useful metadata.
2. **LangChain** splits the text according to our segment rules.
3. **The embedding model** transforms each chunk into a high-dimensional vector.
4. **PgVector** stores both the original text and its embedding for later retrieval.

From PDF to indexed vector store in one call — I love it when things just _click_.
I also implemented a **REST endpoint** so I could easily query the system from a browser or even curl on the command line:
```java
@GET
@Produces(MediaType.TEXT_PLAIN)
public Multi<String> retrieve(@RestQuery String question) {
    return ragAssistant.answer(question);
}
```
Notice it returns a **Multi<String>**. Why? Because when the LLM starts streaming the answer, Quarkus reactive pushes each piece to the client _as it arrives_. No “wait-until-it’s-done” — it’s immediate, conversational.
And what’s behind that ragAssistant? It’s almost ridiculously simple:
```java
@RegisterAiService(retrievalAugmentor = RagRetrievalAugmentor.class)
public interface RagAssistant {
    @SystemMessage("You are an AI assistant specialized in ...")
    Multi<String> answer(@UserMessage String question);
}
```
Here we:
- Register it as an AI service that uses our **RAG implementation**.
- Set the **System prompt** directly in the annotation.
- Mark the user’s question with ```@UserMessage```.

Quarkus takes care of the rest, eliminating a ton of boilerplate.

### But how do we test this?
Even though the app only exposes a REST endpoint, testing is refreshingly straightforward. Using **RestAssured** for HTTP calls and **AssertJ** for fluent assertions, I wrote this:

```java
@QuarkusTest
class RagResourceTest {
    @Test
    void whenAskLLMAboutQuarkusThenResponseContainsKeywords() {
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
```

That’s it — one test sends a real question to the endpoint and verifies that the response includes the expected keywords. Quick, clear feedback on whether the pipeline is working.

## Installation
### Prerequisites
- JDK 21 or higher
- Maven 3.5+
- Docker
- [Ollama](https://ollama.com) installed
- [httpie](https://httpie.io/) installed (optional, but useful for testing)

### Steps
1. Clone the repository:
   ```sh
   git clone https://github.com/fejesa/quarkus-rag.git
    ```
2. Build the project:
   ```sh
   mvn clean install
   ```
3. Run the application in development mode:
   ```sh
    mvn quarkus:dev
    ```
**Note**: No need to run neither the Ollama nor the PostgreSQL manually — Quarkus Dev mode will automatically start them for you. If the LLM models are not available locally, Quarkus will download them automatically, but this may take some time.

## Configuration
The application can be configured via `application.properties`. The following properties are available:
```properties
quarkus.http.port = 8080
# PostgreSQL database automatically created by Dev Services with the following settings
quarkus.devservices.enabled = true
quarkus.datasource.devservices.port = 5432
quarkus.datasource.devservices.db-name = rag
quarkus.datasource.devservices.username = rag
quarkus.datasource.devservices.password = rag
# Run Flyway database schema migrations automatically
quarkus.flyway.migrate-at-start = true

# The dimension of the embedding vectors. This has to be the same as the dimension of vectors produced by the embedding model that you use.
quarkus.langchain4j.pgvector.dimension = 768
# The table name for storing embeddings - see V1__create_schema.sql
quarkus.langchain4j.pgvector.table = embedding_item
# Used if the table and index are created automatically by langchain4j
quarkus.langchain4j.pgvector.use-index = true
quarkus.langchain4j.pgvector.index-list-size= 100

quarkus.langchain4j.log-requests = true
quarkus.langchain4j.log-responses = true
# The temperature to use for the chat model. Temperature is a value between 0 and 1, where lower values make the model more deterministic and higher values make it more creative.
quarkus.langchain4j.temperature = 0.2
# Global timeout for requests to LLM APIs
quarkus.langchain4j.timeout = 120s

# The chat model to use. In case of Ollama, llama3.1 is the default chat model.
quarkus.langchain4j.ollama.chat-model.model-id = gpt-oss
# The format to return a response in. Format can be json or a JSON schema, or text; in this application, we use text.
quarkus.langchain4j.ollama.chat-model.format = text
# In case of Ollama, nomic-embed-text is the default model used for text embeddings.
quarkus.langchain4j.ollama.embedding-model.model-id = nomic-embed-text
# Whether embedding model requests should be logged; default is false
quarkus.langchain4j.ollama.embedding-model.log-requests = true
# Whether embedding model responses should be logged; default is false
quarkus.langchain4j.ollama.embedding-model.log-responses=true

# The location of the documents to be processed; can be relative or absolute path.
rag.document.location = ./documents
# The document loader scheduler period; default is 60 seconds
rag.document.loader.scheduler.period = 10s
# The maximum length (in characters) of a document segment used during ingestion. Default is 550 characters.
rag.embedding.max-segment-size = 500
# The maximum number of characters that can overlap between two segments. Default is 25 characters.
rag.embedding.max-overlap-size = 25
# The maximum number of retrieved embeddings when querying for relevant documents. Default is 200.
rag.retrieval.max-results = 200
# The minimum cosine similarity score for a document to be considered relevant during retrieval. Score ranges between 0 (no similarity) and 1 (identical). Default is 0.8.
rag.retrieval.min-score = 0.8

# LangFuse OpenTelemetry settings
quarkus.otel.enabled = true
quarkus.otel.metrics.enabled = true
# OpenTelemetry defines the encoding of telemetry data and the protocol used to exchange data between the client and the server. Default is grpc.
quarkus.otel.exporter.otlp.protocol=http/protobuf
# LangFuse OpenTelemetry endpoint and authorization header; set your own values here
quarkus.otel.exporter.otlp.headers = Authorization=Basic ***
quarkus.otel.exporter.otlp.endpoint = http://localhost:3000/api/public/otel
quarkus.otel.exporter.otlp.logs.protocol=http/protobuf
quarkus.langchain4j.tracing.include-prompt = true
quarkus.langchain4j.tracing.include-completion = true
```

## Usage
If you have some PDF files that you want to process, place them in the `documents` directory in the project root. The application will automatically pick them up and start processing them. Once the application is running, you can ask AI from command line using for example [httpie](https://httpie.io/) or [curl](https://curl.se/).
```sh
http --stream -v localhost:8080/rag question=="What are the benefits of Quarkus for the Spring developers? Describe it in 10 bullet points, and give back the name of the documents that used for the generating the answer." | grep -v '^$'
```
LLM needs some time to process the request, so you will see the response in chunks as they arrive. The `--stream` option is important here, as it allows you to see the response in real-time.

## **Final Thoughts**
For me, this project was more than just code — it was about exploring the edge where Java meets AI.
I started with no ML background. I ended up with a working **local RAG assistant** that understands my personal library of documents.
If you’re a Java developer curious about AI, I can tell you: Quarkus + LangChain4j + PgVector + Ollama is a fantastic playground.
This can be extended in many ways — a web UI, multi-format ingestion, automated pipelines, summarization, and more — but that’s a story for another time.
