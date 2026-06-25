package com.ecofocus.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${resend.api.key}")
    private String apiKey;

    @Value("${resend.from}")
    private String from;

    private final RestTemplate restTemplate;

    public void sendRegistrationCode(String toEmail, String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = Map.of(
            "from", from,
            "to", new String[]{toEmail},
            "subject", "iLand - E-posta Doğrulama Kodunuz",
            "html", buildRegistrationHtml(code)
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        restTemplate.postForEntity("https://api.resend.com/emails", request, String.class);
    }

    private String buildRegistrationHtml(String code) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 480px; margin: auto; padding: 32px; background: #0D1B2A; border-radius: 16px;">
              <h2 style="color: #7FD8BE; text-align: center;">iLand</h2>
              <p style="color: #B8D0FF; text-align: center; font-size: 16px;">Hesabınızı doğrulamak için kodunuz:</p>
              <div style="background: #1A2E45; border-radius: 12px; padding: 24px; text-align: center; margin: 24px 0;">
                <span style="font-size: 40px; font-weight: 900; letter-spacing: 12px; color: #FFC107;">%s</span>
              </div>
              <p style="color: #6B8CAE; text-align: center; font-size: 13px;">Bu kod 15 dakika geçerlidir.</p>
            </div>
            """.formatted(code);
    }

    public void sendPasswordResetCode(String toEmail, String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = Map.of(
            "from", from,
            "to", new String[]{toEmail},
            "subject", "iLand - Şifre Sıfırlama Kodunuz",
            "html", buildEmailHtml(code)
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        restTemplate.postForEntity("https://api.resend.com/emails", request, String.class);
    }

    private String buildEmailHtml(String code) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 480px; margin: auto; padding: 32px; background: #0D1B2A; border-radius: 16px;">
              <h2 style="color: #7FD8BE; text-align: center;">iLand</h2>
              <p style="color: #B8D0FF; text-align: center; font-size: 16px;">Şifre sıfırlama kodunuz:</p>
              <div style="background: #1A2E45; border-radius: 12px; padding: 24px; text-align: center; margin: 24px 0;">
                <span style="font-size: 40px; font-weight: 900; letter-spacing: 12px; color: #FFC107;">%s</span>
              </div>
              <p style="color: #6B8CAE; text-align: center; font-size: 13px;">Bu kod 15 dakika geçerlidir. Eğer şifre sıfırlama talebinde bulunmadıysanız bu maili görmezden gelebilirsiniz.</p>
            </div>
            """.formatted(code);
    }
}
