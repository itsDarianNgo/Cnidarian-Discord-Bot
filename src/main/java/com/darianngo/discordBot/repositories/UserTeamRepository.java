package com.darianngo.discordBot.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.darianngo.discordBot.entities.TeamEntity;
import com.darianngo.discordBot.entities.UserTeamEntity;

@Repository
public interface UserTeamRepository extends JpaRepository<UserTeamEntity, Long> {
	List<UserTeamEntity> findByTeam(TeamEntity team);

	@Query("SELECT ut FROM UserTeamEntity ut JOIN FETCH ut.user u WHERE ut.team.match.id = :matchId")
	List<UserTeamEntity> findByTeamMatchIdWithUser(@Param("matchId") Long matchId);
}
