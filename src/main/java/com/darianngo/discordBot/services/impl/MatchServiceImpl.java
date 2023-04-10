package com.darianngo.discordBot.services.impl;

import org.springframework.stereotype.Service;

import com.darianngo.discordBot.entities.MatchEntity;
import com.darianngo.discordBot.repositories.MatchRepository;
import com.darianngo.discordBot.services.MatchService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MatchServiceImpl implements MatchService {
	private final MatchRepository matchRepository;

	@Override
	public MatchEntity createMatch() {
		MatchEntity matchEntity = new MatchEntity();
		return matchRepository.save(matchEntity);
	}
}