package com.darianngo.discordBot.services.impl;

import com.darianngo.discordBot.dtos.MatchResultDTO;
import com.darianngo.discordBot.entities.MatchResultEntity;
import com.darianngo.discordBot.mappers.MatchResultMapper;
import com.darianngo.discordBot.repositories.MatchResultRepository;
import com.darianngo.discordBot.services.MatchResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MatchResultServiceImpl implements MatchResultService {

	@Autowired
	private MatchResultRepository matchResultRepository;

	@Autowired
	private MatchResultMapper matchResultMapper;

	@Override
	public MatchResultDTO saveMatchResult(MatchResultDTO matchResultDTO) {
		MatchResultEntity matchResultEntity = matchResultMapper.toEntity(matchResultDTO);
		matchResultEntity = matchResultRepository.save(matchResultEntity);
		return matchResultMapper.toDTO(matchResultEntity);
	}
}
