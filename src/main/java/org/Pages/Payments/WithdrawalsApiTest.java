package org.Pages.Payments;

import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * ✅ Withdrawals API Tests
 * -------------------------
 * - Logs in (sa / 123456)
 * - GET and POST tests for Withdrawals API
 */
public class WithdrawalsApiTest {

    private static final String LOGIN_URL = "https://pay-app-oilbv.ondigitalocean.app/api/v1/auth/login";
    private static final String WITHDRAWALS_URL = "https://pay-app-oilbv.ondigitalocean.app/api/v1/transaction/withdrawals";
    private static final String USERNAME = "sa";
    private static final String PASSWORD = "123456";

    /** 🔐 Logs in and retrieves JWT token */
    private String loginAndGetToken() throws IOException {
        URL url = new URL(LOGIN_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String jsonBody = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", USERNAME, PASSWORD);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes());
        }

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("❌ Login failed! HTTP " + conn.getResponseCode());
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
        }
        conn.disconnect();

        String token = null;
        if (response.toString().contains("\"token\"")) {
            token = response.toString().split("\"token\"\\s*:\\s*\"")[1].split("\"")[0];
        }
        if (token == null) throw new RuntimeException("❌ Token not found!");

        System.out.println("✅ Logged in successfully, token acquired.");
        return token;
    }

    /** 🌐 Generic HTTP request sender */
    private int sendRequest(String urlStr, String method, String token, String body) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + token);

        if (body != null) {
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes());
            }
        }

        int code = conn.getResponseCode();

        if (code != 200) {
            System.err.println("❌ Request failed: " + urlStr);
            System.err.println("HTTP Status: " + code);
        } else {
            System.out.println("✅ Success: " + urlStr + " → " + code);
        }

        conn.disconnect();
        return code;
    }

    // === TEST CASE ===
    @Test
    public void testWithdrawalsAPI() throws IOException {
        String token = loginAndGetToken();
        String jsonBody = "{\"page\":0,\"size\":20,\"sort\":\"createdAt\",\"direction\":\"desc\"}";
        int status = sendRequest(WITHDRAWALS_URL, "POST", token, jsonBody);
        Assert.assertEquals("Expected 200 for Withdrawals API", 200, status);
    }
}
