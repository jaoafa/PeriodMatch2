package com.jaoafa.PeriodMatch2;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.jaoafa.PeriodMatch2.Command.Cmd_PeriodMatch2;
import com.jaoafa.PeriodMatch2.Command.Period;
import com.jaoafa.PeriodMatch2.Event.PeriodFailureCounter;
import com.jaoafa.PeriodMatch2.Event.PeriodSuccessCounter;
import com.jaoafa.PeriodMatch2.Lib.Discord;
import com.jaoafa.PeriodMatch2.Lib.MySQLDBManager;

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

		getCommand(".").setExecutor(new Period(this));
		getCommand("periodmatch2").setExecutor(new Cmd_PeriodMatch2());
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
}
