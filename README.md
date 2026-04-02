# 🧠 RAG System

A comprehensive Retrieval-Augmented Generation (RAG) system built with a robust Spring Boot backend and a modern Angular frontend. This project is structured to efficiently handle documents, generate embeddings, and provide intelligent chat responses using state-of-the-art AI models.

## 🏗 Architecture

The system is divided into two main components:
- **Backend (`/backend/ragsystem`)**: A Java-based Spring Boot application responsible for handling requests, embedding generation, database management, and interacting with LLMs.
- **Frontend (`/frontend/RAGSystem`)**: An Angular-based Single Page Application (SPA) styled with TailwindCSS, providing an intuitive interface for interacting with the AI.

### Technical Stack

#### Backend
* **Framework:** Spring Boot 3.5.13 (Java 17)
* **AI Integration:** Spring AI 1.1.4 (Ollama, OpenAI, AI Advisors, Tika Document Reader)
* **Database:** PostgreSQL with `pgvector` for efficient similarity search of embeddings (VectorStore)
* **Build Tool:** Maven

#### Frontend
* **Framework:** Angular 21.1.0 (TypeScript ~5.9.2)
* **Styling:** TailwindCSS 3.4.19
* **Testing:** Vitest for Unit testing
* **Build Tool:** Angular CLI

## 🚀 Prerequisites

Before you begin, ensure you have met the following requirements:
* **Java 17+**
* **Node.js 18+** & **npm 11+**
* **PostgreSQL** (with `pgvector` extension installed and enabled)
* **Ollama** installed locally (running `llama3.2:1b` for chat and `nomic-embed-text` for embeddings, as configured in the backend properties)
* **Maven** (optional, wrapper is included)

## 🛠️ Setup and Installation

### 1. Database Setup
Ensure PostgreSQL is running and create a database for the application (default name: `ragdb`). Make sure to enable the `vector` extension.
```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

### 2. Backend Setup
1. Navigate to the backend directory:
   ```bash
   cd backend/ragsystem
   ```
2. Configure your environment variables. You can edit the `src/main/resources/application.yml` directly, or set the relevant environment variables:
   * `POSTGRES_HOST`, `POSTGRES_PORT`, `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`
   * `OPENAI_API_KEY`, `OPENAI_API_BASE` (if using OpenAI API instead of Ollama)
3. Start the application using Maven:
   ```bash
   ./mvnw spring-boot:run
   ```
The backend server will start on `http://localhost:8080`.

### 3. Frontend Setup
1. Navigate to the frontend directory:
   ```bash
   cd frontend/RAGSystem
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the development server:
   ```bash
   npm start
   ```
The Angular UI will be available at `http://localhost:4200`.

## 📂 Project Structure

```
retrieval-augmented-generation/
├── backend/
│   └── ragsystem/
│       ├── pom.xml                   # Maven configuration
│       └── src/
│           └── main/
│               ├── java/com/example/ragsystem/
│               │   ├── controller/   # REST API Endpoints (Chat, Workspace)
│               │   └── ...
│               └── resources/
│                   └── application.yml # Spring Boot & AI Configuration
├── frontend/
│   └── RAGSystem/
│       ├── package.json              # NPM dependencies
│       ├── src/
│       │   ├── app/
│       │   │   ├── pages/            # Angular components and views (Chat, etc.)
│       │   │   └── ...
│       └── tailwind.config.js        # TailwindCSS Configuration
└── README.md                         # Project documentation
```

## 🤖 AI Models Configuration
By default, the backend is configured to use **Ollama** for entirely local operation:
- **Chat:** `llama3.2:1b`
- **Embeddings:** `nomic-embed-text`

Ensure you pull these models via Ollama before using the system:
```bash
ollama run llama3.2:1b
ollama pull nomic-embed-text
```
*Note: OpenAI embeddings are explicitly disabled in the configuration by default. If you wish to switch back to OpenAI, modify the `application.yml` accordingly.*

## 🧪 Testing

- **Backend:** Run `./mvnw test` in the `backend/ragsystem` directory.
- **Frontend:** Run `npm run test` (powered by Vitest) in the `frontend/RAGSystem` directory to execute unit tests.

## 👨‍💻 Note for Developers & AI Engineers
This system is architected as a standard Retrieval-Augmented Generation pipeline. 
When data is uploaded (Workspace), Spring AI's `TikaDocumentReader` extracts the text, generates embeddings via Ollama (`nomic-embed-text`), and stores them in PostgreSQL using pgvector. During a chat interaction, the query is embedded, relevant context is fetched using Cosine Similarity (via HNSW index, Dimensions: 768), and the context + query is passed to `llama3.2:1b` to generate a response.
