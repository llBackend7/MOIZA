package com.ll.MOIZA.boundedContext.chat.repository;

import lombok.Getter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

public class Cursor<T> extends SliceImpl<T> {
    @Getter
    private final String nextCursor;

    public Cursor(List<T> content, Pageable pageable, boolean hasNext, String nextCursor) {
        super(content, pageable, hasNext);
        this.nextCursor = nextCursor;
    }

    public Cursor(List<T> content, String nextCursor) {
        super(content);
        this.nextCursor = nextCursor;
    }
}
