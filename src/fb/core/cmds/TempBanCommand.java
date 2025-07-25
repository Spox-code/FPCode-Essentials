package fb.core.cmds;

import fb.core.Main;
import fb.core.api.BanAPI;
import fb.core.api.HexAPI;
import fb.core.data.MySQL;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.time.Instant; // NOWY IMPORT
import java.time.ZoneId;
import java.time.ZonedDateTime; // NOWY IMPORT
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TempBanCommand implements CommandExecutor {

    private final Main plugin;
    private static BanAPI banAPI;
    private final HexAPI hexAPI;
    private final ZoneId displayZoneId; // Zmienna przechowująca ZoneId z Main
    private final DateTimeFormatter dateFormatter; // Zmienna przechowująca DateTimeFormatter
    private final DateTimeFormatter dbformat;

    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d+)([smhd])");

    public TempBanCommand(Main plugin, BanAPI banAPI) {
        this.plugin = plugin;
        TempBanCommand.banAPI = banAPI;
        this.hexAPI = new HexAPI();
        // --- NOWY KOD: Pobieranie strefy czasowej z Main ---
        this.displayZoneId = plugin.getServerZoneId(); // Pobieramy ZoneId z Main
        this.dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"); // Inicjalizujemy formatter
        this.dbformat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.000");
        // --- KONIEC NOWEGO KODU ---
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("fb.tempban")) {
            sender.sendMessage(hexAPI.hex("§cNie posiadasz uprawnienia (fb.tempban)"));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(hexAPI.hex("§8[#0096FC⚡§8] §fUżycie: #0096FC/tempban <gracz> <czas> <powód>"));
            sender.sendMessage(hexAPI.hex("§8[#0096FC⚡§8] §fPrzykłady czasu: #0096FC10s (sekundy), 5m (minuty), 2h (godziny), 7d (dni)"));
            return true;
        }

        String targetName = args[0];
        String durationString = args[1];
        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            reasonBuilder.append(args[i]).append(" ");
        }
        String reason = reasonBuilder.toString().trim();

        if (sender instanceof Player && sender.getName().equalsIgnoreCase(targetName)) {
            sender.sendMessage(hexAPI.hex("§cNie możesz zbanować samego siebie!"));
            return true;
        }

        if (targetName.equalsIgnoreCase("console") || targetName.equalsIgnoreCase(MySQL.CONSOLE_UUID.toString())) {
            sender.sendMessage(hexAPI.hex("§cNie możesz zbanować konsoli!"));
            return true;
        }

        long durationMillis = parseDuration(durationString);
        if (durationMillis <= 0) {
            sender.sendMessage(hexAPI.hex("§cPodano nieprawidłowy format czasu. Użyj np. 10s, 5m, 2h, 7d."));
            return true;
        }

        Timestamp endTime = new Timestamp(System.currentTimeMillis() + durationMillis);

        String adminName = (sender instanceof Player) ? sender.getName() : "CONSOLE";

        if (BanAPI.isPlayerBanned(targetName)) {
            sender.sendMessage(hexAPI.hex("§cGracz §f" + targetName + " §cjest już zbanowany."));
            return true;
        }

        // --- ZMIENIONA LINIA DO WYŚWIETLANIA DATY ZAKOŃCZENIA BANA ---
        // Konwertujemy Timestamp na Instant, a następnie na ZonedDateTime w naszej strefie czasowej
        Instant instant = endTime.toInstant();
        ZonedDateTime zonedEndTime = instant.atZone(displayZoneId); // Używamy zmiennej displayZoneId
        String formattedEndTime = zonedEndTime.format(dateFormatter); // Formatujemy datę
        String endtime_form = zonedEndTime.format(dbformat);
        // --- KONIEC ZMIAN ---
        banAPI.BanPlayer(targetName, reason, adminName, Timestamp.valueOf(endtime_form));
        long remainingMillis = endTime.getTime() - System.currentTimeMillis();
        String remainingTimeFormatted = formatDuration(remainingMillis);
        for(Player ps : Bukkit.getOnlinePlayers()){
            ps.sendMessage("");
            ps.sendMessage(HexAPI.hex("   #0096FCFPCode.PL §7- §fGracz #0096fc" + targetName + " §fzostal zbanowany"));
            ps.sendMessage(HexAPI.hex("   §fCzas bana #0096fc" + remainingTimeFormatted ));
            ps.sendMessage(HexAPI.hex("   §fPowod #0096fc" + reason));
            ps.sendMessage("");
        }

        sender.sendMessage(hexAPI.hex("§8[#0096FC⚡§8] §fZbanowano gracza #0096FC" + targetName + " §fna #0096FC" + durationString + " §fz powodu: #0096FC" + reason + "§f. Wygasa: #0096FC" + formattedEndTime));


        Player targetPlayer = Bukkit.getPlayerExact(targetName);
        if (targetPlayer != null && targetPlayer.isOnline()) {
            String banMessage = hexAPI.hex("§cZostałeś zbanowany!");
            String reasonMessage = hexAPI.hex("§CPowód: §f" + reason);
            String adminMessage = hexAPI.hex("§CAdmin: §f" + adminName);
            // --- ZMIENIONA LINIA DO WYŚWIETLANIA DATY ZAKOŃCZENIA BANA PRZY KICKU ---
            String timeMessage = hexAPI.hex("§CWygasa: §f" + formattedEndTime);
            // --- KONIEC ZMIAN ---

            targetPlayer.kickPlayer(banMessage + "\n" + reasonMessage + "\n" + adminMessage + "\n" + timeMessage);
        }

        return true;
    }

    private long parseDuration(String durationString) {
        Matcher matcher = TIME_PATTERN.matcher(durationString);
        if (matcher.matches()) {
            long value = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2);

            switch (unit) {
                case "s":
                    return TimeUnit.SECONDS.toMillis(value);
                case "m":
                    return TimeUnit.MINUTES.toMillis(value);
                case "h":
                    return TimeUnit.HOURS.toMillis(value);
                case "d":
                    return TimeUnit.DAYS.toMillis(value);
                default:
                    return 0;
            }
        }
        return 0;
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
        sb.append(seconds).append("s"); // Zawsze pokazuj sekundy

        return sb.toString().trim();
    }
}