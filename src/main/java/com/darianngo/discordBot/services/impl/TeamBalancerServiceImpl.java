package com.darianngo.discordBot.services.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

		Map<String, List<UserDTO>> roleGroups = groupPlayersByRole(usersReacted);
		List<UserDTO> team1 = new ArrayList<>();
		List<UserDTO> team2 = new ArrayList<>();
		Set<String> assignedUsers = new HashSet<>();

		// If it's impossible to assign every player to their preferred role, try to
		// assign them to their secondary role
		for (String role : roleGroups.keySet()) {
			List<UserDTO> roleGroup = roleGroups.get(role);
			roleGroup.sort(Comparator.comparingDouble(UserDTO::getElo).reversed());

			for (UserDTO user : roleGroup) {
				if (assignedUsers.contains(user.getDiscordId())) {
					continue;
				}

				if (isValidRoleForTeam(role, team1) && team1.size() < 5) {
					team1.add(user);
					assignedUsers.add(user.getDiscordId());
				} else if (isValidRoleForTeam(role, team2) && team2.size() < 5) {
					team2.add(user);
					assignedUsers.add(user.getDiscordId());
				}
			}
		}

		// If there are still unassigned players, fill the remaining slots using Elo rating
		for (UserDTO user : usersReacted) {
			if (!assignedUsers.contains(user.getDiscordId())) {
				if (team1.size() <= team2.size()) {
					team1.add(user);
				} else {
					team2.add(user);
				}
				assignedUsers.add(user.getDiscordId());
			}
		}

		// Adjust Elo balance
		adjustEloBalance(team1, team2);
		int eloDifference = Math.abs(calculateTeamScore(team1) - calculateTeamScore(team2));

		matchService.saveTeamsWithMatchId(team1, team2, matchId);
		return Pair.of(TeamBalancerEmbed.createEmbed(team1, team2, eloDifference, matchId), false);
	}

	private Map<String, List<UserDTO>> groupPlayersByRole(List<UserDTO> users) {
		Map<String, List<UserDTO>> roleGroups = new HashMap<>();

		for (UserDTO user : users) {
			String mainRole = user.getMainRole();
			String secondaryRole = user.getSecondaryRole();

			roleGroups.putIfAbsent(mainRole, new ArrayList<>());
			roleGroups.get(mainRole).add(user);

			roleGroups.putIfAbsent(secondaryRole, new ArrayList<>());
			roleGroups.get(secondaryRole).add(user);
		}

		return roleGroups;
	}

	private void adjustEloBalance(List<UserDTO> team1, List<UserDTO> team2) {
		int acceptableEloDifference = 250; // Set an acceptable Elo difference threshold
		int eloDifference = Math.abs(calculateTeamScore(team1) - calculateTeamScore(team2));

		if (eloDifference <= acceptableEloDifference) {
			return;
		}

		// Swap players to minimize Elo disparity
		for (UserDTO user1 : team1) {
			for (UserDTO user2 : team2) {
				List<UserDTO> newTeam1 = team1.stream().map(u -> u == user1 ? user2 : u).collect(Collectors.toList());
				List<UserDTO> newTeam2 = team2.stream().map(u -> u == user2 ? user1 : u).collect(Collectors.toList());

				int newEloDifference = Math.abs(calculateTeamScore(newTeam1) - calculateTeamScore(newTeam2));

				if (newEloDifference < eloDifference) {
					team1 = newTeam1;
					team2 = newTeam2;
					eloDifference = newEloDifference;
				}
			}
		}
	}

	private boolean isValidRoleForTeam(String role, List<UserDTO> team) {
		return team.stream().noneMatch(u -> u.getMainRole().equals(role) || u.getSecondaryRole().equals(role));
	}

	private int calculateTeamScore(List<UserDTO> team) {
		return (int) team.stream().mapToDouble(UserDTO::getElo).sum();
	}
}
