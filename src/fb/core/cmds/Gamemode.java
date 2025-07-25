package fb.core.cmds;

import fb.core.Main;
import fb.core.api.HexAPI;
import fb.core.api.RanksAPI;
import fb.core.data.ConfigData;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Gamemode implements CommandExecutor, TabExecutor {

    static RanksAPI ra;
    static ConfigData cd;
    private static String permission = "fb.gamemode";
    static Main plugin;

    public Gamemode(Main m){
        plugin = m;
        ra = new RanksAPI(plugin);
        cd = ConfigData.getInstance();
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(sender instanceof Player){
            Player p = (Player) sender;
            if(ra.hasPermission(p, permission)){
                if(args.length == 1){
                    if(args[0].equalsIgnoreCase("0")){
                        p.setGameMode(GameMode.SURVIVAL);
                        p.sendMessage(HexAPI.hex("§8[#0096FC⚡§8] §fZmieniles tryb gry na #0096FC" + p.getGameMode().toString().toUpperCase()));
                    }else if(args[0].equalsIgnoreCase("1")){
                        p.setGameMode(GameMode.CREATIVE);
                        p.sendMessage(HexAPI.hex("§8[#0096FC⚡§8] §fZmieniles tryb gry na #0096FC" + p.getGameMode().toString().toUpperCase()));
                    }else if(args[0].equalsIgnoreCase("2")){
                        p.setGameMode(GameMode.ADVENTURE);
                        p.sendMessage(HexAPI.hex("§8[#0096FC⚡§8] §fZmieniles tryb gry na #0096FC" + p.getGameMode().toString().toUpperCase()));
                    }else if(args[0].equalsIgnoreCase("3")){
                        p.setGameMode(GameMode.SPECTATOR);
                        p.sendMessage(HexAPI.hex("§8[#0096FC⚡§8] §fZmieniles tryb gry na #0096FC" + p.getGameMode().toString().toUpperCase()));
                    }else{
                        p.sendMessage("§cUzycie /gamemode <0,1,2,3> <gracz>");
                    }
                }else  if(args.length == 2){
                    Player cel = Bukkit.getPlayerExact(args[1]);
                    if(cel != null){
                        if(args[0].equalsIgnoreCase("0")){
                            cel.setGameMode(GameMode.SURVIVAL);
                            p.sendMessage(HexAPI.hex("§8[#0096FC⚡§8] §fZmieniles tryb graczu #0096FC" + cel.getName() + "§f na #0096FC" + cel.getGameMode().toString().toUpperCase()));
                            cel.sendMessage(HexAPI.hex("§8[#0096FC⚡§8] §fTwoj tryb gry zostal zmieniony na #0096FC" + cel.getGameMode().toString().toUpperCase()));
                        }else if(args[0].equalsIgnoreCase("1")){
                            cel.setGameMode(GameMode.CREATIVE);
                            p.sendMessage(HexAPI.hex("§8[#0096FC⚡§8] §fZmieniles tryb graczu #0096FC" + cel.getName() + "§f na #0096FC" + cel.getGameMode().toString().toUpperCase()));
                            cel.sendMessage(HexAPI.hex("§8[#0096FC⚡§8] §fTwoj tryb gry zostal zmieniony na #0096FC" + cel.getGameMode().toString().toUpperCase()));
                        }else if(args[0].equalsIgnoreCase("2")){
                            cel.setGameMode(GameMode.ADVENTURE);
                            p.sendMessage(HexAPI.hex("§8[#0096FC⚡§8] §fZmieniles tryb graczu #0096FC" + cel.getName() + "§f na #0096FC" + cel.getGameMode().toString().toUpperCase()));
                            cel.sendMessage(HexAPI.hex("§8[#0096FC⚡§8] §fTwoj tryb gry zostal zmieniony na #0096FC" + cel.getGameMode().toString().toUpperCase()));
                        }else if(args[0].equalsIgnoreCase("3")){
                            cel.setGameMode(GameMode.SPECTATOR);
                            p.sendMessage(HexAPI.hex("§8[#0096FC⚡§8] §fZmieniles tryb graczu #0096FC" + cel.getName() + "§f na #0096FC" + cel.getGameMode().toString().toUpperCase()));
                            cel.sendMessage(HexAPI.hex("§8[#0096FC⚡§8] §fTwoj tryb gry zostal zmieniony na #0096FC" + cel.getGameMode().toString().toUpperCase()));
                        }else{
                            p.sendMessage("§cUzycie /gamemode <0,1,2,3> <gracz>");
                        }
                    }else{
                        p.sendMessage("§cTen gracz jest OFFLINE");
                    }
                }else{
                    p.sendMessage("§cUzycie /gamemode <0,1,2,3> <gracz>");
                }
            }else{
                p.sendMessage("§cNie posiadasz uprawnien ( " + permission + " )");
            }
        }else{
            sender.sendMessage("§cKomenda tylko dla graczy");
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        List<String> list = new ArrayList<>();
        if(ra.hasPermission((Player) sender, permission)) {
            if (args.length == 1) {
                list.add("0");
                list.add("1");
                list.add("2");
                list.add("3");
            }else if(args.length == 2){
                for(Player ps : Bukkit.getOnlinePlayers()){
                    list.add(ps.getName());
                }
            }
        }
        return list;
    }
}
