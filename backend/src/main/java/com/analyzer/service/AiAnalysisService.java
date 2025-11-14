package com.analyzer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class AiAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(AiAnalysisService.class);

    private final AwsComprehendService awsComprehendService;
    private final boolean useRealAwsService;

    // Constructor injection with optional AWS service
    public AiAnalysisService(@Autowired(required = false) AwsComprehendService awsComprehendService,
            @Value("${aws.comprehend.use-real-service:false}") boolean useRealAwsService) {
        this.awsComprehendService = awsComprehendService;
        this.useRealAwsService = useRealAwsService;

        if (useRealAwsService && awsComprehendService != null && awsComprehendService.isAvailable()) {
            logger.info("AiAnalysisService initialized with real AWS Comprehend service");
        } else {
            logger.info("AiAnalysisService initialized with mock implementation (AWS not available or disabled)");
        }
    }

    private static final String NEUTRAL_SENTIMENT = "NEUTRAL";
    private static final String POSITIVE_SENTIMENT = "POSITIVE";
    private static final String NEGATIVE_SENTIMENT = "NEGATIVE";

    // Simple sentiment analysis based on keywords (fallback when AWS is not
    // available)
    private static final Map<String, Double> POSITIVE_WORDS = Map.of(
            "good", 1.0, "great", 2.0, "excellent", 2.0, "amazing", 2.0, "wonderful", 1.5,
            "fantastic", 2.0, "awesome", 1.5, "perfect", 2.0, "love", 1.5, "best", 1.5);

    private static final Map<String, Double> MORE_POSITIVE_WORDS = Map.of(
            "success", 1.0, "achieve", 1.0, "benefit", 1.0, "improve", 1.0, "positive", 1.0);

    private static final Map<String, Double> NEGATIVE_WORDS = Map.of(
            "bad", -1.0, "terrible", -2.0, "awful", -2.0, "horrible", -2.0, "worst", -2.0,
            "hate", -1.5, "fail", -1.5, "problem", -1.0, "issue", -1.0, "error", -1.0);

    private static final Map<String, Double> MORE_NEGATIVE_WORDS = Map.of(
            "wrong", -1.0, "difficult", -0.5, "challenge", -0.5, "concern", -0.5, "negative", -1.0);

    /**
     * Comprehensive document analysis combining all AI capabilities
     */
    public ComprehensiveAnalysisResult analyzeDocument(String text) {
        SentimentAnalysisResult sentiment = analyzeSentiment(text);
        List<String> keyPhrases = extractKeyPhrases(text);
        List<EntityResult> entities = extractEntities(text);

        return new ComprehensiveAnalysisResult(sentiment, keyPhrases, entities);
    }

    public SentimentAnalysisResult analyzeSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new SentimentAnalysisResult(NEUTRAL_SENTIMENT, 0.0f, 0.0f, 1.0f, 0.0f);
        }

        // Try AWS Comprehend first if available and enabled
        if (useRealAwsService && awsComprehendService != null && awsComprehendService.isAvailable()) {
            try {
                logger.debug("Using AWS Comprehend for sentiment analysis");
                return awsComprehendService.analyzeSentiment(text);
            } catch (Exception e) {
                logger.warn("AWS Comprehend sentiment analysis failed, falling back to mock: {}", e.getMessage());
            }
        }

        // Fallback to mock implementation
        logger.debug("Using mock implementation for sentiment analysis");
        return analyzeSentimentMock(text);
    }

    /**
     * Mock sentiment analysis implementation (fallback when AWS is not available)
     */
    private SentimentAnalysisResult analyzeSentimentMock(String text) {

        String lowerText = text.toLowerCase();
        String[] words = lowerText.split("\\s+");

        double totalScore = 0.0;
        int wordCount = 0;

        for (String word : words) {
            word = cleanWord(word);

            Double positiveScore = getWordScore(word, POSITIVE_WORDS, MORE_POSITIVE_WORDS);
            if (positiveScore != null) {
                totalScore += positiveScore;
                wordCount++;
            } else {
                Double negativeScore = getWordScore(word, NEGATIVE_WORDS, MORE_NEGATIVE_WORDS);
                if (negativeScore != null) {
                    totalScore += negativeScore;
                    wordCount++;
                }
            }
        }

        return calculateSentimentScores(totalScore, wordCount);
    }

    private Double getWordScore(String word, Map<String, Double> primaryWords, Map<String, Double> secondaryWords) {
        Double score = primaryWords.get(word);
        if (score == null) {
            score = secondaryWords.get(word);
        }
        return score;
    }

    private SentimentAnalysisResult calculateSentimentScores(double totalScore, int wordCount) {
        String sentiment;
        float positiveScore = 0.0f;
        float negativeScore = 0.0f;
        float neutralScore = 0.8f;

        if (wordCount > 0) {
            double averageScore = totalScore / wordCount;

            if (averageScore > 0.5) {
                sentiment = POSITIVE_SENTIMENT;
                positiveScore = Math.min(1.0f, (float) averageScore);
                neutralScore = 1.0f - positiveScore;
            } else if (averageScore < -0.5) {
                sentiment = NEGATIVE_SENTIMENT;
                negativeScore = Math.min(1.0f, (float) Math.abs(averageScore));
                neutralScore = 1.0f - negativeScore;
            } else {
                sentiment = NEUTRAL_SENTIMENT;
                neutralScore = 1.0f;
            }
        } else {
            sentiment = NEUTRAL_SENTIMENT;
        }

        return new SentimentAnalysisResult(sentiment, positiveScore, negativeScore, neutralScore, 0.0f);
    }

    public List<String> extractKeyPhrases(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // Try AWS Comprehend first if available and enabled
        if (useRealAwsService && awsComprehendService != null && awsComprehendService.isAvailable()) {
            try {
                logger.debug("Using AWS Comprehend for key phrase extraction");
                return awsComprehendService.extractKeyPhrases(text);
            } catch (Exception e) {
                logger.warn("AWS Comprehend key phrase extraction failed, falling back to mock: {}", e.getMessage());
            }
        }

        // Fallback to mock implementation
        logger.debug("Using mock implementation for key phrase extraction");
        return extractKeyPhrasesMock(text);
    }

    /**
     * Mock key phrase extraction implementation (fallback when AWS is not
     * available)
     */
    private List<String> extractKeyPhrasesMock(String text) {

        Map<String, Integer> phraseFrequency = new HashMap<>();
        String[] words = text.toLowerCase().split("\\s+");

        // Extract meaningful single words and two-word phrases
        for (int i = 0; i < words.length; i++) {
            String word = cleanWord(words[i]);
            if (isValidWord(word) && word.length() > 4) {
                phraseFrequency.put(word, phraseFrequency.getOrDefault(word, 0) + 1);
            }

            // Two-word phrases
            if (i < words.length - 1) {
                String word2 = cleanWord(words[i + 1]);
                if (isValidWord(word) && isValidWord(word2)) {
                    String phrase = word + " " + word2;
                    phraseFrequency.put(phrase, phraseFrequency.getOrDefault(phrase, 0) + 1);
                }
            }
        }

        // Return top phrases
        return phraseFrequency.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .toList();
    }

    public List<EntityResult> extractEntities(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // Try AWS Comprehend first if available and enabled
        if (useRealAwsService && awsComprehendService != null && awsComprehendService.isAvailable()) {
            try {
                logger.debug("Using AWS Comprehend for entity extraction");
                return awsComprehendService.extractEntities(text);
            } catch (Exception e) {
                logger.warn("AWS Comprehend entity extraction failed, falling back to mock: {}", e.getMessage());
            }
        }

        // Fallback to mock implementation
        logger.debug("Using mock implementation for entity extraction");
        return extractEntitiesMock(text);
    }

    /**
     * Mock entity extraction implementation (fallback when AWS is not available)
     */
    private List<EntityResult> extractEntitiesMock(String text) {

        List<EntityResult> entities = new ArrayList<>();

        // Simple pattern-based entity extraction

        // Email addresses
        Pattern emailPattern = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b");
        emailPattern.matcher(text).results()
                .forEach(match -> entities.add(new EntityResult(match.group(), "EMAIL", 0.9f)));

        // Phone numbers (simple pattern)
        Pattern phonePattern = Pattern.compile("\\b\\d{3}[-.]?\\d{3}[-.]?\\d{4}\\b");
        phonePattern.matcher(text).results()
                .forEach(match -> entities.add(new EntityResult(match.group(), "PHONE", 0.8f)));

        // Dates (simple pattern for MM/DD/YYYY or DD/MM/YYYY)
        Pattern datePattern = Pattern.compile("\\b\\d{1,2}[/-]\\d{1,2}[/-]\\d{4}\\b");
        datePattern.matcher(text).results()
                .forEach(match -> entities.add(new EntityResult(match.group(), "DATE", 0.7f)));

        // URLs
        Pattern urlPattern = Pattern.compile("\\bhttps?://[\\w\\.-]+\\.[a-z]{2,}[/\\w\\.-]*\\b",
                Pattern.CASE_INSENSITIVE);
        urlPattern.matcher(text).results()
                .forEach(match -> entities.add(new EntityResult(match.group(), "URL", 0.9f)));

        // Capitalized words (potential names/places)
        Pattern namePattern = Pattern.compile("\\b[A-Z][a-z]{2,}\\s[A-Z][a-z]{2,}\\b");
        namePattern.matcher(text).results()
                .forEach(match -> entities.add(new EntityResult(match.group(), "PERSON", 0.6f)));

        return entities.stream()
                .distinct()
                .limit(20)
                .toList();
    }

    private String cleanWord(String word) {
        return word.replaceAll("[^a-zA-Z]", "").toLowerCase();
    }

    private boolean isValidWord(String word) {
        // Filter out common stop words and short words
        Set<String> stopWords = Set.of("the", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by",
                "is", "are", "was", "were", "be", "been", "have", "has", "had", "will", "would", "could", "should",
                "this", "that", "these", "those", "a", "an");
        return word.length() > 2 && !stopWords.contains(word);
    }

    // Data classes for results
    public static class SentimentAnalysisResult {
        private final String sentiment;
        private final float positiveScore;
        private final float negativeScore;
        private final float neutralScore;
        private final float mixedScore;

        public SentimentAnalysisResult(String sentiment, float positiveScore,
                float negativeScore, float neutralScore, float mixedScore) {
            this.sentiment = sentiment;
            this.positiveScore = positiveScore;
            this.negativeScore = negativeScore;
            this.neutralScore = neutralScore;
            this.mixedScore = mixedScore;
        }

        // Getters
        public String getSentiment() {
            return sentiment;
        }

        public float getPositiveScore() {
            return positiveScore;
        }

        public float getNegativeScore() {
            return negativeScore;
        }

        public float getNeutralScore() {
            return neutralScore;
        }

        public float getMixedScore() {
            return mixedScore;
        }
    }

    public static class EntityResult {
        private final String text;
        private final String type;
        private final float confidence;

        public EntityResult(String text, String type, float confidence) {
            this.text = text;
            this.type = type;
            this.confidence = confidence;
        }

        // Getters
        public String getText() {
            return text;
        }

        public String getType() {
            return type;
        }

        public float getConfidence() {
            return confidence;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null || getClass() != obj.getClass())
                return false;
            EntityResult that = (EntityResult) obj;
            return Objects.equals(text, that.text) && Objects.equals(type, that.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(text, type);
        }
    }

    /**
     * Comprehensive analysis result combining all AI features
     */
    public static class ComprehensiveAnalysisResult {
        private final SentimentAnalysisResult sentiment;
        private final List<String> keyPhrases;
        private final List<EntityResult> entities;

        public ComprehensiveAnalysisResult(SentimentAnalysisResult sentiment, List<String> keyPhrases,
                List<EntityResult> entities) {
            this.sentiment = sentiment;
            this.keyPhrases = keyPhrases;
            this.entities = entities;
        }

        public SentimentAnalysisResult getSentiment() {
            return sentiment;
        }

        public List<String> getKeyPhrases() {
            return keyPhrases;
        }

        public List<EntityResult> getEntities() {
            return entities;
        }
    }
}