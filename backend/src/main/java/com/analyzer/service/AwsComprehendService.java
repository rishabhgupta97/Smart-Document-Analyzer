package com.analyzer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.comprehend.ComprehendClient;
import software.amazon.awssdk.services.comprehend.model.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Real AWS Comprehend service for AI-powered document analysis
 * This service provides actual cloud-based AI capabilities when AWS is properly
 * configured
 */
@Service
@ConditionalOnProperty(name = "aws.comprehend.use-real-service", havingValue = "true")
public class AwsComprehendService {

    private static final Logger logger = LoggerFactory.getLogger(AwsComprehendService.class);

    private final ComprehendClient comprehendClient;
    private final boolean isServiceAvailable;

    public AwsComprehendService(@Value("${aws.region:us-east-1}") String region) {
        ComprehendClient client = null;
        boolean available = false;

        try {
            logger.info("Initializing AWS Comprehend service in region: {}", region);
            client = ComprehendClient.builder()
                    .region(Region.of(region))
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();

            // Test the connection
            client.listDocumentClassifiers(ListDocumentClassifiersRequest.builder().maxResults(1).build());
            available = true;
            logger.info("AWS Comprehend service successfully initialized");

        } catch (Exception e) {
            logger.warn("AWS Comprehend service not available: {}. Falling back to mock implementation.",
                    e.getMessage());
            available = false;
        }

        this.comprehendClient = client;
        this.isServiceAvailable = available;
    }

    public boolean isAvailable() {
        return isServiceAvailable;
    }

    /**
     * Analyze sentiment using AWS Comprehend
     */
    public AiAnalysisService.SentimentAnalysisResult analyzeSentiment(String text) {
        if (!isServiceAvailable || comprehendClient == null) {
            throw new RuntimeException("AWS Comprehend service not available");
        }

        try {
            DetectSentimentRequest request = DetectSentimentRequest.builder()
                    .text(text)
                    .languageCode(LanguageCode.EN)
                    .build();

            DetectSentimentResponse response = comprehendClient.detectSentiment(request);
            SentimentScore scores = response.sentimentScore();

            return new AiAnalysisService.SentimentAnalysisResult(
                    response.sentiment().toString(),
                    scores.positive(),
                    scores.negative(),
                    scores.neutral(),
                    scores.mixed());

        } catch (Exception e) {
            logger.error("Error analyzing sentiment with AWS Comprehend", e);
            throw new RuntimeException("AWS sentiment analysis failed", e);
        }
    }

    /**
     * Extract key phrases using AWS Comprehend
     */
    public List<String> extractKeyPhrases(String text) {
        if (!isServiceAvailable || comprehendClient == null) {
            throw new RuntimeException("AWS Comprehend service not available");
        }

        try {
            DetectKeyPhrasesRequest request = DetectKeyPhrasesRequest.builder()
                    .text(text)
                    .languageCode(LanguageCode.EN)
                    .build();

            DetectKeyPhrasesResponse response = comprehendClient.detectKeyPhrases(request);

            return response.keyPhrases().stream()
                    .map(KeyPhrase::text)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Error extracting key phrases with AWS Comprehend", e);
            throw new RuntimeException("AWS key phrase extraction failed", e);
        }
    }

    /**
     * Extract entities using AWS Comprehend
     */
    public List<AiAnalysisService.EntityResult> extractEntities(String text) {
        if (!isServiceAvailable || comprehendClient == null) {
            throw new RuntimeException("AWS Comprehend service not available");
        }

        try {
            DetectEntitiesRequest request = DetectEntitiesRequest.builder()
                    .text(text)
                    .languageCode(LanguageCode.EN)
                    .build();

            DetectEntitiesResponse response = comprehendClient.detectEntities(request);

            return response.entities().stream()
                    .map(entity -> new AiAnalysisService.EntityResult(
                            entity.text(),
                            entity.type().toString(),
                            entity.score()))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Error extracting entities with AWS Comprehend", e);
            throw new RuntimeException("AWS entity extraction failed", e);
        }
    }

    /**
     * Detect the language of the text
     */
    public String detectLanguage(String text) {
        if (!isServiceAvailable || comprehendClient == null) {
            return "en"; // Default fallback
        }

        try {
            DetectDominantLanguageRequest request = DetectDominantLanguageRequest.builder()
                    .text(text)
                    .build();

            DetectDominantLanguageResponse response = comprehendClient.detectDominantLanguage(request);

            return response.languages().stream()
                    .findFirst()
                    .map(DominantLanguage::languageCode)
                    .orElse("en");

        } catch (Exception e) {
            logger.error("Error detecting language with AWS Comprehend", e);
            return "en";
        }
    }

    public void shutdown() {
        if (comprehendClient != null) {
            try {
                comprehendClient.close();
                logger.info("AWS Comprehend client closed successfully");
            } catch (Exception e) {
                logger.error("Error closing AWS Comprehend client", e);
            }
        }
    }
}