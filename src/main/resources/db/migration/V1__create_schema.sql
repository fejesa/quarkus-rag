CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE embedding_item (
   embedding_id UUID PRIMARY KEY,
   embedding VECTOR(768),
   text TEXT,
   metadata JSON
);

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
