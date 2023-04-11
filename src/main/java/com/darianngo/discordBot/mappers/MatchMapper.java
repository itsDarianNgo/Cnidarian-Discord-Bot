package com.darianngo.discordBot.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.darianngo.discordBot.dtos.MatchDTO;
import com.darianngo.discordBot.entities.MatchEntity;

@Mapper(componentModel = "spring", uses = TeamMapper.class)
public interface MatchMapper {
	@Mapping(source = "winningTeam", target = "winningTeam")
	@Mapping(source = "finalScore", target = "finalScore")
	MatchEntity toEntity(MatchDTO matchDTO);

	@Mapping(source = "winningTeam", target = "winningTeam")
	@Mapping(source = "finalScore", target = "finalScore")
	MatchDTO toDto(MatchEntity matchEntity);

}
