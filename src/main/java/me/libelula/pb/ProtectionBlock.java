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

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @author Diego D'Onofrio <ddonofrio@member.fsf.org>
 */
public class ProtectionBlock implements Comparable<ProtectionBlock> {

    private final LibelulaProtectionBlocks plugin;
    private Location location;
    private UUID uuid;
    private ItemStack is;
    private UUID playerUUID;
    private int sizeX;
    private int sizeY;
    private int sizeZ;
    private BlockVector3 min;
    private BlockVector3 max;
    private String playerName;
    private List<String> loreText;
    private String name;
    private ProtectedCuboidRegion pcr;
    private World world;
    private String pcrId;

    private final TextManager tm;

    public ProtectionBlock(LibelulaProtectionBlocks plugin) {
        this.plugin = plugin;
        uuid = UUID.randomUUID();
        tm = plugin.getTextManager();
    }

    @Override
    public int compareTo(ProtectionBlock o) {
        return o.getUuid().compareTo(uuid);
    }

    public void setLocation(Location location) {
        this.location = location;
        if (location != null) {
            setBlockVectors();
            world = location.getWorld();
            if (this.pcr == null) {
                this.pcr = plugin.getWG().getProtectedRegion(this);
            }
        } else {
            max = null;
            min = null;
            pcr = null;
            world = null;
        }
    }

    public Location getLocation() {
        return location;
    }

