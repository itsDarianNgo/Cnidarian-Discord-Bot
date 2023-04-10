package com.darianngo.discordBot.services.impl;

import org.springframework.stereotype.Service;

import com.darianngo.discordBot.dtos.MatchDTO;
import com.darianngo.discordBot.entities.MatchEntity;
import com.darianngo.discordBot.mappers.MatchMapper;
import com.darianngo.discordBot.repositories.MatchRepository;
import com.darianngo.discordBot.services.MatchService;

@Service
public class MatchServiceImpl implements MatchService {
    private final MatchRepository matchRepository;
    private final MatchMapper matchMapper;

    public MatchServiceImpl(MatchRepository matchRepository, MatchMapper matchMapper) {
        this.matchRepository = matchRepository;
        this.matchMapper = matchMapper;
    }

    @Override
    public MatchDTO createMatch(MatchDTO matchDTO) {
        MatchEntity match = matchMapper.toEntity(matchDTO);
        MatchEntity savedMatch = matchRepository.save(match);
        return matchMapper.toDto(savedMatch);
    }
}
