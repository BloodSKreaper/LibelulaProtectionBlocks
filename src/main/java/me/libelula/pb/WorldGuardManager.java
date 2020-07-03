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

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.*;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.logging.Level;

/**
 * Class WorldGuardManager of the wgPlugin.
 *
 * @author Diego D'Onofrio <ddonofrio@member.fsf.org>
 * @version 1.0
 */
public class WorldGuardManager {

    private final LibelulaProtectionBlocks plugin;
    private WorldGuardPlugin wgp;
    private WorldGuard worldguard;
    private RegionContainer container;
    private final TextManager textManager;
    private final TreeMap<Flag<?>, String> defaultKeys;
    private final TreeMap<World, RegionManager> regionManagers;

    public WorldGuardManager(LibelulaProtectionBlocks plugin) {
        this.plugin = plugin;
        this.textManager = plugin.getTextManager();
        this.defaultKeys = new TreeMap<>(new FlagComparator());
        this.regionManagers = new TreeMap<>(new WorldComparator());
    }

    public WorldGuardPlugin getWorldGuardPlugin() {
        return wgp;
    }

    public WorldGuard getWorldGuard() {
        return worldguard;
    }

    public static class FlagComparator implements Comparator<Flag<?>> {

        @Override
        public int compare(Flag<?> flag1, Flag<?> flag2) {
            return flag1.toString().compareTo(flag2.toString());
        }
    }

    public static class WorldComparator implements Comparator<World> {

        @Override
        public int compare(World world1, World world2) {
            return world1.getUID().compareTo(world2.getUID());
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
            Bukkit.getScheduler().runTask(plugin, this::reloadConfig);
        }
    }

    public void reloadConfig() {
        List<String> ignoredWorlds = plugin.getConfig().getStringList("ignored.worlds");
        for (World world : plugin.getServer().getWorlds()) {
            if (ignoredWorlds.contains(world.getName())) {
                plugin.sendMessage(plugin.getConsole(), textManager.getText("world_ignored", world.getName()));
                continue;
            }

            RegionManager regions = container.get(BukkitAdapter.adapt(world));

            if (regions != null) {
                regionManagers.put(world, regions);
                plugin.sendMessage(plugin.getConsole(), textManager.getText("world_loaded", world.getName()));
            } else {
                plugin.sendMessage(plugin.getConsole(), textManager.getText("world_has_no_rm", world.getName()));
            }

        }
        ConfigurationSection cs = plugin.getConfig().getConfigurationSection("ps-default.flags");
        if (regionManagers.isEmpty()) {
            plugin.alert(textManager.getText("no_available_worlds"));
        } else {
            if (cs != null) {
                for (String keyName : cs.getKeys(false)) {
                    switch (keyName) {
                        case "greeting":
                        case "farewell":
                            defaultKeys.put(Flags.fuzzyMatchFlag(worldguard.getFlagRegistry(), keyName),
                                    textManager.getText(cs.getString(keyName)));
                            break;
                        default:
                            if (Flags.fuzzyMatchFlag(worldguard.getFlagRegistry(), keyName) != null) {

                                defaultKeys.put(Flags.fuzzyMatchFlag(worldguard.getFlagRegistry(), keyName),
                                        cs.getString(keyName));

                                plugin.sendMessage(plugin.getConsole(),
                                        textManager.getText("default_flag_set", keyName, cs.getString(keyName)));

                            } else {
                                plugin.sendMessage(plugin.getConsole(),
                                        ChatColor.RED + textManager.getText("invalid_default_flag", keyName, cs.getString(keyName)));

                            }
                    }

                }
            }
        }
    }

