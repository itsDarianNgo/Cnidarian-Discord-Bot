package com.darianngo.discordBot.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class MatchResultEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne
	@JoinColumn(name = "matchId", referencedColumnName = "id")
	private MatchEntity match;
	private Long WinningTeamId;
	private Long LosingTeamId;
	private Long winningTeamNumber;
	private Long losingTeamNumber;
	private Integer winningScore;
	private Integer losingScore;

	@ManyToOne
	private UserEntity user;
}
