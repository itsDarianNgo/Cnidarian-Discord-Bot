package com.darianngo.discordBot.config;

import javax.security.auth.login.LoginException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.darianngo.discordBot.commands.CreateCustomGameCommand;
import com.darianngo.discordBot.commands.LeaderboardCommand;
import com.darianngo.discordBot.commands.SetupLoLProfileCommand;
import com.darianngo.discordBot.commands.ShowLoLProfileCommand;
import com.darianngo.discordBot.listeners.ButtonClickListener;
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
	public JDA jda(@Autowired MessageReactionListener messageReactionListener,
			@Autowired SlashCommandListener slashCommandListener, @Autowired ButtonClickListener buttonClickListener)
			throws LoginException, InterruptedException {
		JDA jda = JDABuilder.createDefault(token)
				.addEventListeners(messageReactionListener, slashCommandListener, buttonClickListener).build()
				.awaitReady();

		registerSlashCommands(jda);

		return jda;
	}

	private void registerSlashCommands(JDA jda) {
		jda.upsertCommand(SetupLoLProfileCommand.COMMAND_DATA).queue();
		jda.upsertCommand(ShowLoLProfileCommand.COMMAND_DATA).queue();
		jda.upsertCommand(CreateCustomGameCommand.COMMAND_DATA).queue();
		jda.upsertCommand(LeaderboardCommand.COMMAND_DATA).queue();

	}
}
