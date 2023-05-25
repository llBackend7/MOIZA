package com.ll.MOIZA.boundedContext.room.entity;

import com.ll.MOIZA.base.entity.BaseEntity;
import com.ll.MOIZA.boundedContext.member.entity.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class Room extends BaseEntity {
    private String name;
    private String description;
    private LocalDate availableStartDay;
    private LocalDate availableEndDay;
    private LocalTime availableStartTime;
    private LocalTime availableEndTime;
    private LocalTime deadLine;
    private LocalTime meetingDuration;
    private String accessCode;

    @OneToOne
    @JoinColumn(name = "leaderId")
    private Member leader;
}
