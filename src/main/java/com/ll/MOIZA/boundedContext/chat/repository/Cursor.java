package com.ll.MOIZA.boundedContext.chat.repository;

import com.ll.MOIZA.base.entity.Identifiable;
import lombok.Getter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

public class Cursor<T extends Identifiable<ID>, ID> extends SliceImpl<T> {
    @Getter
    private final ID nextCursorId;

    public Cursor(List<T> content, Pageable pageable, boolean hasNext, ID nextCursor) {
        super(content, pageable, hasNext);
        this.nextCursorId = nextCursor;
    }

    public static <T extends Identifiable<ID>, ID> Cursor<T, ID> of(List<T> chats, boolean hasNext, ID nextCursorId) {
        return new Cursor(chats, PageRequest.of(0, 20), hasNext, nextCursorId);
    }

}
