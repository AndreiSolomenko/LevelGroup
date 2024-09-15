package com.levelgroup.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name="ChatMessage")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_date")
    private LocalDateTime messageDate;

    private String userName;
    private String messageText;
    private String messageType;


    @PrePersist
    protected void onCreate() {
        messageDate = LocalDateTime.now();
    }

}
