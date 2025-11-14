package com.analyzer.controller;

import com.analyzer.model.DocumentAnalysis;
import com.analyzer.service.DocumentAnalysisService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "http://localhost:3000") // Allow React frontend
public class DocumentController {

    private final DocumentAnalysisService documentAnalysisService;

    public DocumentController(DocumentAnalysisService documentAnalysisService) {
        this.documentAnalysisService = documentAnalysisService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Object> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Please select a file to upload"));
            }

            if (!documentAnalysisService.isValidFileType(file.getOriginalFilename())) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Unsupported file type. Please upload PDF, DOCX, or TXT files."));
            }

            // Analyze document
            DocumentAnalysis analysis = documentAnalysisService.analyzeDocument(file);

            return ResponseEntity.ok(analysis);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error processing file: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/analysis")
    public ResponseEntity<Object> getAnalysis(@PathVariable String id) {
        DocumentAnalysis analysis = documentAnalysisService.getAnalysis(id);

        if (analysis == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(analysis);
    }

    @GetMapping("/all")
    public ResponseEntity<Map<String, DocumentAnalysis>> getAllAnalyses() {
        return ResponseEntity.ok(documentAnalysisService.getAllAnalyses());
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Smart Document Analyzer API");
        return ResponseEntity.ok(response);
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}