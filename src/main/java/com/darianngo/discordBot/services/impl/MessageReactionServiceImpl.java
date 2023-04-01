package com.darianngo.discordBot.services.impl;

import org.springframework.stereotype.Service;

import com.darianngo.discordBot.dtos.MessageReactionDTO;
import com.darianngo.discordBot.entities.MessageReaction;
import com.darianngo.discordBot.mappers.MessageReactionMapper;
import com.darianngo.discordBot.repositories.MessageReactionRepository;
import com.darianngo.discordBot.services.MessageReactionService;

@Service
public class MessageReactionServiceImpl implements MessageReactionService {
    private final MessageReactionRepository messageReactionRepository;
    private final MessageReactionMapper messageReactionMapper;

    public MessageReactionServiceImpl(MessageReactionRepository messageReactionRepository, MessageReactionMapper messageReactionMapper) {
        this.messageReactionRepository = messageReactionRepository;
        this.messageReactionMapper = messageReactionMapper;
    }

    @Override
    public MessageReactionDTO createReaction(MessageReactionDTO messageReactionDTO) {
        MessageReaction messageReaction = messageReactionMapper.toEntity(messageReactionDTO);
        MessageReaction savedReaction = messageReactionRepository.save(messageReaction);
        return messageReactionMapper.toDto(savedReaction);
    }
}
