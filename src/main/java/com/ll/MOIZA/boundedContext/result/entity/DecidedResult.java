package com.ll.MOIZA.boundedContext.result.entity;

import com.ll.MOIZA.base.entity.BaseEntity;
import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @ManyToMany(fetch = FetchType.LAZY)
    @ToString.Exclude
    @Builder.Default
    private List<Member> participationMembers = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @ToString.Exclude
    @Builder.Default
    private List<Member> nonParticipationMembers = new ArrayList<>();
}