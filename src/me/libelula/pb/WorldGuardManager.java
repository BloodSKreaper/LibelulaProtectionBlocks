/*
 *            This file is part of  LibelulaProtectionBlocks.
 *
 *   LibelulaProtectionBlocks is free software: you can redistribute it and/or 
 *  modify it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *   LibelulaProtectionBlocks is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with  LibelulaProtectionBlocks. 
 *  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package me.libelula.pb;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

/**
 * Class WorldGuardManager of the wgPlugin.
 *
 * @author Diego D'Onofrio <ddonofrio@member.fsf.org>
 * @version 1.0
 */
public class WorldGuardManager {

	private final Main plugin;
	public WorldGuardPlugin wgp;
	public WorldGuard worldguard;
	private RegionContainer container;
	private final TextManager tm;
	private final TreeMap<Flag<?>, String> defaultKeys;
	private final TreeMap<World, RegionManager> regionManagers;

	public WorldGuardManager(Main plugin) {
		this.plugin = plugin;
		this.tm = plugin.tm;
		this.defaultKeys = new TreeMap<>(new FlagComparator());
		this.regionManagers = new TreeMap<>(new WorldComparator());
	}

	public static class FlagComparator implements Comparator<Flag<?>> {

		@Override
		public int compare(Flag<?> o1, Flag<?> o2) {
			return o1.toString().compareTo(o2.toString());
		}
	}

	public static class WorldComparator implements Comparator<World> {

		@Override
		public int compare(World o1, World o2) {
			return o1.getUID().compareTo(o2.getUID());
		}

	}

	public boolean isWorldGuardActive() {
		return worldguard != null;
	}

	public void initialize() {
		Plugin wgPlugin;
		wgPlugin = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
		wgp = (WorldGuardPlugin) wgPlugin;
		if (wgPlugin == null) {
			this.worldguard = null;
		} else {

			worldguard = WorldGuard.getInstance();
			container = worldguard.getPlatform().getRegionContainer();
			Bukkit.getScheduler().runTask(plugin, new Runnable() {
				@Override
				public void run() {
					reloadConfig();
				}
			});
		}
	}

	public void reloadConfig() {
		List<String> ignoredWorlds = plugin.getConfig().getStringList("ignored.worlds");
		for (World world : plugin.getServer().getWorlds()) {
			if (ignoredWorlds.contains(world.getName())) {
				plugin.sendMessage(plugin.getConsole(), tm.getText("world_ignored", world.getName()));
				continue;
			}

			RegionManager regions = container.get(BukkitAdapter.adapt(world));

			if (regions != null) {
				regionManagers.put(world, regions);
				plugin.sendMessage(plugin.getConsole(), tm.getText("world_loaded", world.getName()));
			} else {
				plugin.sendMessage(plugin.getConsole(), tm.getText("world_has_no_rm", world.getName()));
			}

		}
		ConfigurationSection cs = plugin.getConfig().getConfigurationSection("ps-default.flags");
		if (regionManagers.isEmpty()) {
			plugin.alert(tm.getText("no_available_worlds"));
		} else {
			if (cs != null) {
				for (String keyName : cs.getKeys(false)) {
					switch (keyName) {
					case "greeting":
					case "farewell":
						defaultKeys.put(Flags.fuzzyMatchFlag(worldguard.getFlagRegistry(), keyName),
								tm.getText(cs.getString(keyName)));
						break;
					default:
						if (Flags.fuzzyMatchFlag(worldguard.getFlagRegistry(), keyName) != null) {

							defaultKeys.put(Flags.fuzzyMatchFlag(worldguard.getFlagRegistry(), keyName),
									cs.getString(keyName));

							plugin.sendMessage(plugin.getConsole(),
									tm.getText("default_flag_set", keyName, cs.getString(keyName)));

						} else {
							plugin.sendMessage(plugin.getConsole(),
									ChatColor.RED + tm.getText("invalid_default_flag", keyName, cs.getString(keyName)));

						}
					}

				}
			}
		}
	}

