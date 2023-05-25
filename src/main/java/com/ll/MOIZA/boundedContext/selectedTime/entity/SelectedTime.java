package com.ll.MOIZA.boundedContext.selectedTime.entity;

import com.ll.MOIZA.base.entity.BaseEntity;
import com.ll.MOIZA.boundedContext.room.entity.EnterRoom;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class SelectedTimes extends BaseEntity {
    private LocalDate day;
    private LocalTime startTime;
    private LocalTime endTime;
    @ManyToOne
    private EnterRoom enterRoom;
}
