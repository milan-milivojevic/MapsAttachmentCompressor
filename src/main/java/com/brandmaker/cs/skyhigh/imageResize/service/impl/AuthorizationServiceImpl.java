package com.brandmaker.cs.skyhigh.imageResize.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;



import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.brandmaker.cs.skyhigh.imageResize.service.AuthorizationService;

@Service
public class AuthorizationServiceImpl implements AuthorizationService{

	@Autowired
	private Environment env;
	
	public String getToken() throws Exception {

		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			
			String url =env.getProperty("server.token.url");
			
			HttpPost postRequest = new HttpPost(url);

			postRequest.addHeader("content-type", "application/x-www-form-urlencoded");

			StringEntity userEntity = new StringEntity("login=Firat.Kilic&password=Porsche@2020");
			postRequest.setEntity(userEntity);

			HttpResponse response = httpClient.execute(postRequest);

			// verify the valid error code first
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != 200) {
				throw new RuntimeException("Failed with HTTP error code : " + statusCode);
			}

			return IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.toString();
		}
		return null;
	}

}
