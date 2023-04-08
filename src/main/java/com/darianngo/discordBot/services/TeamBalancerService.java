package com.darianngo.discordBot.services;

import com.darianngo.discordBot.dtos.UserDTO;

import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.List;

public interface TeamBalancerService {
	MessageEmbed balanceTeams(List<String> reactions, List<UserDTO> usersReacted);
}
