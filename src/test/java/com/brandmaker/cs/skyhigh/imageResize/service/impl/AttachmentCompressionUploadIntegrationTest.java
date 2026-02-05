package com.brandmaker.cs.skyhigh.imageResize.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLContext;

import org.apache.commons.io.FilenameUtils;
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
class AttachmentCompressionUploadIntegrationTest {

	private static final int DEFAULT_NODE_ID = 743563;
	private static final String DEFAULT_COMMENT = "Local Test upload";
	private static final String SOURCE_RESOURCE_NAME = "Test_Compression_Tool_&#+-()%_äöüß_язык_合気道.png";
	private static final String OUTPUT_PREFIX = "REPORT_REDUCED_";
	private static final int DEFAULT_MAX_HEIGHT = 1024;

	@Autowired
	private Environment env;

	@Autowired
	private AuthorizationService authorizationService;

	@Test
	void compressAndUploadAttachmentToNode() throws Exception {
		String serverMainUrl = env.getProperty("server.main.path");
		assertNotNull(serverMainUrl, "server.main.path must be configured");

		String token = authorizationService.getToken();
		assertNotNull(token, "Authorization token must be provided");

		Path tempDir = Files.createTempDirectory("compression-test");
		Path compressedFile = tempDir.resolve(OUTPUT_PREFIX + SOURCE_RESOURCE_NAME);
		try {
			writeCompressedResource(compressedFile);

			String url = serverMainUrl + "/attachment/node/" + DEFAULT_NODE_ID + "/";
			try (CloseableHttpClient httpClient = createHttpClientForIntegrationSsl()) {
				HttpPost postReq = new HttpPost(url);
				postReq.addHeader("Authorization", "Bearer " + token);

				byte[] payload = Files.readAllBytes(compressedFile);
				MultipartEntityBuilder entity = MultipartEntityBuilder.create()
					.setMimeSubtype("mixed")
					.setMode(HttpMultipartMode.RFC6532)
					.setCharset(StandardCharsets.UTF_8)
					.addTextBody("attachment",
						"{ \"name\": \"" + compressedFile.getFileName() + "\", \"comment\": \"" + DEFAULT_COMMENT
							+ "\", \"link\": \"\", \"newWindow\": false }",
						ContentType.APPLICATION_JSON.withCharset(StandardCharsets.UTF_8))
					.addBinaryBody("file",
						payload,
						ContentType.APPLICATION_OCTET_STREAM,
						compressedFile.getFileName().toString());

				postReq.setEntity(entity.build());

				HttpResponse response = httpClient.execute(postReq);
				HttpEntity httpEntity = response.getEntity();
				if (httpEntity != null) {
					IOUtils.toString(httpEntity.getContent(), StandardCharsets.UTF_8);
				}
				assertEquals(201, response.getStatusLine().getStatusCode(), "Expected attachment to be created");
			}
		} finally {
			Files.deleteIfExists(compressedFile);
			Files.deleteIfExists(tempDir);
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

	private void writeCompressedResource(Path outputPath) throws IOException {
		String extension = FilenameUtils.getExtension(SOURCE_RESOURCE_NAME);
		try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(SOURCE_RESOURCE_NAME)) {
			if (inputStream == null) {
				throw new IOException("Resource not found: " + SOURCE_RESOURCE_NAME);
			}
			BufferedImage image = ImageIO.read(inputStream);
			if (image == null) {
				throw new IOException("Unable to read image: " + SOURCE_RESOURCE_NAME);
			}

			int originalHeight = image.getHeight();
			int originalWidth = image.getWidth();
			int resizedWidth = originalWidth;
			int resizedHeight = originalHeight;

			if (originalHeight > DEFAULT_MAX_HEIGHT) {
				if (originalHeight <= originalWidth) {
					int difference = originalHeight - DEFAULT_MAX_HEIGHT;
					resizedWidth = originalWidth - difference;
					resizedHeight = DEFAULT_MAX_HEIGHT;
				} else {
					resizedHeight = DEFAULT_MAX_HEIGHT;
					resizedWidth = originalWidth;
				}
			}

			BufferedImage resized = image;
			if (originalHeight > DEFAULT_MAX_HEIGHT) {
				resized = ImageResizeImpl.resize(outputPath.getFileName().toString(), image, resizedHeight, resizedWidth);
			}
			ImageIO.write(resized, extension, outputPath.toFile());
		}
	}
}