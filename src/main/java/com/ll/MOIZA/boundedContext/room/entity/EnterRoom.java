package com.ll.MOIZA.boundedContext.room.entity;

import com.ll.MOIZA.base.entity.BaseEntity;
import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.selectedPlace.entity.SelectedPlace;
import com.ll.MOIZA.boundedContext.selectedTime.entity.SelectedTime;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class EnterRoom extends BaseEntity {
    @ManyToOne
    private Room room;
    @ManyToOne
    private Member member;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "enterRoom", cascade = CascadeType.ALL)
    @ToString.Exclude
    @Builder.Default
    private List<SelectedTime> selectedTimes = new ArrayList<>();
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "enterRoom", cascade = CascadeType.ALL)
    @ToString.Exclude
    @Builder.Default
    private List<SelectedPlace> selectedPlaces = new ArrayList<>();
}
