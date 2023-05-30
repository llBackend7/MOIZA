package com.ll.MOIZA.boundedContext.member.entity;

import com.ll.MOIZA.base.entity.BaseEntity;
import com.ll.MOIZA.boundedContext.room.entity.EnterRoom;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class Member extends BaseEntity {
    @Column(unique = true)
    private String name;
    private String password;
    private String email;
    private String profile;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "member")
    @ToString.Exclude
    @Builder.Default
    private List<EnterRoom> enterRooms = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "leader")
    @ToString.Exclude
    @Builder.Default
    private List<Room> rooms = new ArrayList<>();

    public Collection<? extends GrantedAuthority> getGrantedAuthorities() {
        return List.of(new SimpleGrantedAuthority("USER"));
    }

    public void participate(EnterRoom enterRoom) {
        enterRooms.add(enterRoom);
        enterRoom.getRoom().getEnterRoom().add(enterRoom);
    }
}
