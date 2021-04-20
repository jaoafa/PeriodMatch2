package com.jaoafa.periodmatch2.event;

import com.jaoafa.periodmatch2.Main;
import com.jaoafa.periodmatch2.PeriodMatchPlayer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class PeriodSuccessCounter implements Listener {
	JavaPlugin plugin;

	public PeriodSuccessCounter(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onAsyncPlayerChatEvent(AsyncChatEvent event) {
		Player player = event.getPlayer();
		PeriodMatchPlayer pmplayer = PeriodMatchPlayer.getPeriodMatchPlayer(player);
        String message = PlainComponentSerializer.plain().serialize(event.message());

		if (!message.equals(".")) {
			// 「.」以外ならダメ
			return;
		}

		if (pmplayer.isWaiting()) {
			// 待機中だったら開始する。寝てたらキャンセル
			if (player.isSleeping()) {
                Main.sendMessage(player,
                    Component.text("ピリオドマッチを開始できませんでした。", NamedTextColor.GREEN)
                );
                Main.sendMessage(player,
                    Component.text("ルールを確認し、最初からやり直してください。", NamedTextColor.GREEN)
                );
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
