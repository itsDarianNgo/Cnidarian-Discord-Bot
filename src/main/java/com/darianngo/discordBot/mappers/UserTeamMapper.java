package com.darianngo.discordBot.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.darianngo.discordBot.dtos.UserTeamDTO;
import com.darianngo.discordBot.entities.UserTeamEntity;

@Mapper(componentModel = "spring")
public interface UserTeamMapper {
	@Mapping(source = "user.discordId", target = "userId")
	@Mapping(source = "team.id", target = "teamId")
	UserTeamDTO toDto(UserTeamEntity entity);

	@Mapping(source = "userId", target = "user.discordId")
	@Mapping(source = "teamId", target = "team.id")
	UserTeamEntity toEntity(UserTeamDTO dto);
}
