package com.darianngo.discordBot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.darianngo.discordBot.entities.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, String> {
}
