package com.SkillExchange.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true) // Important for inherited fields
@Document(collection = "users")
public class User extends BaseUser {
    private String username;
    private String profilePic;
    private String timezone;

    // ✅ REMOVED: bio, location, skillsOffered, skillsWanted 
    // They are now inherited from BaseUser. Adding them here causes the crash.

    private List<String> languages = new ArrayList<>(); 
    private int karmaPoints = 100; 
    private double trustScore = 0.0;
    
    private Avaliability avaliable = Avaliability.FLEXIBLE;
    public enum Avaliability { FLEXIBLE, WEEKDAYS, WEEKENDS, EVENINGS }

    public User(BaseUser baseUser) {
        this.setId(baseUser.getId());
        this.setName(baseUser.getName());
        this.setEmail(baseUser.getEmail());
        this.setPassword(baseUser.getPassword());
        this.setRole(baseUser.getRole());
        // Inherited fields from BaseUser
        this.setBio(baseUser.getBio());
        this.setLocation(baseUser.getLocation());
        this.setSkillsOffered(baseUser.getSkillsOffered());
        this.setSkillsWanted(baseUser.getSkillsWanted());
    }

    // Cleaning up the helper methods to use inherited fields
    public List<String> getSkills() {
        return this.getSkillsOffered(); 
    }

    public void setSkills(List<String> skills) {
        this.setSkillsOffered(skills);
    }
}