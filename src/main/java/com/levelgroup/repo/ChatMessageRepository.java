package com.levelgroup.repo;

import com.levelgroup.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ChatMessageRepository  extends JpaRepository<ChatMessage, Long> {

    @Modifying
    @Query("DELETE FROM ChatMessage o WHERE o.messageDate < :localDateTime")
    void deleteByMessageDateBefore(@Param("localDateTime") LocalDateTime localDateTime);
}
