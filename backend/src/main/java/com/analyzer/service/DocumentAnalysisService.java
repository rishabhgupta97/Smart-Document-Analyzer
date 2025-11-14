package com.analyzer.service;

import com.analyzer.model.DocumentAnalysis;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DocumentAnalysisService {

    // In-memory storage for demo - replace with database in production
    private final Map<String, DocumentAnalysis> analysisStorage = new ConcurrentHashMap<>();

    private final AiAnalysisService aiAnalysisService;

    public DocumentAnalysisService(AiAnalysisService aiAnalysisService) {
        this.aiAnalysisService = aiAnalysisService;
    }

    public DocumentAnalysis analyzeDocument(MultipartFile file) throws IOException {
        String documentId = UUID.randomUUID().toString();
        String filename = file.getOriginalFilename();
        String fileType = getFileType(filename);
        long fileSize = file.getSize();

        DocumentAnalysis analysis = new DocumentAnalysis(documentId, filename, fileType, fileSize);

        // Extract text based on file type
        String extractedText = extractText(file, fileType);
        analysis.setExtractedText(extractedText);

        // Perform AI analysis on the extracted text
        AiAnalysisService.ComprehensiveAnalysisResult aiResult = aiAnalysisService.analyzeDocument(extractedText);

        // Set AI analysis results
        analysis.setSentiment(aiResult.getSentiment().getSentiment());
        analysis.setSentimentScore(aiResult.getSentiment().getPositiveScore());
        analysis.setKeyPhrases(aiResult.getKeyPhrases());

        // Convert entities to Map format for JSON serialization
        List<Map<String, Object>> entityMaps = aiResult.getEntities().stream()
                .map(entity -> {
                    Map<String, Object> entityMap = new HashMap<>();
                    entityMap.put("text", entity.getText());
                    entityMap.put("type", entity.getType());
                    entityMap.put("confidence", entity.getConfidence());
                    return entityMap;
                })
                .toList();
        analysis.setEntities(entityMaps);

        // Store analysis
        analysisStorage.put(documentId, analysis);

        return analysis;
    }

    public DocumentAnalysis getAnalysis(String documentId) {
        return analysisStorage.get(documentId);
    }

    public Map<String, DocumentAnalysis> getAllAnalyses() {
        return new HashMap<>(analysisStorage);
    }

    private String extractText(MultipartFile file, String fileType) throws IOException {
        switch (fileType.toLowerCase()) {
            case "pdf":
                return extractTextFromPdf(file);
            case "docx":
                return extractTextFromDocx(file);
            case "txt":
                return new String(file.getBytes());
            default:
                throw new IllegalArgumentException("Unsupported file type: " + fileType);
        }
    }

    private String extractTextFromPdf(MultipartFile file) throws IOException {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String extractTextFromDocx(MultipartFile file) throws IOException {
        try (XWPFDocument document = new XWPFDocument(file.getInputStream());
                XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }

    private String getFileType(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "unknown";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    public boolean isValidFileType(String filename) {
        String fileType = getFileType(filename);
        return fileType.matches("pdf|docx|txt");
    }
}