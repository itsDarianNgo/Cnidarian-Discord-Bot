package com.darianngo.discordBot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.darianngo.discordBot.entities.TeamEntity;

@Repository
public interface TeamRepository extends JpaRepository<TeamEntity, Long> {
}
