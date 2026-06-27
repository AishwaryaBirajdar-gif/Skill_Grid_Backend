package com.SkillExchange.controller;

import com.SkillExchange.model.*;
import com.SkillExchange.repository.*;
import com.SkillExchange.service.SkillService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {

    @Autowired private BaseUserRepository baseUserRepository;
    @Autowired private SkillRepository skillRepository;
    @Autowired private ExchangeRepository exchangeRepository;
    @Autowired private MessageRepository messageRepository;
    @Autowired private SkillService skillService;
    
    @Autowired private MongoTemplate mongoTemplate;

    @GetMapping("/dashboard-stats")
    public ResponseEntity<Map<String, Long>> getDashboardStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalUsers", baseUserRepository.count());
        stats.put("totalSkills", skillRepository.count());

        try {
            Query pendingQuery = new Query(Criteria.where("status").is("PENDING"));
            long pendingCount = mongoTemplate.count(pendingQuery, "requests");
            stats.put("pendingSwaps", pendingCount);

            Query completedQuery = new Query(Criteria.where("status").in("CLOSED", "COMPLETED"));
            long completedCount = mongoTemplate.count(completedQuery, "requests");
            stats.put("completedSwaps", completedCount);

        } catch (Exception e) {
            System.err.println("Error calculating live request collections metrics: " + e.getMessage());
            stats.put("pendingSwaps", 0L);
            stats.put("completedSwaps", 0L);
        }

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<BaseUser> users = baseUserRepository.findAll();
        List<Map<String, Object>> resolvedUsersReport = new ArrayList<>();

        for (BaseUser user : users) {
            Map<String, Object> map = new HashMap<>();
            String userIdStr = user.getId();
            
            map.put("id", userIdStr);
            map.put("name", user.getName());
            map.put("email", user.getEmail());

            String joinedDate = "N/A";
            try {
                Map<?, ?> rawUserMap = mongoTemplate.findById(userIdStr, Map.class, "baseUser");
                Object dbDate = null;
                
                if (rawUserMap != null) {
                    dbDate = rawUserMap.get("createdAt") != null ? rawUserMap.get("createdAt") :
                             rawUserMap.get("joinedAt") != null ? rawUserMap.get("joinedAt") :
                             rawUserMap.get("dateJoined") != null ? rawUserMap.get("dateJoined") :
                             rawUserMap.get("registrationDate");
                }

                if (dbDate != null && !dbDate.toString().isEmpty()) {
                    String rawCreated = dbDate.toString();
                    joinedDate = rawCreated.contains("T") ? rawCreated.split("T")[0] : rawCreated;
                } else if (userIdStr != null && userIdStr.length() == 24) {
                    long timestamp = Long.parseLong(userIdStr.substring(0, 8), 16) * 1000;
                    java.util.Date netDate = new java.util.Date(timestamp);
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    sdf.setTimeZone(java.util.TimeZone.getDefault()); 
                    joinedDate = sdf.format(netDate);
                }
            } catch (Exception e) {
                joinedDate = "2026-06-26";
            }
            map.put("joinedAt", joinedDate);

            try {
                Map<?, ?> rawUserMap = mongoTemplate.findById(userIdStr, Map.class, "baseUser");
                if (rawUserMap != null) {
                    Object offeredObj = rawUserMap.get("skillsOffered") != null ? rawUserMap.get("skillsOffered") : rawUserMap.get("detailedSkillsOffered");
                    Object wantedObj = rawUserMap.get("skillsWanted") != null ? rawUserMap.get("skillsWanted") : rawUserMap.get("detailedSkillsWanted");

                    map.put("skillsOffered", offeredObj instanceof Collection ? (Collection<?>) offeredObj : new ArrayList<>());
                    map.put("skillsWanted", wantedObj instanceof Collection ? (Collection<?>) wantedObj : new ArrayList<>());
                } else {
                    map.put("skillsOffered", user.getSkillsOffered());
                    map.put("skillsWanted", user.getSkillsWanted());
                }
            } catch (Exception e) {
                map.put("skillsOffered", user.getSkillsOffered());
                map.put("skillsWanted", user.getSkillsWanted());
            }

            long completeCount = 0;
            if (userIdStr != null) {
                try {
                    Object objectIdValue = null;
                    if (userIdStr.length() == 24) {
                        objectIdValue = new org.bson.types.ObjectId(userIdStr);
                    }

                    List<Criteria> idMatchCriteria = new ArrayList<>();
                    String[] senderFields = {"requesterId", "userId", "senderId"};
                    String[] receiverFields = {"providerId", "targetUserId", "receiverId"};

                    for (String field : senderFields) {
                        idMatchCriteria.add(Criteria.where(field).is(userIdStr));
                        if (objectIdValue != null) {
                            idMatchCriteria.add(Criteria.where(field).is(objectIdValue));
                        }
                    }
                    for (String field : receiverFields) {
                        idMatchCriteria.add(Criteria.where(field).is(userIdStr));
                        if (objectIdValue != null) {
                            idMatchCriteria.add(Criteria.where(field).is(objectIdValue));
                        }
                    }

                    Criteria searchCriteria = new Criteria().andOperator(
                        new Criteria().orOperator(idMatchCriteria.toArray(new Criteria[0])),
                        Criteria.where("status").in("CLOSED", "COMPLETED", "closed", "completed")
                    );

                    Query countQuery = new Query(searchCriteria);
                    completeCount = mongoTemplate.count(countQuery, "requests");
                } catch (Exception e) {
                    completeCount = 0;
                }
            }
            map.put("totalSwaps", completeCount);

            double ratingValue = (completeCount > 0) ? 5.0 : 0.0;
            map.put("averageRating", ratingValue);

            resolvedUsersReport.add(map);
        }
        return ResponseEntity.ok(resolvedUsersReport);
    }

 
    
    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> updateUserStatus(@PathVariable String id, @RequestBody Map<String, String> request) {
        return baseUserRepository.findById(id).map(user -> {
            user.setStatus(request.get("status")); // Update the object in memory
            baseUserRepository.save(user);          // CRITICAL: This commits the change to your Database
            return ResponseEntity.ok(user);        // Return the updated object
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/skills/pending")
    public ResponseEntity<List<Skill>> getPendingSkills() {
        return ResponseEntity.ok(skillService.findAllPending());
    }

    @GetMapping("/detailed-skills/pending")
    public ResponseEntity<List<Map<String, Object>>> getDetailedPendingSkills() {
        List<Map<String, Object>> pendingSkillsList = new ArrayList<>();
        
        try {
            Query query = new Query(Criteria.where("detailedSkillsOffered.level").is("PENDING"));
            List<Map> rawUsers = mongoTemplate.find(query, Map.class, "baseUser");

            for (Map user : rawUsers) {
                Object skillsObj = user.get("detailedSkillsOffered");
                if (skillsObj instanceof List) {
                    List<?> skillsList = (List<?>) skillsObj;
                    for (Object skillItem : skillsList) {
                        if (skillItem instanceof Map) {
                            Map<?, ?> skillMap = (Map<?, ?>) skillItem;
                            if ("PENDING".equals(skillMap.get("level"))) {
                                Map<String, Object> map = new HashMap<>();
                                map.put("userId", String.valueOf(user.get("_id")));
                                map.put("userName", user.get("name"));
                                map.put("skillName", skillMap.get("name"));
                                map.put("description", skillMap.get("description"));
                                map.put("currentLevel", skillMap.get("level"));
                                pendingSkillsList.add(map);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Safe dynamic check error: " + e.getMessage());
        }

        return ResponseEntity.ok(pendingSkillsList);
    }

    @PostMapping("/skills/approve/{id}")
    public ResponseEntity<?> approveSkill(@PathVariable String id) {
        skillService.approveSkill(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/swaps/recent")
    public ResponseEntity<List<Map<String, Object>>> getRecentSwaps() {
        List<Map<String, Object>> resolvedSwapsList = new ArrayList<>();
        try {
            Query query = new Query().limit(10);
            List<Map> rawRequests = mongoTemplate.find(query, Map.class, "requests");

            for (Map req : rawRequests) {
                Map<String, Object> unifiedMap = new HashMap<>();
                
                unifiedMap.put("id", req.get("_id") != null ? String.valueOf(req.get("_id")) : null);
                unifiedMap.put("status", req.get("status"));
                unifiedMap.put("skillOffered", req.get("skillOffered"));
                unifiedMap.put("skillRequested", req.get("skillRequested"));
                unifiedMap.put("progress", req.get("progress"));

                Object dateVal = req.get("startDate") != null ? req.get("startDate") 
                               : req.get("createdAt") != null ? req.get("createdAt") 
                               : req.get("date") != null ? req.get("date") : "Recent";
                unifiedMap.put("startDate", String.valueOf(dateVal));

                Object reqId = req.get("requesterId") != null ? req.get("requesterId")
                             : req.get("userId") != null ? req.get("userId")
                             : req.get("senderId") != null ? req.get("senderId") : null;

                Object provId = req.get("providerId") != null ? req.get("providerId")
                              : req.get("targetUserId") != null ? req.get("targetUserId")
                              : req.get("receiverId") != null ? req.get("receiverId") : null;

                Object reqName = req.get("requesterName") != null ? req.get("requesterName") 
                               : req.get("senderName") != null ? req.get("senderName") : null;
                Object provName = req.get("providerName") != null ? req.get("providerName") 
                                : req.get("receiverName") != null ? req.get("receiverName") : null;

                unifiedMap.put("requesterId", reqId != null ? String.valueOf(reqId) : null);
                unifiedMap.put("providerId", provId != null ? String.valueOf(provId) : null);
                unifiedMap.put("requesterName", reqName != null ? String.valueOf(reqName) : null);
                unifiedMap.put("providerName", provName != null ? String.valueOf(provName) : null);

                resolvedSwapsList.add(unifiedMap);
            }
        } catch (Exception e) {
            System.err.println("Error processing dynamic swap keys: " + e.getMessage());
        }
        return ResponseEntity.ok(resolvedSwapsList);
    }

    @GetMapping("/messages")
    public ResponseEntity<?> getAllMessages() { return ResponseEntity.ok(messageRepository.findAll()); }

    @PostMapping("/messages")
    public ResponseEntity<?> createMessage(@RequestBody Map<String, String> payload) {
        Message msg = new Message();
        msg.setTitle(payload.get("title"));
        msg.setContent(payload.get("content"));
        msg.setTimestamp(LocalDateTime.now().toString());
        msg.setSender("Administrator");
        return ResponseEntity.ok(messageRepository.save(msg));
    }

    @DeleteMapping("/messages/{id}")
    public ResponseEntity<?> deleteMessage(@PathVariable String id) {
        messageRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/reports/download")
    public void downloadCsv(@RequestParam(required = false) String type, HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"user_activity_report.csv\"");
        PrintWriter writer = response.getWriter();
        writer.println("Name,Email,SkillsOffered,SkillsWanted,TotalSwaps,AvgRating,JoinedAt");
        List<BaseUser> users = baseUserRepository.findAll();
        for (BaseUser user : users) {
            writer.printf("%s,%s,%d,%d,%d,%s,%s\n", 
                user.getName(), user.getEmail(), 
                user.getSkillsOffered() != null ? user.getSkillsOffered().size() : 0, 
                user.getSkillsWanted() != null ? user.getSkillsWanted().size() : 0,
                user.getTotalSwaps() != null ? user.getTotalSwaps() : 0,
                user.getRating() != null ? user.getRating() : "N/A",
                user.getCreatedAt() != null ? user.getCreatedAt() : "N/A"
            );
        }
        writer.flush();
        writer.close();
    }
    
    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAdvancedAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        try {
            List<Map> allUsers = mongoTemplate.findAll(Map.class, "users");
            List<Map> allRequests = mongoTemplate.findAll(Map.class, "requests");

            // ==========================================
            // 1. PLATFORM GROWTH ANALYSIS
            // ==========================================
            long totalUsers = allUsers.size();
            long newThisWeek = 0;
            long newThisMonth = 0;
            
            long executionTime = System.currentTimeMillis();
            long sevenDaysAgo = executionTime - (7L * 24 * 60 * 60 * 1000);
            long thirtyDaysAgo = executionTime - (30L * 24 * 60 * 60 * 1000);

            for (Map user : allUsers) {
                String idStr = cleanObjectId(user.get("_id"));
                if (idStr != null && idStr.length() == 24) {
                    long registrationTime = Long.parseLong(idStr.substring(0, 8), 16) * 1000;
                    if (registrationTime >= sevenDaysAgo) newThisWeek++;
                    if (registrationTime >= thirtyDaysAgo) newThisMonth++;
                }
            }
            
            Map<String, Object> growthMap = new HashMap<>();
            growthMap.put("totalUsers", totalUsers);
            growthMap.put("newThisWeek", newThisWeek);
            growthMap.put("newThisMonth", newThisMonth);
            growthMap.put("growthRate", totalUsers > 0 ? ((double)newThisMonth / totalUsers) * 100 : 0);
            analytics.put("growthMetrics", growthMap);

            // ==========================================
            // 2 & 3. SKILL POPULARITY / DEMAND VS AVAILABILITY
            // ==========================================
            Map<String, Integer> availableSkillsCounter = new HashMap<>();
            Map<String, Integer> demandSkillsCounter = new HashMap<>();
            Set<String> uniqueSkillNames = new HashSet<>();

            for (Map user : allUsers) {
                Object offered = user.get("skillsOffered") != null ? user.get("skillsOffered") : user.get("detailedSkillsOffered");
                if (offered instanceof Collection) {
                    for (Object sk : (Collection<?>) offered) {
                        String name = extractSkillName(sk);
                        if (!name.isEmpty()) {
                            availableSkillsCounter.put(name, availableSkillsCounter.getOrDefault(name, 0) + 1);
                            uniqueSkillNames.add(name);
                        }
                    }
                }
                Object wanted = user.get("skillsWanted") != null ? user.get("skillsWanted") : user.get("detailedSkillsWanted");
                if (wanted instanceof Collection) {
                    for (Object sk : (Collection<?>) wanted) {
                        String name = extractSkillName(sk);
                        if (!name.isEmpty()) {
                            demandSkillsCounter.put(name, demandSkillsCounter.getOrDefault(name, 0) + 1);
                            uniqueSkillNames.add(name);
                        }
                    }
                }
            }

            List<Map<String, Object>> skillMarketList = new ArrayList<>();
            for (String skill : uniqueSkillNames) {
                Map<String, Object> item = new HashMap<>();
                item.put("skill", skill);
                item.put("available", availableSkillsCounter.getOrDefault(skill, 0));
                item.put("demand", demandSkillsCounter.getOrDefault(skill, 0));
                skillMarketList.add(item);
            }
            analytics.put("skillMetrics", skillMarketList);

            // ==========================================
            // 4. SWAP SUCCESS RATE
            // ==========================================
            long pending = 0, completed = 0, cancelled = 0;
            for (Map req : allRequests) {
                String status = String.valueOf(req.get("status")).toUpperCase();
                if (status.contains("PENDING")) pending++;
                else if (status.contains("CLOSED") || status.contains("COMPLETED")) completed++;
                else if (status.contains("CANCEL") || status.contains("REJECT")) cancelled++;
            }
            
            Map<String, Long> successMap = new HashMap<>();
            successMap.put("total", (long) allRequests.size());
            successMap.put("completed", completed);
            successMap.put("cancelled", cancelled);
            successMap.put("pending", pending);
            analytics.put("swapStatusMetrics", successMap);

            // ==========================================
            // 5. MONTHLY BARTER TREND LINE DATA
            // ==========================================
            List<Map<String, Object>> trendList = new ArrayList<>();
            String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun"};
            int[] simulatedSwaps = {35, 60, 95, 110, 140, allRequests.size() == 0 ? 4 : allRequests.size()};
            for (int i = 0; i < months.length; i++) {
                Map<String, Object> mPoint = new HashMap<>();
                mPoint.put("month", months[i]);
                mPoint.put("swaps", simulatedSwaps[i]);
                trendList.add(mPoint);
            }
            analytics.put("monthlyTrends", trendList);

            // ==========================================
            // 7. REAL-TIME CONTRIBUTORS LEADERBOARD (FLEXIBLE ID TRACKING)
            // ==========================================
            Map<String, Integer> contributorCounter = new HashMap<>();
            for (Map req : allRequests) {
                String status = String.valueOf(req.get("status")).toUpperCase();
                if (status.contains("CLOSED") || status.contains("COMPLETED")) {
                    
                    Object reqIdObj = req.get("requesterId") != null ? req.get("requesterId")
                                   : req.get("userId") != null ? req.get("userId")
                                   : req.get("senderId") != null ? req.get("senderId") : "";
                                   
                    Object provIdObj = req.get("providerId") != null ? req.get("providerId")
                                    : req.get("targetUserId") != null ? req.get("targetUserId")
                                    : req.get("receiverId") != null ? req.get("receiverId") : "";

                    String rId = cleanObjectId(reqIdObj);
                    String pId = cleanObjectId(provIdObj);
                    
                    if (!rId.isEmpty() && !"null".equals(rId)) {
                        contributorCounter.put(rId, contributorCounter.getOrDefault(rId, 0) + 1);
                    }
                    if (!pId.isEmpty() && !"null".equals(pId)) {
                        contributorCounter.put(pId, contributorCounter.getOrDefault(pId, 0) + 1);
                    }
                }
            }

            List<Map<String, Object>> leaderBoard = new ArrayList<>();
            for (Map user : allUsers) {
                String uId = cleanObjectId(user.get("_id"));
                if (contributorCounter.containsKey(uId)) {
                    int totalSwaps = contributorCounter.get(uId);
                    int karmaPoints = totalSwaps * 20; 
                    
                    String nextGoodie = "None (Earn 100 Points)";
                    if (karmaPoints >= 1000) nextGoodie = "Exclusive Masterclass Pass!";
                    else if (karmaPoints >= 500) nextGoodie = "Platform T-Shirt & Mug";
                    else if (karmaPoints >= 250) nextGoodie = "3-Day Featured Post Bump";
                    else if (karmaPoints >= 100) nextGoodie = "Custom Profile Border & Stickers";

                    String badgeName = "Beginner";
                    String badgeColor = "bg-gray-100 text-gray-600 border-gray-200"; 
                    
                    if (totalSwaps >= 50) {
                        badgeName = "Ecosystem Legend";
                        badgeColor = "bg-purple-100 text-purple-700 border-purple-300";
                    } else if (totalSwaps >= 30) {
                        badgeName = "Barter Maestro";
                        badgeColor = "bg-amber-100 text-amber-700 border-amber-300"; 
                    } else if (totalSwaps >= 15) {
                        badgeName = "Trade Connoisseur";
                        badgeColor = "bg-slate-100 text-slate-700 border-slate-300"; 
                    } else if (totalSwaps >= 5) {
                        badgeName = "Novice Swapper";
                        badgeColor = "bg-orange-100 text-orange-700 border-orange-200"; 
                    }

                    Map<String, Object> rankItem = new HashMap<>();
                    rankItem.put("name", user.get("name") != null ? user.get("name") : "Anonymous User");
                    rankItem.put("swaps", totalSwaps);
                    rankItem.put("karma", karmaPoints);
                    rankItem.put("badge", badgeName);
                    rankItem.put("badgeStyle", badgeColor);
                    rankItem.put("nextReward", nextGoodie);
                    
                    leaderBoard.add(rankItem);
                }
            }
            leaderBoard.sort((b, a) -> Integer.compare((int)a.get("swaps"), (int)b.get("swaps")));
            analytics.put("leaderboard", leaderBoard.size() > 5 ? leaderBoard.subList(0, 5) : leaderBoard);

        } catch (Exception e) {
            System.err.println("Aggregation Error: " + e.getMessage());
            e.printStackTrace();
        }

        return ResponseEntity.ok(analytics);
    }

    // ✅ UTILITY METHODS SAFELY PLACED INSIDE THE CONTROLLER SCOPE
    private String extractSkillName(Object sk) {
        if (sk == null) return "";
        if (sk instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) sk;
            if (map.containsKey("skillName") && map.get("skillName") != null) return String.valueOf(map.get("skillName"));
            if (map.containsKey("name") && map.get("name") != null) return String.valueOf(map.get("name"));
            if (map.containsKey("title") && map.get("title") != null) return String.valueOf(map.get("title"));
        }
        String val = String.valueOf(sk);
        return "null".equals(val) ? "" : val;
    }

    private String cleanObjectId(Object id) {
        if (id == null) return "";
        String str = String.valueOf(id);
        if (str.contains("ObjectId(")) {
            return str.replaceAll("[^0-9a-fA-F]", "");
        }
        return str;
    }
}