package com.darianngo.SampleEloService;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EloService {
	public static void main(String[] args) {
		MlPredictor mlPredictor = null; // Replace this with ML predictor implementation
		EloCalculator eloCalculator = new BayesianEloCalculator(mlPredictor);

		List<Player> teamA = new ArrayList<>();
		List<Player> teamB = new ArrayList<>();

		// Generate mock players with random Elo
		Random random = new Random();
		for (int i = 0; i < 5; i++) {
			teamA.add(new Player("A" + (i + 1), random.nextDouble() * 700 + 100));
			teamB.add(new Player("B" + (i + 1), random.nextDouble() * 700 + 100));
		}

		// Store original Elo
		List<Double> originalEloTeamA = new ArrayList<>();
		List<Double> originalEloTeamB = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			originalEloTeamA.add(teamA.get(i).getElo());
			originalEloTeamB.add(teamB.get(i).getElo());
		}

		// Simulate a match and update Elo
		int matchScore = random.nextBoolean() ? 2 : 1;
		eloCalculator.updateElo(teamA, teamB, matchScore);

		// Update recent match results
		for (Player player : teamA) {
			player.addRecentMatchResult(matchScore == 2 ? 1 : 0);
		}
		for (Player player : teamB) {
			player.addRecentMatchResult(matchScore == 2 ? 0 : 1);
		}

		// Print results
		System.out.println("Match Result: " + (matchScore == 2 ? "2-0" : "2-1"));
		String winningTeam = (matchScore == 2) ? "Team A" : "Team B";
		System.out.println(winningTeam + " won the series.");
		printTeamResults("Team A", teamA, originalEloTeamA);
		printTeamResults("Team B", teamB, originalEloTeamB);
	}

	private static void printTeamResults(String teamName, List<Player> team, List<Double> originalElo) {
		System.out.println("\n" + teamName + " Results:");
		double avgElo = team.stream().mapToDouble(Player::getElo).average().orElse(0);
		System.out.println("Average Elo: " + Math.round(avgElo));

		for (int i = 0; i < team.size(); i++) {
			Player player = team.get(i);
			System.out.printf("\nPlayer %s: %d (%+d)", player.getId(), Math.round(player.getElo()),
					Math.round(player.getRecentEloChange()));
		}
	}

}