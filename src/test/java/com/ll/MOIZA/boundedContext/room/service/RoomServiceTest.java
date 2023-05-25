package com.ll.MOIZA.boundedContext.room.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class RoomServiceTest {
    @Autowired
    RoomService roomService;

    @Test
    void 방_만들기() {

    }
}