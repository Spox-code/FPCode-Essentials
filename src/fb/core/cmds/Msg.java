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

public class Msg implements CommandExecutor, TabExecutor {

    static RanksAPI ra;
    static ConfigData cd;
    static Main plugin;

    public Msg(Main m){
        plugin = m;
        ra = new RanksAPI(plugin);
        cd = ConfigData.getInstance();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(sender instanceof Player) {
            Player p = (Player) sender;
            if(args.length > 1){
                StringBuilder message = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    message.append(args[i]);
                    if (i < args.length - 1) {
                        message.append(" ");
                    }
                }
                String msg = message.toString();
                Player cel = Bukkit.getPlayerExact(args[0]);
                if(cel != null){
                    p.sendMessage(HexAPI.hex("#0096fc§lMSG §fTy§c->" + ra.getRankPrefix(ra.getRank(cel.getName())) + " §f" + cel.getName() + "§8: #0096fc" + msg));
                    cel.sendMessage(HexAPI.hex("#0096fc§lMSG " + ra.getRankPrefix(ra.getRank(p.getName())) + " §f" + p.getName() + "§c->§fTy§8: #0096fc" + msg));
                    for(Player ps : Bukkit.getOnlinePlayers()){
                        if(ra.hasPermission(p, "fb.spy")){
                            ps.sendMessage(HexAPI.hex("#0096fc§lSPY " + ra.getRankPrefix(ra.getRank(p.getName())) + " §f" + p.getName() + "§c-> " + ra.getRankPrefix(ra.getRank(cel.getName())) + " §f" + cel.getName() + "§8: #0096fc" + msg));
                        }
                    }
                }else{
                    p.sendMessage("§cTen gracz jest OFFLINE");
                }
            }else{
                p.sendMessage("§cUzycie /msg <gracz> <wiadomosc>");
            }
        }else{
            sender.sendMessage("§cKomenda tylko dla graczy");
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        List<String> list = new ArrayList<>();
        if(args.length == 1){
            for(Player ps : Bukkit.getOnlinePlayers()){
                list.add(ps.getName());
            }
        }else if(args.length != 1) {
            list.add("<wiadomosc>");
        }
        return list;
    }
}
