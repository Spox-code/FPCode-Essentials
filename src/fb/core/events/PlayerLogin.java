package fb.core.events;

import fb.core.Main;
import fb.core.api.BanAPI;
import fb.core.api.HexAPI; // Upewnij się, że masz tę klasę

import org.bukkit.ChatColor; // Nadal potrzebne, jeśli HexAPI go używa wewnętrznie
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID; // Choć nie używamy UUID bezpośrednio do sprawdzenia bana w tej wersji
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class PlayerLogin implements Listener {

    private static Main plugin;
    private static HexAPI hexAPI; // Instancja do kolorowania wiadomości
    private final ZoneId displayZoneId; // Zmieniamy nazwę i będziemy ją pobierać z Main
    private final DateTimeFormatter dateFormatter;

    public PlayerLogin(Main plugin) {
        this.plugin = plugin;
        this.hexAPI = new HexAPI();
        // --- NOWY KOD: Pobieranie strefy czasowej z Main ---
        this.displayZoneId = plugin.getServerZoneId();
        this.dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        // --- KONIEC NOWEGO KODU ---
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent e) {
        String playerName = e.getPlayer().getName();

        // Sprawdzamy, czy gracz jest zbanowany za pomocą BanAPI
        if (BanAPI.isPlayerBanned(playerName)) {
            // Jeśli jest zbanowany, pobieramy powód i czas zakończenia
            String reason = BanAPI.getBanReason(playerName);
            Timestamp endTime = BanAPI.getBanEndTime(playerName);

            // Tworzymy wiadomość o kicku
            StringBuilder kickMessage = new StringBuilder();
            kickMessage.append(HexAPI.hex("\n&9&lFP&f&lCode\n&cZostałeś zbanowany z serwera!\n"));
            kickMessage.append(HexAPI.hex("&cPowód: &f")).append(reason != null ? reason : "Brak podanego powodu").append("\n");
            kickMessage.append(HexAPI.hex("&cAdministrator: &f" + BanAPI.getBanAdmin(playerName) + "\n"));

            if (endTime != null) {
                long remainingMillis = endTime.getTime() - System.currentTimeMillis();
                if (remainingMillis > 0) {
                    // Formatujemy pozostały czas
                    String remainingTimeFormatted = formatDuration(remainingMillis);
                    kickMessage.append(HexAPI.hex("&cBan wygasa za: &f")).append(remainingTimeFormatted).append("\n");
                } else {
                    // Jeśli ban wygasł, ale isPlayerBanned nadal zwróciło true (co jest mało prawdopodobne, ale dla pewności)
                    kickMessage.append(HexAPI.hex("&cTwój ban wygasł. Spróbuj ponownie za chwilę.\n"));
                    BanAPI.unbanPlayer(playerName, "Konsola"); // Odbanuj gracza, jeśli ban faktycznie wygasł
                }
            } else {
                // Jeśli endTime jest null, zakładamy, że to ban permanentny
                kickMessage.append(HexAPI.hex("&cBan: &fPERMANENTNY\n"));
            }
            kickMessage.append(HexAPI.hex("\n&8----------------------\n"));
            kickMessage.append(HexAPI.hex("&fWejdz na discorda #0096fcdc.fpcode.pl"));

            // Odrzucamy logowanie z wiadomością
            e.disallow(Result.KICK_BANNED, kickMessage.toString());
            plugin.getLogger().log(Level.INFO, "Gracz " + playerName + " próbował dołączyć, ale jest zbanowany.");
        }
        // Jeśli gracz nie jest zbanowany, nic nie robimy - pozwalamy mu dołączyć.
    }

    /**
     * Formatuje czas trwania w milisekundach na czytelny format (np. "29d 13h 53m 14s").
     * Ta metoda jest niezależna od API.
     * @param millis Czas w milisekundach.
     * @return Sformatowany string.
     */
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