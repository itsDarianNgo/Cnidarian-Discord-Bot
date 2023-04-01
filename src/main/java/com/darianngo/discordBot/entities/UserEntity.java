package com.darianngo.discordBot.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.Setter;

@Setter
@Data
@Entity
public class UserEntity {
	@Id
	private String id;
	private String name;
}
