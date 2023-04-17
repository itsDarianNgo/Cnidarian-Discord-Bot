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
	private Integer elo;
	private Integer recentEloChange;
	private Integer totalMatches;
	private Integer wins;
	private Integer losses;
	private List<UserTeamDTO> userTeams;
}
