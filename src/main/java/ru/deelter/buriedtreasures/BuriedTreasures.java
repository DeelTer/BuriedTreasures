package ru.deelter.suspiciousgraves;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class SuspiciousGraves extends JavaPlugin {

	@Override
	public void onEnable() {
		saveDefaultConfig();

		PluginManager pluginManager = Bukkit.getPluginManager();
		pluginManager.registerEvents(new SuspiciousBlockFormListener(this), this);
		pluginManager.registerEvents(new SuspiciousBlockBreakListener(this), this);
	}

	@Override
	public void onDisable() {
		// Plugin shutdown logic
	}
}
