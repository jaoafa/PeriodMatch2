package com.jaoafa.PeriodMatch2;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.connorlinfoot.titleapi.TitleAPI;
import com.jaoafa.PeriodMatch2.Task.Task_MatchEnd;

public class PeriodMatchPlayer {
	public static Map<UUID, PeriodMatchPlayer> players = new HashMap<>();

	Player player; // プレイヤー
	boolean perioding = false; // マッチ中か
	boolean waiting = false;
	int success = 0; // 成功回数
	int failure = 0; // 失敗回数
	int matchTime = -1; // マッチ時間
	long startTime = -1; // 開始UNIXTime

	public PeriodMatchPlayer(Player player) {
		this.player = player;
	}

	public PeriodMatchPlayer(Player player, int matchTime) {
		this.player = player;
		this.matchTime = matchTime;
	}

	/**
	 * PeriodMatchをスタートさせます。
	 */
	public void start() {
		perioding = true;
		waiting = false;

		success = 0;
		failure = 0;

		startTime = System.currentTimeMillis();

		player.sendMessage("[PeriodMatch2] " + ChatColor.GREEN + "ピリオドマッチ計測を開始します。");
		new Task_MatchEnd(player, this).runTaskLater(Main.getJavaPlugin(), matchTime * 20);
		Save();
	}

	/**
	 * PeriodMatchを強制終了させます。
	 */
	public void forceEnd() {
		perioding = false;

		player.sendMessage("[PeriodMatch2] " + ChatColor.GREEN + "ピリオドマッチ計測を強制終了しました。");
		clearTitle();
		Save();
	}

	/**
	 * PeriodMatchを終了させます。
	 */
	public void end() {
		perioding = false;

		player.sendMessage("[PeriodMatch2] " + ChatColor.GREEN + "ピリオドマッチ計測を終了しました。");
		clearTitle();
		Save();
	}

	public void Success() {
		if (!isPerioding()) {
			return;
		}
		success++;
		setTitle();
		Save();
	}

	public void Failure() {
		if (!isPerioding()) {
			return;
		}
		failure++;
		setTitle();
		Save();
	}

	public void Save() {
		players.put(player.getUniqueId(), this);
	}

	public void setTitle() {
		if (!Bukkit.getPluginManager().isPluginEnabled("TitleAPI")) {
			return; // TitleAPIが動作しているときのみ実行
		}
		TitleAPI.sendTitle(
				player,
				0,
				Integer.MAX_VALUE,
				0,
				ChatColor.GOLD + "PeriodMatch2",
				ChatColor.GREEN + "成功: " + getSuccessCount() + ChatColor.RESET + " | " + ChatColor.RED
						+ getFailureCount() + " :失敗");
	}

	public void clearTitle() {
		if (!Bukkit.getPluginManager().isPluginEnabled("TitleAPI")) {
			return; // TitleAPIが動作しているときのみ実行
		}
		TitleAPI.clearTitle(player);
	}

	public boolean isPerioding() {
		return perioding;
	}

	public void setPerioding(boolean perioding) {
		this.perioding = perioding;
	}

	public boolean isWaiting() {
		return waiting;
	}

	public void setWaiting(boolean waiting) {
		this.waiting = waiting;
	}

	public int getSuccessCount() {
		return success;
	}

	public int getFailureCount() {
		return failure;
	}

	public int getMatchTime() {
		return matchTime;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setMatchTime(int matchTime) {
		this.matchTime = matchTime;
	}

	public static PeriodMatchPlayer getPeriodMatchPlayer(Player player) {
		if (players.containsKey(player.getUniqueId())) {
			return players.get(player.getUniqueId());
		} else {
			PeriodMatchPlayer pmplayer = new PeriodMatchPlayer(player);
			pmplayer.Save();
			return pmplayer;
		}
	}
}
