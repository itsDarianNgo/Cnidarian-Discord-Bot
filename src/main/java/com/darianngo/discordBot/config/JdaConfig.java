package com.darianngo.discordBot.config;

import javax.security.auth.login.LoginException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.darianngo.discordBot.commands.CreateReactionMessageCommand;
import com.darianngo.discordBot.commands.MonitorChannelCommand;
import com.darianngo.discordBot.listeners.MessageReactionListener;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

@Configuration
@PropertySource("classpath:application-secrets.properties")
public class JdaConfig {
	@Value("${discord.token}")
	private String token;

	@Bean
	public JDA jda(MessageReactionListener messageReactionListener, MonitorChannelCommand monitorChannelCommand,
			CreateReactionMessageCommand createReactionMessageCommand) throws LoginException, InterruptedException {
		return JDABuilder.createDefault(token)
				.addEventListeners(messageReactionListener, monitorChannelCommand, createReactionMessageCommand)
				.build().awaitReady();
	}

	@Bean
	public MonitorChannelCommand monitorChannelCommand() {
		return new MonitorChannelCommand();
	}

	@Bean
	public CreateReactionMessageCommand createReactionMessageCommand() {
		return new CreateReactionMessageCommand();
	}
}
