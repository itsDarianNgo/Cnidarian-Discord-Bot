package com.darianngo.SampleEloService;

//Player class to store player data
public class Player {
	private String id;
	private double elo;
	private double recentEloChange;
	private int totalMatches;
	private int wins;
	private int losses;

	public Player(String id, double elo) {
		this.id = id;
		this.elo = elo;
		this.recentEloChange = 0;
		this.totalMatches = 0;
		this.wins = 0;
		this.losses = 0;
	}

	public String getId() {
		return id;
	}

	public double getElo() {
		return elo;
	}

	public void setElo(double elo) {
		this.elo = elo;
	}

	public double getRecentEloChange() {
		return recentEloChange;
	}

	public void setRecentEloChange(double recentEloChange) {
		this.recentEloChange = recentEloChange;
	}

	public int getTotalMatches() {
		return totalMatches;
	}

	public void incrementTotalMatches() {
		this.totalMatches++;
	}

	public int getWins() {
		return wins;
	}

	public void incrementWins() {
		this.wins++;
	}

	public int getLosses() {
		return losses;
	}

	public void incrementLosses() {
		this.losses++;
	}
}
