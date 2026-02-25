package com.SkillExchange.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import com.SkillExchange.model.BaseUser.Role;

import java.util.ArrayList;
import java.util.List;

//User.java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User extends BaseUser {
 private String username;
 private String profilePic;
 private String bio;
 private String location;
 private String timezone;

 private List<String> languages; 
 private int karmaPoints = 100; // Updated to 100 per roadmap
 private double trustScore = 0.0;
 
 // MATCHING ROADMAP FIELDS
 private List<String> skillsOffered; 
 private List<String> skillsWanted;

 private Avaliability avaliable = Avaliability.FLEXIBLE;
 public enum Avaliability { FLEXIBLE, WEEKDAYS, WEEKENDS, EVENINGS }

 public User(BaseUser baseUser) {
     this.setId(baseUser.getId());
     this.setName(baseUser.getName());
     this.setEmail(baseUser.getEmail());
     this.setPassword(baseUser.getPassword());
     this.setRole(baseUser.getRole());
 }

public List<String> getSkills() {
	// TODO Auto-generated method stub
	return null;
}

public void setSkills(ArrayList arrayList) {
	// TODO Auto-generated method stub
	
}
}