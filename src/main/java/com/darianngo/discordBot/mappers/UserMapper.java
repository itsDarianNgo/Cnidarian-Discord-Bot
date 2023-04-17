package com.darianngo.discordBot.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.darianngo.discordBot.dtos.UserDTO;
import com.darianngo.discordBot.entities.UserEntity;

import net.dv8tion.jda.api.entities.User;

@Mapper(componentModel = "spring", uses = { UserTeamMapper.class })
public interface UserMapper {
	UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

	@Mapping(source = "userTeams", target = "userTeams")
	UserDTO toDto(UserEntity entity);

	@Mapping(source = "userTeams", target = "userTeams")
	UserEntity toEntity(UserDTO dto);

	@Mapping(source = "id", target = "discordId")
	@Mapping(source = "name", target = "discordName")
	UserDTO jdaUserToDto(User jdaUser);
}
