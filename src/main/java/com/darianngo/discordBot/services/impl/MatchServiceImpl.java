package com.darianngo.discordBot.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.darianngo.discordBot.dtos.MatchDTO;
import com.darianngo.discordBot.dtos.MatchResultDTO;
import com.darianngo.discordBot.dtos.UserDTO;
import com.darianngo.discordBot.entities.MatchEntity;
import com.darianngo.discordBot.entities.MatchResultEntity;
import com.darianngo.discordBot.entities.TeamEntity;
import com.darianngo.discordBot.entities.UserEntity;
import com.darianngo.discordBot.entities.UserTeamEntity;
import com.darianngo.discordBot.exceptions.MatchNotFoundException;
import com.darianngo.discordBot.mappers.MatchMapper;
import com.darianngo.discordBot.mappers.MatchResultMapper;
import com.darianngo.discordBot.mappers.TeamMapper;
import com.darianngo.discordBot.mappers.UserMapper;
import com.darianngo.discordBot.repositories.MatchRepository;
import com.darianngo.discordBot.repositories.MatchResultRepository;
import com.darianngo.discordBot.repositories.TeamRepository;
import com.darianngo.discordBot.repositories.UserRepository;
import com.darianngo.discordBot.repositories.UserTeamRepository;
import com.darianngo.discordBot.services.MatchService;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class MatchServiceImpl implements MatchService {
	private final MatchRepository matchRepository;
	private final MatchMapper matchMapper;
	private final MatchResultMapper matchResultMapper;
	private final UserRepository userRepository;
	private final MatchResultRepository matchResultRepository;
	private final UserMapper userMapper;

	@Autowired
	private UserTeamRepository userTeamRepository;

	@Autowired
	private TeamRepository teamRepository;
	@Autowired
	private TeamMapper teamMapper;
	@Autowired
	private EntityManager entityManager;

	public MatchServiceImpl(MatchRepository matchRepository, MatchMapper matchMapper, UserRepository userRepository,
			MatchResultRepository matchResultRepository, MatchResultMapper matchResultMapper, UserMapper userMapper) {
		this.matchRepository = matchRepository;
		this.matchMapper = matchMapper;
		this.userRepository = userRepository;
		this.matchResultRepository = matchResultRepository;
		this.matchResultMapper = matchResultMapper;
		this.userMapper = userMapper;
	}

	@Override
	public MatchDTO createMatch(MatchDTO matchDTO) {
		MatchEntity match = matchMapper.toEntity(matchDTO);
		MatchEntity savedMatch = matchRepository.save(match);
		return matchMapper.toDto(savedMatch);
	}

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

	@Override
	public void updateMatch(MatchDTO matchDTO) {
		MatchEntity match = matchMapper.toEntity(matchDTO);
		matchRepository.save(match);
	}

	@Transactional
	@Override
	public MatchDTO getMatchById(Long matchId) {
		MatchEntity matchEntity;
		matchEntity = matchRepository.findById(matchId)
				.orElseThrow(() -> new EntityNotFoundException("Match not found with ID: " + matchId));
		return matchMapper.toDto(matchEntity);
	}

	public List<UserDTO> getUsersInMatch(Long matchId) {
		List<UserTeamEntity> userTeamEntities = userTeamRepository.findByTeamMatchId(matchId);
		List<UserDTO> users = userTeamEntities.stream().map(UserTeamEntity::getUser).map(userMapper::toDto)
				.collect(Collectors.toList());
		return users;
	}

	@Override
	public List<UserDTO> getUsersReactedForMatch(Long matchId) {
		EntityGraph<?> entityGraph = entityManager.createEntityGraph("MatchEntity.teamsAndUsers");
		Map<String, Object> properties = new HashMap<>();
		properties.put("javax.persistence.fetchgraph", entityGraph);
		MatchEntity matchEntity = entityManager.find(MatchEntity.class, matchId, properties);

		if (matchEntity == null) {
			throw new MatchNotFoundException("Match not found with id: " + matchId);
		}

		List<TeamEntity> teams = matchEntity.getTeams();
		List<UserDTO> usersReacted = new ArrayList<>();

		for (TeamEntity team : teams) {
			for (UserTeamEntity userTeamEntity : team.getUserTeams()) {
				UserEntity userEntity = userTeamEntity.getUser();
				usersReacted.add(userMapper.toDto(userEntity));
			}
		}

		return usersReacted;
	}

	@Override
	public void saveMatchResult(MatchResultDTO matchResultDTO) {
		MatchResultEntity matchResultEntity = matchResultMapper.toEntity(matchResultDTO);
		matchResultRepository.save(matchResultEntity);
	}
}
