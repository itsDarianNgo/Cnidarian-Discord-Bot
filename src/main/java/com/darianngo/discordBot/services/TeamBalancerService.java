package com.darianngo.discordBot.services;

import com.darianngo.discordBot.dtos.UserDTO;

import java.util.List;

public interface TeamBalancerService {
    String balanceTeams(List<String> reactions, List<UserDTO> usersReacted);
}
