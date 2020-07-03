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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import me.libelula.pb.eventhandler.EventManager;

/**
 * @author Diego D'Onofrio <ddonofrio@member.fsf.org>
 */
public final class LibelulaProtectionBlocks extends JavaPlugin {

    private String prefix;
    private TextManager textManager;
    private ProtectionManager protectionManager;
    private ParticleManager particleManager;
    private WorldGuardManager worldGuardManager;
    private ConsoleCommandSender console;
    private CommandManager commandManager;
    private EventManager eventManager;
    private Economy economy;
    private Shop shop;

    public LibelulaProtectionBlocks() {
        textManager = new TextManager(this);
        console = getServer().getConsoleSender();
        particleManager = new ParticleManager(this);
        commandManager = new CommandManager(this);
        protectionManager = new ProtectionManager(this);
        eventManager = new EventManager(this);
        shop = new Shop(this);
    }

    public TextManager getTextManager() {
        return textManager;
    }

    public ProtectionManager getProtectionManager() {
        return protectionManager;
    }

    public ParticleManager getParticleManager() {
        return particleManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public WorldGuardManager getWorldGuardManager() {
        return worldGuardManager;
    }

    public Shop getShop() {
        return shop;
    }

    @Override
    public void onEnable() {
        final LibelulaProtectionBlocks plugin = this;
        prefix = ChatColor.translateAlternateColorCodes('&', getConfig().getString("prefix"));
        try {
            textManager.initialize();
        } catch (MalformedURLException ex) {
            Logger.getLogger(LibelulaProtectionBlocks.class.getName()).log(Level.SEVERE, null, ex);
        }
        plugin.worldGuardManager = new WorldGuardManager(plugin);
        worldGuardManager.initialize();
        Bukkit.getScheduler().runTaskLater(this, new Runnable() {
            @Override
            public void run() {

                File configFile = new File(getDataFolder(), "config.yml");

                if (!configFile.exists()) {
                    saveResource("config.yml", true);
                    sendMessage(console, "Default config.yml saved.");
                }

                if (getConfig().getInt("config-version") != 3) {
                    prefix = "";
                    alert("The version of this plugin is incompatible with actual directory. You have to rename or erase LibelulaProtectionBlocks diretory from the plugin folder and restart your server.");
                    disable();
                } else {
                    try {
                        if (!worldGuardManager.isWorldGuardActive()) {
                            alert(textManager.getText("wg_not_initialized"));
                            disable();
                        } else {
                            commandManager.initialize();
                            eventManager.initialize();
                            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                                @Override
                                public void run() {
                                    protectionManager.initialize();
                                }
                            });
                            if (!setupEconomy()) {
                                alert(textManager.getText("vault-plugin-not-loaded"));
                            } else {
                                sendMessage(getServer().getConsoleSender(), textManager.getText("vault-plugin-linked"));
                                if (getConfig().getBoolean("shop.enable")) {
                                    shop.initialize();
                                    sendMessage(getServer().getConsoleSender(), textManager.getText("shop_enabled"));
                                }
                            }
                        }
                        if (getConfig().getBoolean("auto-save.enabled")
                                && getConfig().getInt("auto-save.interval-minutes") > 0) {
                            int tics = getConfig().getInt("auto-save.interval-minutes") * 20 * 60;
                            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
                                @Override
                                public void run() {
                                    if (getConfig().getBoolean("auto-save.log-messages")) {
                                        sendMessage(console, textManager.getText("saving"));
                                    }
                                    protectionManager.save();
                                }
                            }, tics, tics);

                        } else {
                            alert(textManager.getText("auto_save_disabled"));
                        }
                    } catch (NoClassDefFoundError | IOException ex) {
                        alert(textManager.getText("unexpected_error", ex));
                        disable();
                    }
                }
            }
        }, 20);
    }

    @Override
    public void onDisable() {
        protectionManager.save();
    }

    public void sendMessage(Player player, final String message) {
        sendMessage((CommandSender) player, message);
    }

    public void sendMessage(final CommandSender cs, List<String> messages) {
        for (String message : messages) {
            sendMessage(cs, message, 1);
        }
    }

    public void sendMessage(final CommandSender cs, final String message) {
        sendMessage(cs, message, 1);
    }

    public void sendMessage(final CommandSender cs, final String message, long later) {
        if (prefix == null) {
            prefix = "";
        }
        if (isEnabled()) {
            Bukkit.getScheduler().runTaskLater(this, new Runnable() {
                @Override
                public void run() {
                    cs.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', message));
                }
            }, later);
        } else {
            cs.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', message));
        }
    }

    public void alert(String message) {
        console.sendMessage(prefix + ChatColor.RED + message);
        for (Player player : getServer().getOnlinePlayers()) {
            if (player.hasPermission("pb.notification.receive")) {
                player.sendMessage(prefix + ChatColor.RED + message);
            }
        }
    }

    public String getPrefix() {
        return prefix;
    }

    public ConsoleCommandSender getConsole() {
        return console;
    }

    public WorldGuardManager getWG() {
        return worldGuardManager;
    }

    public void disable() {
        String text = textManager.getText("plugin_disabled");
        console.sendMessage(prefix + ChatColor.RED + text);
        this.getPluginLoader().disablePlugin(this);
    }

    private boolean setupEconomy() {
        economy = null;
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public void reloadLocalConfig() {
        Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
            @Override
            public void run() {
                if (textManager.isInitialized()) {
                    sendMessage(console, textManager.getText("reloading_config"));
                }
                if (getWG() != null) {
                    getWG().reloadConfig();
                    protectionManager.load();
                }
                if (textManager.isInitialized()) {
                    alert(ChatColor.YELLOW + textManager.getText("config_reloaded"));
                }
            }
        });
    }

    public void logTranslated(String message, Object... params) {
        sendMessage(console, textManager.getText(message, params));
    }

    public Economy getEco() {
        return economy;
    }

}
