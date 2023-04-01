package com.darianngo.discordBot.dtos;

import lombok.Data;

@Data
public class MessageReactionDTO {
    private String messageId;
    private UserDTO user;
}