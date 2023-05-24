package com.ll.MOIZA.boundedContext.member.entity;

import com.ll.MOIZA.base.entity.BaseEntity;
import com.ll.MOIZA.boundedContext.room.entity.EnterRoom;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
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
public class Member extends BaseEntity {
    private String name;
    private String email;
    private String profile;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "member")
    @ToString.Exclude
    @Builder.Default
    private List<EnterRoom> enterRooms = new ArrayList<>();
}
