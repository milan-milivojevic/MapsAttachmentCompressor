package com.brandmaker.cs.skyhigh.imageResize.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import com.brandmaker.cs.skyhigh.imageResize.service.AuthorizationService;

@SpringBootTest
class AttachmentUploadIntegrationTest {

	private static final int DEFAULT_NODE_ID = 743563;
	private static final String DEFAULT_COMMENT = "Compressed upload";
	private static final String DEFAULT_ATTACHMENT_NAME = "REPORT_REDUCED_Lviv. замовлення в системі.png";
	private static final String DEFAULT_RESOURCE_NAME = "REPORT_REDUCED_Lviv. замовлення в системі.png";

	@Autowired
	private Environment env;

	@Autowired
	private AuthorizationService authorizationService;

	@Test
	void uploadCompressedAttachmentToNode() throws Exception {
		String serverMainUrl = env.getProperty("server.main.path");
		assertNotNull(serverMainUrl, "server.main.path must be configured");

		String token = authorizationService.getToken();
		assertNotNull(token, "Authorization token must be provided");

		String url = serverMainUrl + "/attachment/node/" + DEFAULT_NODE_ID + "/";

		byte[] payload = readResource(DEFAULT_RESOURCE_NAME);
		try (CloseableHttpClient httpClient = createHttpClientForIntegrationSsl()) {
			HttpPost postReq = new HttpPost(url);
			postReq.addHeader("Authorization", "Bearer " + token);

			MultipartEntityBuilder entity = MultipartEntityBuilder.create()
				.setMimeSubtype("mixed")
				.setMode(HttpMultipartMode.RFC6532)
				.setCharset(StandardCharsets.UTF_8)
				.addTextBody("attachment",
					"{ \"name\": \"" + DEFAULT_ATTACHMENT_NAME + "\", \"comment\": \"" + DEFAULT_COMMENT
						+ "\", \"link\": \"\", \"newWindow\": false }",
					ContentType.APPLICATION_JSON.withCharset(StandardCharsets.UTF_8))
				.addBinaryBody("file",
					payload,
					ContentType.APPLICATION_OCTET_STREAM,
					DEFAULT_RESOURCE_NAME);

			postReq.setEntity(entity.build());

			HttpResponse response = httpClient.execute(postReq);
			HttpEntity httpEntity = response.getEntity();
			if (httpEntity != null) {
				IOUtils.toString(httpEntity.getContent(), StandardCharsets.UTF_8);
			}
			assertEquals(201, response.getStatusLine().getStatusCode(), "Expected attachment to be created");
		}
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

	private byte[] readResource(String resourceName) throws IOException {
		try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourceName)) {
			if (inputStream == null) {
				throw new IOException("Resource not found: " + resourceName);
			}
			return IOUtils.toByteArray(inputStream);
		}
	}
}