	public ProtectedCuboidRegion getProtectedRegion(ProtectionBlock pb) {

		RegionManager regions = container.get(BukkitAdapter.adapt(pb.getLocation().getWorld()));
		ProtectedCuboidRegion pcr = null;
		if (regions != null) {
			pcr = (ProtectedCuboidRegion) regions.getRegion(pb.getRegionName());
			if (pcr == null) {
				pcr = new ProtectedCuboidRegion(pb.getRegionName(), pb.getMin(), pb.getMax());

				// Default flags.
				for (@SuppressWarnings("rawtypes")
				Flag flag : defaultKeys.keySet()) {
					String value = defaultKeys.get(flag);
					switch (flag.getName()) {
					case "greeting":
					case "farewell":
						value = value.replaceAll("%PLAYER%", pb.getPlayerName());
					}
					setFlag(pcr, pb.getWorld(), flag, value);
				}
				DefaultDomain dd = new DefaultDomain();
				dd.addPlayer(pb.getPlayerUUID());
				pcr.setOwners(dd);
			}
		}
		return pcr;
	}

	public void createRegion(final ProtectionBlock pb) {
		final RegionManager regions = container.get(BukkitAdapter.adapt(pb.getLocation().getWorld()));
		if (regions != null) {
			Bukkit.getScheduler().runTask(plugin, new Runnable() {
				@Override
				public void run() {
					regions.addRegion(pb.getPcr());
					Vector min = pb.getPcr().getMinimumPoint();
					Vector max = pb.getPcr().getMaximumPoint();
					plugin.pam.ShowParticlesonEdges(max, min, pb.getWorld());

					try {
						regions.save();
					} catch (StorageException ex) {
						plugin.alert(tm.getText("unexpected_error", ex.getMessage()));
					}
				}
			});
		}
	}

	public void removeRegion(final ProtectionBlock pb) {
		Bukkit.getScheduler().runTask(plugin, new Runnable() {
			@Override
			public void run() {
				final RegionManager regions = container.get(BukkitAdapter.adapt(pb.getLocation().getWorld()));
				if (regions != null) {
					Bukkit.getScheduler().runTask(plugin, new Runnable() {
						@Override
						public void run() {
							if (pb.getPcr() != null) {
								regions.removeRegion(pb.getPcr().getId());
							}
							pb.setLocation(null);
							try {
								regions.save();
							} catch (StorageException ex) {
								plugin.alert(tm.getText("unexpected_error", ex.getMessage()));
							}
						}
					});
				}
			}
		});
	}

	// CAUTION: UNTESTED AND UNSAFE CODE HERE
	public void setFlag(ProtectedRegion pr, World world, Flag flag, Object value) {
		String str = value.toString();
		if (str.equals("deny")) {
			pr.setFlag(flag, StateFlag.State.DENY);
			return;
		} else if (str.equals("allow")) {
			pr.setFlag(flag, StateFlag.State.ALLOW);
			return;
		} else if (str.contains(",")) {
			String[] splitted = str.split(", ");
			if (flag.getName().equalsIgnoreCase("deny-spawn")) {
				Set<EntityType> data = new HashSet<EntityType>(splitted.length);
				for (String s : splitted) {
					for (EntityType e : EntityType.values()) {
						if (e.getName().equalsIgnoreCase(s)) {
							data.add(e);
							return;
						} else {

						}
					}
				}
				pr.setFlag(flag, data);
			} else {
				plugin.getLogger().log(Level.WARNING,
						"Flag " + flag.getName() + " kann nicht verarbeitet werden. (" + value.toString() + ")");
			}

		} else {
			pr.setFlag(flag, value);
		}
	}

	public boolean overlapsUnownedRegion(ProtectedRegion region, Player player) {
		RegionManager regions = container.get(BukkitAdapter.adapt(player.getWorld()));
		if (regions != null) {
			return regions.overlapsUnownedRegion(region, wgp.wrapPlayer(player));
		} else {
			return false;
		}

	}

	public void addMemberPlayer(final ProtectedRegion pr, final String playerName) {
		Bukkit.getScheduler().runTask(plugin, new Runnable() {
			@Override
			public void run() {
				DefaultDomain dd = pr.getMembers();
				dd.addPlayer(playerName);
			}
		});
	}

	public boolean delMemberPlayer(ProtectedRegion pr, String playerName) {
		boolean result = true;
		DefaultDomain dd = pr.getMembers();
		if (dd.contains(playerName)) {
			dd.removePlayer(playerName);
		} else {
			result = false;
		}
		return result;
	}

	public TreeMap<World, RegionManager> getRegionManagers() {
		return regionManagers;
	}

	public Set<String> getRegionsIDs(World world) {
		return regionManagers.get(world).getRegions().keySet();
	}

	public ProtectedRegion getPcr(World world, String id) {
		return regionManagers.get(world).getRegion(id);
	}
}
