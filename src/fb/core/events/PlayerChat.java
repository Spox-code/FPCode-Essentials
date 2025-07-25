// fb.core.events.PlayerChat.java
package fb.core.events;

import fb.core.Main;
import fb.core.api.BanAPI;
import fb.core.api.HexAPI;
import fb.core.api.RanksAPI;
import fb.core.cmds.Chat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerChat implements Listener {

    private final RanksAPI ranksAPI;
    private final BanAPI muteAPI; // Zmieniamy z BanAPI na MuteAPI
    private final Main plugin;
    private final HexAPI hexAPI;

    // Pattern do wyszukiwania wzmianek o graczach: @nick
    private static final Pattern PLAYER_MENTION_PATTERN = Pattern.compile("(?i)@([a-zA-Z0-9_]{2,16})");

    public PlayerChat(Main m, BanAPI muteAPI) { // Konstruktor przyjmuje MuteAPI
        this.plugin = m;
        this.ranksAPI = new RanksAPI(plugin);
        this.muteAPI = muteAPI;
        this.hexAPI = new HexAPI();
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
        Player sender = e.getPlayer();
        String message = e.getMessage();

        // Ustaw domyślny format czatu na podstawie rangi.
        String prefix = ranksAPI.getRankPrefix(ranksAPI.getRank(sender.getName()));
        String suffix = ranksAPI.getRankSuffix(ranksAPI.getRank(sender.getName()));

        prefix = HexAPI.hex(ChatColor.translateAlternateColorCodes('&', prefix));
        suffix = HexAPI.hex(ChatColor.translateAlternateColorCodes('&', suffix));

        // Format czatu z miejscami na nazwę gracza i wiadomość
        final String chatFormat = prefix + " %s" + ChatColor.GRAY + ": " + suffix + "%s";
        e.setFormat(chatFormat);

        if(!Chat.chat){
            if(!ranksAPI.hasPermission(sender, "fb.chat.bypass")){
                e.setCancelled(true);
                sender.sendMessage("§cChat jest wylaczony");
            }
        }

        if (muteAPI.isMutePlayer(sender.getName())) {
            e.setCancelled(true); // Anuluj wydarzenie, jeśli gracz jest wyciszony

            // Wiadomości o wyciszeniu (wysyłane tylko do wyciszonego gracza)
            Timestamp muteEndTime = muteAPI.getMuteTime(sender.getName());
            String remainingTime;

            if (muteEndTime == null) {
                remainingTime = "PERMANENTNIE";
            } else {
                long millisLeft = muteEndTime.getTime() - System.currentTimeMillis();
                if (millisLeft <= 0) { // Jeśli czas wygasł, ale w bazie nadal jest jako mute
                    remainingTime = "Wygasł (system zaraz powinien odciszyć)";
                    // Możesz tu dodać logikę do natychmiastowego odciszenia,
                    // ale to już wymaga asynchronicznego zapytania do bazy danych.
                    // Na razie, po prostu wyświetlamy, że wygasł.
                } else {
                    remainingTime = formatDuration(millisLeft);
                }
            }

            sender.sendMessage(hexAPI.hex("§8[#0096FC⚡§8] §fJesteś wyciszony! Pozostało czasu wyciszenia: #0096FC" + remainingTime));
            sender.sendMessage(hexAPI.hex("§8[#0096FC⚡§8] §fPowód: #0096FC" + muteAPI.getMuteReason(sender.getName())));
            sender.sendMessage(hexAPI.hex("§8[#0096FC⚡§8] §fPrzez: #0096FC" + muteAPI.getMuteAdmin(sender.getName())));
            return; // Zakończ przetwarzanie, jeśli gracz jest wyciszony
        }

        // --- FUNKCJA PINGOWANIA GRACZY ---
        Matcher matcher = PLAYER_MENTION_PATTERN.matcher(message);
        while (matcher.find()) {
            String mentionedNick = matcher.group(1); // Pobierz nick bez "@"
            Player mentionedPlayer = Bukkit.getPlayerExact(mentionedNick); // Szukaj gracza dokładnie po nicku

            if (mentionedPlayer != null && mentionedPlayer.isOnline() && !mentionedPlayer.equals(sender)) {
                // Wysyłaj tytuł tylko, jeśli wspomniany gracz jest online i to nie jest nadawca
                final Player finalMentionedPlayer = mentionedPlayer; // Musi być final dla lambdy

                // Wysyłamy tytuł asynchronicznie, aby nie blokować wątku czatu
                Bukkit.getScheduler().runTask(plugin, () -> {
                    finalMentionedPlayer.sendTitle(
                            hexAPI.hex("#0096FC⚡ Zostałeś wspomniany!"), // Główny tytuł
                            hexAPI.hex("§fPrzez: #0096FC" + sender.getName()), // Podtytuł
                            10, // Fade In (ticks)
                            70, // Stay (ticks)
                            20  // Fade Out (ticks)
                    );
                });
            }
        }
    }

    /**
     * Formatuje czas w milisekundach na czytelny string (np. "3d 5h 13m 2s").
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
        // Zawsze pokazuj sekundy, jeśli nie ma większych jednostek, lub jeśli to tylko sekundy
        if (seconds > 0 || sb.length() == 0) {
            sb.append(seconds).append("s");
        }

        return sb.toString().trim();
    }
}