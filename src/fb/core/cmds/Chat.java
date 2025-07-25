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


public class Chat implements CommandExecutor, TabExecutor {

    static RanksAPI ra;
    static ConfigData cd;
    static Main plugin;
    public static boolean chat = true;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

    public Chat(Main m){
        plugin = m;
        ra = new RanksAPI(plugin);
        cd = ConfigData.getInstance();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
        if (sender instanceof Player p) {
            if (ra.hasPermission(p, "fb.chat")) {
                if(args.length == 1){
                    for(Player ps : Bukkit.getOnlinePlayers()) {
                        for(int i = 0; i<100;i++){
                            ps.sendMessage("");
                        }
                        if (args[0].equalsIgnoreCase("clear") || args[0].equalsIgnoreCase("cc")) {
                            ps.sendMessage(HexAPI.hex("   #0096fc§lFPCode"));
                            ps.sendMessage(HexAPI.hex("   §fChat zostal #0096fcwyczyszczony"));
                            ps.sendMessage(HexAPI.hex("   §fprzez administratora #0096fc" + p.getName()));
                            ps.sendMessage("");
                        }else if (args[0].equalsIgnoreCase("off") || args[0].equalsIgnoreCase("wylacz")) {
                            chat = false;
                            ps.sendMessage(HexAPI.hex("   #0096fc§lFPCode"));
                            ps.sendMessage(HexAPI.hex("   §fChat zostal #0096fcwylaczony"));
                            ps.sendMessage(HexAPI.hex("   §fprzez administratora #0096fc" + p.getName()));
                            ps.sendMessage("");
                        }else if (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("wlacz")) {
                            chat = false;
                            ps.sendMessage(HexAPI.hex("   #0096fc§lFPCode"));
                            ps.sendMessage(HexAPI.hex("   §fChat zostal #0096fcwlaczony"));
                            ps.sendMessage(HexAPI.hex("   §fprzez administratora #0096fc" + p.getName()));
                            ps.sendMessage("");
                        }else{
                            p.sendMessage("§cUzycie /chat <on/off/clear>");
                        }
                    }
                }else{
                    p.sendMessage("§cUzycie /chat <on/off/clear>");
                }
            } else {
                p.sendMessage(HexAPI.hex("§cNie posiadasz uprawnienia (fb.chat)"));
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
            if(ra.hasPermission(p, "fb.chat")){
                tab.add("clear");
                tab.add("cc");
                tab.add("off");
                tab.add("wylacz");
                tab.add("on");
                tab.add("wlacz");
            }
        }
        return tab;
    }
}
