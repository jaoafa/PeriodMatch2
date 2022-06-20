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
    private static final Map<UUID, PeriodMatchPlayer> players = new HashMap<>();

    private final Player player; // プレイヤー
    private boolean perioding = false; // マッチ中か
    private boolean waiting = false;
    private int success = 0; // 成功回数
    private int failure = 0; // 失敗回数
    private int matchTime = -1; // マッチ時間
    private long startTime = -1; // 開始UNIXTime
    private BukkitTask mainTask = null;

    private PeriodMatchPlayer(Player player) {
        this.player = player;
    }

    public static PeriodMatchPlayer getPeriodMatchPlayer(Player player) {
        if (PeriodMatchPlayer.players.containsKey(player.getUniqueId()))
            return PeriodMatchPlayer.players.get(player.getUniqueId());
        else {
            PeriodMatchPlayer pmplayer = new PeriodMatchPlayer(player);
            pmplayer.Save();
            return pmplayer;
        }
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

        if (mainTask != null && !mainTask.isCancelled()) mainTask.cancel();
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
        if (!isPerioding()) return;
        success++;
        setTitle();
        Save();
    }

    public void Failure() {
        if (!isPerioding()) return;
        failure++;
        setTitle();
        Save();
    }

    public void Save() {
        PeriodMatchPlayer.players.put(player.getUniqueId(), this);
    }

    private void setTitle() {
        player.sendTitlePart(TitlePart.TITLE, Component.text("PeriodMatch2", NamedTextColor.GOLD));
        player.sendTitlePart(TitlePart.SUBTITLE, Component.join(
            JoinConfiguration.noSeparators(),
            Component.text("成功: " + getSuccessCount(), NamedTextColor.GREEN),
            Component.text(" | "),
            Component.text(getFailureCount() + " :失敗", NamedTextColor.RED)));
        player.sendTitlePart(TitlePart.TIMES, Title.Times.times(Duration.ZERO, Duration.ofSeconds(Integer.MAX_VALUE), Duration.ZERO));
    }

    private void clearTitle() {
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

    public void setMatchTime(int matchTime) {
        this.matchTime = matchTime;
    }

    public long getStartTime() {
        return startTime;
    }
}
