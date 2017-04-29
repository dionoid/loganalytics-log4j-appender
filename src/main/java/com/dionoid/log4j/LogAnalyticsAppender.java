package com.dionoid.log4j;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import com.google.gson.Gson;

public class LogAnalyticsAppender extends AppenderSkeleton {

	private Gson gson; 
	
	public LogAnalyticsAppender() {
		gson = new Gson();
		instanceName = ManagementFactory.getRuntimeMXBean().getName();
	}
	
	private String logType;
	private String workspaceId;
	private String sharedKey;
	private String instanceName;
	
	/* LogAnalyticsAppender properties */
	
	public String getLogType() { return this.logType; }
	public void setLogType(String logType) { this.logType = logType; }
	
	public String getWorkspaceId() { return this.workspaceId; }
	public void setWorkspaceId(String workspaceId) { this.workspaceId = workspaceId; }
	
	public String getSharedKey() { return this.sharedKey; }
	public void setSharedKey(String sharedKey) { this.sharedKey = sharedKey; }
	
	public String getInstanceName() { return this.instanceName; }
	public void setInstanceName(String instanceName) { this.instanceName = instanceName; }
    
	@Override
	public void close() {
	}
	
	@Override
	public boolean requiresLayout() {
		return false;
	}
	
	@Override
	protected void append(LoggingEvent event) {	
		if (closed) return;

		//create a name-value JSON dictionary
		Map<String, String> logEvent = new LinkedHashMap<String, String>();
		logEvent.put("LogLevel", event.getLevel().toString());
		logEvent.put("Instance", instanceName);
		logEvent.put("Message", event.getRenderedMessage());
		String json = gson.toJson(logEvent);
		
		//and post it to Log Analytic's Data Collector API
		try {
			postToDataCollectorAPI(json);
		} catch (Exception e) {
			System.err.println("[LogAnalyticsAppender] Could not log event. Exception: " + e);
		}
	}
	
    private void postToDataCollectorAPI(String json) throws InvalidKeyException, NoSuchAlgorithmException, IllegalStateException, ClientProtocolException, IOException {
    	postToDataCollectorAPI(json, "2016-04-01");
    }
    
    private void postToDataCollectorAPI(String json, String apiVersion) throws InvalidKeyException, NoSuchAlgorithmException, IllegalStateException, UnsupportedEncodingException {
    	String dataCollectorUri = "https://" + workspaceId + ".ods.opinsights.azure.com/api/logs?api-version=" + apiVersion;
        
        //create ISO 8601 date string
        SimpleDateFormat fmt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.US);
        fmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        String dateString = fmt.format(Calendar.getInstance().getTime()) + " GMT";
        
        //create a DataCollectorAPI request with the required headers
        HttpPost request = new HttpPost(dataCollectorUri);
        request.addHeader("Content-type", "application/json");
    	request.addHeader("Log-Type", getLogType());
    	request.addHeader("x-ms-date", dateString);
    	String authHeader = getAuthorizationHeader("POST", json.length(), "application/json", dateString, "/api/logs");
    	request.addHeader("Authorization", authHeader);
    	request.setEntity(new ByteArrayEntity(StringUtils.getBytesUtf8(json)));
    	
    	//execute the request
    	try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
    		CloseableHttpResponse result = httpClient.execute(request);
    		
    		int status = result.getStatusLine().getStatusCode();
    		if (status / 100 != 2) {
				String responseBody = null;
				try { 
					responseBody = EntityUtils.toString(result.getEntity()); 
				} catch (ParseException | IOException ignore) { }
				System.err.println("[LogAnalyticsAppender] DataCollectorAPI request returned status code " + status + " - " + responseBody);
			}
    	} catch (IOException e) {
    		System.out.println("[LogAnalyticsAppender] DataCollectorAPI request returned exception: " + e);
		}
    }
    
    private String getAuthorizationHeader(String method, int contentLength, String contentType, String date, String resource) throws InvalidKeyException, NoSuchAlgorithmException, IllegalStateException, UnsupportedEncodingException {
        String signatureString = String.format("%s\n%d\n%s\nx-ms-date:%s\n%s", method, contentLength, contentType, date, resource);
        
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(Base64.decodeBase64(sharedKey), "HmacSHA256"));
        
        String signature = new String(Base64.encodeBase64(mac.doFinal(signatureString.getBytes("UTF-8"))));
        return "SharedKey " + workspaceId + ":" + signature;
    }
}
