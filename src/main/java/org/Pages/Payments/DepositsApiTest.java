package org.Pages.Payments;

import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * ✅ Deposits API Test
 * --------------------
 * - Logs in (sa / 123456)
 * - Gets JWT token
 * - Calls POST /transaction/deposits
 * - Verifies 200 OK response
 */
public class DepositsApiTest {

    private static final String LOGIN_URL = "https://pay-app-oilbv.ondigitalocean.app/api/v1/auth/login";
    private static final String DEPOSITS_URL = "https://pay-app-oilbv.ondigitalocean.app/api/v1/transaction/deposits";

    private static final String USERNAME = "sa";
    private static final String PASSWORD = "123456";

    /** 🔐 Logs in and retrieves JWT token */
    private String loginAndGetToken() throws IOException {
        URL url = new URL(LOGIN_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        String jsonBody = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", USERNAME, PASSWORD);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes());
            os.flush();
        }

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("❌ Login failed! HTTP " + conn.getResponseCode());
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }
        conn.disconnect();

        String response = sb.toString();
        String token = null;
        if (response.contains("\"token\"")) {
            token = response.split("\"token\"\\s*:\\s*\"")[1].split("\"")[0];
        }

        if (token == null || token.isEmpty()) {
            throw new RuntimeException("❌ Token not found in login response: " + response);
        }

        System.out.println("✅ Logged in successfully, token acquired.");
        return token;
    }

    /** 🌐 Sends an authorized POST request to Deposits API */
    private int sendDepositsRequest(String token) throws IOException {
        URL url = new URL(DEPOSITS_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);

        String jsonBody = "{\"page\":0,\"size\":20,\"sort\":\"createdAt\",\"direction\":\"desc\"}";
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes());
            os.flush();
        }

        int status = conn.getResponseCode();

        if (status != 200) {
            System.err.println("❌ Request failed: " + DEPOSITS_URL);
            System.err.println("HTTP Status: " + status);
            if (conn.getErrorStream() != null) {
                try (BufferedReader r = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                    String line;
                    while ((line = r.readLine()) != null) System.err.println(line);
                }
            }
        } else {
            System.out.println("✅ Deposits API success: " + status);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String firstLine = br.readLine();
                if (firstLine != null)
                    System.out.println("🔹 Response preview: " + firstLine.substring(0, Math.min(150, firstLine.length())));
            }
        }

        conn.disconnect();
        return status;
    }

    // === TEST CASE ===

    @Test
    public void testDepositsAPI() throws IOException {
        String token = loginAndGetToken();
        int status = sendDepositsRequest(token);
        Assert.assertEquals("Expected 200 for Deposits API", 200, status);
    }
}
