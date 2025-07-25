package fb.core.api;

import fb.core.Main;
import fb.core.Sidebar;
import fb.core.data.ConfigData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class TabListAPI {

    static RanksAPI ra;
    static Main plugin;
    static ConfigData cd;

    public TabListAPI(Main m){
        plugin = m;
        cd = ConfigData.getInstance();
        ra = new RanksAPI(plugin);
    }

    public static void pupdate(Player p){
        Sidebar.updateScoreboard(p);
        p.setPlayerListName(ra.getRankPrefix(ra.getRank(p.getName())) + " " + p.getName());
        ArrayList<String> headerlist = (ArrayList<String>) cd.getData().getStringList("tablist.header");
        String header = "";
        for(int i = 0; i< headerlist.size(); i++){
            header+= HexAPI.hex(replacePlaceholders(p, headerlist.get(i)));
            if(i < headerlist.size()-1){
                header+="\n";
            }
        }
        p.setPlayerListHeader(header);
        ArrayList<String> footerlist = (ArrayList<String>) cd.getData().getStringList("tablist.footer");
        String footer = "";
        for(int i = 0; i< footerlist.size(); i++){
            footer+= HexAPI.hex(replacePlaceholders(p, footerlist.get(i)));
            if(i < footerlist.size()-1){
                footer+="\n";
            }
        }
        p.setPlayerListFooter(footer);
    }
    private static String replacePlaceholders(Player p, String line) {
        String processedLine = line;

        processedLine = processedLine.replace("{player}", p.getName());
        processedLine = processedLine.replace("{rank_name}", ra.getRank(p.getName()));
        processedLine = processedLine.replace("{rank_prefix}", ra.getRankPrefix(ra.getRank(p.getName())));
        processedLine = processedLine.replace("{online_players}", String.valueOf(Bukkit.getOnlinePlayers().size()));
        processedLine = processedLine.replace("{max_players}", String.valueOf(Bukkit.getMaxPlayers()));

        processedLine = processedLine.replace("{bungee_online_all}", String.valueOf(BungeeAPI.getCachedOnlinePlayers("ALL")));

        ConfigData cd = ConfigData.getInstance();
        if (cd != null && cd.getData().isSet("servers.list")) {
            for (String serverName : cd.getData().getStringList("servers.list")) {
                processedLine = processedLine.replace("{bungee_online_" + serverName.toLowerCase() + "}", String.valueOf(BungeeAPI.getCachedOnlinePlayers(serverName)));
            }
        }

        return processedLine;
    }
}
