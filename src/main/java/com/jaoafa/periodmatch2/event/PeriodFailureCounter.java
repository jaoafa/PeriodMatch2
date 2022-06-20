package com.jaoafa.periodmatch2.event;

import com.jaoafa.periodmatch2.Main;
import com.jaoafa.periodmatch2.PeriodMatchPlayer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public record PeriodFailureCounter(JavaPlugin plugin) implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public static void onAsyncPlayerChatEvent(AsyncChatEvent event) {
        Player player = event.getPlayer();
        PeriodMatchPlayer pmplayer = PeriodMatchPlayer.getPeriodMatchPlayer(player);
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());

        if (pmplayer.isWaiting() && !message.equals(".") && message.contains(".")) {
            // Waitingで、「.」じゃなくて、「.」を含んでたら注意しておく
            Main.sendMessage(player, Component.text("ヒント: 日本語変換がオンのままだと、ピリオドマッチを開始できません。/jp offを実行して日本語変換をオフにしてください。", NamedTextColor.GREEN));
            return;
        }

        if (!pmplayer.isPerioding()) return;

        // 寝てなくて、「.」ならリターン
        if (message.equals(".") && !player.isSleeping()) return;

        if (player.isSleeping()) Main.sendMessage(player, Component.text("寝ながらのピリオドは失敗と判定されます。", NamedTextColor.GREEN));

        pmplayer.Failure();
        pmplayer.Save();

        event.setCancelled(true);
    }
}
