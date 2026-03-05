package com.SkillExchange.service;

import com.SkillExchange.model.SkillSuggestion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    @Value("${gemini.api.key}")
    private String apiKey;

    /**
     * PERMANENT 2026 FIX: 
     * - Changed from 'gemini-1.5-flash' (RETIRED) to 'gemini-3-flash' (ACTIVE)
     * - Used 'v1beta' which is required for the Gemini 3 series preview
     */
    private final String AI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";
    
    public SkillSuggestion getRecommendedPath(String skillSet) {
        RestTemplate restTemplate = new RestTemplate();
        String fullUrl = AI_URL + apiKey;

        // Prompt designed for the 2026 Gemini 3 engine
        String promptText = "User has these skills: '" + skillSet + "'. " +
                            "Suggest the best advanced career role to pursue next. " +
                            "Respond ONLY in this format: NextStep: [Role] | Explanation: [Reason] | Related: [S1, S2, S3]";

        try {
            // Updated JSON request body structure for Gemini 3
            Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                    Map.of("parts", new Object[]{
                        Map.of("text", promptText)
                    })
                }
            );

            // Execute POST
            Map<String, Object> response = restTemplate.postForObject(fullUrl, requestBody, Map.class);
            
            // Safe extraction from the new response format
            if (response != null && response.containsKey("candidates")) {
                List<?> candidates = (List<?>) response.get("candidates");
                Map<?, ?> firstCandidate = (Map<?, ?>) candidates.get(0);
                Map<?, ?> content = (Map<?, ?>) firstCandidate.get("content");
                List<?> parts = (List<?>) content.get("parts");
                Map<?, ?> firstPart = (Map<?, ?>) parts.get(0);
                String aiResponse = (String) firstPart.get("text");

                return parseAiResponse(aiResponse, skillSet);
            }
            
            throw new RuntimeException("Empty response from Gemini 3 API");

        } catch (Exception e) {
            // This will now show the actual error if any, but the 404 should be gone
            System.err.println("CRITICAL AI ERROR: " + e.getMessage());
            return getFallbackSuggestion(skillSet);
        }
    }

    private SkillSuggestion parseAiResponse(String text, String skillSet) {
        SkillSuggestion suggestion = new SkillSuggestion();
        suggestion.setCurrentSkill(skillSet);
        
        try {
            String cleanText = text.replace("**", "").replace("`", "").trim();
            String[] parts = cleanText.split("\\|");
            
            if (parts.length >= 3) {
                suggestion.setNextStep(parts[0].replace("NextStep:", "").trim());
                suggestion.setExplanation(parts[1].replace("Explanation:", "").trim());
                
                String relatedRaw = parts[2].replace("Related:", "").trim();
                List<String> related = Arrays.stream(relatedRaw.split(","))
                                             .map(String::trim)
                                             .filter(s -> !s.isEmpty())
                                             .collect(Collectors.toList());
                suggestion.setRelatedSkills(related);
                return suggestion;
            }
        } catch (Exception e) {
            System.err.println("Parsing error: " + e.getMessage());
        }
        return getFallbackSuggestion(skillSet);
    }

    private SkillSuggestion getFallbackSuggestion(String skillSet) {
        SkillSuggestion suggestion = new SkillSuggestion();
        suggestion.setCurrentSkill(skillSet);
        suggestion.setNextStep("Gemini 3 Connection Error");
        suggestion.setExplanation("Could not reach the Gemini 3.1 server. Check your API key and network.");
        suggestion.setRelatedSkills(Arrays.asList("Update API Key", "Check URL", "Restart Backend"));
        return suggestion;
    }
}