package com.resumescreener.enterpriseresumescreener.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class EmailService {

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.brevo.api-key}")
    private String apiKey;

    private final HttpClient client = HttpClient.newHttpClient();

    public void sendOtpEmail(String toEmail, String otp) {
        String text = "Your OTP is: " + otp
                + "\n\nThis OTP is valid for a few minutes. Do not share it with anyone.";

        String json = "{"
                + "\"sender\":{\"name\":\"Resume Screener\",\"email\":\"" + esc(fromEmail) + "\"},"
                + "\"to\":[{\"email\":\"" + esc(toEmail) + "\"}],"
                + "\"subject\":\"Your OTP for Resume Screener Signup\","
                + "\"textContent\":\"" + esc(text) + "\""
                + "}";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                    .header("api-key", apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("Brevo error " + response.statusCode() + ": " + response.body());
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }

    private String esc(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}