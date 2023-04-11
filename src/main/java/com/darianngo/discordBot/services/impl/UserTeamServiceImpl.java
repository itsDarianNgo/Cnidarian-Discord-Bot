package com.darianngo.discordBot.services.impl;

import org.springframework.stereotype.Service;

import com.darianngo.discordBot.entities.UserTeamEntity;
import com.darianngo.discordBot.repositories.UserTeamRepository;
import com.darianngo.discordBot.services.UserTeamService;

@Service
public class UserTeamServiceImpl implements UserTeamService {
	private final UserTeamRepository userTeamRepository;

	public UserTeamServiceImpl(UserTeamRepository userTeamRepository) {
		this.userTeamRepository = userTeamRepository;
	}

	@Override
	public UserTeamEntity createUserTeam(UserTeamEntity userTeam) {
		return userTeamRepository.save(userTeam);
	}
}
