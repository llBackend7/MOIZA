package com.ll.MOIZA.boundedContext.room.controller;

import com.ll.MOIZA.base.appConfig.AppConfig;
import com.ll.MOIZA.base.mail.MailService;
import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.member.service.MemberService;
import com.ll.MOIZA.boundedContext.result.entity.DecidedResult;
import com.ll.MOIZA.boundedContext.result.service.ResultService;
import com.ll.MOIZA.boundedContext.room.entity.EnterRoom;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.form.PlaceCreateForm;
import com.ll.MOIZA.boundedContext.room.service.EnterRoomService;
import com.ll.MOIZA.boundedContext.room.service.RoomService;
import com.ll.MOIZA.boundedContext.selectedPlace.entity.SelectedPlace;
import com.ll.MOIZA.boundedContext.selectedPlace.service.SelectedPlaceService;
import com.ll.MOIZA.boundedContext.selectedTime.service.SelectedTimeService;
import com.ll.MOIZA.boundedContext.selectedTime.service.TimeRangeWithMember;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/room")
public class RoomController {
    private final RoomService roomService;
    private final MemberService memberService;
    private final MailService mailService;
    private final ResultService resultService;
    private final SelectedTimeService selectedTimeService;
    private final EnterRoomService enterRoomService;
    private final SelectedPlaceService selectedPlaceService;


