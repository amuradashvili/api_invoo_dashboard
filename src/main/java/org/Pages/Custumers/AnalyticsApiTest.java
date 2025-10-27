package org.Pages.Custumers;

import com.microsoft.playwright.*;
import org.junit.*;
import java.util.Map;
import static org.junit.Assert.*;

/**
 * ✅ Playwright API tests
 * Tests 2 endpoints:
 *  - /customers/analytics
 *  - /customers
 * Fails if HTTP status != 200 or response is empty.
 */
public class AnalyticsApiTest {

    private static Playwright playwright;
    private static APIRequestContext request;

    @BeforeClass
    public static void setUpClass() {
        System.out.println("🚀 Initializing Playwright for API testing...");
        playwright = Playwright.create();

        // Create shared request context with headers
        request = playwright.request().newContext(
                new APIRequest.NewContextOptions()
                        .setExtraHTTPHeaders(Map.of(
                                "Accept", "application/json, text/plain, */*",
                                "Authorization",
                                // ⚠️ Replace this token if expired
                                "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJzYSIsImlhdCI6MTc2MTA1MDI0MCwiZXhwIjoxNzYxMTM2NjQwLCJ0eXBlIjoidXNlciIsImF1dGhvcml0aWVzIjoiQURNSU4sUk9MRV9VU0VSIiwibWVyY2hhbnRfaWQiOjF9.qCWRYGGO6KiXbYLlM1CoqeXKWTbd93UOL662yFXCZ_JHktfUC_EVhOBKGOKtWvLwB-TqdIovhRVjwJV_h4LNpw"
                        ))
        );
        System.out.println("✅ Playwright API context created successfully.");
    }

    // 🧪 TEST 1 — Analytics Endpoint
    @Test
    public void testAnalyticsEndpoint() {
        String analyticsUrl =
                "https://pay-app-oilbv.ondigitalocean.app/api/v1/customers/analytics"
                        + "?startDate=2025-09-23T09:16:45.030Z"
                        + "&endDate=2025-10-23T09:16:45.031Z";

        System.out.println("\n=== 🧪 Analytics API Test ===");
        APIResponse response = request.get(analyticsUrl);

        // Validate response not null
        assertNotNull("❌ Analytics API response is null!", response);

        int status = response.status();
        String body = response.text();
        System.out.println("📡 Status: " + status);
        System.out.println("🧾 Body:\n" + body);

        // ❌ Fail if not 200
        if (status != 200) {
            fail("❌ Expected 200 but got " + status);
        }

        // ❌ Fail if response body empty
        assertNotNull("❌ Response body is null!", body);
        assertFalse("❌ Response body is empty!", body.trim().isEmpty());

        System.out.println("✅ Analytics API returned status 200 OK");
    }

    // 🧪 TEST 2 — Customers Endpoint
    @Test
    public void testCustomersEndpoint() {
        String customersUrl =
                "https://pay-app-oilbv.ondigitalocean.app/api/v1/customers"
                        + "?size=10&sortBy=totalRevenue&sortDirection=DESC";

        System.out.println("\n=== 🧪 Customers API Test ===");
        APIResponse response = request.get(customersUrl);

        assertNotNull("❌ Customers API response is null!", response);

        int status = response.status();
        String body = response.text();
        System.out.println("📡 Status: " + status);
        System.out.println("🧾 Body:\n" + body);

        // ❌ Fail if not 200
        if (status != 200) {
            fail("❌ Expected 200 but got " + status);
        }

        // ❌ Fail if response body empty
        assertNotNull("❌ Response body is null!", body);
        assertFalse("❌ Response body is empty!", body.trim().isEmpty());

        System.out.println("✅ Customers API returned status 200 OK");
    }

    @AfterClass
    public static void tearDown() {
        if (playwright != null) {
            playwright.close();
            System.out.println("\n🧹 Playwright closed. Tests finished.");
        }
    }
}
