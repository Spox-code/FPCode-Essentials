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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


public class Food implements CommandExecutor, TabExecutor {

    static RanksAPI ra;
    static ConfigData cd;
    static Main plugin;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

    public Food(Main m){
        plugin = m;
        ra = new RanksAPI(plugin);
        cd = ConfigData.getInstance();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
        if (sender instanceof Player p) {
            if (ra.hasPermission(p, "fb.food")) {
                if(args.length == 1){
                    Player cel = Bukkit.getPlayerExact(args[0]);
                    if(cel != null){
                        cel.setFoodLevel(20);
                        p.sendMessage(HexAPI.hex("§8[#0096FC⚡§8] §fNajadles gracza #0096fc" + cel.getName()));
                        cel.sendMessage(HexAPI.hex("§8[#0096FC⚡§8] §fZostales wlasnie #0096fcNajedzony"));
                    }else{
                        p.sendMessage("§cTen gracz jest OFFLINE");
                    }
                }else if(args.length == 0){
                    p.setFoodLevel(20);
                    p.sendMessage(HexAPI.hex("§8[#0096FC⚡§8] §fZostales wlasnie #0096fcNajedzony"));
                }else{
                    p.sendMessage("§cUzycie /food <gracz>");
                }
            } else {
                p.sendMessage(HexAPI.hex("§cNie posiadasz uprawnienia (fb.food)"));
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
            if(ra.hasPermission(p, "fb.food")){
                for(Player ps : Bukkit.getOnlinePlayers()){
                    tab.add(ps.getName());
                }
            }
        }
        return tab;
    }
}
