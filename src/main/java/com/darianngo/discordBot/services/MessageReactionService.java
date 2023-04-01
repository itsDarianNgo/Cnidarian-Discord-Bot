package com.darianngo.discordBot.services;

import com.darianngo.discordBot.dtos.MessageReactionDTO;

public interface MessageReactionService {
    MessageReactionDTO createReaction(MessageReactionDTO messageReactionDTO);
}
