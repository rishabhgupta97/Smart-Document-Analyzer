package com.analyzer.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class DocumentAnalysis {
    private String id;
    private String filename;
    private String fileType;
    private long fileSize;
    private String extractedText;
    private int wordCount;
    private int characterCount;
    private String readingTime;
    private String summary;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime analyzedAt;

    public DocumentAnalysis() {
        this.analyzedAt = LocalDateTime.now();
    }

    public DocumentAnalysis(String id, String filename, String fileType, long fileSize) {
        this();
        this.id = id;
        this.filename = filename;
        this.fileType = fileType;
        this.fileSize = fileSize;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
        calculateMetrics();
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public int getCharacterCount() {
        return characterCount;
    }

    public void setCharacterCount(int characterCount) {
        this.characterCount = characterCount;
    }

    public String getReadingTime() {
        return readingTime;
    }

    public void setReadingTime(String readingTime) {
        this.readingTime = readingTime;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public LocalDateTime getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(LocalDateTime analyzedAt) {
        this.analyzedAt = analyzedAt;
    }

    private void calculateMetrics() {
        if (extractedText != null && !extractedText.trim().isEmpty()) {
            this.characterCount = extractedText.length();
            this.wordCount = extractedText.trim().split("\\s+").length;

            // Average reading speed is 200 words per minute
            int minutes = Math.max(1, wordCount / 200);
            this.readingTime = minutes + " minute" + (minutes > 1 ? "s" : "");

            // Create simple summary (first 200 characters)
            this.summary = extractedText.length() > 200
                    ? extractedText.substring(0, 200) + "..."
                    : extractedText;
        }
    }
}