package com.darianngo.discordBot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.darianngo.discordBot.entities.MessageReaction;

public interface MessageReactionRepository extends JpaRepository<MessageReaction, String> {

}
