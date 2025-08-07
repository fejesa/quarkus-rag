-- Enable the 'vector' extension if it is not already enabled.
-- This extension allows you to store and query high-dimensional vectors in PostgreSQL.
CREATE EXTENSION IF NOT EXISTS vector;

-- Create a table to store embedding data.
-- Each row represents a single embedding along with its associated text and optional metadata.
CREATE TABLE embedding_item (
    embedding_id UUID PRIMARY KEY,    -- Unique identifier for the embedding (recommended to be UUID for global uniqueness).
    embedding VECTOR(768),            -- The actual embedding vector (e.g., from a language model), 768-dimensional.
    text TEXT,                        -- The original text from which the embedding was generated.
    metadata JSON                     -- Optional metadata about the text (e.g., document source, timestamps).
);

-- Create an index using the IVF_FLAT algorithm on the embedding column.
-- This index improves performance for approximate nearest neighbor (ANN) searches using cosine similarity.
-- 'lists = 100' specifies the number of clusters for the index. Higher numbers = faster search, slower inserts.
CREATE INDEX embedding_item_ivfflat_index
    ON embedding_item USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);

CREATE TABLE document_file (
    id BIGINT not null,
    file_name CHARACTER VARYING(255) not null,
    checksum CHARACTER VARYING(255) not null
);

CREATE SEQUENCE id_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
