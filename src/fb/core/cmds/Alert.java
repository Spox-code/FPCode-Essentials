package fb.core.cmds;

import fb.core.Main;
import fb.core.api.BungeeAPI;
import fb.core.api.HexAPI;
import fb.core.api.RanksAPI;
import fb.core.data.ConfigData;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Alert implements CommandExecutor, TabExecutor {

    static RanksAPI ra;
    static ConfigData cd;
    private static String permission = "fb.helpop";
    static Main plugin;

    public Alert(Main m){
        plugin = m;
        ra = new RanksAPI(plugin);
        cd = ConfigData.getInstance();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
        if (sender instanceof Player p) {
            if (ra.hasPermission(p, "fb.alert")) {
                if(args.length > 0){
                    StringBuilder message = new StringBuilder();
                    for (int i = 0; i < args.length; i++) {
                        message.append(args[i]);
                        if (i < args.length - 1) {
                            message.append(" ");
                        }
                    }
                    String msg = message.toString();
                    BungeeAPI.sendMessage(p, "§c§lAlert§8: §f" + msg);
                }else{
                    p.sendMessage("§cUzycie /alert <wiadomosc>");
                }
            } else {
                p.sendMessage(HexAPI.hex("§cNie posiadasz uprawnienia (fb.alert)"));
            }
        } else {
            sender.sendMessage("§cKomenda tylko dla graczy");
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        List<String> list = new ArrayList<>();
        list.add("<wiadomosc>");
        return list;
    }
}
