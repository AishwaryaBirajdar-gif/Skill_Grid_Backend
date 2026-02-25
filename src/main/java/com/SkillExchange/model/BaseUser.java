package com.SkillExchange.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document; // <-- NEW IMPORT

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "baseUser") // <-- CRITICAL FIX: Link to the 'baseUser' collection
public class BaseUser {
    @Id
    private String id;

    private String name;
    private String email;
    private String password;
    private Role role = Role.USER;
    public enum Role { USER, ADMIN }
 
    public BaseUser(String name, String email, String password, Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }
}