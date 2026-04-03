<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?logo=springboot&logoColor=white" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Spring%20AI-1.1.4-6DB33F?logo=spring&logoColor=white" alt="Spring AI">
  <img src="https://img.shields.io/badge/Angular-21-DD0031?logo=angular&logoColor=white" alt="Angular">
  <img src="https://img.shields.io/badge/Ollama-LLM-000000?logo=ollama&logoColor=white" alt="Ollama">
  <img src="https://img.shields.io/badge/PostgreSQL-pgvector-4169E1?logo=postgresql&logoColor=white" alt="PostgreSQL">
</p>

# Leia.AI — Retrieval-Augmented Generation System

**Leia.AI** is a full-stack Retrieval-Augmented Generation (RAG) system that allows users to upload documents (PDF, TXT, DOCX) and have an AI assistant answer questions strictly based on the uploaded content. The system uses local LLMs via Ollama, semantic search via pgvector, and renders rich Markdown + LaTeX responses.

---

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Setup & Installation](#setup--installation)
- [Configuration](#configuration)
- [API Reference](#api-reference)
- [Frontend Features](#frontend-features)
- [How RAG Works](#how-rag-works)
- [Environment Variables](#environment-variables)

---

## Architecture Overview

```
┌─────────────────┐     HTTP/SSE      ┌──────────────────────────┐
│                 │ ◄──────────────── │     Spring Boot API      │
│  Angular 21     │                   │                          │
│  Frontend       │ ──────────────►  │  ┌────────────────────┐  │
│  (Port 4200)    │   Upload / Query  │  │   RAGService       │  │
│                 │                   │  │   DocumentService   │  │
└─────────────────┘                   │  │   AnythingLLMClient │  │
                                      │  └────────┬───────────┘  │
                                      │           │              │
                                      └───────────┼──────────────┘
                                                  │
                              ┌────────────────────┼───────────────────┐
                              │                    │                   │
                     ┌────────▼────────┐  ┌────────▼────────┐  ┌──────▼──────┐
                     │   Ollama        │  │   PostgreSQL    │  │ AnythingLLM │
                     │   (LLM + Embed) │  │   + pgvector    │  │  (Optional) │
                     │   Port 11434    │  │   Port 5432     │  │  Port 3001  │
                     └─────────────────┘  └─────────────────┘  └─────────────┘
```

---

## Tech Stack

| Layer        | Technology                         | Purpose                                    |
|:-------------|:-----------------------------------|:-------------------------------------------|
| **Backend**  | Spring Boot 3.5 + Java 17         | REST API, SSE streaming, document ingestion |
| **AI**       | Spring AI 1.1.4                    | LLM integration, embeddings, vector store  |
| **LLM**      | Ollama (Llama 3.2 1B)             | Local language model for chat               |
| **Embeddings** | Ollama (nomic-embed-text)        | 768-dimension document embeddings           |
| **Vector DB** | PostgreSQL + pgvector             | Semantic similarity search (HNSW + cosine) |
| **Frontend** | Angular 21 + TailwindCSS          | Chat UI with Markdown + LaTeX rendering    |
| **Parsing**  | Apache Tika                        | PDF, DOCX, TXT document extraction          |

---

## Project Structure

```
retrieval-augmented-generation/
├── backend/ragsystem/
│   ├── src/main/java/com/example/ragsystem/
│   │   ├── RagsystemApplication.java          # Spring Boot entry point
│   │   ├── config/
│   │   │   └── CorsConfig.java                # CORS configuration
│   │   ├── controller/
│   │   │   ├── ChatController.java            # SSE streaming chat endpoint
│   │   │   ├── DocumentController.java        # File upload endpoint
│   │   │   └── WorkspaceController.java       # AnythingLLM workspace proxy
│   │   └── service/
│   │       ├── RAGService.java                # Core RAG logic (retrieve + generate)
│   │       ├── DocumentService.java           # Document parsing & vectorization
│   │       └── AnythingLLMClient.java         # REST client for AnythingLLM
│   └── src/main/resources/
│       ├── application.yml                    # App configuration
│       └── schema.sql                         # pgvector table setup
│
├── frontend/RAGSystem/
│   └── src/app/
│       ├── pages/chat/
│       │   ├── chat.component.ts              # Chat logic + Markdown/LaTeX rendering
│       │   ├── chat.component.html            # Chat UI template
│       │   └── chat.component.css             # Premium styling for responses
│       └── services/
│           ├── chat.service.ts                # SSE streaming client
│           └── document.service.ts            # File upload client
│
└── README.md
```

---

## Prerequisites

| Dependency   | Version  | Install                                           |
|:-------------|:---------|:--------------------------------------------------|
| Java         | 17+      | `sudo apt install openjdk-17-jdk`                 |
| Maven        | 3.9+     | `sudo apt install maven`                          |
| Node.js      | 20+      | [nodejs.org](https://nodejs.org)                  |
| PostgreSQL   | 15+      | `sudo apt install postgresql`                     |
| pgvector     | 0.5+     | `CREATE EXTENSION vector;`                        |
| Ollama       | latest   | [ollama.com](https://ollama.com)                  |

### Ollama Models

```bash
ollama pull llama3.2:1b          # Chat model
ollama pull nomic-embed-text     # Embedding model (768 dimensions)
```

---

## Setup & Installation

### 1. Database

```bash
# Create the database
sudo -u postgres createdb ragdb
sudo -u postgres psql -c "CREATE USER myuser WITH PASSWORD 'mypassword';"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE ragdb TO myuser;"

# The schema is auto-created by Spring AI, but you can run manually:
psql -U myuser -d ragdb -f backend/ragsystem/src/main/resources/schema.sql
```

### 2. Backend

```bash
cd backend/ragsystem
mvn spring-boot:run
# Server starts on http://localhost:8080
```

### 3. Frontend

```bash
cd frontend/RAGSystem
npm install
ng serve
# App opens on http://localhost:4200
```

---

## Configuration

All configuration is in `backend/ragsystem/src/main/resources/application.yml`:

```yaml
spring:
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        model: llama3.2:1b            # Fast 1B parameter model
      embedding:
        model: nomic-embed-text       # 768-dim embeddings
    vectorstore:
      pgvector:
        index-type: HNSW             # Fast approximate nearest neighbor
        distance-type: COSINE_DISTANCE
        dimensions: 768

  datasource:
    url: jdbc:postgresql://localhost:5432/ragdb
    username: myuser
    password: mypassword

  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 50MB

server:
  port: 8080
```

---

## API Reference

### Chat

| Method | Endpoint           | Content-Type         | Description                              |
|:-------|:-------------------|:---------------------|:-----------------------------------------|
| POST   | `/api/chat/query`  | `text/event-stream`  | Stream an AI answer (SSE) based on RAG   |

**Request Body:**
```json
{ "query": "Explain the Lorentz transformation" }
```

**Response:** Server-Sent Events stream with token-by-token AI response.

---

### Documents

| Method | Endpoint          | Content-Type          | Description                  |
|:-------|:------------------|:----------------------|:-----------------------------|
| POST   | `/api/documents`  | `multipart/form-data` | Upload and index a document  |

**Supported formats:** PDF, TXT, DOCX, MD (up to 50MB)

**Processing pipeline:**
1. Apache Tika extracts text from the uploaded file
2. `TokenTextSplitter` breaks text into chunks
3. Ollama generates 768-dim embeddings via `nomic-embed-text`
4. Chunks + embeddings are stored in pgvector

---

### Workspaces (Optional)

| Method | Endpoint                      | Description                          |
|:-------|:------------------------------|:-------------------------------------|
| POST   | `/api/workspaces/{slug}/chat` | Proxy chat to AnythingLLM workspace  |

---

## Frontend Features

- **Real-time streaming** — Token-by-token response via SSE
- **Markdown rendering** — Headers, bold, lists, code blocks, tables, blockquotes via `marked`
- **LaTeX equations** — Inline (`$E = mc^2$`) and block (`$$...$$`) formulas via `KaTeX`
- **Document upload** — Drag-and-drop file upload in the sidebar
- **Responsive design** — TailwindCSS-based layout with a premium dark sidebar

---

## How RAG Works

```
User Question
      │
      ▼
┌─────────────┐    similarity    ┌──────────────────┐
│ Embed query │───────search────►│ pgvector (Top-4) │
│ (nomic)     │                  │ document chunks   │
└─────────────┘                  └────────┬─────────┘
                                          │
                                 Retrieved Context
                                          │
                                          ▼
                                ┌───────────────────┐
                                │   System Prompt    │
                                │  + Context + Query │
                                │                    │
                                │   → Llama 3.2 1B   │
                                │   → Streamed SSE   │
                                └───────────────────┘
                                          │
                                          ▼
                                  Markdown + LaTeX
                                   rendered in UI
```

1. **Upload** — User uploads a document (PDF/TXT/DOCX)
2. **Parse** — Apache Tika extracts raw text
3. **Chunk** — `TokenTextSplitter` divides text into manageable pieces
4. **Embed** — Each chunk is embedded via `nomic-embed-text` (768 dimensions)
5. **Store** — Embeddings are stored in PostgreSQL with pgvector (HNSW index)
6. **Query** — User asks a question, which is embedded and used for similarity search
7. **Retrieve** — Top 4 most similar chunks are retrieved from the vector store
8. **Generate** — Retrieved context + question are sent to Llama 3.2 (1B) via Ollama
9. **Stream** — Response is streamed token-by-token via SSE to the frontend
10. **Render** — Frontend renders Markdown headers, bold, lists, and LaTeX equations

---

## Environment Variables

| Variable           | Default                           | Description                      |
|:-------------------|:----------------------------------|:---------------------------------|
| `POSTGRES_HOST`    | `localhost`                       | PostgreSQL host                  |
| `POSTGRES_PORT`    | `5432`                            | PostgreSQL port                  |
| `POSTGRES_DB`      | `ragdb`                           | Database name                    |
| `POSTGRES_USER`    | `myuser`                          | Database username                |
| `POSTGRES_PASSWORD`| `mypassword`                      | Database password                |
| `OPENAI_API_KEY`   | *(AnythingLLM key)*               | API key for AnythingLLM          |
| `OPENAI_API_BASE`  | `http://localhost:3001/api/v1`    | AnythingLLM base URL             |

---

<p align="center">
  <strong>Leia.AI</strong> — Enterprise Document Intelligence<br>
  <sub>Built with Spring AI + Ollama + pgvector + Angular</sub>
</p>
