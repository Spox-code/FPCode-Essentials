package fb.core.cmds;

import fb.core.Main;
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

public class HelpOP implements CommandExecutor, TabExecutor {

    static RanksAPI ra;
    static ConfigData cd;
    private static String permission = "fb.helpop";
    static Main plugin;

    public HelpOP(Main m){
        plugin = m;
        ra = new RanksAPI(plugin);
        cd = ConfigData.getInstance();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(sender instanceof Player) {
            Player p = (Player) sender;
            if(args.length > 0){
                StringBuilder message = new StringBuilder();
                for (int i = 0; i < args.length; i++) {
                    message.append(args[i]);
                    if (i < args.length - 1) {
                        message.append(" ");
                    }
                }
                String msg = message.toString();
                p.sendMessage(HexAPI.hex("§8[#0096FC⚡§8] §fWyslales wiadomosc do #0096fcAdministracji"));
                for(Player ps : Bukkit.getOnlinePlayers()){
                    if(ra.hasPermission(ps, permission)){
                        ps.sendMessage(HexAPI.hex("§8[#0096FCHelpOP§8] " + ra.getRankPrefix(ra.getRank(p.getName())) + " " + p.getName() + "§8: #0096FC" + msg));
                    }
                }
            }else{
                p.sendMessage("§cUzycie /helpop <wiadomosc>");
            }
        }else{
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
