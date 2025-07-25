package fb.core.cmds;

import fb.core.Main;
import fb.core.api.BungeeAPI;
import fb.core.api.HexAPI;
import fb.core.api.RanksAPI;
import fb.core.data.ConfigData;
import fb.core.gui.Reward;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Odbierz implements CommandExecutor{

    static RanksAPI ra;
    static ConfigData cd;
    static Main plugin;

    public Odbierz(Main m){
        plugin = m;
        ra = new RanksAPI(plugin);
        cd = ConfigData.getInstance();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
        if (sender instanceof Player p) {
            Reward.OpenGUI(p);
        } else {
            sender.sendMessage("§cKomenda tylko dla graczy");
        }

        return false;
    }

}
