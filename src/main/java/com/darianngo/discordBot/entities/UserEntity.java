package com.darianngo.discordBot.entities;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserEntity {
	@Id
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
	@OneToMany(mappedBy = "user")
	private List<UserTeamEntity> userTeams;
}
