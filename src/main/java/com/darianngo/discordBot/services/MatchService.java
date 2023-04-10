package com.darianngo.discordBot.services;

import com.darianngo.discordBot.dtos.MatchDTO;

public interface MatchService {
    MatchDTO createMatch(MatchDTO matchDTO);
}
