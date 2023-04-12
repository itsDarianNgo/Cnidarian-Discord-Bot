package com.darianngo.discordBot.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.darianngo.discordBot.dtos.TeamDTO;
import com.darianngo.discordBot.entities.TeamEntity;

@Mapper(componentModel = "spring")
public interface TeamMapper {
	@Mapping(source = "match.id", target = "matchId")
	TeamDTO toDto(TeamEntity teamEntity);

	@Mapping(target = "match.id", source = "matchId")
	TeamEntity toEntity(TeamDTO teamDTO);
}
