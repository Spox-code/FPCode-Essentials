// fb.core.cmds.TempMuteCommand.java
package fb.core.cmds; // Zmieniamy package na fb.core.cmds dla spójności

import fb.core.Main;
import fb.core.api.BanAPI;
import fb.core.api.HexAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TempMuteCommand implements CommandExecutor {

    private final BanAPI muteAPI; // Zmieniamy na MuteAPI i ustawiamy final
    private final HexAPI hexAPI;
    private final Main plugin;
    private final String commandName = HexAPI.hex("#0096fcWyciszenia"); // Dodajemy nazwę komendy/modułu dla spójności

    // Regular Expression for parsing time (e.g., 10s, 5m, 2h, 3d, 1y)
    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d+)([smhdMwy])");

    public TempMuteCommand(Main plugin, BanAPI muteAPI) { // Konstruktor powinien przyjmować MuteAPI
        this.plugin = plugin;
        this.muteAPI = muteAPI;
        this.hexAPI = new HexAPI();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 1. Sprawdzenie uprawnień
        if (!sender.hasPermission("fb.tempmute")) {
            sender.sendMessage(hexAPI.hex("§8[#0096FC⚡§8] §fNie posiadasz uprawnienia (fb.tempmute)"));
            return true;
        }

        // 2. Weryfikacja argumentów
        if (args.length < 3) {
            sender.sendMessage(hexAPI.hex("§8[#0096FC⚡§8] §fPoprawne użycie: #0096FC/tempmute <gracz> <czas> <powód>"));
            sender.sendMessage(hexAPI.hex("§8[#0096FC⚡§8] §fPrzykład: #0096FC/tempmute Notch 1d3h Spamowanie"));
            sender.sendMessage(hexAPI.hex("§8[#0096FC⚡§8] §fDostępne jednostki czasu: s (sekundy), m (minuty), h (godziny), d (dni), w (tygodnie), M (miesiące), y (lata)"));
            return true;
        }

        String targetNick = args[0];
        String timeString = args[1];
        String reason = "";
        for (int i = 2; i < args.length; i++) {
            reason += args[i] + " ";
        }
        reason = reason.trim(); // Usuń zbędne spacje na końcu

        // 3. Sprawdzenie, czy administrator mutuje samego siebie
        if (sender instanceof Player) {
            Player playerSender = (Player) sender;
            if (playerSender.getName().equalsIgnoreCase(targetNick)) {
                sender.sendMessage(hexAPI.hex("§8[#0096FC⚡§8] §fNie możesz wyciszyć samego siebie!"));
                return true;
            }
        }

        // 4. Parsowanie czasu trwania muta
        long durationMillis = parseTimeString(timeString);
        if (durationMillis <= 0) {
            sender.sendMessage(hexAPI.hex("§8[#0096FC⚡§8] §fNieprawidłowy format czasu lub czas musi być dodatni."));
            sender.sendMessage(hexAPI.hex("§8[#0096FC⚡§8] §fPrzykład: #0096FC1d3h §8(1 dzień i 3 godziny)"));
            sender.sendMessage(hexAPI.hex("§8[#0096FC⚡§8] §fDostępne jednostki czasu: s, m, h, d, w, M, y"));
            return true;
        }

        // 5. Obliczenie czasu zakończenia muta
        long currentTimeMillis = System.currentTimeMillis();
        Timestamp endTime = new Timestamp(currentTimeMillis + durationMillis);

        // 6. Wykonanie muta za pomocą MuteAPI
        String adminNick = sender.getName();
        if (!(sender instanceof Player)) {
            adminNick = "CONSOLE"; // Jeśli komenda jest wywołana z konsoli
        }

        // Zadeklaruj finalne zmienne dla lambdy
        String finalReason = reason;
        String finalAdminNick = adminNick;
        // Asynchroniczne wykonanie operacji na bazie danych, aby nie blokować głównego wątku serwera
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean success = muteAPI.mutePlayer(targetNick, finalReason, finalAdminNick, endTime);

            if (success) {
                // Wysyłamy wiadomość do administratora (z powrotem w wątku głównym, jeśli to gracz)
                Bukkit.getScheduler().runTask(plugin, () -> {
                    sender.sendMessage(hexAPI.hex("§8[#0096FC⚡§8] §fPomyślnie wyciszono gracza #0096FC" + targetNick + " §fz powodu: #0096FC" + finalReason + " §fna czas: #0096FC" + formatDuration(durationMillis)));

                    // Wysyłanie wiadomości do docelowego gracza, jeśli jest online
                    Player targetPlayer = Bukkit.getPlayer(targetNick);
                    if (targetPlayer != null && targetPlayer.isOnline()) {
                        targetPlayer.sendMessage(hexAPI.hex("§8[#0096FC⚡§8] §fZostałeś wyciszony przez #0096FC" + finalAdminNick + " §fz powodu: #0096FC" + finalReason + " §fna czas: #0096FC" + formatDuration(durationMillis) + "."));
                        targetPlayer.sendMessage(hexAPI.hex("§8[#0096FC⚡§8] §fUciszenie zakończy się: #0096FC" + (endTime))); // Użyj formatowania z Main
                    }
                });
            } else {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    sender.sendMessage(hexAPI.hex("§8[#0096FC⚡§8] §fNie udało się wyciszyć gracza #0096FC" + targetNick + ". §fSprawdź logi serwera."));
                });
            }
        });

        return true;
    }

    /**
     * Parsuje string czasu (np. "10s", "5m", "2h", "3d") na milisekundy.
     * Obsługuje złożone formaty (np. "1d3h5m").
     * @param timeString String reprezentujący czas.
     * @return Czas w milisekundach, lub 0 jeśli format jest nieprawidłowy.
     */
    private long parseTimeString(String timeString) {
        long totalMillis = 0;
        Matcher matcher = TIME_PATTERN.matcher(timeString);

        if (!matcher.find()) {
            return 0;
        }
        matcher.reset();

        while (matcher.find()) {
            long value = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2);

            switch (unit) {
                case "s":
                    totalMillis += value * 1000L;
                    break;
                case "m":
                    totalMillis += value * 1000L * 60L;
                    break;
                case "h":
                    totalMillis += value * 1000L * 60L * 60L;
                    break;
                case "d":
                    totalMillis += value * 1000L * 60L * 60L * 24L;
                    break;
                case "w": // Tygodnie
                    totalMillis += value * 1000L * 60L * 60L * 24L * 7L;
                    break;
                case "M": // Miesiące (przyjmujemy średnio 30 dni)
                    totalMillis += value * 1000L * 60L * 60L * 24L * 30L;
                    break;
                case "y": // Lata (przyjmujemy średnio 365 dni)
                    totalMillis += value * 1000L * 60L * 60L * 24L * 365L;
                    break;
                default:
                    return 0;
            }
        }
        return totalMillis;
    }

    /**
     * Formatuje czas w milisekundach na czytelny string (np. "3d 5h 13m 2s").
     * @param millis Czas w milisekundach.
     * @return Sformatowany string.
     */
    private String formatDuration(long millis) {
        if (millis < 0) {
            return "0s";
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
        if (seconds > 0 || sb.length() == 0) {
            sb.append(seconds).append("s");
        }

        return sb.toString().trim();
    }
}