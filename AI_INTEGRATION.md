# 🤖 AI Services Integration Guide

This guide explains how to integrate various AI services into the Smart Document Analyzer to add advanced capabilities like sentiment analysis, keyword extraction, and document categorization.

## 🎯 AI Services Overview

### Current Capabilities (MVP)

- ✅ Text extraction from PDF, DOCX, TXT
- ✅ Basic analysis (word count, reading time)
- ✅ Document summary generation

### AI Services We Can Add

1. **AWS Comprehend** - Sentiment analysis, entity recognition, key phrases
2. **AWS Textract** - Advanced document text extraction and form data
3. **OpenAI GPT** - Advanced text analysis and summarization
4. **Google Cloud AI** - Translation, entity analysis
5. **Azure Cognitive Services** - Text analytics and form recognition

## 🚀 Phase 1: AWS Comprehend Integration (Recommended)

AWS Comprehend is perfect for document analysis. It provides:

- Sentiment analysis
- Key phrase extraction
- Entity recognition
- Language detection
- Text classification

### Step 1: Add AWS Dependencies

Update `backend/pom.xml`:

```xml
<!-- Add these dependencies to your existing pom.xml -->
<dependencies>
    <!-- Existing dependencies... -->

    <!-- AWS SDK for Comprehend -->
    <dependency>
        <groupId>software.amazon.awssdk</groupId>
        <artifactId>comprehend</artifactId>
        <version>2.21.29</version>
    </dependency>

    <!-- AWS SDK Core -->
    <dependency>
        <groupId>software.amazon.awssdk</groupId>
        <artifactId>sdk-core</artifactId>
        <version>2.21.29</version>
    </dependency>

    <!-- AWS Auth -->
    <dependency>
        <groupId>software.amazon.awssdk</groupId>
        <artifactId>auth</artifactId>
        <version>2.21.29</version>
    </dependency>
</dependencies>
```

### Step 2: AWS Configuration

Add to `application.properties`:

```properties
# AWS Configuration
aws.region=us-east-1
aws.comprehend.enabled=true

# AWS Credentials (use environment variables in production)
# aws.accessKeyId=${AWS_ACCESS_KEY_ID}
# aws.secretAccessKey=${AWS_SECRET_ACCESS_KEY}
```

### Step 3: Create AWS Comprehend Service

Create `backend/src/main/java/com/analyzer/service/AwsComprehendService.java`:

```java
package com.analyzer.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.comprehend.ComprehendClient;
import software.amazon.awssdk.services.comprehend.model.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AwsComprehendService {

    private final ComprehendClient comprehendClient;

    public AwsComprehendService(@Value("${aws.region}") String region) {
        this.comprehendClient = ComprehendClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    public SentimentAnalysisResult analyzeSentiment(String text) {
        try {
            DetectSentimentRequest request = DetectSentimentRequest.builder()
                    .text(text)
                    .languageCode("en")
                    .build();

            DetectSentimentResponse response = comprehendClient.detectSentiment(request);

            return new SentimentAnalysisResult(
                    response.sentiment().toString(),
                    response.sentimentScore().positive(),
                    response.sentimentScore().negative(),
                    response.sentimentScore().neutral(),
                    response.sentimentScore().mixed()
            );
        } catch (Exception e) {
            // Fallback for when AWS is not configured
            return new SentimentAnalysisResult("NEUTRAL", 0.0f, 0.0f, 1.0f, 0.0f);
        }
    }

    public List<String> extractKeyPhrases(String text) {
        try {
            DetectKeyPhrasesRequest request = DetectKeyPhrasesRequest.builder()
                    .text(text)
                    .languageCode("en")
                    .build();

            DetectKeyPhrasesResponse response = comprehendClient.detectKeyPhrases(request);

            return response.keyPhrases().stream()
                    .map(KeyPhrase::text)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of("AWS Comprehend not configured");
        }
    }

    public List<EntityResult> extractEntities(String text) {
        try {
            DetectEntitiesRequest request = DetectEntitiesRequest.builder()
                    .text(text)
                    .languageCode("en")
                    .build();

            DetectEntitiesResponse response = comprehendClient.detectEntities(request);

            return response.entities().stream()
                    .map(entity -> new EntityResult(
                            entity.text(),
                            entity.type().toString(),
                            entity.score()
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
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
        public String getSentiment() { return sentiment; }
        public float getPositiveScore() { return positiveScore; }
        public float getNegativeScore() { return negativeScore; }
        public float getNeutralScore() { return neutralScore; }
        public float getMixedScore() { return mixedScore; }
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
        public String getText() { return text; }
        public String getType() { return type; }
        public float getConfidence() { return confidence; }
    }
}
```

