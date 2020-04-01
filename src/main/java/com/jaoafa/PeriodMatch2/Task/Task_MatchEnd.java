package com.jaoafa.PeriodMatch2.Task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.jaoafa.PeriodMatch2.Main;
import com.jaoafa.PeriodMatch2.PeriodMatchPlayer;
import com.jaoafa.PeriodMatch2.Lib.MySQLDBManager;

public class Task_MatchEnd extends BukkitRunnable {
	Player player;
	PeriodMatchPlayer pmplayer;

	public Task_MatchEnd(Player player, PeriodMatchPlayer pmplayer) {
		this.player = player;
		this.pmplayer = pmplayer;
	}
	/*
		private Integer Calc(int success, int unsuccess) {
			if (success == 0)
				success = 1;
			return success * (success / (success + unsuccess)) - unsuccess;
		}*/

	@Override
	public void run() {
		pmplayer.end();
		long endTime = System.currentTimeMillis();

		int successCount = pmplayer.getSuccessCount();
		int failureCount = pmplayer.getFailureCount();
		int matchTime = pmplayer.getMatchTime();
		long startTime = pmplayer.getStartTime();

		MySQLDBManager sqlmanager = Main.getMySQLDBManager();
		try {
			Connection conn = sqlmanager.getConnection();

			PreparedStatement statement = conn.prepareStatement(
					"INSERT INTO periodmatch2 (player, uuid, success, failure, match_time, real_match_time, start_time, end_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
					Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, player.getName());
			statement.setString(2, player.getUniqueId().toString());
			statement.setInt(3, successCount);
			statement.setInt(4, failureCount);
			statement.setInt(5, matchTime);
			statement.setFloat(6, endTime - startTime);
			statement.setTimestamp(7, new Timestamp(startTime));
			statement.setTimestamp(8, new Timestamp(endTime));
			int res = statement.executeUpdate();
			if (res == 0) {
				player.sendMessage("[PeriodMatch2] " + ChatColor.GREEN + "データの登録に失敗した恐れがあります。開発部にお問い合わせください。(1)");
				player.sendMessage("[PeriodMatch2] " + ChatColor.GREEN + "successCount: " + successCount
						+ " / failureCount: " + failureCount + " / matchTime: " + matchTime);
			}
			ResultSet res_genkey = statement.getGeneratedKeys();
			int id = -1;
			if (res_genkey != null && res_genkey.next()) {
				id = res_genkey.getInt(1);
			} else {
				player.sendMessage("[PeriodMatch2] " + ChatColor.GREEN + "データの登録に失敗した恐れがあります。開発部にお問い合わせください。(2)");
				player.sendMessage("[PeriodMatch2] " + ChatColor.GREEN + "successCount: " + successCount
						+ " / failureCount: " + failureCount + " / matchTime: " + matchTime);
			}
			player.sendMessage("[PeriodMatch2] " + ChatColor.GREEN + "お疲れさまでした！");

			int ranking = getRanking(matchTime, id);

			Bukkit.broadcastMessage("[PeriodMatch2] " + ChatColor.GREEN + player.getName() + "さんのピリオドマッチ(" + matchTime
					+ "秒部門)が終了しました。");

			Bukkit.broadcastMessage(
					"[PeriodMatch2] " + ChatColor.GREEN + "成功回数: " + successCount + " / 失敗回数: " + failureCount);
			Bukkit.broadcastMessage("[PeriodMatch2] " + ChatColor.GREEN + "順位: " + ranking + "位");
			Bukkit.broadcastMessage("ランキングはこちらからご覧ください: https://jaoafa.com/p/");

			if (Main.getDiscord() != null) {
				Main.getDiscord().sendMessage("597423199227084800",
						"[PeriodMatch2] " + player.getName() + "さんのピリオドマッチ(" + matchTime + "秒部門)が終了しました。");
				Main.getDiscord().sendMessage("597423199227084800",
						"[PeriodMatch2] 成功回数: " + successCount + " / 失敗回数: " + failureCount);
				Main.getDiscord().sendMessage("597423199227084800", "[PeriodMatch2] 順位: " + ranking + "位");
				Main.getDiscord().sendMessage("597423199227084800", "ランキングはこちらからご覧ください: https://jaoafa.com/p/");
			}
			return;
		} catch (SQLException e) {
			player.sendMessage("[PeriodMatch2] " + ChatColor.GREEN + "データベースサーバに接続できなかったか、操作に失敗しました。開発部にお問い合わせください。");
			player.sendMessage("[PeriodMatch2] " + ChatColor.GREEN + e.getErrorCode() + " - " + e.getMessage());
			player.sendMessage("[PeriodMatch2] " + ChatColor.GREEN + "successCount: " + successCount
					+ " / failureCount: " + failureCount + " / matchTime: " + matchTime);
			player.sendMessage(
					"[PeriodMatch2] " + ChatColor.GREEN + "startTime: " + startTime + " / endTime: " + endTime);
			e.printStackTrace();
			return;
		}
	}

	int getRanking(int matchTime, int id) throws SQLException {
		MySQLDBManager sqlmanager = Main.getMySQLDBManager();
		Connection conn = sqlmanager.getConnection();
		PreparedStatement statement = conn.prepareStatement(
				"SELECT *, (success+(success/(success+failure))-failure)/real_match_time AS calc FROM periodmatch2 WHERE match_time = ? ORDER BY calc DESC;");
		statement.setInt(1, matchTime);
		ResultSet res = statement.executeQuery();
		int rank = 1;
		int oldCalc = Integer.MIN_VALUE;
		while (res.next()) {
			if (res.getInt("id") == id) {
				break;
			}
			int calc = res.getInt("calc");
			rank++;
			if (oldCalc != Integer.MIN_VALUE && oldCalc == calc)
				rank--;
			oldCalc = rank;
		}
		return rank;
	}
}
