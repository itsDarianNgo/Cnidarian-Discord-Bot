package com.darianngo.discordBot.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.darianngo.discordBot.config.DiscordChannelConfig;
import com.darianngo.discordBot.dtos.MatchDTO;
import com.darianngo.discordBot.dtos.MatchResultDTO;
import com.darianngo.discordBot.dtos.UserDTO;
import com.darianngo.discordBot.embeds.FinalResultEmbed;
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
import com.darianngo.discordBot.services.EloService;
import com.darianngo.discordBot.services.MatchService;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageChannel;

@Service
public class MatchServiceImpl implements MatchService {
	private final MatchRepository matchRepository;
	private final MatchMapper matchMapper;
	private final MatchResultMapper matchResultMapper;
	private final UserRepository userRepository;
	private final MatchResultRepository matchResultRepository;
	private final UserMapper userMapper;
	private JDA jda;

	@Autowired
	public void setJDA(@Lazy JDA jda) {
		this.jda = jda;
	}

	@Autowired
	private UserTeamRepository userTeamRepository;
	@Autowired
	private TeamRepository teamRepository;
	@Autowired
	private TeamMapper teamMapper;
	@Autowired
	private EntityManager entityManager;
	@Autowired
	private DiscordChannelConfig discordChannelConfig;
	@Autowired
	private EloService eloService;

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
	public void cancelMatch(Long matchId) {
		MatchEntity matchEntity = matchRepository.findById(matchId)
				.orElseThrow(() -> new IllegalArgumentException("Match not found with id: " + matchId));

		matchRepository.delete(matchEntity); // Delete the match from the database
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

	@Transactional
	@Override
	public Map<Long, List<UserDTO>> getTeamsWithMatchId(Long matchId) {
		// Find the match by its ID
		MatchEntity matchEntity = matchRepository.findById(matchId)
				.orElseThrow(() -> new IllegalArgumentException("Match not found with id: " + matchId));

		// Find the teams associated with the match
		List<TeamEntity> teams = teamRepository.findByMatch(matchEntity);

		// Prepare a map to store the result
		Map<Long, List<UserDTO>> resultMap = new HashMap<>();

		// Iterate through the teams and find their associated users
		for (TeamEntity team : teams) {
			List<UserTeamEntity> userTeams = userTeamRepository.findByTeam(team);
			List<UserDTO> usersInTeam = userTeams.stream().map(userTeam -> userMapper.toDto(userTeam.getUser()))
					.collect(Collectors.toList());

			resultMap.put(team.getId(), usersInTeam);
		}

		return resultMap;
	}

	@Override
	public void updateMatch(MatchDTO matchDTO) {
		MatchEntity match = matchMapper.toEntity(matchDTO);
		matchRepository.save(match);
	}

	@Transactional
	@Override
	public MatchDTO getMatchById(Long matchId) {
		MatchEntity matchEntity = matchRepository.findById(matchId)
				.orElseThrow(() -> new RuntimeException("Match not found with ID: " + matchId));
		return matchMapper.toDto(matchEntity);
	}

	@Transactional
	@Override
	public MatchEntity getMatchEntityById(Long matchId) {
		return matchRepository.findById(matchId)
				.orElseThrow(() -> new EntityNotFoundException("Match not found with ID: " + matchId));
	}

	@Override
	public List<UserDTO> getUsersInMatch(Long matchId) {
		List<UserTeamEntity> userTeamEntities = userTeamRepository.findByTeamMatchIdWithUser(matchId);
		List<UserDTO> users = userTeamEntities.stream().map(ute -> userMapper.toDto(ute.getUser()))
				.collect(Collectors.toList());
		return users;
	}

	@Transactional
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

	@Override
	public Map<Long, List<UserDTO>> getTeamMembers(List<TeamEntity> teams) {
		Map<Long, List<UserDTO>> teamMembers = new HashMap<>();

		for (TeamEntity team : teams) {
			Long teamId = team.getId();
			List<UserEntity> userEntities = team.getUserTeams().stream().map(UserTeamEntity::getUser)
					.collect(Collectors.toList());

			List<UserDTO> userDTOs = userEntities.stream().map(userEntity -> {
				UserDTO userDTO = new UserDTO();
				userDTO.setDiscordId(userEntity.getDiscordId());
				userDTO.setDiscordName(userEntity.getDiscordName());
				return userDTO;
			}).collect(Collectors.toList());

			teamMembers.put(teamId, userDTOs);
		}

		return teamMembers;
	}

	@Override
	public void sendMatchResultToDesignatedChannel(MatchResultDTO matchResult, MatchDTO match) {
		MessageChannel designatedChannel = jda.getTextChannelById(discordChannelConfig.getMatchChannelId());
		if (designatedChannel == null) {
			System.out.println("Error: Designated channel not found.");
			return;
		}

		designatedChannel.sendMessageEmbeds(FinalResultEmbed.createEmbed(matchResult, match)).queue();
	}

}
