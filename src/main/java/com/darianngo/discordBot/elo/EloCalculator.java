//package com.darianngo.discordBot.elo;
//import java.util.List;
//import java.util.Queue;
//
//import org.apache.commons.math3.distribution.NormalDistribution;
//
//import com.darianngo.SampleEloService.MlPredictor;
//public class EloCalculator {
//
//
//		private static final double ELO_STD_DEV = 400;
//		private static final double ELO_CHANGE_FACTOR_MIN = 0.8;
//		private static final double ELO_CHANGE_FACTOR_MAX = 1.2;
//
//		private final MlPredictor mlPredictor;
//		private final NormalDistribution normalDistribution;
//
//		public BayesianEloCalculator(MlPredictor mlPredictor) {
//			this.mlPredictor = mlPredictor;
//			this.normalDistribution = new NormalDistribution(0, 1);
//		}
//
//		@Override
//		public void updateElo(List<Player> teamA, List<Player> teamB, int matchScore) {
//			double teamARating = teamA.stream().mapToDouble(Player::getElo).average().orElse(0);
//			double teamBRating = teamB.stream().mapToDouble(Player::getElo).average().orElse(0);
//			boolean teamAWon = matchScore == 2;
//
//			for (Player player : teamA) {
//				double expectedOutcomeA = normalDistribution
//						.cumulativeProbability((player.getElo() - teamBRating) / ELO_STD_DEV);
//				double actualOutcomeA = teamAWon ? 1.0 : 0.0;
//
//				double eloChangeFactor = calculateDynamicEloChangeFactor(player);
//				double eloChange = eloChangeFactor * calculateK(player) * (actualOutcomeA - expectedOutcomeA);
//
//				if (teamAWon) {
//					eloChange = Math.max(0, eloChange);
//				} else {
//					eloChange = Math.min(0, eloChange);
//				}
//
//				player.setElo(player.getElo() + eloChange);
//				player.setRecentEloChange(eloChange);
//				player.incrementTotalMatches();
//				if (actualOutcomeA > expectedOutcomeA) {
//					player.incrementWins();
//				} else {
//					player.incrementLosses();
//				}
//			}
//
//			for (Player player : teamB) {
//				double expectedOutcomeB = normalDistribution
//						.cumulativeProbability((player.getElo() - teamARating) / ELO_STD_DEV);
//				double actualOutcomeB = teamAWon ? 0.0 : 1.0;
//
//				double eloChangeFactor = calculateDynamicEloChangeFactor(player);
//				double eloChange = eloChangeFactor * calculateK(player) * (actualOutcomeB - expectedOutcomeB);
//
//				if (!teamAWon) {
//					eloChange = Math.max(0, eloChange);
//				} else {
//					eloChange = Math.min(0, eloChange);
//				}
//
//				player.setElo(player.getElo() + eloChange);
//				player.setRecentEloChange(eloChange);
//				player.incrementTotalMatches();
//				if (actualOutcomeB > expectedOutcomeB) {
//					player.incrementWins();
//				} else {
//					player.incrementLosses();
//				}
//			}
//		}
//
//		private double calculateDynamicEloChangeFactor(Player player) {
//			Queue<Integer> recentMatchesResults = player.getRecentMatchesResults();
//			int recentWins = (int) recentMatchesResults.stream().filter(result -> result == 1).count();
//			double winRate = recentMatchesResults.isEmpty() ? 0.5 : (double) recentWins / recentMatchesResults.size();
//
//			double eloChangeFactor = ELO_CHANGE_FACTOR_MIN + (winRate * (ELO_CHANGE_FACTOR_MAX - ELO_CHANGE_FACTOR_MIN));
//			return Math.max(ELO_CHANGE_FACTOR_MIN, Math.min(ELO_CHANGE_FACTOR_MAX, eloChangeFactor));
//		}
//
//		private int calculateK(Player player) {
//			if (player.getTotalMatches() < 30) {
//				return 40;
//			} else if (player.getElo() < 2400) {
//				return 20;
//			} else {
//				return 10;
//			}
//		}
//	}
//}
