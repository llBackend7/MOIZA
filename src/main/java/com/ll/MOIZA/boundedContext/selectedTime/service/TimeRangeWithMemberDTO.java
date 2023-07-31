package com.ll.MOIZA.boundedContext.selectedTime.service;

import com.ll.MOIZA.boundedContext.member.dto.MemberDTO;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TimeRangeWithMemberDTO {

    LocalDate date;
    LocalTime start;
    LocalTime end;
    List<MemberDTO> participationMembers;
    List<MemberDTO> nonParticipationMembers;

}
