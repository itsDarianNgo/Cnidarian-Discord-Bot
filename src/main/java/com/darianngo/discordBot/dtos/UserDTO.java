package com.darianngo.discordBot.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
	private String discordId;
	private String discordName;
	private String summonerName;
	private Integer ranking;
	private String mainRole;
	private String secondaryRole;
	private String region;
	private Double elo;
	private Double recentEloChange;
	private Double sigma;
	private Integer totalMatches;
	private Integer wins;
	private Integer losses;
	private Integer winningStreak;
	private List<UserTeamDTO> userTeams;
}
