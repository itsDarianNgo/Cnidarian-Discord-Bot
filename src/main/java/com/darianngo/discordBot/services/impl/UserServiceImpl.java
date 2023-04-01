package com.darianngo.discordBot.services.impl;

import org.springframework.stereotype.Service;

import com.darianngo.discordBot.dtos.UserDTO;
import com.darianngo.discordBot.entities.UserEntity;
import com.darianngo.discordBot.mappers.UserMapper;
import com.darianngo.discordBot.repositories.UserRepository;
import com.darianngo.discordBot.services.UserService;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public UserDTO createUser(UserDTO userDTO) {
        UserEntity user = userMapper.toEntity(userDTO);
        UserEntity savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }
}