    @Data
    public static class RoomForm {
        @NotNull
        @NotEmpty
        String name;
        @NotNull
        @NotEmpty
        String description;
        @NotNull
        LocalDate startDate;
        @NotNull
        LocalDate endDate;
        @NotNull
        LocalTime availableStartTime;
        @NotNull
        LocalTime availableEndTime;
        @NotNull
        LocalTime duration;
        @NotNull
        LocalDateTime deadLine;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create")
    public String createRoom(RoomForm roomForm) {
        return "/room/create";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    @ResponseBody
    public Map<String, Object> createRoom(@AuthenticationPrincipal User user,
                                          @Valid RoomForm roomForm,
                                          BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Map.of("error", bindingResult.getAllErrors());
        }

        Member member = memberService.loginMember(user);
        Room room = roomService.createRoom(member,
                roomForm.name,
                roomForm.description,
                roomForm.startDate,
                roomForm.endDate,
                roomForm.availableStartTime,
                roomForm.availableEndTime,
                roomForm.duration,
                roomForm.deadLine);
        String accessToken = roomService.getAccessToken(room);
        Long roomId = room.getId();

        return Map.of("link", "http://localhost:8080/room/enter?roomId=%d&accessToken=%s".formatted(roomId, accessToken));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{roomId}/invite")
    @ResponseBody
    public String invite(@AuthenticationPrincipal User user,
                         @PathVariable Long roomId,
                         @RequestParam Long memberId) {
        Member actor = memberService.loginMember(user);
        Member friend = memberService.getMember(memberId);
        Room room = roomService.getRoom(roomId);

        String accessToken = roomService.getAccessToken(room);

        String mailContent = "<h1>%s님으로부터의 모임초대링크입니다.</h1>".formatted(actor.getName())
                + "<a href='http://localhost:8080/room/enter?roomId=%d&accessToken=%s'>모임참여하기</a>".formatted(roomId, accessToken);

        mailService.sendMailTo(friend, mailContent);

        return "{'result':'초대링크를 발송했습니다.'}";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{roomId}/result")
    public String showResult(Model model, @PathVariable Long roomId) {
        DecidedResult result = resultService.getResult(roomId);
        model.addAttribute("result", result);
        model.addAttribute("appKey", AppConfig.getAppKey());
        return "/room/result";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{roomId}/date")
    public String showRoomDate(@PathVariable Long roomId, @AuthenticationPrincipal User user, Model model) {
        Room room = roomService.getRoom(roomId);
        Member actor = memberService.loginMember(user);
        List<TimeRangeWithMember> overlappingTimes = selectedTimeService.findOverlappingTimeRanges(room);

        List<List<Member>> availableMembers = overlappingTimes.stream()
                .map(TimeRangeWithMember::getParticipationMembers)
                .toList();

        List<List<Member>> unavailableMembers = overlappingTimes.stream()
                .map(TimeRangeWithMember::getNonParticipationMembers)
                .toList();

        model.addAttribute("room", room);
        model.addAttribute("actor", actor);
        model.addAttribute("overlappingTimes", overlappingTimes);
        model.addAttribute("availableMembers", availableMembers);
        model.addAttribute("unavailableMembers", unavailableMembers);
        return "status/date";
    }

    @GetMapping("/{roomId}/place")
    public String showRoomPlace(@PathVariable Long roomId, @AuthenticationPrincipal User user, Model model) {
        Room room = roomService.getRoom(roomId);
        List<EnterRoom> enterRooms = room.getEnterRoom();

        Map<SelectedPlace, Long> selectedPlaceMap = enterRooms.stream()
                .flatMap(enterRoom -> enterRoom.getSelectedPlaces().stream())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<SelectedPlace, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        model.addAttribute("selectedPlaceMap", selectedPlaceMap);
        model.addAttribute("room", room);
        return "status/place";
    }

    @PreAuthorize("isAuthenticated() && hasAuthority('ROOM#' + #roomId + '_MEMBER')")
    @GetMapping("/{roomId}/chat")
    public String showRoomChat(@PathVariable Long roomId, Model model) {
        Room room = roomService.getRoom(roomId);
        model.addAttribute("room", room);
        return "status/chat";
    }

    @PostMapping("/{roomId}/place")
    public String createPlace(@PathVariable Long roomId, PlaceCreateForm form, @AuthenticationPrincipal User user) {
        Member member = memberService.loginMember(user);
        Optional<EnterRoom> opEnterRoom = enterRoomService.findByMemberIdAndRoomId(member.getId(), roomId);


        selectedPlaceService.CreateSelectedPlace(form.getName(), opEnterRoom.get());

        return "redirect:/room/" + roomId + "/place";
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SelectedDayWithTime {
        @NotNull
        LocalDate date;
        @NotNull
        LocalTime startTime;
        @NotNull
        LocalTime endTime;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/enter")
    public String enterRoom(@RequestParam long roomId, @RequestParam String accessToken, Model model) {
        Room room = roomService.getRoom(roomId);

        if (roomService.validateToken(room,accessToken)) {
            model.addAttribute("room", room);
            model.addAttribute("availableDayList", roomService.getAvailableDayList(roomId));
            model.addAttribute("availableTimeList", roomService.getAvailableTimeList(roomId));

            return "/room/enter";
        }

        throw new AuthorizationServiceException("토큰값이 유효하지 않습니다.");
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/enter")
    public String enterRoom(@AuthenticationPrincipal User user,
                            @RequestParam long roomId,
                            @RequestParam String accessToken,
                            @RequestBody List<SelectedDayWithTime> selectedDayWhitTimeList,
                            Model model) {
        Room room = roomService.getRoom(roomId);

        if (roomService.validateToken(room,accessToken)) {
            Member member = memberService.loginMember(user);
            enterRoomService.enterRoomWithSelectedTime(room, member, selectedDayWhitTimeList);

            addUserAuthority(user, roomId);

            return "redirect:/room/%d/date".formatted(roomId);
        }

        throw new AuthorizationServiceException("토큰값이 유효하지 않습니다.");
    }

    private void addUserAuthority(User user, long roomId) {
        // 사용자 권한 추가
        List<GrantedAuthority> updatedAuthorities = new ArrayList<>(user.getAuthorities());
        updatedAuthorities.add(new SimpleGrantedAuthority("ROOM#%d_MEMBER".formatted(roomId)));

        User updatedUser = new User(user.getUsername(), user.getPassword(), updatedAuthorities);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(updatedUser, user.getPassword(), updatedAuthorities));
    }
}
