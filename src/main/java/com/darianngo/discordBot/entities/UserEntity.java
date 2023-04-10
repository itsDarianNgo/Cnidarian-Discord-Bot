package com.darianngo.discordBot.entities;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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

    @OneToMany(mappedBy = "user")
    private List<UserTeamEntity> userTeams;
}
