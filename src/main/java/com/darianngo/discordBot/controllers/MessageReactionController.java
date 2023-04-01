package com.darianngo.discordBot.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.darianngo.discordBot.dtos.MessageReactionDTO;
import com.darianngo.discordBot.services.MessageReactionService;

@RestController
@RequestMapping("/reactions")
public class MessageReactionController {
    private final MessageReactionService messageReactionService;

    public MessageReactionController(MessageReactionService messageReactionService) {
        this.messageReactionService = messageReactionService;
    }

    @PostMapping
    public ResponseEntity<MessageReactionDTO> createReaction(@RequestBody MessageReactionDTO messageReactionDTO) {
        MessageReactionDTO createdReaction = messageReactionService.createReaction(messageReactionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReaction);
    }
}