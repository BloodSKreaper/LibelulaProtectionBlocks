package me.libelula.pb;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.Vector;

public class ParticleManager {
	private Plugin plugin;

	public ParticleManager(Plugin plugin) {
		this.plugin = plugin;
	}

	public void ShowParticlesonEdges(Vector min, Vector max, final World w) {

		final int x1 = min.getBlockX();
		final int x2 = max.getBlockX();
		final int z1 = min.getBlockZ();
		final int z2 = max.getBlockZ();

		for (int i = 0; i < 6; i++) {
			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
				@Override
				public void run() {
					for (double i = 0.0D; i < w.getMaxHeight(); i += 1.0D) {
						w.spawnParticle(Particle.VILLAGER_HAPPY, new Location(w, x1 + 0.5D, i, z1 + 0.5D), 1);
						w.spawnParticle(Particle.VILLAGER_HAPPY, new Location(w, x1 + 0.5D, i, z2 + 0.5D), 1);
						w.spawnParticle(Particle.VILLAGER_HAPPY, new Location(w, x2 + 0.5D, i, z1 + 0.5D), 1);
						w.spawnParticle(Particle.VILLAGER_HAPPY, new Location(w, x2 + 0.5D, i, z2 + 0.5D), 1);
						if (i % 5.0D == 0.0D) {
							for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
								w.spawnParticle(Particle.VILLAGER_HAPPY, new Location(w, x + 0.5D, i, z1 + 0.5D), 1);
								w.spawnParticle(Particle.VILLAGER_HAPPY, new Location(w, x + 0.5D, i, z2 + 0.5D), 1);
							}
							for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
								w.spawnParticle(Particle.VILLAGER_HAPPY, new Location(w, x1 + 0.5D, i, z + 0.5D), 1);
								w.spawnParticle(Particle.VILLAGER_HAPPY, new Location(w, x2 + 0.5D, i, z + 0.5D), 1);
							}
						}
					}
				}
			}, 20L);
		}

	}

}
