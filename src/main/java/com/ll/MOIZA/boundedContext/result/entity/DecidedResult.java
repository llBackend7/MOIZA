package com.ll.MOIZA.boundedContext.result.entity;

import com.ll.MOIZA.base.entity.BaseEntity;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class DecidedResult extends BaseEntity {
    private LocalDateTime decidedDayTime;
    private String decidedPlace;
    @OneToOne
    private Room room;
}
