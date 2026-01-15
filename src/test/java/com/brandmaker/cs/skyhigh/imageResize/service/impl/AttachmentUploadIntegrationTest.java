package com.brandmaker.cs.skyhigh.imageResize.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import com.brandmaker.cs.skyhigh.imageResize.service.AuthorizationService;

@SpringBootTest
class AttachmentUploadIntegrationTest {

	private static final int DEFAULT_NODE_ID = 898692;
	private static final String DEFAULT_COMMENT = "Compressed upload";
	private static final String DEFAULT_ATTACHMENT_NAME = "REPORT_REDUCED_Screenshot 2025-02-07 at 12.57.20 PM.png";
	private static final String DEFAULT_RESOURCE_NAME = "REPORT_REDUCED_Screenshot 2025-02-07 at 12.57.20 PM.png";

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
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			HttpPost postReq = new HttpPost(url);
			postReq.addHeader("Authorization", "Bearer " + token.replace("{\"access_token\":", "")
				.replace("}", "").replace("\"", ""));

			MultipartEntityBuilder entity = MultipartEntityBuilder.create()
				.setMimeSubtype("mixed")
				.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
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

	private byte[] readResource(String resourceName) throws IOException {
		try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourceName)) {
			if (inputStream == null) {
				throw new IOException("Resource not found: " + resourceName);
			}
			return IOUtils.toByteArray(inputStream);
		}
	}
}