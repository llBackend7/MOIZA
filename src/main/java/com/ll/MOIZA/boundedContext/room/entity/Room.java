package com.ll.MOIZA.boundedContext.room.entity;

import com.ll.MOIZA.base.entity.BaseEntity;
import com.ll.MOIZA.boundedContext.member.entity.Member;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

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
    private LocalDateTime deadLine;
    private LocalTime meetingDuration;
    private String accessCode;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "room", cascade = CascadeType.ALL)
    @ToString.Exclude
    @Builder.Default
    private List<EnterRoom> enterRoom = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "leaderId")
    private Member leader;
}
