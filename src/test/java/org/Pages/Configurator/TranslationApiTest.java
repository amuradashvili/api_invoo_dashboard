package org.Pages.Configurator;

import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * ✅ Translation API Tests
 * -------------------------
 * - Logs in (sa / 123456)
 * - Gets JWT token
 * - Tests:
 *   1. GET /assets/locale/en-US.json (no auth)
 *   2. GET /api/v1/translations/keys?page=0&size=20&sort=keyCode,asc (auth)
 * - Verifies HTTP 200 responses
 */
public class TranslationApiTest {

    private static final String LOGIN_URL = "https://pay-app-oilbv.ondigitalocean.app/api/v1/auth/login";
    private static final String LOCALE_URL = "https://pay-web-ntwda.ondigitalocean.app/assets/locale/en-US.json";
    private static final String TRANSLATION_KEYS_URL = "https://pay-app-oilbv.ondigitalocean.app/api/v1/translations/keys?page=0&size=20&sort=keyCode,asc";

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

    /** 🌐 Generic GET request sender */
    private int sendGetRequest(String urlStr, String token, boolean withAuth) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);

        if (withAuth && token != null) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }

        int code = conn.getResponseCode();

        if (code != 200) {
            System.err.println("❌ Request failed: " + urlStr);
            System.err.println("HTTP Status: " + code);
            if (conn.getErrorStream() != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.err.println(line);
                    }
                }
            }
        } else {
            System.out.println("✅ Success: " + urlStr + " → " + code);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String firstLine = br.readLine();
                if (firstLine != null)
                    System.out.println("🔹 Response preview: " + firstLine.substring(0, Math.min(150, firstLine.length())));
            }
        }

        conn.disconnect();
        return code;
    }

    // === TEST CASES ===

    /** 🌍 Locale File (no auth) */
    @Test
    public void testLocaleFileAPI() throws IOException {
        int status = sendGetRequest(LOCALE_URL, null, false);
        Assert.assertEquals("Expected 200 for Locale file API", 200, status);
    }

    /** 🗝️ Translation Keys API (auth) */
    @Test
    public void testTranslationKeysAPI() throws IOException {
        String token = loginAndGetToken();
        int status = sendGetRequest(TRANSLATION_KEYS_URL, token, true);
        Assert.assertEquals("Expected 200 for Translation Keys API", 200, status);
    }
}
