package ca.crystal.cleaner;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class CrystalWorldCleaner extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        // Détecte si le joueur tente de réapparaître sur une Respawn Anchor
        if (event.isAnchorSpawn()) {
            Player player = event.getPlayer();
            
            // Récupère le point de spawn principal du monde par défaut
            Location mainSpawn = Bukkit.getWorlds().get(0).getSpawnLocation();
            
            // Force la réapparition au spawn du serveur au lieu de l'ancre
            event.setRespawnLocation(mainSpawn);
            player.sendMessage("§cLes ancres de réapparition sont désactivées !");
        }
    }
}
