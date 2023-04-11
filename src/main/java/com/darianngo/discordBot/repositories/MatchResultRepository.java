package com.darianngo.discordBot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.darianngo.discordBot.entities.MatchResultEntity;

@Repository
public interface MatchResultRepository extends JpaRepository<MatchResultEntity, Long> {
}
