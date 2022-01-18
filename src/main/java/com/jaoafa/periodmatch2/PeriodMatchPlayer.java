package com.jaoafa.periodmatch2;

import com.jaoafa.periodmatch2.task.Task_MatchEnd;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PeriodMatchPlayer {
	public static final Map<UUID, PeriodMatchPlayer> players = new HashMap<>();

	final Player player; // プレイヤー
	boolean perioding = false; // マッチ中か
	boolean waiting = false;
	int success = 0; // 成功回数
	int failure = 0; // 失敗回数
	int matchTime = -1; // マッチ時間
	long startTime = -1; // 開始UNIXTime
	BukkitTask mainTask = null;

	public PeriodMatchPlayer(Player player) {
		this.player = player;
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
		mainTask = new Task_MatchEnd(player, this).runTaskLater(Main.getJavaPlugin(), matchTime * 20L);
		Save();
	}

	/**
	 * PeriodMatchを強制終了させます。
	 */
	public void forceEnd() {
		perioding = false;
		waiting = false;

		if (mainTask != null && !mainTask.isCancelled()) {
			mainTask.cancel();
		}
		mainTask = null;

		player.sendMessage("[PeriodMatch2] " + ChatColor.GREEN + "ピリオドマッチ計測を強制終了しました。");
		clearTitle();
		Save();
	}

	/**
	 * PeriodMatchを終了させます。
	 */
	public void end() {
		perioding = false;
		mainTask = null;

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
        player.sendTitlePart(TitlePart.TITLE, Component.text("PeriodMatch2", NamedTextColor.GOLD));
        player.sendTitlePart(TitlePart.SUBTITLE, Component.join(JoinConfiguration.noSeparators(),
            Component.text("成功: " + getSuccessCount(), NamedTextColor.GREEN),
            Component.text(" | "),
            Component.text(getFailureCount() + " :失敗", NamedTextColor.RED)));
        player.sendTitlePart(TitlePart.TIMES, Title.Times.of(Duration.ZERO, Duration.ofSeconds(Long.MAX_VALUE), Duration.ZERO));
	}

	public void clearTitle() {
	    player.resetTitle();
	}

	public boolean isPerioding() {
		return perioding;
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