    public ProtectedCuboidRegion getProtectedRegion(ProtectionBlock pb) {
        if (pb == null || pb.getLocation() == null || pb.getLocation().getWorld() == null) return null;

        RegionManager regions = container.get(BukkitAdapter.adapt(pb.getLocation().getWorld()));
        ProtectedCuboidRegion pcr = null;
        if (regions != null) {
            pcr = (ProtectedCuboidRegion) regions.getRegion(pb.getRegionName());
            if (pcr == null) {
                pcr = new ProtectedCuboidRegion(pb.getRegionName(), pb.getMin(), pb.getMax());

                // Default flags.
                for (Flag flag : defaultKeys.keySet()) {
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
        if (pb == null || pb.getLocation() == null || pb.getLocation().getWorld() == null) return;
        final RegionManager regions = container.get(BukkitAdapter.adapt(pb.getLocation().getWorld()));
        if (regions != null) {
            regions.addRegion(pb.getPcr());
            BlockVector3 min = pb.getPcr().getMinimumPoint();
            BlockVector3 max = pb.getPcr().getMaximumPoint();
            plugin.getParticleManager().ShowParticlesonEdges(max, min, pb.getWorld());
        }
    }

    public void removeRegion(final ProtectionBlock pb) {
        if (pb == null || pb.getLocation() == null || pb.getLocation().getWorld() == null) return;
        Bukkit.getScheduler().runTask(plugin, () -> {
            final RegionManager regions = container.get(BukkitAdapter.adapt(pb.getLocation().getWorld()));
            if (regions != null) {
                if (pb.getPcr() != null) {
                    regions.removeRegion(pb.getPcr().getId());
                }
                pb.setLocation(null);
            }
        });
    }

    public void setFlag(ProtectedRegion pr, World world, Flag flag, Object value) {
        if (flag instanceof StateFlag) {
            setStateFlag(pr, (StateFlag) flag, value);
            return;
        }
        if (flag instanceof StringFlag) {
            setStringFlag(pr, (StringFlag) flag, value);
            return;
        }
        if (flag instanceof IntegerFlag) {
            setIntegerFlag(pr, (IntegerFlag) flag, value);
            return;
        }
        if (flag instanceof DoubleFlag) {
            setDoubleFlag(pr, (DoubleFlag) flag, value);
            return;
        }
        /*
        LOCATION FLAG NOT SUPPORTED
        if (flag instanceof LocationFlag) {
            setLocationFlag(pr, world, (LocationFlag) flag, value);
            return;
        }*/
        if (flag instanceof BooleanFlag) {
            setBooleanFlag(pr, (BooleanFlag) flag, value);
            return;
        }
        if (flag instanceof SetFlag) {
            setSetFlag(pr, (SetFlag) flag, value);
        }

    }

    public void setStateFlag(ProtectedRegion pr, StateFlag flag, Object value) {
        StateFlag.State state;
        if (value instanceof StateFlag.State) {
            state = (StateFlag.State) value;
        } else {
            if (value instanceof String) {
                String valueString = (String) value;
                valueString = valueString.toUpperCase();
                try {
                    state = StateFlag.State.valueOf(valueString);
                } catch (IllegalArgumentException ex) {
                    plugin.getLogger().log(Level.SEVERE, valueString + " cannot be assigned to StateFlag " + flag.getName());
                    ex.printStackTrace();
                    return;
                }
            } else {
                plugin.getLogger().log(Level.SEVERE, value.toString() + " cannot be assigned to StateFlag " + flag.getName());
                return;
            }
        }
        pr.setFlag(flag, state);
    }

    public void setStringFlag(ProtectedRegion pr, StringFlag flag, Object value) {
        String valueString;
        if (value instanceof String) {
            valueString = (String) value;
        } else {
            plugin.getLogger().log(Level.SEVERE, value.toString() + " cannot be assigned to StringFlag " + flag.getName());
            return;
        }
        pr.setFlag(flag, valueString);
    }

    public void setIntegerFlag(ProtectedRegion pr, IntegerFlag flag, Object value) {
        int valueInteger;
        if (value instanceof Integer) {
            valueInteger = (Integer) value;
        } else {
            if (value instanceof String) {
                try {
                    valueInteger = Integer.parseInt((String) value);
                } catch (NumberFormatException ex) {
                    plugin.getLogger().log(Level.SEVERE, value.toString() + " cannot be assigned to IntegerFlag " + flag.getName());
                    ex.printStackTrace();
                    return;
                }
            } else {
                plugin.getLogger().log(Level.SEVERE, value.toString() + " cannot be assigned to IntegerFlag " + flag.getName());
                return;
            }
        }
        pr.setFlag(flag, valueInteger);
    }

    public void setDoubleFlag(ProtectedRegion pr, DoubleFlag flag, Object value) {
        double valueDouble;
        if (value instanceof Double) {
            valueDouble = (Double) value;
        } else {
            if (value instanceof String) {
                try {
                    valueDouble = Double.parseDouble((String) value);
                } catch (NumberFormatException ex) {
                    plugin.getLogger().log(Level.SEVERE, value.toString() + " cannot be assigned to DoubleFlag " + flag.getName());
                    ex.printStackTrace();
                    return;
                }
            } else {
                plugin.getLogger().log(Level.SEVERE, value.toString() + " cannot be assigned to DoubleFlag " + flag.getName());
                return;
            }
        }
        pr.setFlag(flag, valueDouble);
    }
    /*
    !!!!LOCATION FLAG NOT SUPPORTED!!!!

    public void setLocationFlag(ProtectedRegion pr, World world, LocationFlag flag, Object value) {
        pr.setFlag(flag, value);
    }*/

    public void setBooleanFlag(ProtectedRegion pr, BooleanFlag flag, Object value) {
        boolean valueBoolean;
        if (value instanceof Boolean) {
            valueBoolean = (Boolean) value;
        } else {
            if (value instanceof String) {
                String valueString = (String) value;
                if (valueString.equalsIgnoreCase("true")) {
                    valueBoolean = true;
                } else if (valueString.equalsIgnoreCase("false")) {
                    valueBoolean = false;
                } else {
                    plugin.getLogger().log(Level.SEVERE, value.toString() + " cannot be assigned to BooleanFlag " + flag.getName());
                    return;
                }
            } else {
                plugin.getLogger().log(Level.SEVERE, value.toString() + " cannot be assigned to BooleanFlag " + flag.getName());
                return;
            }
        }
        pr.setFlag(flag, valueBoolean);
    }

    /* OH NO - DANGER AHEAD*/
    public void setSetFlag(ProtectedRegion pr, SetFlag flag, Object value) {
        if (flag.getName().equalsIgnoreCase("deny-spawn")) {
            HashSet<com.sk89q.worldedit.world.entity.EntityType> data = new HashSet<>();
            if (value instanceof String) {
                String valueString = ((String) value).toUpperCase();
                String[] valueSplitted = valueString.split(",");
                for (String splitter : valueSplitted) {
                    splitter = splitter.trim();
                    try {
                        EntityType type = EntityType.valueOf(splitter);
                        data.add(BukkitAdapter.adapt(type));
                    } catch (IllegalArgumentException ex) {
                        plugin.getLogger().log(Level.SEVERE, splitter + " is no valid entity and cannot be added to " + flag.getName());
                    }
                }
            } else {
                plugin.getLogger().log(Level.SEVERE, value.toString() + " cannot be assigned to SetFlag<Entity> " + flag.getName());
            }
            pr.setFlag(flag, data);
        } else if (flag.getName().

                equalsIgnoreCase("blocked-cmds") || flag.getName().

                equalsIgnoreCase("allowed-cmds")) {
            Set<String> data = new HashSet<>();
            if (value instanceof String) {
                String valueString = (String) value;
                String[] valueSplitted = valueString.split(", ");
                Collections.addAll(data, valueSplitted);

            } else {
                plugin.getLogger().log(Level.SEVERE, value.toString() + " cannot be assigned to SetFlag<String> " + flag.getName());
            }
            pr.setFlag(flag, data);
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
        Bukkit.getScheduler().runTask(plugin, () -> {
            DefaultDomain dd = pr.getMembers();
            dd.addPlayer(playerName);
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
