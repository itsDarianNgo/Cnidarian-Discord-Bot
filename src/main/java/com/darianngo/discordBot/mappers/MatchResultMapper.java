package com.darianngo.discordBot.mappers;

import org.mapstruct.Mapper;

import com.darianngo.discordBot.dtos.MatchResultDTO;
import com.darianngo.discordBot.entities.MatchResultEntity;

@Mapper(componentModel = "spring")
public interface MatchResultMapper {
	MatchResultDTO toDTO(MatchResultEntity matchResultEntity);

	MatchResultEntity toEntity(MatchResultDTO matchResultDTO);
}
