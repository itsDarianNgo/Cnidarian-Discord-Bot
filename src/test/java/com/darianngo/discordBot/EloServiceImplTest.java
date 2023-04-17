package com.darianngo.discordBot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.darianngo.discordBot.dtos.MatchDTO;
import com.darianngo.discordBot.dtos.MatchResultDTO;
import com.darianngo.discordBot.dtos.UserDTO;
import com.darianngo.discordBot.dtos.UserTeamDTO;
import com.darianngo.discordBot.mappers.UserMapper;
import com.darianngo.discordBot.repositories.UserRepository;
import com.darianngo.discordBot.services.EloService;
import com.darianngo.discordBot.services.impl.EloServiceImpl;

@ExtendWith(MockitoExtension.class)
public class EloServiceImplTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private UserMapper userMapper;

	private EloService eloService;

	@BeforeEach
	public void setUp() {
		eloService = new EloServiceImpl(userRepository, userMapper);
	}

	@Test
	public void testUpdateElo() {
		MatchResultDTO matchResult = new MatchResultDTO(1L, 2L, 1L, 2L, 1L, 2L, 10, 5, new MatchDTO());
		List<Long> user1TeamIds = Arrays.asList(2L, 3L, 6L, 7L, 9L);
		List<Long> user2TeamIds = Arrays.asList(1L, 4L, 5L, 7L, 10L);
		List<Long> user3TeamIds = Arrays.asList(1L, 3L, 6L, 7L, 10L);
		List<Long> user4TeamIds = Arrays.asList(2L, 4L, 6L, 8L, 10L);
		List<Long> user5TeamIds = Arrays.asList(2L, 3L, 5L, 8L, 9L);
		List<Long> user6TeamIds = Arrays.asList(1L, 4L, 5L, 8L, 10L);
		List<Long> user7TeamIds = Arrays.asList(1L, 3L, 5L, 8L, 9L);
		List<Long> user8TeamIds = Arrays.asList(2L, 4L, 6L, 7L, 9L);
		List<Long> user9TeamIds = Arrays.asList(1L, 3L, 5L, 7L, 9L);
		List<Long> user10TeamIds = Arrays.asList(2L, 4L, 6L, 8L, 10L);

		UserDTO user1 = createUser("1", "User1", 1200.0, 400.0, user1TeamIds);
		UserDTO user2 = createUser("2", "User2", 1250.0, 400.0, user2TeamIds);
		UserDTO user3 = createUser("3", "User3", 1100.0, 400.0, user3TeamIds);
		UserDTO user4 = createUser("4", "User4", 1150.0, 400.0, user4TeamIds);
		UserDTO user5 = createUser("5", "User5", 1300.0, 400.0, user5TeamIds);
		UserDTO user6 = createUser("6", "User6", 1350.0, 400.0, user6TeamIds);
		UserDTO user7 = createUser("7", "User7", 1400.0, 400.0, user7TeamIds);
		UserDTO user8 = createUser("8", "User8", 1450.0, 400.0, user8TeamIds);
		UserDTO user9 = createUser("9", "User9", 1500.0, 400.0, user9TeamIds);
		UserDTO user10 = createUser("10", "User10", 1550.0, 400.0, user10TeamIds);

		List<UserDTO> usersInMatch = Arrays.asList(user1, user2, user3, user4, user5, user6, user7, user8, user9,
				user10);
		eloService.updateElo(matchResult, usersInMatch);

		printTeamInfo(matchResult, usersInMatch);
	}

	private void printTeamInfo(MatchResultDTO matchResult, List<UserDTO> usersInMatch) {
		System.out.println("Winning Team (Team ID: " + matchResult.getWinningTeamId() + "):");
		printTeam(matchResult.getWinningTeamId(), usersInMatch);
		System.out.println("\nLosing Team (Team ID: " + matchResult.getLosingTeamId() + "):");
		printTeam(matchResult.getLosingTeamId(), usersInMatch);
	}

	private void printTeam(Long teamId, List<UserDTO> usersInMatch) {
		for (UserDTO user : usersInMatch) {
			for (UserTeamDTO userTeam : user.getUserTeams()) {
				if (userTeam.getTeamId().equals(teamId)) {
					double oldElo = user.getElo() - user.getRecentEloChange();
					System.out.printf("%s: Elo: %.2f (%+.2f) = %.2f%n", user.getSummonerName(), oldElo,
							user.getRecentEloChange(), user.getElo());
				}
			}
		}
	}

	private UserDTO createUser(String discordId, String summonerName, double elo, double sigma, List<Long> teamIds) {
		UserDTO user = new UserDTO();
		user.setDiscordId(discordId);
		user.setSummonerName(summonerName);
		user.setElo(elo);
		user.setSigma(sigma);
		user.setRecentEloChange(0.0);
		user.setTotalMatches(0);
		user.setWins(0);
		user.setLosses(0);

		List<UserTeamDTO> userTeams = new ArrayList<>();
		for (Long teamId : teamIds) {
			UserTeamDTO userTeam = new UserTeamDTO();
			userTeam.setId(teamId);
			userTeam.setTeamId(teamId);
			userTeam.setUserId(user.getDiscordId());
			userTeams.add(userTeam);
		}
		user.setUserTeams(userTeams);

		return user;
	}
}
