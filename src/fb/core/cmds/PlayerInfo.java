package fb.core.cmds;

import fb.core.Main;
import fb.core.api.BanAPI;
import fb.core.api.BungeeAPI;
import fb.core.api.HexAPI;
import fb.core.api.RanksAPI;
import fb.core.data.ConfigData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class PlayerInfo implements CommandExecutor {

    static RanksAPI ra;
    static ConfigData cd;
    static Main plugin;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

    public PlayerInfo(Main m){
        plugin = m;
        ra = new RanksAPI(plugin);
        cd = ConfigData.getInstance();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
        if (sender instanceof Player p) {
            if (ra.hasPermission(p, "fb.info")) {
                if(args.length == 1){
                    String playername = args[0];
                    p.sendMessage("");
                    p.sendMessage(HexAPI.hex("§8§m--------§r#0096fc" + playername.toUpperCase() + "§8§m--------"));
                    p.sendMessage("");
                    p.sendMessage(HexAPI.hex("  §8➡ §fRanga #0096fc" + ra.getRank(playername)));
                    p.sendMessage("");
                    p.sendMessage(HexAPI.hex("      §8➡ #0096fc§lBAN§r §8⬅"));
                    if(BanAPI.isPlayerBanned(playername)){
                        Timestamp endTime = BanAPI.getBanEndTime(playername);
                        long remainingMillis = endTime.getTime() - System.currentTimeMillis();
                        String remainingTimeFormatted = formatDuration(remainingMillis);
                        p.sendMessage(HexAPI.hex("  §8➡ §fPowod #0096fc"+BanAPI.getBanReason(playername)));
                        p.sendMessage(HexAPI.hex("  §8➡ §fAdministrator #0096fc" + BanAPI.getBanAdmin(playername)));
                        p.sendMessage(HexAPI.hex("  §8➡ §fPozostalo #0096fc" + remainingTimeFormatted));
                    }else{
                        p.sendMessage(HexAPI.hex("  §8➡ §fBan #0096fcBrak"));
                    }
                    p.sendMessage("");
                    p.sendMessage(HexAPI.hex("      §8➡ #0096fc§lMUTE§r §8⬅"));
                    if(BanAPI.isMutePlayer(playername)){
                        Timestamp endTime = BanAPI.getMuteTime(playername);
                        long remainingMillis = endTime.getTime() - System.currentTimeMillis();
                        String remainingTimeFormatted = formatDuration(remainingMillis);
                        p.sendMessage(HexAPI.hex("  §8➡ §fPowod #0096fc"+BanAPI.getMuteReason(playername)));
                        p.sendMessage(HexAPI.hex("  §8➡ §fAdministrator #0096fc" + BanAPI.getMuteAdmin(playername)));
                        p.sendMessage(HexAPI.hex("  §8➡ §fPozostalo #0096fc" + remainingTimeFormatted));
                    }else{
                        p.sendMessage(HexAPI.hex("  §8➡ §fMute #0096fcBrak"));
                    }
                    List<Object[]> warns = BanAPI.getWarnsPlayer(playername); // Ta linia wywołuje Twoje API, a API MySQL
                    p.sendMessage("");
                    p.sendMessage(HexAPI.hex("      §8➡ #0096fc§lWARNS§r §8⬅"));
                    if(warns.isEmpty()){
                        p.sendMessage(HexAPI.hex("  §8➡ §fWarny #0096fcBrak"));
                    }else{
                        for (Object[] warnData : warns) {
                            // String nick = (String) warnData[0];
                            String reason = (String) warnData[1];
                            long timestamp = (Long) warnData[2];

                            String formattedTime = DATE_FORMAT.format(new Date(timestamp));

                            sender.sendMessage(HexAPI.hex("  §8➡ §fPowód: #0096FC" + reason +" §f| Czas: #0096FC" + formattedTime));
                        }
                    }
                    p.sendMessage("");
                    p.sendMessage(HexAPI.hex("§8§m--------§r#0096fc" + playername.toUpperCase() + "§8§m--------"));
                    p.sendMessage("");
                }else{
                    p.sendMessage(HexAPI.hex("§cUzycie /playerinfo <gracz>"));
                }
            } else {
                p.sendMessage(HexAPI.hex("§cNie posiadasz uprawnienia (fb.info)"));
            }
        } else {
            sender.sendMessage("§cKomenda tylko dla graczy");
        }

        return false;
    }

    private String formatDuration(long millis) {
        if (millis < 0) {
            return "Wygasł";
        }

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("d ");
        }
        if (hours > 0) {
            sb.append(hours).append("h ");
        }
        if (minutes > 0) {
            sb.append(minutes).append("m ");
        }
        sb.append(seconds).append("s");

        return sb.toString().trim();
    }

}
