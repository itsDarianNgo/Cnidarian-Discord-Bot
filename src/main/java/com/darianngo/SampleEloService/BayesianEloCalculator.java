package com.darianngo.SampleEloService;

import java.util.List;

import org.apache.commons.math3.distribution.NormalDistribution;

public class BayesianEloCalculator implements EloCalculator {
	private static final double ELO_STD_DEV = 400;

	private final MlPredictor mlPredictor;
	private final NormalDistribution normalDistribution;

	public BayesianEloCalculator(MlPredictor mlPredictor) {
		this.mlPredictor = mlPredictor;
		this.normalDistribution = new NormalDistribution(0, 1);
	}

	@Override
	public void updateElo(List<Player> teamA, List<Player> teamB, int matchScore) {
		double teamARating = teamA.stream().mapToDouble(Player::getElo).average().orElse(0);
		double teamBRating = teamB.stream().mapToDouble(Player::getElo).average().orElse(0);
		boolean teamAWon = matchScore == 2;

		for (Player player : teamA) {
//			double predictedOutcomeA = mlPredictor.predictOutcome(teamA, teamB);
			double expectedOutcomeA = normalDistribution
					.cumulativeProbability((player.getElo() - teamBRating) / ELO_STD_DEV);
			double actualOutcomeA = teamAWon ? 1.0 : 0.0;

			// Take into account the individual player's Elo relative to the enemy team's
			// average Elo
			double eloChangeFactor = (player.getElo() < teamBRating) ? 1.2 : 0.8;
			double eloChange = eloChangeFactor * calculateK(player) * (actualOutcomeA - expectedOutcomeA);

			// Ensure players only gain Elo when their team wins
			if (teamAWon) {
				eloChange = Math.max(0, eloChange);
			} else {
				eloChange = Math.min(0, eloChange);
			}

			player.setElo(player.getElo() + eloChange);
			player.setRecentEloChange(eloChange);
			player.incrementTotalMatches();
			if (actualOutcomeA > expectedOutcomeA) {
				player.incrementWins();
			} else {
				player.incrementLosses();
			}
		}

		for (Player player : teamB) {
//			double predictedOutcomeB = 1 - mlPredictor.predictOutcome(teamA, teamB);
			double expectedOutcomeB = normalDistribution
					.cumulativeProbability((player.getElo() - teamARating) / ELO_STD_DEV);
			double actualOutcomeB = teamAWon ? 0.0 : 1.0;

			// Take into account the individual player's Elo relative to the enemy team's
			// average Elo
			double eloChangeFactor = (player.getElo() < teamARating) ? 1.2 : 0.8;
			double eloChange = eloChangeFactor * calculateK(player) * (actualOutcomeB - expectedOutcomeB);

			// Ensure players only gain Elo when their team wins
			if (!teamAWon) {
				eloChange = Math.max(0, eloChange);
			} else {
				eloChange = Math.min(0, eloChange);
			}

			player.setElo(player.getElo() + eloChange);
			player.setRecentEloChange(eloChange);
			player.incrementTotalMatches();
			if (actualOutcomeB > expectedOutcomeB) {
				player.incrementWins();
			} else {
				player.incrementLosses();
			}
		}

	}

	private int calculateK(Player player) {
		if (player.getTotalMatches() < 30) {
			return 40;
		} else if (player.getElo() < 2400) {
			return 20;
		} else {
			return 10;
		}
	}
}