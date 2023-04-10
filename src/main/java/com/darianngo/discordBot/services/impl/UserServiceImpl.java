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
	public UserDTO setRanking(String discordId, int ranking, String discordName) {
		UserEntity userEntity = userRepository.findById(discordId).orElse(null);

		if (userEntity == null) {
			UserDTO newUserDTO = new UserDTO(discordId, discordName, null, ranking, null, null, null, null, null, null);
			UserEntity newUser = userRepository.save(userMapper.toEntity(newUserDTO));
			return userMapper.toDto(newUser);
		} else {
			userEntity.setRanking(ranking);
			UserEntity updatedUser = userRepository.save(userEntity);
			return userMapper.toDto(updatedUser);
		}
	}

	@Override
	public UserDTO setRoles(String discordId, String mainRole, String secondaryRole, String discordName,
			String summonerName) {
		UserEntity userEntity = userRepository.findById(discordId).orElse(null);

		if (userEntity == null) {
			UserDTO newUserDTO = new UserDTO(discordId, discordName, mainRole, null, secondaryRole, null, null, null, null, null);
			UserEntity newUser = userRepository.save(userMapper.toEntity(newUserDTO));
			return userMapper.toDto(newUser);
		} else {
			userEntity.setMainRole(mainRole);
			userEntity.setSecondaryRole(secondaryRole);
			UserEntity updatedUser = userRepository.save(userEntity);
			return userMapper.toDto(updatedUser);
		}
	}

	@Override
	public UserDTO updateUser(UserDTO userDTO) {
		UserEntity userEntity = userMapper.toEntity(userDTO);
		UserEntity updatedUser = userRepository.save(userEntity);
		return userMapper.toDto(updatedUser);
	}

}
