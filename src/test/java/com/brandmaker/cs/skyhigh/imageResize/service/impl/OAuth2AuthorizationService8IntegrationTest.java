package com.brandmaker.cs.skyhigh.imageResize.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import com.brandmaker.cs.skyhigh.imageResize.service.AuthorizationService;

@SpringBootTest
@ActiveProfiles("integration")
class OAuth2AuthorizationService8IntegrationTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(OAuth2AuthorizationService8IntegrationTest.class);

	@Autowired
	private AuthorizationService authorizationService;

	@Autowired
	private Environment env;

	@Test
	void shouldRetrieveOAuth2TokenAndCallMaps80Api() throws Exception {
		String token = authorizationService.getToken();

		assertNotNull(token, "OAuth2 token should not be null");
		assertFalse(token.trim().isEmpty(), "OAuth2 token should not be empty");
		LOGGER.info("OAuth2 access token: {}", token);

		String wmDomain = env.getProperty("application.authentication.wmDomain");
		assertNotNull(wmDomain, "application.authentication.wmDomain must be configured");

		String treeEndpoint = wmDomain + "/maps/rest/api/latest/tree";
		String attachmentsEndpoint = wmDomain + "/maps/rest/api/latest/attachment/node/933871";

		try (CloseableHttpClient httpClient = createHttpClientForIntegrationSsl()) {
			HttpResponse treeResponse = executeGet(httpClient, treeEndpoint, token);
			String treeBody = readEntity(treeResponse.getEntity());
			LOGGER.info("Response /maps/rest/api/latest/tree status: {}", treeResponse.getStatusLine().getStatusCode());
			LOGGER.info("Response /maps/rest/api/latest/tree body: {}", treeBody);

			assertEquals(200, treeResponse.getStatusLine().getStatusCode(),
				"8.0 API should return HTTP 200 for /maps/rest/api/latest/tree");

			HttpResponse attachmentResponse = executeGet(httpClient, attachmentsEndpoint, token);
			String attachmentBody = readEntity(attachmentResponse.getEntity());
			LOGGER.info("Response /maps/rest/api/latest/attachment/node/933871 status: {}",
				attachmentResponse.getStatusLine().getStatusCode());
			LOGGER.info("Response /maps/rest/api/latest/attachment/node/933871 body: {}", attachmentBody);

			assertEquals(200, attachmentResponse.getStatusLine().getStatusCode(),
				"8.0 API should return HTTP 200 for /maps/rest/api/latest/attachment/node/933871");
		}
	}

	private HttpResponse executeGet(CloseableHttpClient httpClient, String endpoint, String token) throws Exception {
		HttpGet getRequest = new HttpGet(endpoint);
		getRequest.addHeader("Authorization", "Bearer " + token);
		return httpClient.execute(getRequest);
	}

	private String readEntity(HttpEntity entity) throws Exception {
		if (entity == null) {
			return "";
		}
		return EntityUtils.toString(entity, StandardCharsets.UTF_8);
	}

	private CloseableHttpClient createHttpClientForIntegrationSsl()
		throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

		boolean allowInsecureSsl = Boolean.parseBoolean(
			env.getProperty("application.integration.allowInsecureSsl", "true"));

		if (!allowInsecureSsl) {
			return HttpClientBuilder.create().build();
		}

		SSLContext sslContext = SSLContextBuilder.create()
			.loadTrustMaterial((TrustStrategy) (certificateChain, authType) -> true)
			.build();

		SSLConnectionSocketFactory sslSocketFactory =
			new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);

		return HttpClientBuilder.create()
			.setSSLSocketFactory(sslSocketFactory)
			.build();
	}
}