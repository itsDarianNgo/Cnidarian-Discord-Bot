package com.darianngo.SampleEloService;

import java.util.List;

public interface MlPredictor {
	double predictOutcome(List<Player> teamA, List<Player> teamB);
}
