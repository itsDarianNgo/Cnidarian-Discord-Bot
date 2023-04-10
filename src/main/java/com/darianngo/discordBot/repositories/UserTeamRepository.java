package com.darianngo.discordBot.repositories;

import com.darianngo.discordBot.entities.UserTeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserTeamRepository extends JpaRepository<UserTeamEntity, Long> {
}
