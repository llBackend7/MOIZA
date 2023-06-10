package com.ll.MOIZA.boundedContext.selectedTime.service;

import com.ll.MOIZA.boundedContext.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class TimeRangeWithMember implements Comparable<TimeRangeWithMember> {

    LocalDate date;
    LocalTime start;
    LocalTime end;
    List<Member> members;

    @Override
    public int compareTo(TimeRangeWithMember o1) {
        if (o1.members.size() == members.size()) {
            return start.compareTo(o1.start);
        }
        return o1.members.size() - members.size();
    }
}
