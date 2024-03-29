package com.ll.MOIZA.boundedContext.room.controller;

import com.ll.MOIZA.base.appConfig.AppConfig;
import com.ll.MOIZA.base.calendar.service.CalendarService;
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
import com.ll.MOIZA.boundedContext.selectedTime.service.TimeRangeWithMemberDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/room")
public class RoomController {
    private final RoomService roomService;
    private final MemberService memberService;
    private final ResultService resultService;
    private final SelectedTimeService selectedTimeService;
    private final EnterRoomService enterRoomService;
    private final SelectedPlaceService selectedPlaceService;
    private final CalendarService calendarService;

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
        return "room/create";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String createRoom(@AuthenticationPrincipal User user,
                                          @Valid RoomForm roomForm,
                                          BindingResult bindingResult,
                             Model model) {
        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors();
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

        enterRoomService.createEnterRoom(room, member);

        String accessToken = roomService.getAccessToken(room);
        String redirectUrl = "%s/room/enter?roomId=%d&accessToken=%s".formatted(AppConfig.getBaseUrl(), room.getId(), accessToken);

        model.addAttribute("redirectUrl", redirectUrl);
        addUserAuthority(user, room.getId());

        return "redirect:/groups";
    }

    @GetMapping("/{roomId}/invite")
    public String invite(@AuthenticationPrincipal User user,
                               @PathVariable Long roomId,
                               Model model, HttpServletRequest request) {

        request.getSession().setAttribute("prevPage", request.getRequestURL().toString());

        Room room = roomService.getRoom(roomId);
        String accessToken = roomService.getAccessToken(room);
        String redirectUrl = "%s/room/enter?roomId=%d&accessToken=%s".formatted(AppConfig.getBaseUrl(), room.getId(), accessToken);

        model.addAttribute("redirectUrl", redirectUrl);
        return "redirect:%s".formatted(redirectUrl);
    }

    @GetMapping("/{roomId}/changeTime")
    public ModelAndView changeTime(@PathVariable long roomId){

        Room room = roomService.getRoom(roomId);
        String accessToken = roomService.getAccessToken(room);

        String redirectUrl = "%s/room/enter?roomId=%d&accessToken=%s".formatted(AppConfig.getBaseUrl(), roomId, accessToken);
        return new ModelAndView("redirect:" + redirectUrl);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/enter")
    public String enterRoom(@AuthenticationPrincipal User user,
                            @RequestParam long roomId,
                            @RequestParam String accessToken,
                            Model model) {
        Room room = roomService.getRoom(roomId);

        if (roomService.validateToken(room, accessToken)) {
            Member member = memberService.loginMember(user);
            EnterRoom enterRoom = enterRoomService.enter(room, member);
            model.addAttribute("room", room);
            model.addAttribute("selectedTimes", enterRoom.getSelectedTimes());
            return "room/enter";
        }

        throw new AuthorizationServiceException("토큰값이 유효하지 않습니다.");
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/enter")
    @ResponseBody
    public ResponseEntity enterRoom(@AuthenticationPrincipal User user,
                                    @RequestParam long roomId,
                                    @RequestParam String accessToken,
                                    @RequestBody List<SelectedDayWithTime> selectedDayWhitTimeList
                                    ) throws IOException {
        Room room = roomService.getRoom(roomId);

        if (roomService.validateToken(room, accessToken)) {
            Member member = memberService.loginMember(user);
            enterRoomService.enterRoomWithSelectedTime(room, member, selectedDayWhitTimeList);

            addUserAuthority(user, roomId);

//            enterRoomService.clearCache(room);

            Map<String, String> response = new HashMap<>();
            response.put("redirectUrl", String.format("/room/%d/place", roomId));
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        throw new AuthorizationServiceException("토큰값이 유효하지 않습니다.");
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{roomId}/date")
    public String showRoomDate(@PathVariable Long roomId, @AuthenticationPrincipal User user, Model model) {
        Room room = roomService.getRoom(roomId);
        Member actor = memberService.loginMember(user);

        model.addAttribute("room", room);
        model.addAttribute("actor", actor);

        return "status/date";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{roomId}/selected-time")
    @ResponseBody
    public List<TimeRangeWithMemberDTO> selectedTimes(@PathVariable Long roomId) {
        Room room = roomService.getRoom(roomId);

        return selectedTimeService.Top10(room);
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
    public String createPlace(@PathVariable Long roomId, PlaceCreateForm form, @AuthenticationPrincipal User user, RedirectAttributes redirectAttributes) {
        Member member = memberService.loginMember(user);
        Optional<EnterRoom> opEnterRoom = enterRoomService.findByMemberIdAndRoomId(member.getId(), roomId);

        try {
            selectedPlaceService.CreateSelectedPlace(form.getName(), opEnterRoom.get());
        } catch (ResponseStatusException ex) {
            redirectAttributes.addAttribute("message", ex.getReason());
        }

        return "redirect:/room/%d/place".formatted(roomId);
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
        return "room/result";
    }

    @PostMapping("/{roomId}/close")
    public String closeRoom(@PathVariable Long roomId) {
        Room room = roomService.getRoom(roomId);
        roomService.closeRoom(roomId);

        List<TimeRangeWithMember> overlappingTimes = selectedTimeService.findOverlappingTimeRanges(room);
        TimeRangeWithMember timeRangeWithMember = overlappingTimes.size() > 0 ? overlappingTimes.get(0) : null;

        Map<SelectedPlace, Long> selectedPlaces = selectedPlaceService.getSelectedPlaces(room);
        String place = "";
        Map.Entry<SelectedPlace, Long> firstPlaceEntry = selectedPlaces.entrySet().stream().findFirst().orElse(null);
        if (firstPlaceEntry != null) {
            SelectedPlace firstSelectedPlace = firstPlaceEntry.getKey();
            place = firstSelectedPlace.getName();
        }

        resultService.createResult(room, timeRangeWithMember, place);
        return "redirect:/room/%d/result".formatted(roomId);
    }

    @GetMapping("/addEvent")
    public String addCalendarEvent(@RequestParam Long roomId) throws GeneralSecurityException, IOException {
        String url = calendarService.GoogleOauth(roomId);
        return "redirect:%s".formatted(url);
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
