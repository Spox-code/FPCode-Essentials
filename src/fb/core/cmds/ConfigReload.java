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
import java.util.Iterator;
import java.util.List;

public class ConfigReload implements CommandExecutor {
    Main plugin;
    static RanksAPI ra;
    static HexAPI h;
    static ConfigData cd;
    private final String name = HexAPI.hex("#0096fcRanks");

    public ConfigReload(Main m) {
        this.plugin = m;
        ra = new RanksAPI(plugin);
        h = new HexAPI();
        cd = ConfigData.getInstance();
    }
    public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
        if (sender instanceof Player p) {
            if (ra.hasPermission(p, "fb.config")) {
                cd.reloadData();
                p.sendMessage(HexAPI.hex("§fPrzeladowoano #0096fcconfig.yml"));
            } else {
                p.sendMessage(HexAPI.hex("§cNie posiadasz uprawnienia (fb.config)"));
            }
        } else {
            sender.sendMessage("§cKomenda tylko dla graczy");
        }

        return false;
    }

}
