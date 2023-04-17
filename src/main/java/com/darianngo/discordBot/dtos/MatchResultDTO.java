package com.darianngo.discordBot.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchResultDTO {
	private Long id;
	private Long matchId;
	private Long WinningTeamId;
	private Long LosingTeamId;
	private Long winningTeamNumber;
	private Long losingTeamNumber;
	private Integer winningScore;
	private Integer losingScore;
	private MatchDTO match;
}
