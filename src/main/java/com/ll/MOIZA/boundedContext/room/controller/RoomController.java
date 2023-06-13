package com.ll.MOIZA.boundedContext.room.controller;

import com.ll.MOIZA.base.appConfig.AppConfig;
import com.ll.MOIZA.base.mail.MailService;
import com.ll.MOIZA.base.security.CustomOAuth2User;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    @GetMapping("/{roomId}/date")
    public String showRoomDate(@PathVariable Long roomId,
                               @RequestParam(value = "timeIndex", defaultValue = "-1") int timeIndex,
                               @AuthenticationPrincipal User user, Model model) {
        Room room = roomService.getRoom(roomId);
        Member actor = memberService.loginMember(user);
        List<TimeRangeWithMember> overlappingTimes = selectedTimeService.findOverlappingTimeRanges(room);

        List<List<Member>> availableMembers;
        List<List<Member>> unavailableMembers;
        List<Member> selectedAvailableMembers = null;
        List<Member> selectedUnavailableMembers = null;

        model.addAttribute("room", room);
        model.addAttribute("actor", actor);
        model.addAttribute("overlappingTimes", overlappingTimes);

        if (timeIndex >= 0) {
            availableMembers = overlappingTimes.stream()
                    .map(TimeRangeWithMember::getParticipationMembers)
                    .toList();

            unavailableMembers = overlappingTimes.stream()
                    .map(TimeRangeWithMember::getNonParticipationMembers)
                    .toList();

            selectedAvailableMembers = availableMembers.get(timeIndex);
            selectedUnavailableMembers = unavailableMembers.get(timeIndex);
        }
        model.addAttribute("selectedAvailableMembers", selectedAvailableMembers);
        model.addAttribute("selectedUnavailableMembers", selectedUnavailableMembers);
        return "status/date";
    }

    @GetMapping("/{roomId}/place")
    public String showRoomPlace(@PathVariable Long roomId, @AuthenticationPrincipal User user, Model model) {
        Room room = roomService.getRoom(roomId);
        Member actor = memberService.loginMember(user);
        Map<SelectedPlace, Long> selectedPlaceMap = selectedPlaceService.getSelectedPlaces(room);

        model.addAttribute("selectedPlaceMap", selectedPlaceMap);
        model.addAttribute("room", room);
        model.addAttribute("actor", actor);
        return "status/place";
    }

    @PostMapping("/{roomId}/place")
    public String createPlace(@PathVariable Long roomId, PlaceCreateForm form, @AuthenticationPrincipal User user) {
        Member member = memberService.loginMember(user);
        Optional<EnterRoom> opEnterRoom = enterRoomService.findByMemberIdAndRoomId(member.getId(), roomId);
        selectedPlaceService.CreateSelectedPlace(form.getName(), opEnterRoom.get());

        return "redirect:/room/" + roomId + "/place";
    }

    @PreAuthorize("isAuthenticated() && hasAuthority('ROOM#' + #roomId + '_MEMBER')")
    @GetMapping("/{roomId}/chat")
    public String showRoomChat(@PathVariable Long roomId, @AuthenticationPrincipal User user, Model model) {
        Room room = roomService.getRoom(roomId);
        Member actor = memberService.loginMember(user);
        model.addAttribute("room", room);
        model.addAttribute("actor", actor);
        return "status/chat";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{roomId}/result")
    public String showResult(Model model, @PathVariable Long roomId) {
        DecidedResult result = resultService.getResult(roomId);
        model.addAttribute("result", result);
        model.addAttribute("appKey", AppConfig.getAppKey());
        return "/room/result";
    }

    @PostMapping("/{roomId}/close")
    public String closeRoom(@PathVariable Long roomId) {
        Room room = roomService.getRoom(roomId);

        List<TimeRangeWithMember> overlappingTimes = selectedTimeService.findOverlappingTimeRanges(room);
        TimeRangeWithMember timeRangeWithMember = overlappingTimes.get(0);

        Map<SelectedPlace, Long> selectedPlaces = selectedPlaceService.getSelectedPlaces(room);
        String place = "";
        Map.Entry<SelectedPlace, Long> firstPlaceEntry = selectedPlaces.entrySet().stream().findFirst().orElse(null);
        if (firstPlaceEntry != null) {
            SelectedPlace firstSelectedPlace = firstPlaceEntry.getKey();
            place = firstSelectedPlace.getName();
        }

        resultService.createResult(room, timeRangeWithMember, place);
        return "redirect:/room/" + roomId + "/result";
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

        if (roomService.validateToken(room, accessToken)) {
            model.addAttribute("room", room);
            model.addAttribute("availableDayList", roomService.getAvailableDayList(roomId));
            model.addAttribute("availableTimeList", roomService.getAvailableTimeList(roomId));

            return "/room/enter";
        }

        throw new AuthorizationServiceException("토큰값이 유효하지 않습니다.");
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/enter")
    @ResponseBody
    public ResponseEntity enterRoom(@AuthenticationPrincipal User user,
                                    @RequestParam long roomId,
                                    @RequestParam String accessToken,
                                    @RequestBody List<SelectedDayWithTime> selectedDayWhitTimeList,
                                    Model model) {
        Room room = roomService.getRoom(roomId);

        if (roomService.validateToken(room, accessToken)) {
            Member member = memberService.loginMember(user);
            enterRoomService.enterRoomWithSelectedTime(room, member, selectedDayWhitTimeList);

            addUserAuthority(user, roomId);

            Map<String, String> response = new HashMap<>();
            response.put("redirectUrl", String.format("/room/%d/date", roomId));
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        throw new AuthorizationServiceException("토큰값이 유효하지 않습니다.");
    }

    private void addUserAuthority(User user, long roomId) {
        // 사용자 권한 추가
        List<GrantedAuthority> updatedAuthorities = new ArrayList<>(user.getAuthorities());
        updatedAuthorities.add(new SimpleGrantedAuthority("ROOM#%d_MEMBER".formatted(roomId)));
        User updatedUser;

        if (user instanceof CustomOAuth2User) {
            updatedUser = new CustomOAuth2User(user.getUsername(),
                    ((CustomOAuth2User) user).getId(),
                    ((CustomOAuth2User) user).getProfile(),
                    "", updatedAuthorities);
        } else {
            updatedUser = new User(user.getUsername(), "", updatedAuthorities);
        }

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(updatedUser, "", updatedAuthorities));
    }
}
