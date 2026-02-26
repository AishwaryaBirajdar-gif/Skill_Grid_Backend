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
@Document(collection = "baseUser")
public class BaseUser {
    @Id
    private String id;
    
    private String name;
    private String email;
    private String password;
    private Role role = Role.USER;
    public enum Role { USER, ADMIN }

    @Field("base_location") // This tells MongoDB to save this specifically as 'base_location'
    private String location;
    private String bio;
    
    // ✅ SKILL LISTS
    private List<String> skillsOffered = new ArrayList<>();
    private List<String> skillsWanted = new ArrayList<>();

    // ✅ INCREMENT BOXES: Counts for your UI
    private int skillsOfferedCount = 0;
    private int skillsWantedCount = 0;

    public BaseUser(String name, String email, String password, Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    /**
     * Helper method to update counts automatically before saving.
     * You can call this in your Service layer before calling repository.save().
     */
    public void refreshCounts() {
        this.skillsOfferedCount = (skillsOffered != null) ? skillsOffered.size() : 0;
        this.skillsWantedCount = (skillsWanted != null) ? skillsWanted.size() : 0;
    }
}