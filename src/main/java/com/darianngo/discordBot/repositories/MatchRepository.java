package com.darianngo.discordBot.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.darianngo.discordBot.entities.MatchEntity;

@Repository
public interface MatchRepository extends JpaRepository<MatchEntity, Long> {
	@Query("SELECT m FROM MatchEntity m JOIN FETCH m.teams WHERE m.id = :matchId")
	Optional<MatchEntity> findByIdWithTeams(@Param("matchId") Long matchId);
}
