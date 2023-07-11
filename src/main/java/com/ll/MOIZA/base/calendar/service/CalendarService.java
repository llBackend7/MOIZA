package com.ll.MOIZA.base.calendar.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.calendar.Calendar;
import com.ll.MOIZA.boundedContext.result.entity.DecidedResult;
import com.ll.MOIZA.boundedContext.result.service.ResultService;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.squareup.okhttp.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalendarService {
    private final ResultService resultService;
    private static Long roomId;

    private final String APPLICATION_NAME = "MOIZA";
    private final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private final List<String> SCOPES =
            Collections.singletonList(CalendarScopes.CALENDAR_EVENTS);
    private final String TOKEN_PATH = "tokens/token.json";

    private final String AUTHORIZATION_GRANT_TYPE = "authorization_code";
    private final String AUTH_URI = "https://accounts.google.com/o/oauth2/v2/auth";
    private final String TOKEN_URI = "https://oauth2.googleapis.com/token";
    private final String REDIRECT_URI = "http://localhost:8080/callback";
    private final String RESPONSE_TYPE = "code";

    private static String CLIENT_ID;
    private static String CLIENT_SECRET;

    @Value("${spring.security.oauth2.client.registration.google.clientId}")
    private void setClientId(String clientId) {
        CalendarService.CLIENT_ID = clientId;
    }
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private void setClientSecret(String clientSecret) {
        CalendarService.CLIENT_SECRET = clientSecret;
    }

    public String GoogleOauth(Long id) {
        roomId = id;

        String redirectUrl = AUTH_URI +
                "?scope=" + SCOPES.get(0) +
                "&access_type=offline" +
                "&response_type=" + RESPONSE_TYPE +
                "&redirect_uri=" + REDIRECT_URI +
                "&client_id=" + CLIENT_ID;

        return redirectUrl;
    }

    public String setEvent(String accessToken) throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = new GoogleCredential().setAccessToken(accessToken);

        Calendar service =
                new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                        .setApplicationName(APPLICATION_NAME)
                        .build();


        DecidedResult result = resultService.getResult(roomId);
        Room room = result.getRoom();

        Event event = new Event()
                .setSummary(room.getName())
                .setDescription(room.getDescription());

        String place = result.getDecidedPlace();
        if(place != null)
            event.setLocation(place);

        if(result.getDecidedDayTime() != null) {
            LocalDateTime startLocalDateTime = result.getDecidedDayTime();
            DateTime startDateTime = new DateTime(startLocalDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            EventDateTime start = new EventDateTime()
                    .setDateTime(startDateTime)
                    .setTimeZone("Asia/Seoul");
            event.setStart(start);

            LocalDateTime endLocalDateTime = startLocalDateTime.plusHours(room.getMeetingDuration().getHour()).plusMinutes(room.getMeetingDuration().getMinute());
            DateTime endDateTime = new DateTime(endLocalDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            EventDateTime end = new EventDateTime()
                    .setDateTime(endDateTime)
                    .setTimeZone("Asia/Seoul");
            event.setEnd(end);
        } else {
            DateTime startDateTime = new DateTime(System.currentTimeMillis());
            EventDateTime start = new EventDateTime()
                    .setDateTime(startDateTime)
                    .setTimeZone("Asia/Seoul");
            event.setStart(start);

            DateTime endDateTime = new DateTime(System.currentTimeMillis());
            EventDateTime end = new EventDateTime()
                    .setDateTime(endDateTime)
                    .setTimeZone("Asia/Seoul");
            event.setEnd(end);
        }

        event = service.events().insert("primary", event).execute();
        System.out.printf("이벤트가 생성되었습니다. 이벤트 ID: %s\n", event.getHtmlLink());
        return event.getHtmlLink();
    }

    public String getAccessTokenJsonData(String code) {
        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(TOKEN_URI).newBuilder();
        urlBuilder.addQueryParameter("client_id", CLIENT_ID);
        urlBuilder.addQueryParameter("client_secret", CLIENT_SECRET);
        urlBuilder.addQueryParameter("code", code);
        urlBuilder.addQueryParameter("grant_type", AUTHORIZATION_GRANT_TYPE);
        urlBuilder.addQueryParameter("redirect_uri", REDIRECT_URI);

        RequestBody requestBody = RequestBody.create(null, new byte[0]);

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .post(requestBody)
                .build();

        try (ResponseBody response = client.newCall(request).execute().body()) {
            File dir = new File("tokens");
            if(!dir.exists()) {
                boolean created = dir.mkdir();
            }

            String responseBody = response.string();
            File file = new File(TOKEN_PATH);
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));

            writer.write(responseBody);
            writer.flush();
            writer.close();

            return "/token";
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Error";
    }

    public String readAccessTokenFromFile() {
        String projectPath = System.getProperty("user.dir");
        String filePath = projectPath + File.separator + TOKEN_PATH;
        String accessToken = null;

        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(new FileReader(filePath));
            accessToken = (String) json.get("access_token");

            if (accessToken == null) {
                System.out.println("Access Token not found in the JSON file.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return accessToken;
    }
}