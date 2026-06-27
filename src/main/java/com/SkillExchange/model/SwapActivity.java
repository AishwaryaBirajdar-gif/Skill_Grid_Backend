package com.SkillExchange.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "swapActivity")
public class SwapActivity {
    @Id
    private String id;
    private String senderName;
    private String receiverName;
    private String skillExchanged;
    private String status; // "COMPLETED", "REQUESTED"
    private LocalDateTime timestamp = LocalDateTime.now();
}