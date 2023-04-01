package com.darianngo.discordBot.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.darianngo.discordBot.dtos.UserDTO;
import com.darianngo.discordBot.entities.UserEntity;

import net.dv8tion.jda.api.entities.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);
    
	UserDTO toDto(UserEntity userEntity);

    UserEntity toEntity(UserDTO userDTO);

    UserDTO jdaUserToDto(User jdaUser);

}
