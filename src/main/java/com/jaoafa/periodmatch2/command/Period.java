package com.jaoafa.periodmatch2.command;

import com.jaoafa.periodmatch2.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.jaoafa.periodmatch2.PeriodMatchPlayer;
import org.jetbrains.annotations.NotNull;

public class Period implements CommandExecutor {
	JavaPlugin plugin;

	public Period(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String commandLabel, String[] args) {
		if (!(sender instanceof Player)) {
			// プレイヤー以外からのコマンド実行
            Main.sendMessage(sender,
                Component.text("このコマンドはゲーム内でのみ使用できます。", NamedTextColor.GREEN)
            );
			return true;
		}
		Player player = (Player) sender;
		PeriodMatchPlayer pmplayer = PeriodMatchPlayer.getPeriodMatchPlayer(player);
		if (args.length == 0) {
			if (pmplayer.isPerioding()) {
                Main.sendMessage(sender,
                    Component.text(String.format("現在あなたはピリオドマッチを%d秒で行っています。", pmplayer.getMatchTime()), NamedTextColor.GREEN)
                );
                Main.sendMessage(sender,
                    Component.text("「/. stop」で行っているピリオドマッチを強制終了できます。", NamedTextColor.GREEN)
                );
				return true;
			} else if (pmplayer.isWaiting()) {
                Main.sendMessage(sender,
                    Component.text(String.format("現在あなたはピリオドマッチを%d秒で待機しています。", pmplayer.getMatchTime()), NamedTextColor.GREEN)
                );
                Main.sendMessage(sender,
                    Component.text("次に「.」と打った瞬間から開始します。", NamedTextColor.GREEN)
                );
                Main.sendMessage(sender,
                    Component.text("「/. stop」で行っているピリオドマッチを強制終了できます。", NamedTextColor.GREEN)
                );
				return true;
			}

			// デフォルト値(30秒)で開始
			pmplayer.setMatchTime(60);
			pmplayer.setWaiting(true);
            Main.sendMessage(sender,
                Component.text("ピリオドマッチを開始します。次に「.」を打った瞬間から60秒間計測します。", NamedTextColor.GREEN)
            );
            Main.sendMessage(sender,
                Component.text("ピリオドマッチを開始します。次に「.」を打った瞬間から60秒間計測します。", NamedTextColor.GREEN)
            );
            Main.sendMessage(sender,
                Component.text("キー設定はデフォルトで実施してください。また外部ツールなどの利用を厳しく禁止します。", NamedTextColor.GREEN)
            );
			return true;
		} else if (args.length == 1) {
			if (args[0].equalsIgnoreCase("stop")) {
				pmplayer.forceEnd();
				return true;
			} else if (isNumber(args[0])) {
				if (pmplayer.isPerioding()) {
                    Main.sendMessage(sender,
                        Component.text("現在あなたはピリオドマッチを実施中です。強制終了するには「/. stop」を実行してください。", NamedTextColor.GREEN)
                    );
					return true;
				}
				if (pmplayer.isWaiting()) {
                    Main.sendMessage(sender,
                        Component.text("現在あなたはピリオドマッチを待機中です。強制終了するには「/. stop」を実行してください。", NamedTextColor.GREEN)
                    );
					return true;
				}
				int sec = Integer.parseInt(args[0]);
				pmplayer.setMatchTime(sec);
				pmplayer.setWaiting(true);
                Main.sendMessage(sender,
                    Component.text(String.format("ピリオドマッチを開始します。次に「.」を打った瞬間から%d秒間計測します。", sec), NamedTextColor.GREEN)
                );
                Main.sendMessage(sender,
                    Component.text("正確にピリオド判定を行うため、かなローマ字変換をオフにして(/jp off)ご利用ください。", NamedTextColor.GREEN)
                );
                Main.sendMessage(sender,
                    Component.text("キー設定はデフォルトで実施してください。また外部ツールなどの利用を厳しく禁止します。", NamedTextColor.GREEN)
                );
				return true;
			}
		}
        Main.sendMessage(sender,
            Component.text("----- Period -----", NamedTextColor.GREEN)
        );
        Main.sendMessage(sender,
            Component.text("/. [TimeSecond]: TimeSecond秒のピリオドマッチを行います。", NamedTextColor.GREEN)
        );
        Main.sendMessage(sender,
            Component.text("ピリオドマッチでは次の秒数のみランキングが公開されます。それ以外の秒数ではランキング付けがされません。", NamedTextColor.GREEN)
        );
        Main.sendMessage(sender,
            Component.text("10秒(/. 10)・60秒(/. 60)・300秒(/. 300)", NamedTextColor.GREEN)
        );
        Main.sendMessage(sender,
            Component.text("/. stop: 既に開始しているピリオドマッチを強制終了します。", NamedTextColor.GREEN)
        );
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
