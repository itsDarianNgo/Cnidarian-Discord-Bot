package com.darianngo.discordBot.services.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.darianngo.discordBot.dtos.UserDTO;
import com.darianngo.discordBot.embeds.MissingEloEmbed;
import com.darianngo.discordBot.embeds.TeamBalancerEmbed;
import com.darianngo.discordBot.services.MatchService;
import com.darianngo.discordBot.services.TeamBalancerService;
import com.darianngo.discordBot.services.UserService;

import net.dv8tion.jda.api.entities.MessageEmbed;

@Service
public class TeamBalancerServiceImpl implements TeamBalancerService {

	private final UserService userService;
	private MatchService matchService;

	// Parameters for simulated annealing
	private static final int MAX_ITERATIONS = 1000;
	private static final double INITIAL_TEMPERATURE = 100.0;
	private static final double COOLING_RATE = 0.995;

	@Autowired
	public TeamBalancerServiceImpl(UserService userService) {
		this.userService = userService;
	}

	@Autowired
	public void setMatchService(@Lazy MatchService matchService) {
		this.matchService = matchService;
	}

	@Override
	public Pair<MessageEmbed, Boolean> balanceTeams(List<String> reactions, List<UserDTO> usersReacted, Long matchId) {
		List<UserDTO> usersWithMissingElo = usersReacted.stream().filter(user -> user.getElo() == null)
				.collect(Collectors.toList());

		if (!usersWithMissingElo.isEmpty()) {
			return Pair.of(MissingEloEmbed.createEmbed(usersWithMissingElo), true);
		}

		List<UserDTO> team1 = new ArrayList<>();
		List<UserDTO> team2 = new ArrayList<>();

		// Sort users based on their rank
		Collections.sort(usersReacted, Comparator.comparingDouble(UserDTO::getElo).reversed());

		// Distribute players based on their roles and ranking
		for (UserDTO user : usersReacted) {
			if (team1.size() == 5 && team2.size() == 5) {
				break;
			}

			String mainRole = user.getMainRole();
			String secondaryRole = user.getSecondaryRole();
			int team1Score = calculateTeamScore(team1);
			int team2Score = calculateTeamScore(team2);

			boolean addedToTeam = false;

			// Check mainRole
			if (isValidRoleForTeam(mainRole, team1) && isValidRoleForTeam(mainRole, team2)) {
				if (team1Score <= team2Score && team1.size() < 5) {
					team1.add(user);
					addedToTeam = true;
				} else if (team2.size() < 5) {
					team2.add(user);
					addedToTeam = true;
				}
			} else if (isValidRoleForTeam(mainRole, team1) && team1.size() < 5) {
				team1.add(user);
				addedToTeam = true;
			} else if (isValidRoleForTeam(mainRole, team2) && team2.size() < 5) {
				team2.add(user);
				addedToTeam = true;
			}

			// Check secondaryRole only if not added based on mainRole
			if (!addedToTeam) {
				if (isValidRoleForTeam(secondaryRole, team1) && isValidRoleForTeam(secondaryRole, team2)) {
					if (team1Score <= team2Score && team1.size() < 5) {
						team1.add(user);
						addedToTeam = true;
					} else if (team2.size() < 5) {
						team2.add(user);
						addedToTeam = true;
					}
				} else if (isValidRoleForTeam(secondaryRole, team1) && team1.size() < 5) {
					team1.add(user);
					addedToTeam = true;
				} else if (isValidRoleForTeam(secondaryRole, team2) && team2.size() < 5) {
					team2.add(user);
					addedToTeam = true;
				}
			}

			if (!addedToTeam) {
				// If user can't fit in their preferred roles, add to the team with the lowest
				// score
				if (team1Score <= team2Score && team1.size() < 5) {
					team1.add(user);
				} else if (team2.size() < 5) {
					team2.add(user);
				}
			}
		}

		// Replace the for loop with the following method call
		List<UserDTO> initialTeams = new ArrayList<>(usersReacted);
		Pair<List<UserDTO>, List<UserDTO>> optimizedTeams = optimizeWithSimulatedAnnealing(initialTeams);
		team1 = optimizedTeams.getLeft();
		team2 = optimizedTeams.getRight();

		// Build Embed
		int eloDifference = Math.abs(calculateTeamScore(team1) - calculateTeamScore(team2));
		// Save teams to the database
		matchService.saveTeamsWithMatchId(team1, team2, matchId);
		return Pair.of(TeamBalancerEmbed.createEmbed(team1, team2, eloDifference, matchId), false);
	}

