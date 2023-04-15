package com.darianngo.discordBot.services.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.darianngo.discordBot.dtos.UserDTO;
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
	public MessageEmbed balanceTeams(List<String> reactions, List<UserDTO> usersReacted, Long matchId) {
		List<UserDTO> team1 = new ArrayList<>();
		List<UserDTO> team2 = new ArrayList<>();

		// Sort users based on their rank
		Collections.sort(usersReacted, Comparator.comparingInt(UserDTO::getRanking).reversed());

		// Distribute players based on their roles and ranking
		for (UserDTO user : usersReacted) {
			List<String> userRoles = Arrays.asList(user.getMainRole(), user.getSecondaryRole());
			int team1Score = calculateTeamScore(team1);
			int team2Score = calculateTeamScore(team2);

			boolean addedToTeam = false;
			for (String role : userRoles) {
				if (!addedToTeam) {
					if (isValidRoleForTeam(role, team1)) {
						if (team1Score <= team2Score) {
							team1.add(user);
							addedToTeam = true;
						}
					}
					if (!addedToTeam && isValidRoleForTeam(role, team2)) {
						if (team2Score <= team1Score) {
							team2.add(user);
							addedToTeam = true;
						}
					}
				}
			}

			if (!addedToTeam) {
				// If user can't fit in their preferred roles, add to the team with the lowest
				// score
				if (team1Score <= team2Score) {
					team1.add(user);
				} else {
					team2.add(user);
				}
			}
		}
// Build Embed
		int eloDifference = Math.abs(calculateTeamScore(team1) - calculateTeamScore(team2));
		// Save teams to the database
		matchService.saveTeamsWithMatchId(team1, team2, matchId);

		return TeamBalancerEmbed.createEmbed(team1, team2, eloDifference, matchId);
	}

	// Create a custom game
	// try {
	// TournamentAPI tournamentCodeCreator = new TournamentAPI();
	// String tournamentCode =
	// tournamentCodeCreator.createTournamentCode(usersReacted);
	// event.getChannel().sendMessage("Tournament code: " + tournamentCode).queue();
	// } catch (IOException e) {
	// e.printStackTrace();
	// event.getChannel().sendMessage("Error creating the custom game.").queue();
	// }

	// Helper methods

	private boolean isValidRoleForTeam(String role, List<UserDTO> team) {
		return team.stream().noneMatch(u -> u.getMainRole().equals(role) || u.getSecondaryRole().equals(role));
	}

	private int calculateTeamScore(List<UserDTO> team) {
		return team.stream().mapToInt(UserDTO::getRanking).sum();
	}
}