### Step 4: Update DocumentAnalysis Model

Update `backend/src/main/java/com/analyzer/model/DocumentAnalysis.java`:

```java
// Add these new fields to the DocumentAnalysis class
private String sentiment;
private float sentimentPositive;
private float sentimentNegative;
private float sentimentNeutral;
private List<String> keyPhrases;
private List<EntityResult> entities;

// Add corresponding getters and setters
public String getSentiment() { return sentiment; }
public void setSentiment(String sentiment) { this.sentiment = sentiment; }

public float getSentimentPositive() { return sentimentPositive; }
public void setSentimentPositive(float sentimentPositive) { this.sentimentPositive = sentimentPositive; }

public float getSentimentNegative() { return sentimentNegative; }
public void setSentimentNegative(float sentimentNegative) { this.sentimentNegative = sentimentNegative; }

public float getSentimentNeutral() { return sentimentNeutral; }
public void setSentimentNeutral(float sentimentNeutral) { this.sentimentNeutral = sentimentNeutral; }

public List<String> getKeyPhrases() { return keyPhrases; }
public void setKeyPhrases(List<String> keyPhrases) { this.keyPhrases = keyPhrases; }

public List<EntityResult> getEntities() { return entities; }
public void setEntities(List<EntityResult> entities) { this.entities = entities; }
```

### Step 5: Update DocumentAnalysisService

Update the service to use AI:

```java
// Add this to DocumentAnalysisService.java
@Autowired
private AwsComprehendService comprehendService;

// Update the analyzeDocument method
public DocumentAnalysis analyzeDocument(MultipartFile file) throws IOException {
    // ... existing code ...

    // Extract text based on file type
    String extractedText = extractText(file, fileType);
    analysis.setExtractedText(extractedText);

    // NEW: Add AI analysis
    if (extractedText != null && !extractedText.trim().isEmpty()) {
        // Sentiment Analysis
        AwsComprehendService.SentimentAnalysisResult sentiment =
            comprehendService.analyzeSentiment(extractedText);
        analysis.setSentiment(sentiment.getSentiment());
        analysis.setSentimentPositive(sentiment.getPositiveScore());
        analysis.setSentimentNegative(sentiment.getNegativeScore());
        analysis.setSentimentNeutral(sentiment.getNeutralScore());

        // Key Phrases
        List<String> keyPhrases = comprehendService.extractKeyPhrases(extractedText);
        analysis.setKeyPhrases(keyPhrases);

        // Entities
        List<AwsComprehendService.EntityResult> entities =
            comprehendService.extractEntities(extractedText);
        analysis.setEntities(entities);
    }

    // Store analysis
    analysisStorage.put(documentId, analysis);

    return analysis;
}
```

## 🌟 Phase 2: OpenAI Integration (Alternative/Additional)

For more advanced text analysis and summarization:

### Add OpenAI Dependencies

```xml
<dependency>
    <groupId>com.theokanning.openai-gpt3-java</groupId>
    <artifactId>service</artifactId>
    <version>0.18.2</version>
</dependency>
```

### OpenAI Service Implementation

```java
@Service
public class OpenAiService {

    private final OpenAiApi openAiApi;

    public OpenAiService(@Value("${openai.api.key}") String apiKey) {
        this.openAiApi = new OpenAiApi(apiKey);
    }

    public String generateAdvancedSummary(String text) {
        CompletionRequest request = CompletionRequest.builder()
            .model("gpt-3.5-turbo-instruct")
            .prompt("Summarize the following document in 2-3 sentences, highlighting key points:\n\n" + text)
            .maxTokens(150)
            .temperature(0.3)
            .build();

        return openAiApi.createCompletion(request)
            .getChoices().get(0).getText().trim();
    }

    public List<String> extractTopics(String text) {
        CompletionRequest request = CompletionRequest.builder()
            .model("gpt-3.5-turbo-instruct")
            .prompt("Extract the main topics and themes from this document. Return as comma-separated list:\n\n" + text)
            .maxTokens(100)
            .temperature(0.2)
            .build();

        String response = openAiApi.createCompletion(request)
            .getChoices().get(0).getText().trim();

        return Arrays.stream(response.split(","))
            .map(String::trim)
            .collect(Collectors.toList());
    }
}
```

