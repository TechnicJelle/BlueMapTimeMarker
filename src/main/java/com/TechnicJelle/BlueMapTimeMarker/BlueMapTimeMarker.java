package com.TechnicJelle.BlueMapTimeMarker;

import com.technicjelle.UpdateChecker;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.BlueMapWorld;
import de.bluecolored.bluemap.api.markers.HtmlMarker;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public final class BlueMapTimeMarker extends JavaPlugin {
	private Config config;
	private UpdateChecker updateChecker;
	private int bukkitTask = -1;
	private Map<World, MarkerSet> worldMarkerSets = new HashMap<>();

	@Override
	public void onEnable() {
		new Metrics(this, 30913);

		updateChecker = new UpdateChecker("TechnicJelle", "BlueMapTimeMarker", getDescription().getVersion());
		updateChecker.checkAsync();

		BlueMapAPI.onEnable(onEnableListener);
		BlueMapAPI.onDisable(onDisableListener);
	}

	final Consumer<BlueMapAPI> onEnableListener = api -> {
		config = new Config(this);
		updateChecker.logUpdateMessage(getLogger());

		worldMarkerSets = new HashMap<>();
		for (World bukkitWorld : Bukkit.getWorlds()) {
			Optional<BlueMapWorld> oBlueMapWorld = api.getWorld(bukkitWorld);
			if (oBlueMapWorld.isEmpty()) continue;
			BlueMapWorld blueMapWorld = oBlueMapWorld.get();

			MarkerSet markerSet = MarkerSet.builder()
					.label(config.markerSetName)
					.toggleable(config.toggleable)
					.defaultHidden(config.defaultHidden)
					.build();

			for (BlueMapMap map : blueMapWorld.getMaps()) {
				Map<String, MarkerSet> markerSets = map.getMarkerSets();
				markerSets.put(Config.MARKER_SET_ID, markerSet);
			}

			worldMarkerSets.put(bukkitWorld, markerSet);
		}

		final int seconds = 10; //BlueMap only updates markers every 10 seconds, so updating faster than this is futile.
		bukkitTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, this::addTimeMarkers, 0, seconds * 20);
	};

	private void addTimeMarkers() {
		Optional<BlueMapAPI> oApi = BlueMapAPI.getInstance();
		if (oApi.isEmpty()) return;

		worldMarkerSets.forEach((world, markerSet) -> {
			long time = world.getTime();
			String html = config.html
					.replace("{{ticks}}", Long.toString(time))
					.replace("{{HH}}", hoursFromTicks(time))
					.replace("{{MM}}", minutesFromTicks(time));

			HtmlMarker marker = HtmlMarker.builder()
					.label(config.markerSetName)
					.position(config.x, config.y, config.z)
					.html(html)
					.build();

			markerSet.put(Config.MARKER_SET_ID, marker);
		});
	}

	private static String hoursFromTicks(long ticks) {
		double hours = (ticks / 1000.0 + 6.0) % 24.0;
		return String.format("%02d", (int) hours);
	}

	private static String minutesFromTicks(long ticks) {
		double minutes = (ticks % 1000.0) / 1000.0 * 60.0;
		return String.format("%02d", (int) minutes);
	}

	final Consumer<BlueMapAPI> onDisableListener = api -> {
		Bukkit.getScheduler().cancelTask(bukkitTask);
	};

	@Override
	public void onDisable() {
		Bukkit.getScheduler().cancelTask(bukkitTask);
	}
}
