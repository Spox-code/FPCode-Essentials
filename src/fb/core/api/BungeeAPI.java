package fb.core.api;

import fb.core.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener; // Dodaj ten import

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayInputStream; // Dodaj ten import
import java.io.DataInputStream;     // Dodaj ten import
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class BungeeAPI implements PluginMessageListener { // BungeeAPI musi implementować PluginMessageListener

    private static Main plugin; // Zmieniamy na statyczne pole, jak chciałeś
    // Mapa do przechowywania aktualnej liczby graczy dla każdego serwera
    private static final Map<String, Integer> onlinePlayersCache = new HashMap<>();
    // Mapa do przechowywania CompletableFuture dla oczekujących zapytań o liczbę graczy
    private static final Map<String, CompletableFuture<Integer>> pendingPlayerCountRequests = new HashMap<>();

    public BungeeAPI(Main m){
        plugin = m; // Inicjalizacja statycznego pola pluginu

        // Rejestracja kanałów BungeeCord do wysyłania i odbierania wiadomości
        // Te rejestracje MUSZĄ być wykonane JEDNOKROTNIE i muszą być powiązane z instancją listenera.
        // Najlepiej jest to zrobić w konstruktorze lub w onEnable w Main.
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, "BungeeCord", this); // "this" odnosi się do instancji BungeeAPI jako listenera
    }

    public static void sendMessage(Player p, String msg){
        if (plugin == null) {
            Bukkit.getLogger().severe("BungeeAPI: Plugin nie został zainicjalizowany! Nie można wysłać wiadomości.");
            return;
        }
        try{
            ByteArrayOutputStream arr = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(arr);
            out.writeUTF("Message");
            out.writeUTF("ALL"); // Możesz zmienić na konkretnego gracza lub serwer
            out.writeUTF(msg);
            p.sendPluginMessage(plugin, "BungeeCord", arr.toByteArray());
        }catch (Exception e){
            plugin.getLogger().log(Level.SEVERE, "Błąd podczas wysyłania wiadomości BungeeCord: " + e.getMessage(), e);
        }
    }

    public static CompletableFuture<Integer> requestOnlinePlayers(String server) {
        if (plugin == null) {
            // Zwróć zakończony Future z błędem lub 0, jeśli plugin nie zainicjalizowany
            CompletableFuture<Integer> future = new CompletableFuture<>();
            future.complete(0); // lub future.completeExceptionally(new IllegalStateException("Plugin not initialized"));
            //Bukkit.getLogger().severe("BungeeAPI: Plugin nie został zainicjalizowany! Nie można wysłać zapytania o graczy.");
            return future;
        }

        // Stwórz nowy CompletableFuture, aby zwrócić wynik, gdy nadejdzie
        CompletableFuture<Integer> future = new CompletableFuture<>();
        pendingPlayerCountRequests.put(server, future); // Przechowuj future dla danej nazwy serwera

        // Musi być co najmniej jeden gracz online, aby wysłać wiadomość pluginową.
        Player player = Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
        if (player == null) {
            future.complete(0); // Brak graczy online na tym serwerze Bukkit, więc nie można wysłać zapytania
            //plugin.getLogger().warning("BungeeAPI: Brak graczy online, nie można wysłać zapytania o listę graczy dla serwera: " + server);
            pendingPlayerCountRequests.remove(server); // Usuń future, bo nie zostanie rozwiązany
            return future;
        }

        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            out.writeUTF("PlayerCount"); // Podkomenda do liczenia graczy
            out.writeUTF(server);       // Nazwa serwera, dla którego chcemy liczbę graczy
            player.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
            //plugin.getLogger().info("BungeeAPI: Wysłano zapytanie o liczbę graczy dla serwera: " + server);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Błąd podczas wysyłania zapytania PlayerCount dla serwera " + server + ": " + e.getMessage(), e);
            future.completeExceptionally(e);
            pendingPlayerCountRequests.remove(server); // Usuń future w przypadku błędu
        }
        return future;
    }

    public static int getCachedOnlinePlayers(String server) {
        return onlinePlayersCache.getOrDefault(server, 0);
    }

    @Override // Ta metoda jest częścią interfejsu PluginMessageListener
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }

        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
            String subchannel = in.readUTF(); // Subkomenda (np. "PlayerCount", "GetServers")

            if (subchannel.equals("PlayerCount")) {
                String server = in.readUTF(); // Nazwa serwera (np. "ALL", "survival")
                int playerCount = in.readInt(); // Liczba graczy

                //plugin.getLogger().info("BungeeAPI: Otrzymano liczbę graczy dla serwera '" + server + "': " + playerCount);

                // Zaktualizuj cache
                onlinePlayersCache.put(server, playerCount);

                // Dokończ CompletableFuture, jeśli jakieś zapytanie czeka
                CompletableFuture<Integer> future = pendingPlayerCountRequests.remove(server);
                if (future != null) {
                    future.complete(playerCount);
                }
            }
            // Możesz tutaj obsłużyć inne podkomendy BungeeCord, np. "GetServers"
            // else if (subchannel.equals("GetServers")) { ... }

        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Błąd podczas odbierania wiadomości BungeeCord: " + e.getMessage(), e);
        }
    }
    public static void sendPlayerToServer(Player p, String server){
        // WAŻNE: W tej strategii, to Twój plugin będzie odpowiedzialny za **jedyny** komunikat błędu.
        // Jeśli BungeeCord wyświetli coś na czacie, ten kod tego nie powstrzyma,
        // ale gracz zobaczy Twój tytuł i wiadomość, co może "przykryć" domyślny komunikat.

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeUTF("Connect");
            dos.writeUTF(server);
        } catch (IOException e) {
            // Ten blok catch dotyczy BŁĘDÓW WEWNĘTRZNYCH podczas PRZYGOTOWYWANIA wiadomości,
            // a nie błędów połączenia z serwerem BungeeCord.
            e.printStackTrace();
            // W przypadku błędu przygotowania wiadomości, od razu wysyłamy tytuł błędu
            // i nie próbujemy wysyłać wiadomości pluginowej, bo jest uszkodzona.
            sendErrorTitle(p); // Wyślij nasz niestandardowy tytuł błędu
            p.sendMessage(HexAPI.hex("§cWystąpił wewnętrzny błąd. Spróbuj ponownie.")); // Wiadomość na czat
            return;
        }

        // Zawsze wysyłamy wiadomość do BungeeCorda, aby spróbował przenieść gracza.
        // Nawet jeśli serwer jest offline, BungeeCord spróbuje go przenieść, a gracz pozostanie na obecnym serwerze.
        p.sendPluginMessage(plugin, "BungeeCord", baos.toByteArray());

        // Informacja dla gracza o próbie przeniesienia - to zostanie wyświetlone ZAWSZE.
        p.sendMessage(HexAPI.hex("§fPrzenoszenie na tryb #0096fc"+server));

        // --- Logika do sprawdzania błędu przeniesienia ---
        // Planujemy zadanie, które sprawdzi po krótkim czasie, czy gracz nadal jest na tym samym serwerze.
        // Jeśli tak, zakładamy, że transfer się nie powiódł i wyświetlamy NASZ błąd.
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Sprawdź, czy gracz jest nadal online i czy to nadal jest ten sam gracz
            if (p.isOnline() && p.isValid()) {
                // Heurystyka: Jeśli gracz po 3 sekundach nadal jest na tym serwerze,
                // to oznacza, że transfer BungeeCord prawdopodobnie się nie powiódł.
                // W tym momencie, jeśli BungeeCord wyświetlił swój błąd, to już się stało.
                // My po prostu wyświetlimy NASZ tytuł i wiadomość, niezależnie od tego, co BungeeCord pokazał.
                sendErrorTitle(p);
            }
        }, 2 * 20L); // 3 sekundy (60 tyknięć)
    }

    // Metoda sendErrorTitle pozostaje bez zmian, jak ją zmodyfikowałeś ostatnio
    private static void sendErrorTitle(Player p) {
            // Dla Spigot (starsze wersje)
            p.sendTitle(
                    HexAPI.hex("#ff0000✕ ʙʟᴀᴅ ᴘᴏʟᴀᴄᴢᴇɴɪᴀ! #ff0000✕"),
                    HexAPI.hex("§7ѕᴇʀᴡᴇʀ ɴɪᴇᴅᴏѕᴛᴇᴘɴʏ!"),
                    10, // fadeIn (tyki)
                    60, // stay (tyki)
                    20  // fadeOut (tyki)
            );
        p.sendMessage(HexAPI.hex("§cBłąd: Nie udało się połączyć z serwerem.")); // Dodatkowa wiadomość na czat
    }
}