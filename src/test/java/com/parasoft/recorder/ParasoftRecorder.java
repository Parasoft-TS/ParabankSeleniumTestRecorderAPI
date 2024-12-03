package com.parasoft.recorder;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestWatcher;

import java.nio.charset.StandardCharsets;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.openqa.selenium.Proxy;
import org.openqa.selenium.chrome.ChromeOptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ParasoftRecorder implements BeforeEachCallback, AfterEachCallback, TestWatcher, ParameterResolver {

	private static final Logger log = LoggerFactory.getLogger(ParasoftRecorder.class);

	private static String SOATEST_HOST = ""; // = "localhost";
	private static String SOATEST_PORT = ""; // = "9080";
	private static String RECORDER_HOST = ""; // = "localhost";
	private static String RECORDER_PORT = ""; // = "40090";
	private static String RECORDER_BASE_URL = ""; // = "http://"+RECORDER_HOST+":"+RECORDER_PORT;

	private String recordingProxyPort = "";
	private String recordingSessionId = "";

	static {
		SOATEST_HOST = System.getProperty("SOATEST_HOST","localhost");
		SOATEST_PORT = System.getProperty("SOATEST_PORT","9080");
		RECORDER_HOST = System.getProperty("localhost","localhost");
		RECORDER_PORT = System.getProperty("RECORDER_PORT","40090");
		RECORDER_BASE_URL = "http://"+RECORDER_HOST+":"+RECORDER_PORT;
	}
	
	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		ChromeOptions opts = new ChromeOptions();
		
		// Start Recording Session
		Boolean sessionsEmpty = isSessionsEmpty();
		if (sessionsEmpty) {
			Boolean sessionStarted = startNewSession();
			if (!sessionStarted) {
				log.error("could not start recording session");
			}
		} else {
			log.error("sessions were not empty, recording did not start");
		}		
		opts = setupChromeOptions(opts);
		
		// Store ChromeOptions in ExtensionContext store
		context.getStore(ExtensionContext.Namespace.GLOBAL).put("opts",opts);
	}
	
	@Override
	public void afterEach(ExtensionContext context) {
		String testId = context.getTestClass().get().getName() + '#' + context.getTestMethod().get().getName();
		Boolean sessionStopped = stopSession();
		if (sessionStopped) {
			Boolean trafficSent = sendTrafficToSOAtest(testId);
			if (trafficSent) {
				Boolean sessionEnded = endRecordingSession();
				if (!sessionEnded) {
					log.error("session stopped and traffic sent, but session could not be ended");
				}
			} else {
				log.error("session stopped but traffic was not sent");
			}
		} else {
			log.error("session could not be stopped, traffic was not sent");
		}
	}

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        // We want to inject ExtensionContext into the test class' beforeEach method
        return parameterContext.getParameter().getType().equals(ExtensionContext.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        // Return the ExtensionContext for injection
        return extensionContext;
    }
	
	public String getRecordingSessionId() {
		return this.recordingSessionId;
	}

	public String getRecordingProxyPort() {
		return this.recordingProxyPort;
	}

	public void setRecordingSessionId(String id) {
		this.recordingSessionId = id;
	}

	public void setRecordingProxyPort(String port) {
		this.recordingProxyPort = port;
	}

	private ChromeOptions setupChromeOptions(ChromeOptions opts) {
		// Setup Chrome Driver
		if (this.recordingProxyPort.isEmpty() || this.recordingSessionId.isEmpty()) {
			log.error("recording session has a problem, id: <{}> or proxy port: <{}> is empty - returning normal ChromeDriver",this.recordingSessionId,this.recordingProxyPort);
		} else {
			// initialize the proxy with the proxy port returned by the Parasoft Recorder API
			Proxy proxy = new Proxy();
			proxy.setHttpProxy(RECORDER_HOST + ":" + recordingProxyPort); // proxy http connections
			proxy.setSslProxy(RECORDER_HOST + ":" + recordingProxyPort); // proxy https connections
			proxy.setNoProxy("<-loopback>"); // override proxying localhost connections
			
			// tell Selenium to set the UI to use the Proxy
			opts.setProxy(proxy);
		}

		return opts;
	}
	
	// Calling GET /api/v1/sessions and checking for 200 response
	private Boolean isSessionsEmpty() {
		log.info("checking for empty sessions");

		try {
			// Create HttpClient
			HttpClient client = HttpClient.newBuilder()
					.connectTimeout(java.time.Duration.ofSeconds(30))
					.build();
			
			// Build the HTTP GET request
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(RECORDER_BASE_URL + "/api/v1/sessions"))
					.timeout(java.time.Duration.ofSeconds(30))
					.GET()
					.build();
			
			// Send the request and get the response
			HttpResponse<String> response = client.send(request,  HttpResponse.BodyHandlers.ofString());
			
			// Check response
			if (response.statusCode() == 200) {
				log.debug("GET /api/v1/sessions - 200 response");
				
				// Parse the JSON response
                JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
                log.debug("Response body: {}", response.body());

                JsonArray sessions = jsonObject.getAsJsonArray("sessions");
                return sessions.isEmpty();
			} else {
				log.error("GET /api/v1/sessions - Unexpected response status: {}", response.statusCode());
                log.error("Response body: {}", response.body());
			}

		} catch (Exception e) {
			log.error("could not execute GET /api/v1/sessions", e);
		}
			
		return false;
	}

	// Calling POST /api/v1/sessions to start a session and retrieve the recordingSessionId and recordingProxyPort
	private Boolean startNewSession() {
		log.info("starting new session");

		// Start new session
		try {
			// Create HttpClient
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(30))
                    .build();

            // Construct JSON request body
            String requestBody = String.format(
                "{\"soavirt\":{\"host\":\"%s\",\"port\":\"%s\",\"secure\":false}}",
                SOATEST_HOST, SOATEST_PORT
            );
            
            // Build HTTP POST request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(RECORDER_BASE_URL + "/api/v1/sessions"))
                    .header("Content-Type", "application/json")
                    .timeout(java.time.Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();
            
            // Send the request and handle response
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                log.debug("POST /api/v1/sessions - 200 response");

                // Parse the JSON response
                JsonObject responseObject = JsonParser.parseString(response.body()).getAsJsonObject();
                log.debug("Response body: {}", responseObject);

                // Set session ID and proxy port
                setRecordingSessionId(responseObject.get("id").getAsString());
                setRecordingProxyPort(responseObject.getAsJsonObject("proxySettings").get("port").getAsString());

                return true;
            } else {
                log.error("POST /api/v1/sessions - Unexpected response status: {}", response.statusCode());
                log.error("Response body: {}", response.body());
            }
		} catch (Exception e) {
			log.error("Error during POST /api/v1/sessions", e);
		}
		
		return false;
	}

	// Calling PUT /api/v1/sessions/{recordingSessionId} to stop the recording session
	private Boolean stopSession() {
		log.info("stopping session");

		try {
            // Create HttpClient
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(30))
                    .build();

            // Construct JSON request body
            String requestBody = "{\"state\":\"stopped\"}";

            // Build HTTP PUT request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(RECORDER_BASE_URL + "/api/v1/sessions/" + recordingSessionId))
                    .header("Content-Type", "application/json")
                    .timeout(java.time.Duration.ofSeconds(30))
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            // Send the request and handle response
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                log.debug("PUT /api/v1/sessions/{} - 200 response", recordingSessionId);
                return true;
            } else {
                log.error("PUT /api/v1/sessions/{} - Unexpected response status: {}", recordingSessionId, response.statusCode());
                log.error("Response body: {}", response.body());
            }
        } catch (Exception e) {
            log.error("Error during PUT /api/v1/sessions/" + recordingSessionId, e);
        }

        return false;
	}

	// Calling POST /api/v1/sessions/{recordingSessionId}/tsts to send recorded HTTP traffic to SOAtest for API test creation
	private Boolean sendTrafficToSOAtest(String testName) {
		log.info("sending API traffic to SOAtest");

        try {
            // Create HttpClient
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(30))
                    .build();

            // Construct JSON request body
            String requestBody = String.format("{\"name\":\"%s\"}", testName);

            // Build HTTP POST request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(RECORDER_BASE_URL + "/api/v1/sessions/" + recordingSessionId + "/tsts"))
                    .header("Content-Type", "application/json")
                    .timeout(java.time.Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            // Send the request and handle response
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                log.debug("POST /api/v1/sessions/{}/tsts - 200 response", recordingSessionId);
                return true;
            } else {
                log.error("POST /api/v1/sessions/{}/tsts - Unexpected response status: {}", recordingSessionId, response.statusCode());
                log.error("Response body: {}", response.body());
            }
        } catch (Exception e) {
            log.error("Error during POST /api/v1/sessions/" + recordingSessionId + "/tsts", e);
        }

        return false;
	}

	// Calling DELETE /api/v1/sessions/{recordingSessionId} to end recording session
	private Boolean endRecordingSession() {
		log.info("ending recording session");

		// Delete call that ends the recording session
		try {
            // Create HttpClient
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(30))
                    .build();

            // Build HTTP DELETE request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(RECORDER_BASE_URL + "/api/v1/sessions/" + recordingSessionId))
                    .timeout(java.time.Duration.ofSeconds(30))
                    .DELETE()
                    .build();

            // Send the request and handle response
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                log.debug("DELETE /api/v1/sessions/{} - 200 response", recordingSessionId);
                return true;
            } else {
                log.error("DELETE /api/v1/sessions/{} - Unexpected response status: {}", recordingSessionId, response.statusCode());
                log.error("Response body: {}", response.body());
            }
        } catch (Exception e) {
            log.error("Error during DELETE /api/v1/sessions/" + recordingSessionId, e);
        }

        return false;
	}
}