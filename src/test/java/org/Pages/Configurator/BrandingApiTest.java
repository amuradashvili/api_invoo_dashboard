package org.Pages.Configurator;

import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * ✅ Branding Configuration API Tests
 * ------------------------------------
 * - Logs in (sa / 123456)
 * - Gets JWT token
 * - Tests:
 *   1. GET /assets/locale/en-US.json
 *   2. GET /api/v1/merchants/current/branding/v2
 *   3. GET /assets/translations/en.json
 *   4. POST /api/v1/merchants/branding/validate-colors
 *   5. GET /eu.api.fpjs.io (Fingerprint service)
 * - Verifies HTTP 200 responses
 */
public class BrandingApiTest {

    private static final String LOGIN_URL = "https://pay-app-oilbv.ondigitalocean.app/api/v1/auth/login";
    private static final String LOCALE_URL = "https://pay-web-ntwda.ondigitalocean.app/assets/locale/en-US.json";
    private static final String BRANDING_V2_URL = "https://pay-app-oilbv.ondigitalocean.app/api/v1/merchants/current/branding/v2";
    private static final String TRANSLATION_URL = "https://sea-lion-app-3vtnz.ondigitalocean.app/assets/translations/en.json";
    private static final String VALIDATE_COLORS_URL = "https://pay-app-oilbv.ondigitalocean.app/api/v1/merchants/branding/validate-colors";
    private static final String FINGERPRINT_URL = "https://eu.api.fpjs.io/DwmA/JNZ1Dz7/7?q=rAYkICVaA2hXcDRGO6Nm";

    private static final String USERNAME = "sa";
    private static final String PASSWORD = "123456";

    /** 🔐 Login and retrieve JWT token */
    private String loginAndGetToken() throws IOException {
        URL url = new URL(LOGIN_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

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

    /** 🌐 Generic GET request */
    private int sendGetRequest(String urlStr, String token, boolean auth) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        if (auth && token != null) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }

        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);

        int code = conn.getResponseCode();
        logResponse(urlStr, conn, code);
        conn.disconnect();
        return code;
    }

    /** 🌐 POST request with optional JSON body */
    private int sendPostRequest(String urlStr, String token, String jsonBody) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Content-Type", "application/json");

        if (token != null) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }

        conn.setDoOutput(true);
        if (jsonBody != null && !jsonBody.isEmpty()) {
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes());
                os.flush();
            }
        }

        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);

        int code = conn.getResponseCode();
        logResponse(urlStr, conn, code);
        conn.disconnect();
        return code;
    }

    /** 🧾 Logging helper */
    private void logResponse(String url, HttpURLConnection conn, int code) throws IOException {
        if (code == 200) {
            System.out.println("✅ Success: " + url + " → " + code);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String first = br.readLine();
                if (first != null)
                    System.out.println("🔹 Response preview: " + first.substring(0, Math.min(150, first.length())));
            }
        } else {
            System.err.println("❌ Failed: " + url + " → HTTP " + code);
            if (conn.getErrorStream() != null) {
                try (BufferedReader err = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                    String line;
                    while ((line = err.readLine()) != null)
                        System.err.println(line);
                }
            }
        }
    }

    // === TEST CASES ===

    /** 🌍 Locale */
    @Test
    public void testLocaleAPI() throws IOException {
        int status = sendGetRequest(LOCALE_URL, null, false);
        Assert.assertEquals("Expected 200 for Locale API", 200, status);
    }

    /** 🎨 Current Branding (auth) */
    @Test
    public void testBrandingV2API() throws IOException {
        String token = loginAndGetToken();
        int status = sendGetRequest(BRANDING_V2_URL, token, true);
        Assert.assertEquals("Expected 200 for Branding v2 API", 200, status);
    }

    /** 🌐 Translations file */
    @Test
    public void testTranslationsAPI() throws IOException {
        int status = sendGetRequest(TRANSLATION_URL, null, false);
        Assert.assertEquals("Expected 200 for Translations file", 200, status);
    }

    /** 🎨 Validate Colors (auth + POST) */
    @Test
    public void testValidateColorsAPI() throws IOException {
        String token = loginAndGetToken();
        String body = "{\"primaryColor\":\"#1E90FF\",\"secondaryColor\":\"#FFFFFF\"}";
        int status = sendPostRequest(VALIDATE_COLORS_URL, token, body);
        Assert.assertEquals("Expected 200 for Validate Colors API", 200, status);
    }

    /** 🕵️ FingerprintJS 3rd party endpoint */
    @Test
    public void testFingerprintService() throws IOException {
        int status = sendGetRequest(FINGERPRINT_URL, null, false);
        // external services may respond with 200 or 204
        Assert.assertTrue("Expected 200 or 204 for Fingerprint service", status == 200 || status == 204);
    }
}
