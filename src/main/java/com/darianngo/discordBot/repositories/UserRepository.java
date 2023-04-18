package com.darianngo.discordBot.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.darianngo.discordBot.entities.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {
	@EntityGraph(attributePaths = "userTeams")
	Optional<UserEntity> findById(String id);

	@Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.userTeams WHERE u.id = :id")
	Optional<UserEntity> findByIdWithUserTeams(@Param("id") String id);
}
