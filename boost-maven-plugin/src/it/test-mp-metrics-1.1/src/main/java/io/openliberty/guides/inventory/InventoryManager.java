package io.openliberty.guides.inventory;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import javax.ws.rs.ProcessingException;

import io.openliberty.guides.inventory.model.InventoryList;
import io.openliberty.guides.inventory.model.SystemData;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.metrics.annotation.Gauge;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import io.openliberty.guides.inventory.client.SystemClient;
import io.openliberty.guides.inventory.client.UnknownUrlException;
import io.openliberty.guides.inventory.client.UnknownUrlExceptionMapper;

@ApplicationScoped
public class InventoryManager {

    @Inject
    @ConfigProperty(name = "back.end.system.port")
    private String backEndSystemServicePort;

    private List<SystemData> systems = Collections.synchronizedList(new ArrayList<>());

    @Timed(name = "inventoryPropertiesRequestTime", absolute = true, description = "Time needed to get the properties of"
            + "a system from the given hostname")
    public Properties get(String hostname) {
        String customURLString = "http://" + hostname + ":" + backEndSystemServicePort + "/system";
        URL customURL = null;
        try {
            customURL = new URL(customURLString);
            SystemClient customRestClient = RestClientBuilder.newBuilder().baseUrl(customURL)
                    .register(UnknownUrlExceptionMapper.class).build(SystemClient.class);
            return customRestClient.getProperties();
        } catch (ProcessingException ex) {
            handleProcessingException(ex);
        } catch (UnknownUrlException e) {
            System.err.println("The given URL is unreachable.");
        } catch (MalformedURLException e) {
            System.err.println("The given URL is not formatted correctly: " + customURLString);
        }
        return null;
    }

    public void add(String hostname, Properties systemProps) {
        Properties props = new Properties();
        props.setProperty("os.name", systemProps.getProperty("os.name"));
        props.setProperty("user.name", systemProps.getProperty("user.name"));

        SystemData host = new SystemData(hostname, props);
        if (!systems.contains(host))
            systems.add(host);
    }

    @Counted(name = "inventoryAccessCount", absolute = true, monotonic = true, description = "Number of times the list of systems method is requested")
    public InventoryList list() {
        return new InventoryList(systems);
    }

    @Gauge(unit = MetricUnits.NONE, name = "inventorySizeGuage", absolute = true, description = "Number of systems in the inventory")
    public int getTotal() {
        return systems.size();
    }

    private void handleProcessingException(ProcessingException ex) {
        Throwable rootEx = ExceptionUtils.getRootCause(ex);
        if (rootEx != null && rootEx instanceof UnknownHostException) {
            System.err.println("The specified host is unknown.");
        } else {
            throw ex;
        }
    }

}
