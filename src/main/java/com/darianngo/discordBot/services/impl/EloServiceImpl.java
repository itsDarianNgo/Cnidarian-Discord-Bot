package com.darianngo.discordBot.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.darianngo.discordBot.dtos.MatchResultDTO;
import com.darianngo.discordBot.dtos.UserDTO;
import com.darianngo.discordBot.dtos.UserTeamDTO;
import com.darianngo.discordBot.entities.UserEntity;
import com.darianngo.discordBot.mappers.UserMapper;
import com.darianngo.discordBot.repositories.UserRepository;
import com.darianngo.discordBot.services.EloService;

@Service
public class EloServiceImpl implements EloService {
	private static final double K_FACTOR = 32;

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private UserMapper userMapper;

	@Override
	public void updateElo(MatchResultDTO matchResult, List<UserDTO> usersInMatch) {
		// Set initial Elo to 1200 if null or empty
		usersInMatch.forEach(user -> {
			if (user.getElo() == null || user.getElo() == 0) {
				user.setElo(1200);
			}
		});

		System.out.println("usersInMatch: " + usersInMatch);
		Map<Long, List<UserDTO>> teams = groupUsersByTeam(usersInMatch);
		System.out.println("teams: " + teams);

		List<UserDTO> winningTeam = teams.get(matchResult.getWinningTeamId());
		List<UserDTO> losingTeam = teams.get(matchResult.getLosingTeamId());

		double avgEloWinningTeam = calculateAverageElo(winningTeam);
		double avgEloLosingTeam = calculateAverageElo(losingTeam);

		double matchWeight = calculateMatchWeight(matchResult.getWinningScore(), matchResult.getLosingScore());

		for (UserDTO winner : winningTeam) {
			for (UserDTO loser : losingTeam) {
				double eloDifference = winner.getElo() - loser.getElo();
				NormalDistribution normalDistribution = new NormalDistribution(eloDifference, 200);
				double expectedOutcome = normalDistribution.cumulativeProbability(0);
				double actualOutcome = 1;

				double eloChange = K_FACTOR * matchWeight * (actualOutcome - expectedOutcome);
				updateEloForUser(winner, eloChange);
			}
		}

		for (UserDTO loser : losingTeam) {
			for (UserDTO winner : winningTeam) {
				double eloDifference = loser.getElo() - winner.getElo();
				NormalDistribution normalDistribution = new NormalDistribution(eloDifference, 200);
				double expectedOutcome = normalDistribution.cumulativeProbability(0);
				double actualOutcome = 0;

				double eloChange = K_FACTOR * matchWeight * (actualOutcome - expectedOutcome);
				updateEloForUser(loser, eloChange);
			}
		}
	}

	private Map<Long, List<UserDTO>> groupUsersByTeam(List<UserDTO> usersInMatch) {
		Map<Long, List<UserDTO>> teams = new HashMap<>();

		for (UserDTO user : usersInMatch) {
			System.out.println("groupUserByTeam: " + user);
			for (UserTeamDTO userTeam : user.getUserTeams()) {
				Long teamId = userTeam.getTeamId();
				if (!teams.containsKey(teamId)) {
					teams.put(teamId, new ArrayList<>());
				}
				teams.get(teamId).add(user);
			}
		}

		return teams;
	}

	private double calculateAverageElo(List<UserDTO> team) {
		double sumElo = team.stream().mapToInt(UserDTO::getElo).sum();
		return sumElo / team.size();
	}

	private double calculateMatchWeight(int winningScore, int losingScore) {
		double scoreDifference = Math.abs(winningScore - losingScore);
		double totalScore = winningScore + losingScore;
		return (scoreDifference + 1) / (totalScore + 2);
	}

	private void updateEloForUser(UserDTO user, double eloChange) {
		int roundedEloChange = (int) Math.ceil(eloChange);
		user.setElo(user.getElo() + roundedEloChange);
		UserEntity userEntity = userMapper.toEntity(user);
		userRepository.save(userEntity);
	}
}
