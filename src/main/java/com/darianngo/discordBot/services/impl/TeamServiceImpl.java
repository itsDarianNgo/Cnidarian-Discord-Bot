package com.darianngo.discordBot.services.impl;

import org.springframework.stereotype.Service;

import com.darianngo.discordBot.entities.TeamEntity;
import com.darianngo.discordBot.repositories.TeamRepository;
import com.darianngo.discordBot.services.TeamService;

@Service
public class TeamServiceImpl implements TeamService {
	private final TeamRepository teamRepository;

	public TeamServiceImpl(TeamRepository teamRepository) {
		this.teamRepository = teamRepository;
	}

	@Override
	public TeamEntity createTeam(TeamEntity team) {
		return teamRepository.save(team);
	}
}
