package com.darianngo.discordBot.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.darianngo.discordBot.dtos.MatchDTO;
import com.darianngo.discordBot.entities.MatchEntity;

@Mapper(componentModel = "spring", uses = TeamMapper.class)
public interface MatchMapper {
	@Mapping(source = "id", target = "id")
	@Mapping(source = "winningTeam", target = "winningTeam")
	@Mapping(source = "finalScore", target = "finalScore")
	@Mapping(source = "teams", target = "teams")
	MatchEntity toEntity(MatchDTO matchDTO);

	@Mapping(source = "id", target = "id")
	@Mapping(source = "winningTeam", target = "winningTeam")
	@Mapping(source = "finalScore", target = "finalScore")
	@Mapping(source = "teams", target = "teams")
	MatchDTO toDto(MatchEntity matchEntity);
}
