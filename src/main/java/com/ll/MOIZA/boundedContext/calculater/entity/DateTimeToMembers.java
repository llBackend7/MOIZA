package com.ll.MOIZA.boundedContext.calculater.entity;

import com.ll.MOIZA.boundedContext.member.entity.Member;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

public class DateTimeToMembers {

    private final Map<LocalDateTime, Set<Member>> dateTimeToMembers = new ConcurrentHashMap<>();

    public Map<LocalDateTime, Set<Member>> getDateTimeToMembers() {
        return dateTimeToMembers;
    }

    public void setDateTimeToMembers(LocalDateTime localDateTime, Member member) {
        dateTimeToMembers.computeIfAbsent(localDateTime, key -> new ConcurrentSkipListSet<>())
                .add(member);
    }

    public void deleteDateTimeToMembers(LocalDateTime localDateTime, Member member) {
        Set<Member> members = dateTimeToMembers.get(localDateTime);
        if (members != null) {
            members.remove(member);
            if (members.isEmpty()) {
                dateTimeToMembers.remove(localDateTime);
            }
        }
    }

    public List<Entry<LocalDateTime, Set<Member>>> getSortedEntries() {
        return dateTimeToMembers.entrySet().stream()
                .sorted(Comparator.<Entry<LocalDateTime, Set<Member>>>comparingInt(entry -> entry.getValue().size())
                        .reversed()
                        .thenComparing(Entry::getKey))
                .collect(Collectors.toList());
    }
}