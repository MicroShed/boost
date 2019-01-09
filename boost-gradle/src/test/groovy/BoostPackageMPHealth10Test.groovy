/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import static org.gradle.testkit.runner.TaskOutcome.*
 
import static org.junit.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.BeforeClass
import org.junit.AfterClass

import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter

import javax.ws.rs.client.Client
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.core.Response
import javax.json.JsonArray
import javax.json.JsonObject

import org.apache.cxf.jaxrs.provider.jsrjsonp.JsrJsonpProvider

public class BoostPackageMPHealth10Test extends AbstractBoostTest {

	static BuildResult result
	
	static File resourceDir = new File("build/resources/test/test-mpHealth-1.0")
	static File testProjectDir = new File(integTestDir, "BoostPackageMPHealth10Test")
	static String buildFilename = "testMPHealth10.gradle"
	
    private JsonArray servicesStates
    private static HashMap<String, String> dataWhenServicesUP
    private static HashMap<String, String> dataWhenInventoryDown

    static {
        dataWhenServicesUP = new HashMap<String, String>()
        dataWhenInventoryDown = new HashMap<String, String>()

        dataWhenServicesUP.put("SystemResource", "UP")
        dataWhenServicesUP.put("InventoryResource", "UP")

        dataWhenInventoryDown.put("SystemResource", "UP")
        dataWhenInventoryDown.put("InventoryResource", "DOWN")
    }
	
    @BeforeClass
    public static void setup() {
        createDir(testProjectDir)
        createTestProject(testProjectDir, resourceDir, buildFilename)

        result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .forwardOutput()
            .withArguments("boostPackage", "boostStart", "-i", "-s")
            .build()
    }

    @AfterClass
    public static void teardown() {
		
		HealthTestUtil.cleanUp()
			
        result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .forwardOutput()
            .withArguments("boostStop", "-i", "-s")
            .build()
       
        assertEquals(SUCCESS, result.task(":boostStop").getOutcome())
		
    }

    @Test
    public void testIfServicesAreUp() {
        servicesStates = HealthTestUtil.connectToHealthEnpoint(200)
        checkStates(dataWhenServicesUP, servicesStates)
    }

    @Test
    public void testIfInventoryServiceIsDown() {
        servicesStates = HealthTestUtil.connectToHealthEnpoint(200)
        checkStates(dataWhenServicesUP, servicesStates)
        HealthTestUtil.changeInventoryProperty(HealthTestUtil.INV_MAINTENANCE_FALSE, 
                                               HealthTestUtil.INV_MAINTENANCE_TRUE)
        servicesStates = HealthTestUtil.connectToHealthEnpoint(503)
        checkStates(dataWhenInventoryDown, servicesStates)
    }

    private void checkStates(HashMap<String, String> testData, JsonArray servStates) {
        testData.forEach { service, expectedState -> 
            assertEquals("The state of " + service + " service is not matching.", 
                         expectedState, 
                         HealthTestUtil.getActualState(service, servStates));
        }
    }

	
	private class HealthTestUtil {
		
		  private static String port
		  private static String baseUrl
		  private final static String HEALTH_ENDPOINT = "health"
		  public static final String INV_MAINTENANCE_FALSE = "io_openliberty_guides_inventory_inMaintenance\":false"
		  public static final String INV_MAINTENANCE_TRUE = "io_openliberty_guides_inventory_inMaintenance\":true"
		
		  static {
			//port = System.getProperty("liberty.test.port");
			port = "9080"
			baseUrl = "http://localhost:" + port + "/"
		  }
		
		  public static JsonArray connectToHealthEnpoint(int expectedResponseCode) {
			String healthURL = baseUrl + HEALTH_ENDPOINT
			Client client = ClientBuilder.newClient().register(JsrJsonpProvider.class)
			Response response = client.target(healthURL).request().get()
			assertEquals("Response code is not matching " + healthURL,
						 expectedResponseCode, response.getStatus())
			JsonArray servicesStates = response.readEntity(JsonObject.class)
											   .getJsonArray("checks")
			response.close()
			client.close()
			return servicesStates
		  }
		
		  public static String getActualState(String service,
			  JsonArray servicesStates) {
			String state = ""
			for (Object obj : servicesStates) {
			  if (obj instanceof JsonObject) {
				if (service.equals(((JsonObject) obj).getString("name"))) {
				  state = ((JsonObject) obj).getString("state")
				}
			  }
			}
			return state
		  }
		
		  public static void changeInventoryProperty(String oldValue, String newValue) {
			try {
				//String fileName = System.getProperty("user.dir").split("target")[0]
				//		+ "target/it/test-mpHealth-1.0/src/main/resources/CustomConfigSource.json"
				
			  String fileName = new File("build/resources/test/test-mpHealth-1.0/src/main/resources/CustomConfigSource.json") 
			  BufferedReader reader = new BufferedReader(new FileReader(new File(fileName)))
			  String line = ""
			  String oldContent = "", newContent = ""
			  while ((line = reader.readLine()) != null) {
				oldContent += line + "\r\n"
			  }
			  reader.close()
			  newContent = oldContent.replaceAll(oldValue, newValue)
			  FileWriter writer = new FileWriter(fileName)
			  writer.write(newContent)
			  writer.close()
			  Thread.sleep(600)
			} catch (Exception e) {
			  e.printStackTrace()
			}
		  }
		
		  public static void cleanUp() {
			changeInventoryProperty(INV_MAINTENANCE_TRUE, INV_MAINTENANCE_FALSE)
		  }
		
		}
}
