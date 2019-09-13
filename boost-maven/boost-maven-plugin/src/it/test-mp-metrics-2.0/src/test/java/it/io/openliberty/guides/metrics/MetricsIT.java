package it.io.openliberty.guides.metrics;

import static org.junit.Assert.*;
import java.io.*;
import java.util.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.provider.jsrjsonp.JsrJsonpProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MetricsIT {
    private static String httpPort;
    private static String httpsPort;
    private static String baseHttpUrl;
    private static String baseHttpsUrl;

    private List<String> metrics;
    private Client client;

    private final String INVENTORY_HOSTS = "inventory/systems";
    private final String INVENTORY_HOSTNAME = "inventory/systems/localhost";
    private final String METRICS_APPLICATION = "metrics/application";

    @BeforeClass
    public static void oneTimeSetup() {
        httpPort = System.getProperty("boost.http.port");
        baseHttpUrl = "http://localhost:" + httpPort + "/";
    }

    @Before
    public void setup() {
        client = ClientBuilder.newClient();
        client.register(JsrJsonpProvider.class);
    }

    @After
    public void teardown() {
        client.close();
    }

    @Test
    public void testSuite() {
        this.testPropertiesRequestTimeMetric();
        this.testInventoryAccessCountMetric();
        this.testInventorySizeGaugeMetric();
    }

    public void testPropertiesRequestTimeMetric() {
        connectToEndpoint(baseHttpUrl + INVENTORY_HOSTNAME);
        metrics = getMetrics();
        for (String metric : metrics) {
            if (metric.startsWith("application:inventory_properties_request_time_rate_per_second")) {
                float seconds = Float.parseFloat(metric.split(" ")[1]);
                assertTrue(4 > seconds);
            }
        }
    }

    public void testInventoryAccessCountMetric() {
        connectToEndpoint(baseHttpUrl + INVENTORY_HOSTS);
        metrics = getMetrics();
        for (String metric : metrics) {
            if (metric.startsWith("application:inventory_access_count")) {
                assertTrue(1 == Character.getNumericValue(metric.charAt(metric.length() - 1)));
            }
        }
    }

    public void testInventorySizeGaugeMetric() {
        metrics = getMetrics();
        for (String metric : metrics) {
            if (metric.startsWith("application:inventory_size_guage")) {
                assertTrue(1 == Character.getNumericValue(metric.charAt(metric.length() - 1)));
            }
        }

    }

    public void connectToEndpoint(String url) {
        Response response = this.getResponse(url);
        this.assertResponse(url, response);
        response.close();
    }

    private List<String> getMetrics() {
        Response metricsResponse = client.target(baseHttpUrl + METRICS_APPLICATION).request(MediaType.TEXT_PLAIN).get();

        BufferedReader br = new BufferedReader(new InputStreamReader((InputStream) metricsResponse.getEntity()));
        List<String> result = new ArrayList<String>();
        try {
            String input;
            while ((input = br.readLine()) != null) {
                result.add(input);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }

        metricsResponse.close();
        return result;
    }

    private Response getResponse(String url) {
        return client.target(url).request().get();
    }

    private void assertResponse(String url, Response response) {
        assertEquals("Incorrect response code from " + url, 200, response.getStatus());
    }
}
