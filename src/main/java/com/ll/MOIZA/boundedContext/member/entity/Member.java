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
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class Member extends BaseEntity {
    @Column(unique = true)
    private String name;
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
        List<SimpleGrantedAuthority> authorities = new ArrayList<>(enterRooms.stream()
                .map(EnterRoom::getRoom)
                .mapToLong(BaseEntity::getId)
                .mapToObj(id -> new SimpleGrantedAuthority("ROOM#%d_MEMBER".formatted(id)))
                .toList());
        authorities.add(new SimpleGrantedAuthority("USER"));

        return authorities;
    }
}
