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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * @author Diego D'Onofrio <ddonofrio@member.fsf.org>
 */
public class Shop {

    private final LibelulaProtectionBlocks plugin;
    private final TextManager tm;
    private String signFirstLine;
    private int priceDecimals;
    private String ownerName;

    private final class Listener implements org.bukkit.event.Listener {

        @SuppressWarnings("unused")
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSignEdit(SignChangeEvent e) {
            Player player = e.getPlayer();
            Sign sign = (Sign) e.getBlock().getState();
            SignShop signShop = parseSign(e.getLines());
            if (signShop.isShop()) {
                if (!player.hasPermission("pb.shop.create")) {
                    plugin.sendMessage(player, ChatColor.RED + tm.getText("create_shop_no_perms"));
                    sign.setLine(0, ChatColor.STRIKETHROUGH + sign.getLine(0));
                } else {
                    if (signShop.isValid()) {
                        updateSignShop(signShop, e);
                        plugin.sendMessage(player, tm.getText("shop_created"));
                    } else {
                        plugin.sendMessage(player, ChatColor.RED + tm.getText("shop_not_valid"));
                        if (signShop.sizeX == null || signShop.sizeY == null || signShop.sizeZ == null) {
                            sign.setLine(1, ChatColor.STRIKETHROUGH + sign.getLine(1));
                        }
                        if (signShop.price == null) {
                            sign.setLine(2, ChatColor.STRIKETHROUGH + sign.getLine(2));
                        }
                        if (signShop.material == null) {
                            sign.setLine(3, ChatColor.STRIKETHROUGH + sign.getLine(3));
                        }

                        sign.update();
                    }
                }
            }
        }

        @SuppressWarnings("unused")
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onPlayerUse(final PlayerInteractEvent event) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (Utils.isSign(event.getClickedBlock())) {
                    Sign sign = (Sign) event.getClickedBlock().getState();
                    SignShop signShop = parseSign(sign.getLines());
                    if (signShop.isValid()) {
                        ProtectionBlock pb = plugin.getProtectionManager().generateBlock(signShop.material, null, ownerName,
                                signShop.sizeX, signShop.sizeY, signShop.sizeZ);
                        sellPB(event.getPlayer(), pb, signShop.price);
                    }
                }
            }
        }

    }

    private class SignShop {

        Integer sizeX;
        Integer sizeY;
        Integer sizeZ;
        Double price;
        Material material;
        boolean valid;
        boolean shop;

        public boolean isShop() {
            return shop;
        }

        public boolean isValid() {
            return valid;
        }

        @Override
        public String toString() {
            String result = "";
            if (sizeX != null) {
                result = result + "| X: " + sizeX;
            }
            if (sizeY != null) {
                result = result + "| Y: " + sizeY;
            }
            if (sizeZ != null) {
                result = result + "| Z: " + sizeZ;
            }
            if (price != null) {
                result = result + "| Price: " + price;
            }
            if (material != null) {
                result = result + "| Material: " + material.name();
            }
            result = result + "| Shop: " + shop;

            return result;
        }
    }

    public void initialize() {
        signFirstLine = plugin.getConfig().getString("shop.sign-first-line", "[lpb]").toLowerCase();
        priceDecimals = plugin.getConfig().getInt("shop.price-decimals");
        plugin.getServer().getPluginManager().registerEvents(new Listener(), plugin);
        ownerName = plugin.getConfig().getString("shop.owner-name", "Admin Shop");
    }

    public Shop(LibelulaProtectionBlocks plugin) {
        this.plugin = plugin;
        this.tm = plugin.getTextManager();
    }

    private SignShop parseSign(String[] text) {
        SignShop result = new SignShop();
        if (text[0].toLowerCase().startsWith(signFirstLine)) {
            result.shop = true;
            if (text[1].contains("x")) {
                String[] sizeString = text[1].replace(" ", "").split("x");
                if (sizeString.length == 3) {
                    try {
                        result.sizeX = Integer.parseInt(sizeString[0]);
                    } catch (NumberFormatException dummy) {
                        result.sizeX = null;
                    }
                    try {
                        result.sizeY = Integer.parseInt(sizeString[1]);
                    } catch (NumberFormatException dummy) {
                        result.sizeY = null;
                    }
                    try {
                        result.sizeZ = Integer.parseInt(sizeString[2]);
                    } catch (NumberFormatException dummy) {
                        result.sizeZ = null;
                    }
                }
            } else {
                try {
                    result.sizeX = Integer.parseInt(text[1]);
                    result.sizeY = result.sizeX;
                    result.sizeZ = result.sizeX;
                } catch (NumberFormatException dummy) {
                    result.sizeX = null;
                }
            }
            if (result.sizeX != null && result.sizeY != null && result.sizeZ != null) {
                if ((result.sizeX & 1) == 0) {
                    result.sizeX = null;
                }
                if (result.sizeY != 0 && (result.sizeY & 1) == 0) {
                    result.sizeY = null;
                }
                if ((result.sizeZ & 1) == 0) {
                    result.sizeZ = null;
                }
            }
            try {
                result.price = Double.parseDouble(text[2].replace("$", "").replace(",", "."));
            } catch (NumberFormatException ex) {
                result.price = null;
            }
            result.material = Material.getMaterial(text[3].toUpperCase());
            if (result.material != null) {
                if (!ProtectionBlock.validateMaterial(result.material)) {
                    result.material = null;
                }
            }
            if (result.sizeX != null && result.sizeY != null && result.sizeZ != null && result.price != null
                    && result.material != null) {
                result.valid = true;
            }
        }
        return result;
    }


    private void updateSignShop(SignShop signShop, SignChangeEvent sign) {
        String sizeLine = signShop.sizeX + " x ";
        if (signShop.sizeY == 0) {
            sizeLine = sizeLine.concat('\u221E' + " x ");
        } else {
            sizeLine = sizeLine + signShop.sizeY + " x ";
        }
        sizeLine = sizeLine + signShop.sizeZ;
        sign.setLine(1, sizeLine);
        sign.setLine(2, "$ " + String.format("%." + priceDecimals + "f", signShop.price));
        sign.setLine(3, signShop.material.name());
    }

    public void sellPB(final Player player, final ProtectionBlock pb, final double price) {
        Bukkit.getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                if (plugin.getEco().getBalance(player) < price) {
                    //player has to few money
                    plugin.sendMessage(player, ChatColor.RED + tm.getText("not_enough_money"));
                    plugin.getProtectionManager().removePb(pb);
                } else {
                    //player has enough money
                    if (player.getInventory().addItem(pb.getItemStack()).isEmpty()) {
                        //item can be added to player inventory
                        plugin.getEco().withdrawPlayer(player, price);
                        plugin.sendMessage(player, tm.getText("pb-bought"));
                    } else {
                        //item cannot be added to inventory
                        plugin.sendMessage(player, ChatColor.RED + tm.getText("not_inventory_space"));
                        plugin.getProtectionManager().removePb(pb);
                    }
                }
            }
        });

    }

}
