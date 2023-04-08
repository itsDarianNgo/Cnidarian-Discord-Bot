package com.darianngo.discordBot.config;

import javax.security.auth.login.LoginException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.darianngo.discordBot.commands.SetupLoLProfileCommand;
import com.darianngo.discordBot.commands.ShowLoLProfileCommand;
import com.darianngo.discordBot.listeners.MessageReactionListener;
import com.darianngo.discordBot.listeners.SlashCommandListener;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

@Configuration
@PropertySource("classpath:application-secrets.properties")
public class JdaConfig {
	@Value("${discord.token}")
	private String token;

	@Bean
	public JDA jda(MessageReactionListener messageReactionListener, SlashCommandListener slashCommandListener)
			throws LoginException, InterruptedException {
		JDA jda = JDABuilder.createDefault(token).addEventListeners(messageReactionListener, slashCommandListener)
				.build().awaitReady();

		registerSlashCommands(jda);

		return jda;
	}

	private void registerSlashCommands(JDA jda) {
		jda.upsertCommand(SetupLoLProfileCommand.COMMAND_DATA).queue();
		jda.upsertCommand(ShowLoLProfileCommand.COMMAND_DATA).queue();
	}
}
