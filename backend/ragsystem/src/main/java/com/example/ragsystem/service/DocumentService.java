package com.example.ragsystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ByteArrayResource;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final VectorStore vectorStore;

    public void processAndStoreDocument(MultipartFile file) {
        log.info("Processing document: {}", file.getOriginalFilename());
        try {
            Resource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            TikaDocumentReader documentReader = new TikaDocumentReader(resource);
            List<Document> documents = documentReader.get();
            log.info("Parsed {} documents/pages from file", documents.size());

            TokenTextSplitter textSplitter = new TokenTextSplitter();
            List<Document> splitDocuments = textSplitter.apply(documents);
            log.info("Split into {} chunks", splitDocuments.size());
            vectorStore.add(splitDocuments);
            log.info("Successfully stored {} document chunks in pgvector", splitDocuments.size());

        } catch (IOException e) {
            log.error("Failed to process document: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Document processing failed", e);
        }
    }
}
