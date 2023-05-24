package com.ll.MOIZA.boundedContext.selectedTime.entity;

import com.ll.MOIZA.base.entity.BaseEntity;
import com.ll.MOIZA.boundedContext.room.entity.EnterRoom;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class SelectedTime extends BaseEntity {
    private LocalDate day;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    @ManyToOne
    private EnterRoom enterRoom;
}
