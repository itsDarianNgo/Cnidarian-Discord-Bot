package com.darianngo.discordBot.services;

import com.darianngo.discordBot.dtos.UserDTO;

public interface UserService {
    UserDTO createUser(UserDTO userDTO);
}
