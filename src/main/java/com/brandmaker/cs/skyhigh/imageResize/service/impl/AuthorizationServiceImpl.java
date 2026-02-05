package com.brandmaker.cs.skyhigh.imageResize.service.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.brandmaker.cs.skyhigh.imageResize.service.AuthorizationService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class AuthorizationServiceImpl implements AuthorizationService {

	private static final String OAUTH_TOKEN_ENDPOINT = "https://up.%s/access/realms/%s/protocol/openid-connect/token";
	private static final long EXPIRATION_SAFETY_WINDOW_SECONDS = 30L;

	@Autowired
	private Environment env;

	private final ObjectMapper objectMapper = new ObjectMapper();
	private volatile String cachedAccessToken;
	private volatile long cachedAccessTokenExpiresAtEpochMillis;

	@Override
	public String getToken() throws Exception {
		if (isCachedTokenValid()) {
			return cachedAccessToken;
		}

		synchronized (this) {
			if (isCachedTokenValid()) {
				return cachedAccessToken;
			}

			TokenResponse tokenResponse = requestToken();
			cachedAccessToken = tokenResponse.getAccessToken();

			long expiresIn = tokenResponse.getExpiresIn() == null
				? EXPIRATION_SAFETY_WINDOW_SECONDS
				: tokenResponse.getExpiresIn();
			long effectiveExpiresIn = Math.max(1L, expiresIn - EXPIRATION_SAFETY_WINDOW_SECONDS);
			cachedAccessTokenExpiresAtEpochMillis = System.currentTimeMillis() + (effectiveExpiresIn * 1000L);

			return cachedAccessToken;
		}
	}

	private TokenResponse requestToken() {
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			String authBaseDomain = env.getProperty("application.authentication.authBaseDomain");
			String tenantId = env.getProperty("application.authentication.tenantId");
			String clientId = env.getProperty("application.authentication.clientId");
			String clientSecret = env.getProperty("application.authentication.clientSecret");
			String grantType = env.getProperty("application.authentication.grantType");

			if (isBlank(authBaseDomain) || isBlank(tenantId) || isBlank(clientId)
				|| isBlank(clientSecret) || isBlank(grantType)) {
				throw new IllegalStateException(
					"OAuth2 configuration missing. Expected properties: application.authentication.authBaseDomain, "
						+ "application.authentication.tenantId, application.authentication.clientId, "
						+ "application.authentication.clientSecret, application.authentication.grantType");
			}

			String url = String.format(OAUTH_TOKEN_ENDPOINT, authBaseDomain, tenantId);
			HttpPost postRequest = new HttpPost(url);
			postRequest.addHeader("content-type", "application/x-www-form-urlencoded");

			List<NameValuePair> params = new ArrayList<>();
			params.add(new BasicNameValuePair("client_id", clientId));
			params.add(new BasicNameValuePair("client_secret", clientSecret));
			params.add(new BasicNameValuePair("grant_type", grantType));
			postRequest.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

			HttpResponse response = httpClient.execute(postRequest);
			int statusCode = response.getStatusLine().getStatusCode();

			HttpEntity responseEntity = response.getEntity();
			String body = responseEntity == null
				? ""
				: IOUtils.toString(responseEntity.getContent(), StandardCharsets.UTF_8);

			if (statusCode != 200) {
				throw new RuntimeException("Failed to retrieve OAuth2 token. HTTP status code: " + statusCode
					+ ", response body: " + body);
			}

			TokenResponse tokenResponse = objectMapper.readValue(body, TokenResponse.class);
			if (tokenResponse == null || isBlank(tokenResponse.getAccessToken())) {
				throw new RuntimeException("OAuth2 token response did not include access_token");
			}
			return tokenResponse;
		} catch (IOException e) {
			log.error("Failed to retrieve OAuth2 token", e);
			throw new RuntimeException("Failed to retrieve OAuth2 token", e);
		}
	}

	private boolean isCachedTokenValid() {
		return !isBlank(cachedAccessToken) && System.currentTimeMillis() < cachedAccessTokenExpiresAtEpochMillis;
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class TokenResponse {
		@JsonProperty("access_token")
		private String accessToken;

		@JsonProperty("expires_in")
		private Long expiresIn;

		public String getAccessToken() {
			return accessToken;
		}

		public void setAccessToken(String accessToken) {
			this.accessToken = accessToken;
		}

		public Long getExpiresIn() {
			return expiresIn;
		}

		public void setExpiresIn(Long expiresIn) {
			this.expiresIn = expiresIn;
		}
	}
}