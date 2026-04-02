-- Enable pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- Create the vector table expected by Spring AI pgvector
CREATE TABLE IF NOT EXISTS vector_store (
	id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
	content text,
	metadata json,
	embedding vector(768) -- Should match the dimensions of your embedding model
);

CREATE INDEX IF NOT EXISTS vector_store_embedding_idx ON vector_store USING hnsw (embedding vector_cosine_ops);