## 🎨 Frontend Updates for AI Features

Update `AnalysisResult.js` to show AI results:

```jsx
// Add this section after the metrics section
{
  analysis.sentiment && (
    <div className="ai-analysis-section">
      <h3>🤖 AI Analysis</h3>

      {/* Sentiment Analysis */}
      <div className="sentiment-card">
        <h4>Sentiment: {analysis.sentiment}</h4>
        <div className="sentiment-scores">
          <div className="score">
            <span>Positive:</span>
            <span>{(analysis.sentimentPositive * 100).toFixed(1)}%</span>
          </div>
          <div className="score">
            <span>Negative:</span>
            <span>{(analysis.sentimentNegative * 100).toFixed(1)}%</span>
          </div>
          <div className="score">
            <span>Neutral:</span>
            <span>{(analysis.sentimentNeutral * 100).toFixed(1)}%</span>
          </div>
        </div>
      </div>

      {/* Key Phrases */}
      {analysis.keyPhrases && analysis.keyPhrases.length > 0 && (
        <div className="key-phrases-section">
          <h4>🔑 Key Phrases</h4>
          <div className="phrases-list">
            {analysis.keyPhrases.slice(0, 10).map((phrase, index) => (
              <span key={index} className="phrase-tag">
                {phrase}
              </span>
            ))}
          </div>
        </div>
      )}

      {/* Entities */}
      {analysis.entities && analysis.entities.length > 0 && (
        <div className="entities-section">
          <h4>🏷️ Detected Entities</h4>
          <div className="entities-list">
            {analysis.entities.map((entity, index) => (
              <div key={index} className="entity-item">
                <span className="entity-text">{entity.text}</span>
                <span className="entity-type">{entity.type}</span>
                <span className="entity-confidence">{(entity.confidence * 100).toFixed(0)}%</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
```

## 🔧 Setup Instructions

### 1. AWS Setup (For AWS Comprehend)

```bash
# Install AWS CLI
brew install awscli  # macOS
# or
sudo apt install awscli  # Linux

# Configure AWS credentials
aws configure
# Enter your:
# - AWS Access Key ID
# - AWS Secret Access Key
# - Default region (e.g., us-east-1)
# - Default output format (json)
```

### 2. Environment Variables

Create `.env` file in backend root:

```properties
AWS_ACCESS_KEY_ID=your_access_key
AWS_SECRET_ACCESS_KEY=your_secret_key
AWS_REGION=us-east-1
OPENAI_API_KEY=your_openai_key_if_using
```

### 3. Build and Run

```bash
# Backend
cd backend
mvn clean install
java -jar target/smart-document-analyzer-0.0.1-SNAPSHOT.jar

# Frontend (new terminal)
cd frontend
npm start
```

## 📊 AI Services Comparison

| Service             | Pros                            | Cons                  | Use Case                         |
| ------------------- | ------------------------------- | --------------------- | -------------------------------- |
| **AWS Comprehend**  | Easy integration, comprehensive | AWS account needed    | Sentiment, entities, key phrases |
| **OpenAI GPT**      | Very advanced, creative         | Costs per request     | Advanced summaries, insights     |
| **Google Cloud AI** | Good translation support        | Google account needed | Multi-language documents         |
| **Azure Cognitive** | Great for forms/tables          | Azure account needed  | Structured document analysis     |

## 🎯 Recommended Implementation Order

1. **Start with AWS Comprehend** - Most comprehensive for document analysis
2. **Add OpenAI** - For advanced summarization
3. **Integrate AWS Textract** - For form/table extraction
4. **Add other services** - Based on specific needs

## 💰 Cost Considerations

- **AWS Comprehend**: ~$0.0001 per 100 characters
- **OpenAI GPT-3.5**: ~$0.002 per 1K tokens
- **Free tiers available** for most services
- **Cache results** to minimize repeated analysis

## 🔒 Security Best Practices

1. **Never hardcode API keys**
2. **Use environment variables**
3. **Implement rate limiting**
4. **Validate and sanitize input**
5. **Use AWS IAM roles** in production

## 🚀 Next Steps After Integration

1. **Add document categorization**
2. **Implement batch processing**
3. **Create analysis history**
4. **Add export functionality**
5. **Build analytics dashboard**

---

This integration will transform your Simple Document Analyzer into a powerful AI-driven tool that provides deep insights into uploaded documents! 🤖✨
