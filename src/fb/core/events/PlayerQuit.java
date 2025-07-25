package fb.core.events;

import fb.core.Main;
import fb.core.Sidebar;
import fb.core.api.HexAPI;
import fb.core.api.TabListAPI;
import fb.core.data.ConfigData;
import org.bukkit.Bukkit;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuit implements Listener {

    static ConfigData configData;
    static Main plugin;

    public PlayerQuit(Main m){
        plugin = m;
        configData = ConfigData.getInstance();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        String quitMessage = configData.getData().getString("messages.quit", "&fGracz &c{player} &fwyszedl/a z serwera");
        quitMessage = quitMessage.replace("{player}", p.getName());
        e.setQuitMessage(HexAPI.hex(quitMessage));

        // Usuń sidebar dla gracza, który wyszedł - NATYCHMIAST
        Sidebar.removeScoreboard(p); // Wywołaj na instancji!

        // Aktualizuj sidebar dla WSZYSTKICH POZOSTAŁYCH graczy, aby odzwierciedlić ZMNIEJSZENIE liczby online
        if (configData.getData().getBoolean("sidebar.enabled", false)) {
            // Natychmiastowe odświeżenie dla pozostałych graczy.
            // Ważne: ten kod uruchamia się PO tym, jak gracz faktycznie opuści serwer
            // (tj. Bukkit.getOnlinePlayers() już go nie zawiera).
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) { // Iterujemy tylko po tych, którzy ZOSTALI
                    if (onlinePlayer.isOnline()) { // Upewnij się, że gracz nadal jest online (np. nie crashował)
                        Bukkit.getScheduler().runTask(plugin, () -> Sidebar.updateScoreboard(onlinePlayer)); // Wywołaj na instancji!
                    }
                }
            });
        }
    }
}
