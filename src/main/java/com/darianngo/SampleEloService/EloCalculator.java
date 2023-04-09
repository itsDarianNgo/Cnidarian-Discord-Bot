package com.darianngo.SampleEloService;

import java.util.List;

public interface EloCalculator {
	void updateElo(List<Player> teamA, List<Player> teamB, int matchScore);
}
