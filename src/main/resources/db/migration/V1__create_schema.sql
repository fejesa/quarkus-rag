CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE items (
   embedding_id UUID PRIMARY KEY,
   embedding VECTOR(768),
   text TEXT,
   metadata JSON
);

-- Create the ivfflat index on the embedding column using cosine similarity
CREATE INDEX items_ivfflat_index
    ON items
        USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);
