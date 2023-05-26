package com.ll.MOIZA.boundedContext.room.service;

import com.ll.MOIZA.base.mail.MailService;
import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class MailServiceTest {
    @Mock
    private SendGrid sendGrid;

    @InjectMocks
    MailService mailService;

    @Test
    void 메일발송_테스트() throws IOException {
        String email = "test@email.com";
        String content = "Test content";
        Member testMember = Member.builder()
                .name("testMember")
                .email(email)
                .build();

        Response mockedResponse = new Response(202, null, null);

        // sendgrid에 어떤 요청이든 무조건 응답은 202로 설정
        when(sendGrid.api(any(Request.class))).thenReturn(mockedResponse);

        mailService.sendMailTo(testMember, content);

        verify(sendGrid, times(1)).api(any(Request.class));
    }
}