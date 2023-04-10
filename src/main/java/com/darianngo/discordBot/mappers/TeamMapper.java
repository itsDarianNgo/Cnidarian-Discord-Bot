package com.darianngo.discordBot.mappers;

import org.mapstruct.Mapper;

import com.darianngo.discordBot.dtos.TeamDTO;
import com.darianngo.discordBot.entities.TeamEntity;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface TeamMapper {
	TeamDTO toDto(TeamEntity teamEntity);

	TeamEntity toEntity(TeamDTO teamDTO);
}
