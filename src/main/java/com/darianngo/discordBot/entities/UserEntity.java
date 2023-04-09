package com.darianngo.discordBot.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {
    @Id
    private String discordId;
    private String discordName;
    private String summonerName;   
    private Integer ranking;
    private String mainRole;
    private String secondaryRole;
    private String region;
}
