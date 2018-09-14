package me.libelula.pb;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.Vector;

public class ParticleManager {
	private Plugin plugin;
	public ParticleManager(Plugin plugin) {
		this.plugin = plugin;
	}


	public void ShowParticlesonEdges(Vector min, Vector max, final World w){

		final int x1 = min.getBlockX();
		final int x2 = max.getBlockX();
		final int z1 = min.getBlockZ();
		final int z2 = max.getBlockZ();
		this.showParticles(x1,z1, w);
		this.showParticles(x1, z2, w);
		this.showParticles(x2, z1, w);
		this.showParticles(x2, z2, w);
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
        		showParticles(x1,z1, w);
        		showParticles(x1, z2, w);
        		showParticles(x2, z1, w);
        		showParticles(x2, z2, w);
                Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                    @Override
                    public void run() {
                		showParticles(x1,z1, w);
                		showParticles(x1, z2, w);
                		showParticles(x2, z1, w);
                		showParticles(x2, z2, w);
                        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                            @Override
                            public void run() {
                        		showParticles(x1,z1, w);
                        		showParticles(x1, z2, w);
                        		showParticles(x2, z1, w);
                        		showParticles(x2, z2, w);
                                Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                                    @Override
                                    public void run() {
                                		showParticles(x1,z1, w);
                                		showParticles(x1, z2, w);
                                		showParticles(x2, z1, w);
                                		showParticles(x2, z2, w);
                                        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                                            @Override
                                            public void run() {
                                        		showParticles(x1,z1, w);
                                        		showParticles(x1, z2, w);
                                        		showParticles(x2, z1, w);
                                        		showParticles(x2, z2, w);
                                                Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                                                    @Override
                                                    public void run() {
                                                		showParticles(x1,z1, w);
                                                		showParticles(x1, z2, w);
                                                		showParticles(x2, z1, w);
                                                		showParticles(x2, z2, w);
                                                        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                                                            @Override
                                                            public void run() {
                                                        		showParticles(x1,z1, w);
                                                        		showParticles(x1, z2, w);
                                                        		showParticles(x2, z1, w);
                                                        		showParticles(x2, z2, w);
                                                                Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                		showParticles(x1,z1, w);
                                                                		showParticles(x1, z2, w);
                                                                		showParticles(x2, z1, w);
                                                                		showParticles(x2, z2, w);
                                                                        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                        		showParticles(x1,z1, w);
                                                                        		showParticles(x1, z2, w);
                                                                        		showParticles(x2, z1, w);
                                                                        		showParticles(x2, z2, w);
                                                                        		
                                                                            }
                                                                            
                                                                        }, 20L);
                                                                    }
                                                                    
                                                                }, 20L);
                                                            }
                                                            
                                                        }, 20L);
                                                    }
                                                    
                                                }, 20L);
                                            }
                                            
                                        }, 20L);
                                    }
                                    
                                }, 20L);
                            }
                        }, 20L);
                    }
                }, 20L);
            }
        }, 20L);
	}
	
	
    private void showParticles(int xraw, int zraw, World w){
    	double x = xraw + 0.5;
    	double z = zraw + 0.5;
    	for(double i= 0; i<256;){
			w.playEffect(new Location(w, x, i, z), Effect.HAPPY_VILLAGER, 10, 100);
        	i = i +0.5;
    	}
    	

    }
    
    
    
}
