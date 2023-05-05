package com.darianngo.discordBot.services;

import java.util.List;

import com.darianngo.discordBot.dtos.UserDTO;

public interface UserService {
	UserDTO createUser(UserDTO userDTO);

	UserDTO getUserById(String discordId);

	UserDTO setRanking(String discordId, int ranking, String discordName);

	UserDTO setRoles(String discordId, String mainRole, String secondaryRole, String discordName, String summonerName);

	UserDTO updateUser(UserDTO userDTO);

	List<UserDTO> getLeaderboard();

}
