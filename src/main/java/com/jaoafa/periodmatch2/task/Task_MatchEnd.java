package com.jaoafa.periodmatch2.task;

import com.jaoafa.periodmatch2.lib.MySQLDBManager;
import com.jaoafa.periodmatch2.Main;
import com.jaoafa.periodmatch2.PeriodMatchPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.util.Arrays;
import java.util.List;

public class Task_MatchEnd extends BukkitRunnable {
    final Player player;
    final PeriodMatchPlayer pmplayer;
    final List<Integer> rankingSecs = Arrays.asList(
        -1,
        0,
        1,
        10,
        20,
        30,
        60,
        100,
        300
    );

    public Task_MatchEnd(Player player, PeriodMatchPlayer pmplayer) {
        this.player = player;
        this.pmplayer = pmplayer;
    }

    @Override
    public void run() {
        pmplayer.end();
        long endTime = System.currentTimeMillis();

        int successCount = pmplayer.getSuccessCount();
        int failureCount = pmplayer.getFailureCount();
        int matchTime = pmplayer.getMatchTime();
        long startTime = pmplayer.getStartTime();

        long calc_match_time = endTime - startTime;
        if (calc_match_time < (matchTime * 1000L)) {
            calc_match_time = matchTime * 1000L;
        }

        MySQLDBManager sqlmanager = Main.getMySQLDBManager();
        try {
            Connection conn = sqlmanager.getConnection();

            PreparedStatement statement = conn.prepareStatement(
                "INSERT INTO periodmatch2 (player, uuid, success, failure, match_time, real_match_time, calc_match_time, start_time, end_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, player.getName());
            statement.setString(2, player.getUniqueId().toString());
            statement.setInt(3, successCount);
            statement.setInt(4, failureCount);
            statement.setInt(5, matchTime);
            statement.setFloat(6, endTime - startTime);
            statement.setFloat(7, calc_match_time);
            statement.setTimestamp(8, new Timestamp(startTime));
            statement.setTimestamp(9, new Timestamp(endTime));
            int res = statement.executeUpdate();
            if (res == 0) {
                Main.sendMessage(player,
                    Component.text("データの登録に失敗した恐れがあります。開発部にお問い合わせください。(1)", NamedTextColor.GREEN)
                );
                Main.sendMessage(player,
                    Component.text(String.format("successCount: %d / failureCount: %d / matchTime: %d",
                        successCount,
                        failureCount,
                        matchTime
                    ), NamedTextColor.GREEN)
                );
            }
            ResultSet res_genkey = statement.getGeneratedKeys();
            int id = -1;
            if (res_genkey != null && res_genkey.next()) {
                id = res_genkey.getInt(1);
            } else {
                Main.sendMessage(player,
                    Component.text("データの登録に失敗した恐れがあります。開発部にお問い合わせください。(2)", NamedTextColor.GREEN)
                );
                Main.sendMessage(player,
                    Component.text(String.format("successCount: %d / failureCount: %d / matchTime: %d",
                        successCount,
                        failureCount,
                        matchTime
                    ), NamedTextColor.GREEN)
                );
            }
            Main.sendMessage(player,
                Component.text("お疲れさまでした！", NamedTextColor.GREEN)
            );

            int ranking = getRanking(matchTime, id);

            Main.broadcast(Component.text(
                String.format("%sさんのピリオドマッチ(%d秒部門)が終了しました。",
                    player.getName(),
                    matchTime
                ), NamedTextColor.GREEN));

            Main.broadcast(Component.text(
                String.format("成功回数: %d / 失敗回数: %d / 純マッチタイム: %d",
                    successCount,
                    failureCount,
                    endTime - startTime), NamedTextColor.GREEN));

            if (rankingSecs.contains(matchTime)) {
                Main.broadcast(Component.text(
                    String.format("順位: %d位", ranking), NamedTextColor.GREEN));
                Main.broadcast(Component.text().append(
                    Component.text("ランキングはこちらからご覧ください: ", NamedTextColor.GREEN),
                    Component.text("https://jaoafa.com/p/")
                        .hoverEvent(HoverEvent.showText(Component.text("クリックすると「https://jaoafa.com/p/」をブラウザで開きます。")))
                        .clickEvent(ClickEvent.openUrl("https://jaoafa.com/p/"))
                ).build());
            }

            if (Main.getDiscord() != null) {
                Main.getDiscord().sendMessage("597423199227084800",
                    "[PeriodMatch2] " + player.getName() + "さんのピリオドマッチ(" + matchTime + "秒部門)が終了しました。");
                Main.getDiscord().sendMessage("597423199227084800",
                    "[PeriodMatch2] 成功回数: " + successCount + " / 失敗回数: " + failureCount);
                Main.getDiscord().sendMessage("597423199227084800",
                    "[PeriodMatch2] 順位: " + ranking + "位");
            }
        } catch (SQLException e) {
            Main.broadcast(Component.text(
                "データベースサーバに接続できなかったか、操作に失敗しました。開発部にお問い合わせください。", NamedTextColor.GREEN
            ));
            Main.broadcast(Component.text(
                String.format("%d - %s", e.getErrorCode(), e.getMessage()), NamedTextColor.GREEN
            ));
            Main.broadcast(Component.text(
                String.format("successCount: %d / failureCount: %d / matchTime: %d", successCount, failureCount, matchTime), NamedTextColor.GREEN
            ));
            Main.broadcast(Component.text(
                String.format("startTime: %d / endTime: %d", startTime, endTime), NamedTextColor.GREEN
            ));
            e.printStackTrace();
        }
    }

	int getRanking(int matchTime, int id) throws SQLException {
		MySQLDBManager sqlmanager = Main.getMySQLDBManager();
		Connection conn = sqlmanager.getConnection();
        try (PreparedStatement statement = conn.prepareStatement(
            "SELECT *, (success*(success/(success+failure))-failure)/calc_match_time AS calc FROM periodmatch2 WHERE match_time = ? ORDER BY calc DESC;")) {
            statement.setInt(1, matchTime);
            try (ResultSet res = statement.executeQuery()) {
                int rank = 1;
                double oldCalc = Double.MIN_VALUE;
                while (res.next()) {
                    if (res.getInt("id") == id) {
                        break;
                    }
                    double calc = res.getDouble("calc");
                    rank++;
                    if (oldCalc != Double.MIN_VALUE && oldCalc == calc)
                        rank--;
                    oldCalc = calc;
                }
                return rank;
            }
        }
	}
}
