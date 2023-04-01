package com.darianngo.discordBot.mappers;

import org.mapstruct.Mapper;

import com.darianngo.discordBot.dtos.MessageReactionDTO;
import com.darianngo.discordBot.entities.MessageReaction;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface MessageReactionMapper {
    MessageReactionDTO toDto(MessageReaction messageReaction);
    MessageReaction toEntity(MessageReactionDTO messageReactionDTO);
}