    @SuppressWarnings("unused")
    public void setPcr(ProtectedCuboidRegion pcr) {
        this.pcr = pcr;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isPlaced() {
        return location != null;
    }

    public void setItemStack(ItemStack itemStack) {
        this.is = itemStack;
    }

    public ItemStack getItemStack() {
        return is;
    }

    public Material getMaterial() {
        return is.getType();
    }

    public void setPlayerUUID(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(playerUUID);
    }

    public void setSizeX(int sizeX) {
        this.sizeX = sizeX;
    }

    public void setSizeY(int sizeY) {
        this.sizeY = sizeY;
    }

    public void setSizeZ(int sizeZ) {
        this.sizeZ = sizeZ;
    }

    @SuppressWarnings("unused")
    public int getSizeX() {
        return sizeX;
    }

    @SuppressWarnings("unused")
    public int getSizeY() {
        return sizeY;
    }

    @SuppressWarnings("unused")
    public int getSizeZ() {
        return sizeZ;
    }

    private void setBlockVectors() {
        int minX = location.getBlockX() - ((sizeX - 1) / 2);
        int minY = location.getBlockY() - ((sizeY - 1) / 2);
        int minZ = location.getBlockZ() - ((sizeZ - 1) / 2);
        int maxX = location.getBlockX() + ((sizeX - 1) / 2);
        int maxY = location.getBlockY() + ((sizeY - 1) / 2);
        int maxZ = location.getBlockZ() + ((sizeZ - 1) / 2);
        if (minY < 0) {
            minY = 0;
        }
        if(location.getWorld() != null) {
            if (maxY > location.getWorld().getMaxHeight()) {
                maxY = location.getWorld().getMaxHeight();
            }
        }else{
            maxY = 256; //default height value
        }
        this.min = BlockVector3.at(minX, minY, minZ);
        this.max = BlockVector3.at(maxX, maxY, maxZ);

    }

    public String getRegionName() {
        String result;
        if (pcr != null) {
            result = pcr.getId();
        } else if (pcrId != null) {
            result = pcrId;
        } else {
            result = "lpb-" + location.getBlockX() + "x" + location.getBlockY() + "y" + location.getBlockZ() + "z";
        }
        return result;
    }

    public BlockVector3 getMin() {
        return min;
    }

    public BlockVector3 getMax() {
        return max;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    @SuppressWarnings("unused")
    public List<String> getLoreText() {
        return loreText;
    }

    public void setLoreText(List<String> loreText) {
        this.loreText = loreText;
        if (is != null) {
            ItemMeta dtMeta = is.getItemMeta();
            if(dtMeta != null) {
                dtMeta.setLore(loreText);
                is.setItemMeta(dtMeta);
            }
        }
    }

    @SuppressWarnings("unused")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public ProtectedCuboidRegion getPcr() {
        return pcr;
    }

    public World getWorld() {
        return world;
    }

    public List<String> getInfo(World w) {
        List<String> result = new ArrayList<>();
        if (location != null) {
            result.add(tm.getText("pb_info_header"));
            result.add(tm.getText("pb_info_name", name));
            result.add(tm.getText("pb_info_creator_text", loreText.get(0)));
            result.add(tm.getText("region_info_header", loreText.get(0)));
            result.add(tm.getText("region_info_title", getRegionName()));

            result.add(tm.getText("region_info_flags", StringFromMap(pcr.getFlags())));

            if (!pcr.getOwners().getPlayers().isEmpty()) {
                result.add(tm.getText("region_info_owners", PlayersFromList(pcr.getOwners().getPlayers())));
            } else {
                result.add(tm.getText("region_info_owners", ChatColor.ITALIC + tm.getText("no_players")));
            }
            if (!pcr.getMembers().getPlayers().isEmpty()) {
                result.add(tm.getText("region_info_members", PlayersFromList(pcr.getMembers().getPlayers())));
            } else {
                result.add(tm.getText("region_info_members", ChatColor.ITALIC + tm.getText("no_players")));
            }
            result.add(tm.getText("region_info_bounds", min.getBlockX(), min.getBlockY(), min.getBlockZ(),
                    max.getBlockX(), max.getBlockY(), max.getBlockZ()));
        }
        plugin.getParticleManager().ShowParticlesonEdges(min, max, w);
        return result;
    }

    private String StringFromMap(Map<Flag<?>, Object> map) {
        String result = "";
        int i = 1;
        for (Flag f : map.keySet()) {
            result = result + ChatColor.GOLD + f.getName() + ": " + ChatColor.YELLOW + map.get(f).toString();
            if (i < map.size())
                result = result + ChatColor.WHITE + ", ";
            i++;
        }
        return result;
    }

    private String PlayersFromList(Set<String> set) {
        String result = "";
        int i = 0;
        for (String p : set) {
            result = result + ChatColor.GOLD + p;

            if (i != 0 && i < set.size() - 1)
                result = result + ChatColor.WHITE + ", ";
            i++;
        }

        return result;
    }

    public static boolean validateMaterial(Material mat) {
        //Material has to be a block, has to be solid, shall not have gravitiy and should not be edible.
        return !(!mat.isBlock() || !mat.isSolid() || mat.hasGravity() || mat.isEdible() || mat.isAir()
                || mat == Material.DIRT || mat == Material.GRASS_BLOCK || mat == Material.ICE || mat == Material.SNOW_BLOCK
                || mat == Material.CACTUS || mat.name().contains("PISTON")
                || mat == Material.FURNACE || mat == Material.MYCELIUM || mat.name().contains("LEAVES")
                || mat == Material.SPRUCE_LEAVES
                || mat == Material.HEAVY_WEIGHTED_PRESSURE_PLATE || mat == Material.LIGHT_WEIGHTED_PRESSURE_PLATE
                || mat == Material.SPONGE || mat == Material.TNT);
    }

    public ConfigurationSection getConfigurationSection() {
        ConfigurationSection result = new YamlConfiguration().createSection(uuid.toString());
        result.set("name", name);
        if (location != null) {
            result.set("placed", YamlUtils.getSection(location));
            if (min != null) {
                result.set("bounds.min", YamlUtils.getSection(min));
            }
            if (max != null) {
                result.set("bounds.max", YamlUtils.getSection(max));
            }
        }

        if (uuid != null) {
            result.set("uuid", uuid.toString());
        }
        if (is != null) {
            result.set("item.material", is.getType().toString());
            if (loreText != null) {
                result.set("item.lore", loreText);
            }
        }
        if (playerUUID != null) {
            result.set("owner.uuid", playerUUID.toString());
        }
        if (playerName != null) {
            result.set("owner.name", playerName);
        }
        result.set("size.X", sizeX);
        result.set("size.Y", sizeY);
        result.set("size.Z", sizeZ);

        return result;
    }

    public void load(ConfigurationSection cs) {
        name = cs.getString("name");
        String uuidString = cs.getString("uuid");
        String ownerUuidString = cs.getString("owner.uuid");
        if (ownerUuidString != null) {
            playerUUID = UUID.fromString(ownerUuidString);
        }
        playerName = cs.getString("owner.name");
        sizeX = cs.getInt("size.X");
        sizeY = cs.getInt("size.Y");
        sizeZ = cs.getInt("size.Z");
        min = YamlUtils.getBlockVector(cs.getConfigurationSection("bounds.min"));
        max = YamlUtils.getBlockVector(cs.getConfigurationSection("bounds.max"));
        if (uuidString != null) {
            uuid = UUID.fromString(uuidString);
        }
        if (cs.contains("placed")) {
            setLocation(YamlUtils.getLocation(cs.getConfigurationSection("placed"), plugin.getServer()));
        }
        Material mat = Material.getMaterial(cs.getString("item.material"));
        if (mat != null) {
            ItemStack isAux = new ItemStack(mat, 1);
            this.is = isAux;
            setLoreText(cs.getStringList("item.lore"));
            ItemMeta itemMeta = is.getItemMeta();
            itemMeta.setDisplayName(name);
            is.setItemMeta(itemMeta);
        }
    }

    public void setPcrId(String pcrID) {
        this.pcrId = pcrID;
    }

    public void removeRegion() {
        if (location != null) {
            Bukkit.getScheduler().runTask(plugin, new Runnable() {
                @Override
                public void run() {
                    location.getBlock().setType(Material.AIR);
                }
            });
        }
        if (pcr != null) {
            plugin.getWG().removeRegion(this);
        }
    }
}
