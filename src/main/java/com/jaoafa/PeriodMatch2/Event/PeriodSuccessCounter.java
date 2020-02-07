package com.jaoafa.PeriodMatch2.Event;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.jaoafa.PeriodMatch2.PeriodMatchPlayer;

public class PeriodSuccessCounter implements Listener {
	JavaPlugin plugin;

	public PeriodSuccessCounter(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		PeriodMatchPlayer pmplayer = PeriodMatchPlayer.getPeriodMatchPlayer(player);
		String message = event.getMessage();

		if (!message.equals(".")) {
			// 「.」以外ならダメ
			return;
		}

		if (pmplayer.isWaiting()) {
			// 待機中だったら開始する。寝てたらキャンセル
			if (player.isSleeping()) {
				player.sendMessage("[PeriodMatch2] " + ChatColor.GREEN + "ピリオドマッチを開始できませんでした。");
				player.sendMessage("[PeriodMatch2] " + ChatColor.GREEN + "ルールを確認し、最初からやり直してください。");
				pmplayer.setWaiting(false);
				pmplayer.Save();
				return;
			}
			pmplayer.start();
			pmplayer.Save();
		}

		if (player.isSleeping()) {
			// 寝てたらダメ
			return;
		}

		if (!pmplayer.isPerioding()) {
			return;
		}

		pmplayer.Success();
		pmplayer.Save();

		event.setCancelled(true);
	}
}
