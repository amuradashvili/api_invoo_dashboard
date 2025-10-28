package org.Pages.Developers;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * ✅ Webhooks API Test
 * ---------------------
 * - Verifies that the Locale JSON file is accessible
 * - No authentication required
 * - Tests:
 *   1. GET /assets/locale/en-US.json
 * - Expects HTTP 200 OK
 */
public class WebhooksApiTest {

    private static final String LOCALE_URL = "https://pay-web-ntwda.ondigitalocean.app/assets/locale/en-US.json";

    /**
     * 🌐 Sends GET request and returns status code
     */
    private int sendGetRequest(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

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

    // === TEST CASE ===

    /** 🌍 Test for Locale File Availability */
    @Test
    public void testLocaleFileAPI() throws IOException {
        int status = sendGetRequest(LOCALE_URL);
        Assert.assertEquals("Expected 200 for Locale en-US.json", 200, status);
    }
}
