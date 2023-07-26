package com.ll.MOIZA.boundedContext.result.service;

import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.result.entity.DecidedResult;
import com.ll.MOIZA.boundedContext.result.repository.ResultRepository;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.selectedTime.service.TimeRangeWithMember;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ResultService {

    private final ResultRepository resultRepository;
    @Value("${custom.webdriver.port}")
    private int port;

    @Value("${custom.webdriver.path}")
    private String path;

    @Transactional
    public void createResult(Room room, TimeRangeWithMember timeRangeWithMember, String decidedPlace) {
        LocalDateTime decidedDateTime = null;
        List<Member> participationMembers = null;
        List<Member> nonParticipationMembers = null;
        String placeId = null;

        if (timeRangeWithMember != null) {
            decidedDateTime = (timeRangeWithMember.getDate() != null) ? timeRangeWithMember.getDate().atTime(timeRangeWithMember.getStart()) : null;
            participationMembers = (timeRangeWithMember.getParticipationMembers() != null) ? timeRangeWithMember.getParticipationMembers() : null;
            nonParticipationMembers = (timeRangeWithMember.getNonParticipationMembers() != null) ? timeRangeWithMember.getNonParticipationMembers() : null;
        }

//        if(decidedPlace != null) {
//            placeId = getPlaceId(decidedPlace);
//        }

        DecidedResult result = DecidedResult.builder()
                .decidedDayTime(decidedDateTime)
                .decidedPlace(decidedPlace)
                .participationMembers(participationMembers)
                .nonParticipationMembers(nonParticipationMembers)
                .room(room)
                .placeId(placeId)
                .build();

        resultRepository.save(result);
    }

    public DecidedResult getResult(Long roomId) {
        return resultRepository
                .findByRoomId(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "모임의 결과를 찾을 수 없습니다."));
    }

    public String getPlaceId(String place) {
        String url = "https://map.kakao.com/?q="+place;
        String id;

        // Chrome 드라이버 경로 설정
        System.setProperty("webdriver.chrome.driver", path);
        // ChromeDriverService 설정
        ChromeDriverService service = new ChromeDriverService.Builder()
                .usingPort(port)
                .build();
        // Headless Chrome 옵션 설정
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");

        // Chrome WebDriver 인스턴스 생성
        WebDriver driver = new ChromeDriver(service, options);

        // 웹 페이지 열기
        driver.get(url);

        WebElement element = driver.findElement(By.xpath("//*[@id=\"info.search.place.list\"]/li[1]/div[5]/div[4]/a[1]"));
        id = element.getAttribute("href").split("/")[3];

        // WebDriver 종료
        driver.quit();
        return id;
    }
}
