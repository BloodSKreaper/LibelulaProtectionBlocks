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
package me.libelula.pb.eventhandler;

import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import me.libelula.pb.LibelulaProtectionBlocks;

/**
 * @author Diego D'Onofrio <ddonofrio@member.fsf.org>
 */
public class EventManager implements Listener {

    private final LibelulaProtectionBlocks plugin;

    public EventManager(LibelulaProtectionBlocks plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        plugin.getServer().getPluginManager().
                registerEvents(this, plugin);
    }

    /*
    All relevant events for ProtectionBlocks:
    * BlockBurnEvent | Event gets cancelled if block is ProtectionBlock
    * BlockExplodeEvent | Event gets cancelled if block is ProtectionBlock
    * BlockPistonEvent (BlockPistonExtendEvent, BlockPistonRetractEvent) | ProtectionBlock cannot be moved
    * BlockPlaceEvent | Gets handled by the ProtectionManager
    * BlockBreakEvent | Gets handled by the ProtectionManager
    * EntityChangeBlockEvent | Event gets cancelled if block is ProtectionBlock
     */
    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        if (plugin.getProtectionManager().isPB(e.getItemInHand())) {
            plugin.getProtectionManager().placePb(e);
        }
    }

    /*
    Handles the breaking of a protection block
     */
    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        if (plugin.getProtectionManager().isPB(e.getBlock())) {
            plugin.getProtectionManager().breakPb(e);
        }
    }

    /*
    Handles the breaking of a protection block by entities (e.g. endermen)
    */
    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityGrief(EntityChangeBlockEvent e) {
        if (e.getEntityType() != EntityType.PLAYER) {
            if (plugin.getProtectionManager().isPB(e.getBlock())) {
                e.setCancelled(true);
            }
        }
    }

    /*
    Handles the burning of a protection block
    */
    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent e) {
        if (plugin.getProtectionManager().isPB(e.getBlock())) {
            e.setCancelled(true);
        }
    }


    /*
    Prevents a protection block from being moved when a piston is extended
     */
    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent e) {
        for (Block block : e.getBlocks()) {
            if (plugin.getProtectionManager().isPB(block)) {
                e.setCancelled(true);
            }
        }
    }

    /*
    Prevents a protection block from being moved when a piston is retracted
    */
    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent e) {
        for (Block RetractedBlock : e.getBlocks()) {
            if (plugin.getProtectionManager().isPB(RetractedBlock)) {
                e.setCancelled(true);
            }
        }
    }

    /*
    Prevents a protection block from being removed when an explosion happens
     */
    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent e) {
        e.blockList().removeIf(block -> plugin.getProtectionManager().isPB(block));
    }

}
