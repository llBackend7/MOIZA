package com.ll.MOIZA.boundedContext.result.repository;

import com.ll.MOIZA.boundedContext.result.entity.DecidedResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResultRepository extends JpaRepository<DecidedResult, Long> {
    Optional<DecidedResult> findByRoomId(Long roomId);
}
