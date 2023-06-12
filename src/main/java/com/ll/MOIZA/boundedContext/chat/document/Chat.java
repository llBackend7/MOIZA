package com.ll.MOIZA.boundedContext.chat.document;

import jakarta.persistence.EntityListeners;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "chat")
@Getter
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Chat implements Comparable<Chat>{
    @Id
    private String id;

    @Indexed
    private String roomId;
    private String memberId;

    private String writer;
    private String profile;
    private String content;

    @CreatedDate
    @Setter
    private LocalDateTime createDate;

    @LastModifiedDate
    @Setter
    private LocalDateTime modifyDate;

    @Override
    public int compareTo(Chat o) {
        return createDate.isBefore(o.createDate) ? -1 : 1;
    }
}
