package com.darianngo.discordBot.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserTeamDTO {
	private Long id;
	private String userId;
	private Long teamId;
}
