package com.darianngo.discordBot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.darianngo.discordBot.entities.MatchEntity;

public interface MatchRepository extends JpaRepository<MatchEntity, Long> {
}
