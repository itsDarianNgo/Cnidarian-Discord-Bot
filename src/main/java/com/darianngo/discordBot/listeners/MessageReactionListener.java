package com.darianngo.discordBot.listeners;

import javax.annotation.Nonnull;

import org.springframework.stereotype.Component;

import com.darianngo.discordBot.dtos.MessageReactionDTO;
import com.darianngo.discordBot.dtos.UserDTO;
import com.darianngo.discordBot.mappers.UserMapper;
import com.darianngo.discordBot.services.MessageReactionService;
import com.darianngo.discordBot.services.UserService;

import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;


@Component
public class MessageReactionListener extends ListenerAdapter {
    private final UserService userService;
    private final MessageReactionService messageReactionService;
    private final UserMapper userMapper;

    public MessageReactionListener(UserService userService, MessageReactionService messageReactionService, UserMapper userMapper) {
        this.userService = userService;
        this.messageReactionService = messageReactionService;
        this.userMapper = userMapper;
    }

    @Override
    public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
        if (event.getUser().isBot()) {
            return;
        }

        UserDTO userDTO = userMapper.jdaUserToDto(event.getUser());
        userService.createUser(userDTO);

        MessageReactionDTO messageReactionDTO = new MessageReactionDTO();
        messageReactionDTO.setMessageId(event.getMessageId());
        messageReactionDTO.setUser(userDTO);

        messageReactionService.createReaction(messageReactionDTO);
    }
}
