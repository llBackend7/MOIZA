package com.ll.MOIZA.boundedContext.selectedTime.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectedTimeDto {
    private String start;
    private String end;
    private String day;
}
