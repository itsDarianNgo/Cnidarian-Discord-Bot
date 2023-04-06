package com.darianngo.discordBot.util;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.darianngo.discordBot.config.RiotApiConfig;
import com.darianngo.discordBot.dtos.UserDTO;
import com.google.gson.JsonObject;

public class TournamentCodeCreator {

	public String createTournamentCode(List<UserDTO> users) throws IOException {
		String url = "https://americas.api.riotgames.com/lol/tournament-stub/v4/codes";
		String apiKey = RiotApiConfig.API_KEY;

		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(url);
		httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");
		httpPost.setHeader("X-Riot-Token", apiKey);

		JsonObject requestBody = new JsonObject();
		requestBody.addProperty("mapType", "SUMMONERS_RIFT");
		requestBody.addProperty("pickType", "TOURNAMENT_DRAFT");
		requestBody.addProperty("teamSize", 5);

		httpPost.setEntity(new StringEntity(requestBody.toString()));

		CloseableHttpResponse response = httpClient.execute(httpPost);
		int statusCode = response.getStatusLine().getStatusCode();
		String responseBody = EntityUtils.toString(response.getEntity());

		if (statusCode == HttpStatus.SC_OK) {
			return responseBody;
		} else {
			throw new IOException("Error creating the tournament code. Status code: " + statusCode + ". Response body: "
					+ responseBody);
		}
	}
}
