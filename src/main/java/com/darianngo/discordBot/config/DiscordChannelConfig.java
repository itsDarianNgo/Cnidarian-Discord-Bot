package com.darianngo.discordBot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DiscordChannelConfig {

	@Value("${approvalChannelId}")
	private String approvalChannelId;

	public String getApprovalChannelId() {
		return approvalChannelId;
	}
	
	@Value("${matchChannelId}")
	private String matchChannelId;

	public String getMatchChannelId() {
		return matchChannelId;
	}
}
