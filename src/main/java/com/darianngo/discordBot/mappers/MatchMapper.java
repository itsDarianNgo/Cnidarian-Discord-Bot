package com.darianngo.discordBot.mappers;

import org.mapstruct.Mapper;

import com.darianngo.discordBot.dtos.MatchDTO;
import com.darianngo.discordBot.entities.MatchEntity;

@Mapper(componentModel = "spring", uses = TeamMapper.class)
public interface MatchMapper {
	MatchDTO toDto(MatchEntity matchEntity);

	MatchEntity toEntity(MatchDTO matchDTO);
}
