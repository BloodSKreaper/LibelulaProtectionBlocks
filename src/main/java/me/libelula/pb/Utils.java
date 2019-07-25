package me.libelula.pb;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class Utils {

    public static boolean isSign(Block block){
        if(block == null) return false;
        Material material = block.getType();
        if(material.toString().contains("SIGN")) return true;
        return false;
    }
}
