package com.ll.MOIZA.boundedContext.member.dto;

import jakarta.persistence.Entity;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberDTO implements Comparable<MemberDTO> {

    private long id;
    private String name;
    private String email;
    private String profile;

    @Override
    public int compareTo(MemberDTO o) {
        return name.compareTo(o.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MemberDTO memberDTO = (MemberDTO) o;

        return Objects.equals(this.getId(), memberDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId());
    }
}
