package com.darianngo.discordBot.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.darianngo.discordBot.entities.MatchResultEntity;
import com.darianngo.discordBot.entities.UserEntity;

@Repository
public interface MatchResultRepository extends JpaRepository<MatchResultEntity, Long> {

    List<MatchResultEntity> findAllByMatchId(Long matchId);

    MatchResultEntity findByMatchIdAndUser(Long matchId, UserEntity user);
}
