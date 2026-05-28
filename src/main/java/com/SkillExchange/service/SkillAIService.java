package com.SkillExchange.service;

import org.springframework.stereotype.Service;
import com.SkillExchange.DTO.SkillResponse;
import java.util.Arrays;
import java.util.List;

@Service
public class SkillAIService {

    public SkillResponse analyzeLocalSkill(String skillName, String description) {
        if (description == null || description.trim().isEmpty()) {
            return new SkillResponse(skillName, "Beginner", "No description provided.");
        }

        String cleanText = description.toLowerCase();

        // 🌍 1. UNIVERSAL INTERMEDIATE KEYWORDS (Across 5 major domains)
        List<String> intermediateTokens = Arrays.asList(
            // 💻 Technology
            "jwt", "security", "mongodb", "hibernate", "jpa", "database", "sql", "api", "restful", "hooks", "axios", "tailwind", "pandas", "oop",
            // 📚 Literature & Writing
            "grammar", "editing", "blog writing", "creative writing", "structure", "vocabulary", "character development", "proofreading", "essay",
            // 💪 Fitness & Health
            "nutrition", "calisthenics", "weight training", "cardio", "flexibility", "meal prep", "form", "posture", "stretching",
            // 🎵 Music
            "chords", "scales", "rhythm", "sight reading", "tabs", "tuning", "vocal exercises", "metronome", "acoustic",
            // 🍳 Lifestyle / Cooking / Art
            "baking", "plating", "sketching", "shading", "watercolor", "knife skills", "recipe modification"
        );

        // 🚀 2. UNIVERSAL EXPERT KEYWORDS (High complexity, professional execution)
        List<String> expertTokens = Arrays.asList(
            // 💻 Advanced Technology
            "microservices", "pipeline", "multi-tenant", "latency", "scalable", "concurrency", "multi-threading", "distributed", "apriori", "deep learning", "websockets", "stomp", "kubernetes",
            // 📚 Advanced Literature & Publishing
            "manuscript", "copyediting", "literary theory", "screenwriting", "rhetorical analysis", "publishing protocols", "investigative journalism", "phonetics",
            // 💪 Advanced Fitness & Sports Science
            "hypertrophy", "biomechanics", "periodization", "progressive overload", "kinesiology", "macro split", "vo2 max", "olympic lifting",
            // 🎵 Advanced Music Theory & Production
            "composition", "orchestration", "arpeggios", "audio mixing", "mastering", "daw", "jazz improvisation", "harmony matrices", "polyphony",
            // 🍳 Advanced Culinary & Fine Arts
            "molecular gastronomy", "sous vide", "oil painting", "digital rendering", "perspective construction", "fermentation chemistry"
        );

        // 🚨 3. ABSOLUTE SYSTEM-LOCK OVERRIDE RULES
        String evaluatedLevel = "Beginner";
        
        // Expert Check
        if (cleanText.contains("microservices") || cleanText.contains("pipeline") || 
            cleanText.contains("apriori") || cleanText.contains("deep learning") || 
            cleanText.contains("websockets") || cleanText.contains("manuscript") || 
            cleanText.contains("biomechanics") || cleanText.contains("periodization") || 
            cleanText.contains("orchestration") || cleanText.contains("composition") ||
            cleanText.contains("molecular gastronomy") || cleanText.contains("oil painting")) {
            
            evaluatedLevel = "Expert";
            
        // Intermediate Check
        } else if (cleanText.contains("jwt") || cleanText.contains("mongodb") || 
                   cleanText.contains("database") || cleanText.contains("creative writing") || 
                   cleanText.contains("proofreading") || cleanText.contains("nutrition") || 
                   cleanText.contains("calisthenics") || cleanText.contains("chords") || 
                   cleanText.contains("scales") || cleanText.contains("baking") ||
                   cleanText.contains("sketching")) {
            
            evaluatedLevel = "Intermediate";
            
        } else {
            // 📊 4. DENSITY MATRIC MULTIPLIER FALLBACK
            int score = 0;
            for (String t : intermediateTokens) { if (cleanText.contains(t)) score += 3; }
            for (String t : expertTokens) { if (cleanText.contains(t)) score += 5; }
            
            if (score >= 8) evaluatedLevel = "Expert";
            else if (score >= 3) evaluatedLevel = "Intermediate";
        }

        String summary = "Verified profile highlighting structural depth context optimized for an " 
                + evaluatedLevel.toLowerCase() + " level skill exchange environment.";

        return new SkillResponse(skillName, evaluatedLevel, summary);
    }

    public SkillResponse analyzeSkill(String skill) {
        return analyzeLocalSkill("Technology", skill);
    }
}