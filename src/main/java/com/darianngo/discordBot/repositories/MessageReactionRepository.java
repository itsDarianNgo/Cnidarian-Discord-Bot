package com.darianngo.discordBot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.darianngo.discordBot.entities.MessageReaction;

@Repository
public interface MessageReactionRepository extends JpaRepository<MessageReaction, String> {

}
