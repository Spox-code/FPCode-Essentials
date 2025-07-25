package fb.core.events;

import fb.core.Main;
import fb.core.Sidebar;
import fb.core.api.BossBarAPI;
import fb.core.api.HexAPI;
import fb.core.api.TabListAPI;
import fb.core.data.ConfigData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {

    static ConfigData cd;
    static BossBarAPI bar;
    static Main plugin;

    public PlayerJoin(Main m){
        plugin = m;
        cd = ConfigData.getInstance();
        bar = new BossBarAPI(plugin);
        bar.createBar();
    }

    @EventHandler
    public void PlayerJoin(PlayerJoinEvent e){
        Player p = e.getPlayer();
        TabListAPI.pupdate(p);
        e.setJoinMessage(HexAPI.hex(cd.getData().getString("messages.join")).replace("{player}", p.getName()));
        for(Player ps : Bukkit.getOnlinePlayers()){
            Sidebar.updateScoreboard(ps);
        }

        if(!bar.getBar().getPlayers().contains(p)){
            bar.addPlayer(p);
        }
    }
}
