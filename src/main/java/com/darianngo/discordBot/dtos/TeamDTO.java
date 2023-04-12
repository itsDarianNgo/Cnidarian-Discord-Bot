package com.darianngo.discordBot.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamDTO {
	private Long id;
	private Long matchId;
	private List<UserDTO> members;
}