	private Pair<List<UserDTO>, List<UserDTO>> optimizeWithSimulatedAnnealing(List<UserDTO> initialTeams) {
		Random random = new Random();
		double temperature = INITIAL_TEMPERATURE;

		List<UserDTO> currentTeams = new ArrayList<>(initialTeams);
		List<UserDTO> bestTeams = new ArrayList<>(initialTeams);

		for (int i = 0; i < MAX_ITERATIONS; i++) {
			List<UserDTO> newTeams = new ArrayList<>(currentTeams);
			// Swap two random players between teams
			int index1 = random.nextInt(5);
			int index2 = 5 + random.nextInt(5);
			Collections.swap(newTeams, index1, index2);

			// Calculate ELO difference
			int currentEloDifference = calculateEloDifference(currentTeams);
			int newEloDifference = calculateEloDifference(newTeams);

			// Calculate the acceptance probability and accept the new solution if it's
			// better or with a probability based on temperature
			double acceptanceProbability = Math.exp((currentEloDifference - newEloDifference) / temperature);
			if (acceptanceProbability > random.nextDouble()) {
				currentTeams = newTeams;
			}

			// Update best solution found so far
			if (calculateEloDifference(currentTeams) < calculateEloDifference(bestTeams)) {
				bestTeams = currentTeams;
			}

			// Cool down the temperature
			temperature *= COOLING_RATE;
		}

		// Split bestTeams into team1 and team2
		List<UserDTO> team1 = bestTeams.subList(0, 5);
		List<UserDTO> team2 = bestTeams.subList(5, 10);

		return Pair.of(team1, team2);
	}

	// Helper methods

	private boolean isValidRoleForTeam(String role, List<UserDTO> team) {
		return team.stream().noneMatch(u -> u.getMainRole().equals(role) || u.getSecondaryRole().equals(role));
	}

	private int calculateTeamScore(List<UserDTO> team) {
		return (int) team.stream().mapToDouble(UserDTO::getElo).sum();
	}

	private int calculateEloDifference(List<UserDTO> teams) {
		int team1Score = calculateTeamScore(teams.subList(0, 5));
		int team2Score = calculateTeamScore(teams.subList(5, 10));
		int eloDifference = Math.abs(team1Score - team2Score);

		int rolePenalty = calculateRolePenalty(teams);
		return eloDifference + rolePenalty;
	}

	private int calculateRolePenalty(List<UserDTO> teams) {
		int rolePenalty = 0;
		int penaltyPerRoleMismatch = 10000; // Increase this value to give more priority to role distribution

		List<UserDTO> team1 = teams.subList(0, 5);
		List<UserDTO> team2 = teams.subList(5, 10);

		for (String role : new String[] { "TOP", "JUNGLE", "MID", "ADC", "SUPPORT" }) {
			int team1RoleCount = countRoleInTeam(role, team1);
			int team2RoleCount = countRoleInTeam(role, team2);

			rolePenalty += penaltyPerRoleMismatch * (Math.abs(team1RoleCount - 1) + Math.abs(team2RoleCount - 1));
		}

		return rolePenalty;
	}

	private int countRoleInTeam(String role, List<UserDTO> team) {
		return (int) team.stream()
				.filter(user -> user.getMainRole().equals(role) || user.getSecondaryRole().equals(role)).count();
	}

}
