package com.sfdc;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.*;

public class AccountTest {

	static final String USERNAME = "ulfat.a.ashraf1@gmail.com";
	static final String PASSWORD = "ws85926056";
	static final String LOGINURL = "https://login.salesforce.com";
	static final String GRANTSERVICE = "/services/oauth2/token?grant_type=password";
	static final String CLIENTID = "3MVG9szVa2RxsqBYpxeMrlnEUu5rOuSIdmMgUKiiQ2sGyD6KFCyyGxAxUsdIr6xd94KHcqLaS67lLADkefKaD";
	static final String CLIENTSECRET = "4626093501901612035";
	private static String REST_ENDPOINT = "/services/data";
	
	private static String API_VERSION = "/v32.0"; // Where exactly in API doc
													// does it come from
	private static String baseUri; // Uniform Resource Identifier. The most
									// common form of URI is the Uniform
									// Resource Locator (URL), frequently
									// referred to informally as a web address
	private static Header oauthHeader; // this is an object of Header
	private static Header prettyPrintHeader = new BasicHeader("X-PrettyPrint", "1");
	// Assemble the login request URL
	static String loginURL = LOGINURL + GRANTSERVICE + "&client_id=" + CLIENTID + "&client_secret=" + CLIENTSECRET
			+ "&username=" + USERNAME + "&password=" + PASSWORD;

	// Login requests must be POSTs

	static HttpPost httpPost = new HttpPost(loginURL);
	static HttpResponse response = null;
	private static String accountId;
	private static String accountName;
	@BeforeClass
	public static void onceExecutedBeforeAll() {

		HttpClient httpclient = HttpClientBuilder.create().build();

		try {
			// Execute the login POST request
			response = httpclient.execute(httpPost);
		} catch (ClientProtocolException cpException) {
			cpException.printStackTrace();
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}

		// verify response is HTTP OK
		final int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode != HttpStatus.SC_OK) {
			System.out.println("Error authenticating to Force.com: " + statusCode);
			// Error is in EntityUtils.toString(response.getEntity())
			return;
		}

		String getResult = null;
		try {
			getResult = EntityUtils.toString(response.getEntity());
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}

		JSONObject jsonObject = null;
		String loginAccessToken = null;
		String loginInstanceUrl = null;

		try {
			jsonObject = (JSONObject) new JSONTokener(getResult).nextValue();
			loginAccessToken = jsonObject.getString("access_token");
			loginInstanceUrl = jsonObject.getString("instance_url");
		} catch (JSONException jsonException) {
			jsonException.printStackTrace();
		}

		baseUri = loginInstanceUrl + REST_ENDPOINT + API_VERSION;
		oauthHeader = new BasicHeader("Authorization", "OAuth " + loginAccessToken);
		System.out.println("oauthHeader1: " + oauthHeader);
		System.out.println("\n" + response.getStatusLine());
		System.out.println("Successful login");
		System.out.println("instance URL: " + loginInstanceUrl);
		System.out.println("access token/session ID: " + loginAccessToken);
		System.out.println("baseUri: " + baseUri);
		System.out.println("@BeforeClass: onceExecutedBeforeAll");
	}

	@Test
	public void ValidateTotalAccountsObject() {

        System.out.println("\n_______________ Accounts QUERY to find All Accounts _______________");
        try {
 
            //Set up the HTTP objects needed to make the request.
            HttpClient httpClient = HttpClientBuilder.create().build();
 
            String uri = baseUri + "/query?q=Select+Id+,+name+From+Account";
          //  String uri = baseUri + "/query?q=Select+Id+,+name+From+Account+Limit+5";
            System.out.println("Query URL: " + uri);
            HttpGet httpGet = new HttpGet(uri);
            System.out.println("oauthHeader2: " + oauthHeader);
            httpGet.addHeader(oauthHeader);
            httpGet.addHeader(prettyPrintHeader);
 
            // Make the request.
            HttpResponse response = httpClient.execute(httpGet);
 
            // Process the result
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                String response_string = EntityUtils.toString(response.getEntity());
                try {
                    JSONObject json = new JSONObject(response_string);
                    System.out.println("JSON result of Query:\n" + json.toString(1));
                    JSONArray j = json.getJSONArray("records");
                 assertEquals(15,j.length());
                } catch (JSONException je) {
                    je.printStackTrace();
                }
            } else {
                System.out.println("Query was unsuccessful. Status code returned is " + statusCode);
                System.out.println("An error has occured. Http status: " + response.getStatusLine().getStatusCode());
                System.out.println(getBody(response.getEntity().getContent()));
                System.exit(-1);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
    
	}

	@AfterClass
	public static void onceExecutedAfterAll() {
		// release connection
		httpPost.releaseConnection();

		System.out.println("@AfterClass: onceExecutedAfterAll and successfully logout");
	}
	private static String getBody(InputStream inputStream) {
		String result = "";
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				result += inputLine;
				result += "\n";
			}
			in.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return result;
	
	}
}