package com.TechnicJelle.BlueMapTimeMarker;

import com.technicjelle.MCUtils.ConfigUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Objects;

public class Config {
	public static final String MARKER_SET_ID = "time-marker";
	public final String markerSetName;
	public final boolean toggleable;
	public final boolean defaultHidden;
	public final boolean inWorld;
	public final double x;
	public final double y;
	public final double z;
	public final String html;

	public Config(JavaPlugin plugin) {
		try {
			ConfigUtils.copyPluginResourceToConfigDir(plugin, "config.yml", "config.yml", false);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		//Load config from disk
		plugin.reloadConfig();

		//Load config values into variables
		markerSetName = plugin.getConfig().getString("MarkerSetName", "Time Marker");
		toggleable = plugin.getConfig().getBoolean("Toggleable", true);
		defaultHidden = plugin.getConfig().getBoolean("DefaultHidden", false);
		inWorld = plugin.getConfig().getBoolean("InWorld", false);
		x = plugin.getConfig().getDouble("x", 0.5);
		y = plugin.getConfig().getDouble("y", 64);
		z = plugin.getConfig().getDouble("z", 0.5);
		html = Objects.requireNonNullElse(plugin.getConfig().getString("HTML"), "{{HH}}:{{MM}}");
	}
}
