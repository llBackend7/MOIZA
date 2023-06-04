package com.ll.MOIZA.boundedContext.selectedPlace.entity;

import com.ll.MOIZA.base.entity.BaseEntity;
import com.ll.MOIZA.boundedContext.room.entity.EnterRoom;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(of = "name", callSuper = false)
public class SelectedPlace extends BaseEntity {
    private String name;

    @ManyToOne
    private EnterRoom enterRoom;
}
