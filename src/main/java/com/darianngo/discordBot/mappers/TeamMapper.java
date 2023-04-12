package com.darianngo.discordBot.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.darianngo.discordBot.dtos.TeamDTO;
import com.darianngo.discordBot.entities.TeamEntity;

@Mapper(componentModel = "spring")
public interface TeamMapper {
	@Mapping(source = "match.id", target = "matchId")
    @Mapping(source = "userTeams", target = "members")
	TeamDTO toDto(TeamEntity teamEntity);

	@Mapping(target = "match.id", source = "matchId")
    @Mapping(source = "members", target = "userTeams")
	TeamEntity toEntity(TeamDTO teamDTO);
}
