package com.jaoafa.PeriodMatch2.Command;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.jaoafa.PeriodMatch2.PeriodMatchPlayer;

public class Period implements CommandExecutor {
	JavaPlugin plugin;

	public Period(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	static Map<String, Integer> waiting = new HashMap<String, Integer>();
	static Map<String, Integer> running = new HashMap<String, Integer>();

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (!(sender instanceof Player)) {
			// プレイヤー以外からのコマンド実行
			sender.sendMessage("[PeriodMatch2] " + ChatColor.GREEN + "このコマンドはゲーム内でのみ使用できます。");
			return true;
		}
		Player player = (Player) sender;
		PeriodMatchPlayer pmplayer = PeriodMatchPlayer.getPeriodMatchPlayer(player);
		if (args.length == 0) {
			if (pmplayer.isPerioding()) {
				player.sendMessage(
						"[PeriodMatch2] " + ChatColor.GREEN + "現在あなたはピリオドマッチを" + pmplayer.getMatchTime() + "秒で行っています。");
				player.sendMessage("[PeriodMatch2] " + ChatColor.GREEN + "「/. stop」で行っているピリオドマッチを強制終了できます。");
				return true;
			} else if (pmplayer.isWaiting()) {
				player.sendMessage(
						"[PeriodMatch2] " + ChatColor.GREEN + "現在あなたはピリオドマッチを" + pmplayer.getMatchTime()
								+ "秒で待機しています。");
				player.sendMessage("[PeriodMatch2] " + ChatColor.GREEN + "次に「.」と打った瞬間から開始します。");
				player.sendMessage("[PeriodMatch2] " + ChatColor.GREEN + "「/. stop」で行っているピリオドマッチを強制終了できます。");
				return true;
			}

			// デフォルト値(30秒)で開始
			pmplayer.setMatchTime(60);
			pmplayer.setWaiting(true);
			player.sendMessage(
					"[PeriodMatch2] " + ChatColor.GREEN + "ピリオドマッチを開始します。次に「.」を打った瞬間から60秒間計測します。");
			player.sendMessage(
					"[PeriodMatch2] " + ChatColor.GREEN + "正確にピリオド判定を行うため、かなローマ字変換をオフにして(/jp off)ご利用ください。");
			return true;
		} else if (args.length == 1) {
			if (isNumber(args[0])) {
				if (pmplayer.isPerioding()) {
					player.sendMessage(
							"[PeriodMatch2] " + ChatColor.GREEN + "現在あなたはピリオドマッチを実施中です。強制終了するには「/. stop」を実行してください。");
					return true;
				}
				if (pmplayer.isWaiting()) {
					player.sendMessage(
							"[PeriodMatch2] " + ChatColor.GREEN + "現在あなたはピリオドマッチを待機中です。強制終了するには「/. stop」を実行してください。");
					return true;
				}
				int sec = Integer.parseInt(args[0]);
				pmplayer.setMatchTime(sec);
				pmplayer.setWaiting(true);
				player.sendMessage(
						"[PeriodMatch2] " + ChatColor.GREEN + "ピリオドマッチを開始します。次に「.」を打った瞬間から" + sec + "秒間計測します。");
				player.sendMessage(
						"[PeriodMatch2] " + ChatColor.GREEN + "正確にピリオド判定を行うため、かなローマ字変換をオフにして(/jp off)ご利用ください。");
				return true;
			} else if (args[0].equalsIgnoreCase("stop")) {
				pmplayer.forceEnd();
				return true;
			}
		}
		player.sendMessage("[PeriodMatch2] " + ChatColor.GREEN + "----- Period -----");
		player.sendMessage("[PeriodMatch2] " + ChatColor.GREEN + "/. [TimeSecond]: TimeSecond秒のピリオドマッチを行います。");
		player.sendMessage("[PeriodMatch2] " + ChatColor.GREEN + "ピリオドマッチでは次の秒数のみランキングが公開されます。それ以外の秒数ではランキング付けがされません。");
		player.sendMessage("[PeriodMatch2] " + ChatColor.GREEN + "10秒(/. 10)・60秒(/. 60)・300秒(/. 300)");
		player.sendMessage("[PeriodMatch2] " + ChatColor.GREEN + "/. stop: 既に開始しているピリオドマッチを強制終了します。");
		return true;
	}

	private boolean isNumber(String num) {
		try {
			Integer.parseInt(num);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}
