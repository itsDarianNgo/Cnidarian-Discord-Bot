package com.darianngo.discordBot.services;

import java.util.List;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;


public interface DirectMessageService {

	void sendEndMatchButtons(List<User> usersReacted, Long matchId);

	void onButtonClick(ButtonClickEvent event);
}
