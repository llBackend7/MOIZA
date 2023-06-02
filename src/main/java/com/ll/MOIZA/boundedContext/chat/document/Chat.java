package com.ll.MOIZA.boundedContext.chat.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "chat")
@Getter
@Builder
public class Chat {
    @Id
    private String id;

    @Indexed
    private String roomId;
    private String memberId;

    private String writer;
    private String content;
}
