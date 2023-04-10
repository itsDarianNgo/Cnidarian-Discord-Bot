package com.darianngo.discordBot.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.darianngo.discordBot.dtos.MatchDTO;
import com.darianngo.discordBot.dtos.UserDTO;
import com.darianngo.discordBot.entities.MatchEntity;
import com.darianngo.discordBot.entities.TeamEntity;
import com.darianngo.discordBot.entities.UserEntity;
import com.darianngo.discordBot.entities.UserTeamEntity;
import com.darianngo.discordBot.mappers.MatchMapper;
import com.darianngo.discordBot.repositories.MatchRepository;
import com.darianngo.discordBot.repositories.TeamRepository;
import com.darianngo.discordBot.repositories.UserRepository;
import com.darianngo.discordBot.repositories.UserTeamRepository;
import com.darianngo.discordBot.services.MatchService;

@Service
public class MatchServiceImpl implements MatchService {
	private final MatchRepository matchRepository;
	private final MatchMapper matchMapper;
	private final UserRepository userRepository;

	public MatchServiceImpl(MatchRepository matchRepository, MatchMapper matchMapper, UserRepository userRepository) {
		this.matchRepository = matchRepository;
		this.matchMapper = matchMapper;
		this.userRepository = userRepository;
	}

	@Override
	public MatchDTO createMatch(MatchDTO matchDTO) {
		MatchEntity match = matchMapper.toEntity(matchDTO);
		MatchEntity savedMatch = matchRepository.save(match);
		return matchMapper.toDto(savedMatch);
	}

	@Autowired
	private UserTeamRepository userTeamRepository;

	@Autowired
	private TeamRepository teamRepository;

	@Override
	public void saveTeamsWithMatchId(List<UserDTO> team1, List<UserDTO> team2, Long matchId) {
		MatchEntity matchEntity = matchRepository.findById(matchId)
				.orElseThrow(() -> new IllegalArgumentException("Match not found with id: " + matchId));

		TeamEntity team1Entity = new TeamEntity();
		team1Entity.setMatch(matchEntity);
		team1Entity = teamRepository.save(team1Entity);

		TeamEntity team2Entity = new TeamEntity();
		team2Entity.setMatch(matchEntity);
		team2Entity = teamRepository.save(team2Entity);

		for (UserDTO user : team1) {
			UserEntity userEntity = userRepository.findById(user.getDiscordId())
					.orElseThrow(() -> new IllegalArgumentException("User not found with id: " + user.getDiscordId()));
			UserTeamEntity userTeam = new UserTeamEntity();
			userTeam.setUser(userEntity);
			userTeam.setTeam(team1Entity);
			userTeamRepository.save(userTeam);
		}

		for (UserDTO user : team2) {
			UserEntity userEntity = userRepository.findById(user.getDiscordId())
					.orElseThrow(() -> new IllegalArgumentException("User not found with id: " + user.getDiscordId()));
			UserTeamEntity userTeam = new UserTeamEntity();
			userTeam.setUser(userEntity);
			userTeam.setTeam(team2Entity);
			userTeamRepository.save(userTeam);
		}

	}

}
