package com.darianngo.discordBot.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserVoteDTO {
	private Long teamId;
	private Integer winningScore;
	private Integer losingScore;
	private Long teamVote;
}

