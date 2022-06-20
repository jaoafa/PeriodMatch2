package com.jaoafa.periodmatch2;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.jaoafa.periodmatch2.command.Cmd_PeriodMatch2;
import com.jaoafa.periodmatch2.command.Period;
import com.jaoafa.periodmatch2.event.PeriodFailureCounter;
import com.jaoafa.periodmatch2.event.PeriodSuccessCounter;
import com.jaoafa.periodmatch2.lib.Discord;
import com.jaoafa.periodmatch2.lib.MySQLDBManager;

import java.util.Objects;

public class Main extends JavaPlugin {
	static MySQLDBManager sqlmanager;
	static JavaPlugin javaplugin;
	static Discord discord = null;

	/**
	 * プラグインが起動したときに呼び出し
	 * @author mine_book000
	 * @since 2020/02/07
	 */
	@Override
	public void onEnable() {
		// クレジット
		getLogger().info("(c) jao Minecraft Server PeriodMatch2 Project.");
		getLogger().info("Product by tomachi.");

		FileConfiguration conf = getConfig();
		if (!conf.contains("sqlserver")) {
			getLogger().warning("sqlserverが定義されていません。プラグインを無効化します。");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		if (!conf.contains("sqlport")) {
			getLogger().warning("sqlportが定義されていません。プラグインを無効化します。");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		if (!conf.contains("sqldatabase")) {
			getLogger().warning("sqldatabaseが定義されていません。プラグインを無効化します。");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		if (!conf.contains("sqlusername")) {
			getLogger().warning("sqlusernameが定義されていません。プラグインを無効化します。");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		if (!conf.contains("sqlpassword")) {
			getLogger().warning("sqlpasswordが定義されていません。プラグインを無効化します。");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		try {
			sqlmanager = new MySQLDBManager(
					conf.getString("sqlserver"),
					conf.getString("sqlport"),
					conf.getString("sqldatabase"),
					conf.getString("sqlusername"),
					conf.getString("sqlpassword"));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			getLogger().warning("データベースの接続に失敗しました。プラグインを無効化します。");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		if (!conf.contains("discordtoken")) {
			getLogger().warning("discordtokenが定義されていません。Discordとの連携機能を無効化します。");
		} else {
			discord = new Discord(conf.getString("discordtoken"));
		}

		javaplugin = this;

		Objects.requireNonNull(getCommand(".")).setExecutor(new Period(this));
		Objects.requireNonNull(getCommand("periodmatch2")).setExecutor(new Cmd_PeriodMatch2());
		getServer().getPluginManager().registerEvents(new PeriodSuccessCounter(this), this);
		getServer().getPluginManager().registerEvents(new PeriodFailureCounter(this), this);
	}

	public static MySQLDBManager getMySQLDBManager() {
		return sqlmanager;
	}

	public static Discord getDiscord() {
		return discord;
	}

	public static JavaPlugin getJavaPlugin() {
		return javaplugin;
	}

	public static void broadcast(Component component){
        Bukkit.getServer().sendMessage(Component.text().append(
            Component.text("[PeriodMatch2]"),
            Component.space(),
            component
        ));
    }

    public static void sendMessage(CommandSender sender, Component component){
        sender.sendMessage(Component.text().append(
            Component.text("[PeriodMatch2]"),
            Component.space(),
            component
        ));
    }
}
