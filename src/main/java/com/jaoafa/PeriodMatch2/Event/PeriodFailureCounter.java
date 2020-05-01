package com.jaoafa.PeriodMatch2.Event;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.jaoafa.PeriodMatch2.PeriodMatchPlayer;

public class PeriodFailureCounter implements Listener {
	JavaPlugin plugin;

	public PeriodFailureCounter(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		PeriodMatchPlayer pmplayer = PeriodMatchPlayer.getPeriodMatchPlayer(player);
		String message = event.getMessage();

		if (pmplayer.isWaiting() && !message.equals(".") && message.contains(".")) {
			// Waitingで、「.」じゃなくて、「.」を含んでたら注意しておく
			player.sendMessage("[PeriodMatch2] " + ChatColor.GREEN
					+ "ヒント: 日本語変換がオンのままだと、ピリオドマッチを開始できません。/jp offを実行して日本語変換をオフにしてください。");
			return;
		}

		if (!pmplayer.isPerioding()) {
			return;
		}

		if (message.equals(".") && !player.isSleeping()) {
			// 寝てなくて、「.」ならリターン
			return;
		}

		if (player.isSleeping()) {
			player.sendMessage("[PeriodMatch2] " + ChatColor.GREEN + "寝ながらのピリオドは失敗と判定されます。");
		}

		pmplayer.Failure();
		pmplayer.Save();

		event.setCancelled(true);
	}
}
