package com.darianngo.discordBot.services;

import com.darianngo.discordBot.dtos.UserDTO;

public interface UserService {
    UserDTO createUser(UserDTO userDTO);
    UserDTO getUserById(String id);
    UserDTO setRanking(String id, int ranking, String name);
}
