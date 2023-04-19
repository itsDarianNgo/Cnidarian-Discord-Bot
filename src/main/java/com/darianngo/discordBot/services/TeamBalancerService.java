package com.darianngo.discordBot.services;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.darianngo.discordBot.dtos.UserDTO;

import net.dv8tion.jda.api.entities.MessageEmbed;

public interface TeamBalancerService {

	Pair<MessageEmbed, Boolean> balanceTeams(List<String> reactions, List<UserDTO> usersReacted, Long matchId);

}
