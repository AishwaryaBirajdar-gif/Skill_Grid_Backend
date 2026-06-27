package com.SkillExchange.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users") // Ensure this is mapped to your MongoDB collection
public class BaseUser {
    @Id
    private String id;
    
    private String name;
    private String email;
    private String password;
    private Role role = Role.USER;
    
    public enum Role { USER, ADMIN }
    
    private String status;
    private Double rating;
    
    @Field("base_location")
    private String location;
    private String bio;
    
    private Integer totalSwaps = 0;
    private String createdAt;

    private List<Skill> skillsOffered = new ArrayList<>();
    private List<Skill> skillsWanted = new ArrayList<>();

    // Fields for UI counts
    private int skillsOfferedCount = 0;
    private int skillsWantedCount = 0;

    // --- Explicit Getters and Setters for Controller Compatibility ---

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public Integer getTotalSwaps() { return totalSwaps != null ? totalSwaps : 0; }
    public void setTotalSwaps(Integer totalSwaps) { this.totalSwaps = totalSwaps; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    // These are critical for the size() calls in your AdminController
    public List<Skill> getSkillsOffered() { return skillsOffered; }
    public void setSkillsOffered(List<Skill> skillsOffered) { this.skillsOffered = skillsOffered; }

    public List<Skill> getSkillsWanted() { return skillsWanted; }
    public void setSkillsWanted(List<Skill> skillsWanted) { this.skillsWanted = skillsWanted; }

    // --- Constructors ---

    public BaseUser(String name, String email, String password, Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public void refreshCounts() {
        this.skillsOfferedCount = (skillsOffered != null) ? skillsOffered.size() : 0;
        this.skillsWantedCount = (skillsWanted != null) ? skillsWanted.size() : 0;
    }
}