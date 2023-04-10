package com.darianngo.discordBot.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchDTO {
    private Long id;
    private List<TeamDTO> teams;
}
