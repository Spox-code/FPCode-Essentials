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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


public class Live implements CommandExecutor, TabExecutor {

    static RanksAPI ra;
    static ConfigData cd;
    static Main plugin;

    public Live(Main m){
        plugin = m;
        ra = new RanksAPI(plugin);
        cd = ConfigData.getInstance();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
        if (sender instanceof Player p) {
            if (ra.hasPermission(p, "fb.live")) {
                if(args.length == 1) {
                    BungeeAPI.sendMessage(p, "");
                    BungeeAPI.sendMessage(p, HexAPI.hex("#0096fc§lFPCode§r §8§m-§r #0096fc§lLIVE"));
                    BungeeAPI.sendMessage(p, HexAPI.hex("§fGracz #0096fc" + p.getName() + " §faktualnie prowadzi #0096fclive"));
                    BungeeAPI.sendMessage(p, HexAPI.hex("#0096fc" + args[0]));
                    BungeeAPI.sendMessage(p, "");
                }else{
                    p.sendMessage("§cUzycie /live <link do live>");
                }
            } else {
                p.sendMessage(HexAPI.hex("§cNie posiadasz uprawnienia (fb.live)"));
            }
        } else {
            sender.sendMessage("§cKomenda tylko dla graczy");
        }

        return false;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        List<String> tab = new ArrayList<>();
        if(sender instanceof Player p){
            if(ra.hasPermission(p, "fb.live")){
                tab.add("<link do live>");
            }
        }
        return tab;
    }
}
