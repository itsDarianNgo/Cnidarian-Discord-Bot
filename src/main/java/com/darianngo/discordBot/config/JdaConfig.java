package com.darianngo.discordBot.config;

import javax.security.auth.login.LoginException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.darianngo.discordBot.listeners.MessageReactionListener;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

@Configuration
@PropertySource("classpath:application-secrets.properties")
public class JdaConfig {
    @Value("${discord.token}")
    private String token;

    @Bean
    public JDA jda(MessageReactionListener messageReactionListener) throws LoginException, InterruptedException {
        return JDABuilder.createDefault(token)
                .addEventListeners(messageReactionListener)
                .build()
                .awaitReady();
    }
}

