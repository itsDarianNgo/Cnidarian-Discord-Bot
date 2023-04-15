package com.darianngo.discordBot.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.darianngo.discordBot.entities.UserTeamEntity;

@Repository
public interface UserTeamRepository extends JpaRepository<UserTeamEntity, Long> {

	List<UserTeamEntity> findByTeamMatchId(Long matchId);

}
