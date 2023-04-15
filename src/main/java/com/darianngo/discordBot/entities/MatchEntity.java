package com.darianngo.discordBot.entities;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedSubgraph;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@NamedEntityGraph(name = "MatchEntity.teamsAndUsers", attributeNodes = {
		@NamedAttributeNode(value = "teams", subgraph = "teams.users") }, subgraphs = {
				@NamedSubgraph(name = "teams.users", attributeNodes = @NamedAttributeNode("userTeams")) })
public class MatchEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	private List<TeamEntity> teams;
	private String winningTeam;
	private String finalScore;

}
