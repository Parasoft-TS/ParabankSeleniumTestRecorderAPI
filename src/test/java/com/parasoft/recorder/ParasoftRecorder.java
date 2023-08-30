package com.parasoft.recorder;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ParasoftRecorder {

	private static final Logger log = LoggerFactory.getLogger(ParasoftRecorder.class);

	private String SOATEST_HOST = ""; // = "localhost";
	private String SOATEST_PORT = ""; // = "9080";
	private String RECORDER_HOST = ""; // = "localhost";
	private String RECORDER_BASE_URL = ""; // = "http://"+RECORDER_HOST+":"+"40090";

	private String recordingProxyPort = "";
	private String recordingSessionId = "";

	public ParasoftRecorder(String soatestHost, String soatestPort, String recorderHost, String recorderPort) {
		this.SOATEST_HOST = soatestHost;
		this.SOATEST_PORT = soatestPort;
		this.RECORDER_HOST = recorderHost;
		this.RECORDER_BASE_URL = "http://" + recorderHost + ":" + recorderPort;
	}

	// use with localhost default ports execution
	public ParasoftRecorder() {
		this("localhost", "9080", "localhost", "40090");
	}

	public WebDriver startRecordingAndSetupChromeDriver(ChromeOptions opts) {
		WebDriver driver = null;

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

		// Setup Chrome Driver
		if (this.recordingProxyPort.isEmpty() || this.recordingSessionId.isEmpty()) {
			log.error("recording session has a problem, id or proxy port is empty - returning normal ChromeDriver");
			driver = new ChromeDriver(opts);
		} else {
			// initialize the proxy with the proxy port returned by the Parasoft Recorder
			// API
			Proxy proxy = new Proxy();
			proxy.setHttpProxy(RECORDER_HOST + ":" + recordingProxyPort);
			proxy.setSslProxy(RECORDER_HOST + ":" + recordingProxyPort);

			// tell Selenium to set the UI to use the Proxy
			opts.setCapability("proxy", proxy);

			driver = new ChromeDriver(opts);
		}

		return driver;
	}

	public void stopRecordingAndCreateTST(String testName) {
		Boolean sessionStopped = stopSession();
		if (sessionStopped) {
			Boolean trafficSent = sendTrafficToSOAtest(testName);
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

	public String getSOAtestHost() {
		return this.SOATEST_HOST;
	}

	public String getSOAtestPort() {
		return this.SOATEST_PORT;
	}

	public String getRecorderBaseURL() {
		return this.RECORDER_BASE_URL;
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

	private Boolean isSessionsEmpty() {
		Boolean isEmpty = false;

		log.info("checking for empty sessions");

		// Check to make sure no active sessions are open
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			CloseableHttpResponse response = null;
			HttpGet httpGet = new HttpGet(RECORDER_BASE_URL + "/api/v1/sessions");
			RequestConfig.Builder requestConfig = RequestConfig.custom();
			requestConfig.setConnectionRequestTimeout(30L, TimeUnit.SECONDS);
			httpGet.setConfig(requestConfig.build());

			try {
				response = httpClient.execute(httpGet);

				if (String.valueOf(response.getCode()).equals("200")) {
					log.debug("GET /api/v1/sessions - 200 response");
					HttpEntity entity = response.getEntity();
					JsonObject object = (JsonObject) JsonParser
							.parseReader(new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8));

					log.debug("GET /api/v1/sessions - response");
					log.debug(object.toString());

					// check if sessions response is empty
					JsonArray sessions = object.getAsJsonArray("sessions");
					if (sessions.isEmpty())
						isEmpty = true;

					EntityUtils.consume(entity);
				} else {
					log.error("GET /api/v1/sessions - " + String.valueOf(response.getCode()) + " response");
					log.error(response.toString());

					HttpEntity entity = response.getEntity();
					String responseString = EntityUtils.toString(entity);
					log.error(responseString);
				}
			} catch (Exception e) {
				log.error("could not execute GET /api/v1/sessions", e);
			} finally {
				if (response != null) {
					try {
						response.close();
					} catch (IOException e) {
						log.error("could not close CloseableHttpResponse", e);
					}
				}
			}
		} catch (IOException e1) {
			log.error("could not create CloseableHttpClient", e1);
		}

		return isEmpty;
	}

	private Boolean startNewSession() {
		Boolean sessionStarted = false;
		JsonObject responseObject = null;

		log.info("starting new session");

		// Start new session
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			CloseableHttpResponse response = null;
			HttpPost httpPost = new HttpPost(RECORDER_BASE_URL + "/api/v1/sessions");
			RequestConfig.Builder requestConfig = RequestConfig.custom();
			requestConfig.setConnectionRequestTimeout(30L, TimeUnit.SECONDS);
			httpPost.setConfig(requestConfig.build());

			StringEntity requestEntity = new StringEntity("{\"soavirt\":{\"host\":\"" + SOATEST_HOST + "\",\"port\":\""
					+ SOATEST_PORT + "\",\"secure\":false}}", ContentType.APPLICATION_JSON);
			httpPost.setEntity(requestEntity);

			String request = "";
			try {
				log.debug("POST /api/v1/sessions - request body");
				request = EntityUtils.toString(requestEntity);
				log.debug(request);
			} catch (ParseException e1) {
				log.error("could not parse request", e1);
			}

			try {
				response = httpClient.execute(httpPost);

				if (String.valueOf(response.getCode()).equals("200")) {
					log.debug("POST /api/v1/sessions - 200 response");
					sessionStarted = true;

					HttpEntity entity = response.getEntity();
					responseObject = (JsonObject) JsonParser
							.parseReader(new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8));

					// set sessionId and proxyPort
					setRecordingSessionId(responseObject.get("id").getAsString());
					setRecordingProxyPort(responseObject.getAsJsonObject("proxySettings").get("port").getAsString());

					log.debug("POST /api/v1/sessions response");
					log.debug(responseObject.toString());

					EntityUtils.consume(entity);
				} else {
					log.error("POST /api/v1/sessions - " + String.valueOf(response.getCode()) + " response");
					log.error(response.toString());

					HttpEntity entity = response.getEntity();
					String responseString = EntityUtils.toString(entity);
					log.error(responseString);
				}
			} catch (Exception e) {
				log.error("could not execute POST /api/v1/sessions", e);
			} finally {
				if (response != null) {
					try {
						response.close();
					} catch (IOException e) {
						log.error("could not close CloseableHttpResponse", e);
					}
				}
			}
		} catch (IOException e1) {
			log.error("could not create CloseableHttpClient", e1);
		}

		return sessionStarted;
	}

	private Boolean stopSession() {
		Boolean isStopped = false;

		log.info("stopping session");

		// Put call that stops the recording session
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			CloseableHttpResponse response = null;
			HttpPut httpPut = new HttpPut(RECORDER_BASE_URL + "/api/v1/sessions/" + this.recordingSessionId);
			RequestConfig.Builder requestConfig = RequestConfig.custom();
			requestConfig.setConnectionRequestTimeout(30L, TimeUnit.SECONDS);
			httpPut.setConfig(requestConfig.build());

			StringEntity requestEntity = new StringEntity("{\"state\":\"stopped\"}", ContentType.APPLICATION_JSON);
			httpPut.setEntity(requestEntity);

			try {
				response = httpClient.execute(httpPut);

				if (String.valueOf(response.getCode()).equals("200")) {
					isStopped = true;
					log.debug("PUT /api/v1/sessions/" + this.recordingSessionId + " - 200 response");
				} else {
					log.error("PUT /api/v1/sessions/" + this.recordingSessionId + " - "
							+ String.valueOf(response.getCode()) + " response");
					log.error(response.toString());

					HttpEntity entity = response.getEntity();
					String responseString = EntityUtils.toString(entity);
					log.error(responseString);
				}

				EntityUtils.consume(response.getEntity());
			} catch (Exception e) {
				log.error("could not execute PUT /api/v1/sessions/" + this.recordingSessionId, e);
			} finally {
				if (response != null) {
					try {
						response.close();
					} catch (IOException e) {
						log.error("could not close CloseableHttpResponse", e);
					}
				}
			}
		} catch (IOException e1) {
			log.error("could not create CloseableHttpClient", e1);
		}

		return isStopped;
	}

	private Boolean sendTrafficToSOAtest(String testName) {
		Boolean success = false;

		log.info("sending API traffic to SOAtest");

		// Post call that sends recorded API traffic to SOAtest Server
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			CloseableHttpResponse response = null;
			HttpPost httpPost = new HttpPost(
					RECORDER_BASE_URL + "/api/v1/sessions/" + this.recordingSessionId + "/tsts");
			RequestConfig.Builder requestConfig = RequestConfig.custom();
			requestConfig.setConnectionRequestTimeout(30L, TimeUnit.SECONDS);
			httpPost.setConfig(requestConfig.build());

			StringEntity requestEntity = new StringEntity("{" + "\"name\":\"" + testName + "\"" + "}",
					ContentType.APPLICATION_JSON);
			httpPost.setEntity(requestEntity);

			String request = "";
			try {
				log.debug("POST /api/v1/sessions/" + this.recordingSessionId + "/tsts - request body");
				request = EntityUtils.toString(requestEntity);
				log.debug(request);
			} catch (ParseException e1) {
				log.error("could not parse request", e1);
			}

			try {
				response = httpClient.execute(httpPost);

				if (String.valueOf(response.getCode()).equals("200")) {
					success = true;
					log.debug("POST /api/v1/sessions/" + this.recordingSessionId + "/tsts - 200 response");
				} else {
					log.error("POST /api/v1/sessions/" + this.recordingSessionId + "/tsts - "
							+ String.valueOf(response.getCode()) + " response");
					log.error(response.toString());

					HttpEntity entity = response.getEntity();
					String responseString = EntityUtils.toString(entity);
					log.error(responseString);
				}

				EntityUtils.consume(response.getEntity());
			} catch (Exception e) {
				log.error("could not execute POST /api/v1/sessions/" + this.recordingSessionId + "/tsts", e);
			} finally {
				if (response != null) {
					try {
						response.close();
					} catch (IOException e) {
						log.error("could not close CloseableHttpResponse", e);
					}
				}
			}
		} catch (IOException e1) {
			log.error("could not create CloseableHttpClient", e1);
		}

		return success;
	}

	private Boolean endRecordingSession() {
		Boolean isEnded = false;

		log.info("ending recording session");

		// Delete call that ends the recording session
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			CloseableHttpResponse response = null;
			HttpDelete httpDelete = new HttpDelete(RECORDER_BASE_URL + "/api/v1/sessions/" + this.recordingSessionId);
			RequestConfig.Builder requestConfig = RequestConfig.custom();
			requestConfig.setConnectionRequestTimeout(30L, TimeUnit.SECONDS);
			httpDelete.setConfig(requestConfig.build());

			try {
				response = httpClient.execute(httpDelete);

				if (String.valueOf(response.getCode()).equals("200")) {
					isEnded = true;
					log.debug("DELETE /api/v1/sessions/" + this.recordingSessionId + " - 200 response");
				} else {
					log.error("DELETE /api/v1/sessions/" + this.recordingSessionId + " - "
							+ String.valueOf(response.getCode()) + " response");
					log.error(response.toString());

					HttpEntity entity = response.getEntity();
					String responseString = EntityUtils.toString(entity);
					log.error(responseString);
				}

				EntityUtils.consume(response.getEntity());
			} catch (Exception e) {
				log.error("could not execute DELETE /api/v1/sessions/" + this.recordingSessionId, e);
			} finally {
				if (response != null) {
					try {
						response.close();
					} catch (IOException e) {
						log.error("could not close CloseableHttpResponse", e);
					}
				}
			}
		} catch (IOException e1) {
			log.error("could not create CloseableHttpClient", e1);
		}

		return isEnded;
	}
}