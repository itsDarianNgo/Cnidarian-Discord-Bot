package com.darianngo.discordBot.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.darianngo.discordBot.entities.MatchEntity;
import com.darianngo.discordBot.entities.TeamEntity;

@Repository
public interface TeamRepository extends JpaRepository<TeamEntity, Long> {
	List<TeamEntity> findByMatch(MatchEntity match);

	List<TeamEntity> findByMatchId(Long matchId);
}
