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

    @Override
    public UserDTO getUserById(String id) {
        UserEntity userEntity = userRepository.findById(id).orElse(null);
        return userEntity != null ? userMapper.toDto(userEntity) : null;
    }

    @Override
    public UserDTO setRanking(String id, int ranking, String name) {
        UserEntity userEntity = userRepository.findById(id).orElse(null);

        if (userEntity == null) {
            UserDTO newUserDTO = new UserDTO(id, name, ranking, null, null, null);
            UserEntity newUser = userRepository.save(userMapper.toEntity(newUserDTO));
            return userMapper.toDto(newUser);
        } else {
            userEntity.setRanking(ranking);
            UserEntity updatedUser = userRepository.save(userEntity);
            return userMapper.toDto(updatedUser);
        }
    }
    @Override
    public UserDTO setRoles(String id, String primaryRole, String secondaryRole, String tertiaryRole, String name) {
        UserEntity userEntity = userRepository.findById(id).orElse(null);

        if (userEntity == null) {
            UserDTO newUserDTO = new UserDTO(id, name, null, primaryRole, secondaryRole, tertiaryRole);
            UserEntity newUser = userRepository.save(userMapper.toEntity(newUserDTO));
            return userMapper.toDto(newUser);
        } else {
            userEntity.setPrimaryRole(primaryRole);
            userEntity.setSecondaryRole(secondaryRole);
            userEntity.setTertiaryRole(tertiaryRole);
            UserEntity updatedUser = userRepository.save(userEntity);
            return userMapper.toDto(updatedUser);
        }
    }
}
