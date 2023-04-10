package com.darianngo.discordBot.services;

import java.util.List;

import com.darianngo.discordBot.dtos.UserDTO;

import net.dv8tion.jda.api.entities.MessageEmbed;

public interface TeamBalancerService {

	MessageEmbed balanceTeams(List<String> reactions, List<UserDTO> usersReacted, Long matchId);
}
