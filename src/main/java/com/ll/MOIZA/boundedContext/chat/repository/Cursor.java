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

    public Cursor(List<T> content, ID nextCursor) {
        super(content);
        this.nextCursorId = nextCursor;
    }

    public static <T extends Identifiable<ID>, ID> Cursor<T, ID> of(List<T> chats, boolean hasNext, ID nextCursorId) {
        return new Cursor(chats, PageRequest.of(0, 1), hasNext, nextCursorId);
    }

    public Cursor<T, ID> wrapUp(List<T> contents, int contentSize) {
        boolean hasNext = false;
        ID nextCursorId = null;
        if (contents.size() == contentSize + 1) {
            hasNext = true;
            nextCursorId = contents.get(contentSize).getId();
            contents = contents.subList(0, contentSize);
        }

        return new Cursor<>(contents, this.getPageable(), hasNext, nextCursorId);
    }

    public Cursor<T, ID> setContents(List<T> contents) {
        return new Cursor<>(contents, this.getPageable(), this.hasNext(), this.getNextCursorId());
    }

    public Cursor<T, ID> setNextCursorId(ID nextCursorId) {
        return new Cursor<>(this.getContent(), this.getPageable(), this.hasNext(), nextCursorId);
    }

    public Cursor<T, ID> setHasNext(boolean hasNext) {
        return new Cursor<>(this.getContent(), this.getPageable(), hasNext, this.getNextCursorId());
    }
}
