package me.libelula.pb;

import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;


public class ParticleManager {
	private final Plugin plugin;

	public ParticleManager(Plugin plugin) {
		this.plugin = plugin;
	}

	public void ShowParticlesonEdges(final BlockVector3 min, final BlockVector3 max, final World w) {

		final int x1 = min.getBlockX();
		final int x2 = max.getBlockX();
		final int y1 = min.getBlockY();
		final int y2 = max.getBlockY();
		final int z1 = min.getBlockZ();
		final int z2 = max.getBlockZ();

		for (int i = 0; i < 6; i++) {
			Bukkit.getScheduler().runTaskLater(plugin, () -> {
				for (double i1 = y1; i1 < y2; i1 += 1.0) {
					w.spawnParticle(Particle.VILLAGER_HAPPY, new Location(w, x1 + 0.5D, i1, z1 + 0.5D), 1);
					w.spawnParticle(Particle.VILLAGER_HAPPY, new Location(w, x1 + 0.5D, i1, z2 + 0.5D), 1);
					w.spawnParticle(Particle.VILLAGER_HAPPY, new Location(w, x2 + 0.5D, i1, z1 + 0.5D), 1);
					w.spawnParticle(Particle.VILLAGER_HAPPY, new Location(w, x2 + 0.5D, i1, z2 + 0.5D), 1);
					if (i1 % 5.0D == 0.0D) {
						for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
							w.spawnParticle(Particle.VILLAGER_HAPPY, new Location(w, x + 0.5D, i1, z1 + 0.5D), 1);
							w.spawnParticle(Particle.VILLAGER_HAPPY, new Location(w, x + 0.5D, i1, z2 + 0.5D), 1);
						}
						for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
							w.spawnParticle(Particle.VILLAGER_HAPPY, new Location(w, x1 + 0.5D, i1, z + 0.5D), 1);
							w.spawnParticle(Particle.VILLAGER_HAPPY, new Location(w, x2 + 0.5D, i1, z + 0.5D), 1);
						}
					}
				}
			}, 20L);
		}

	}

}
