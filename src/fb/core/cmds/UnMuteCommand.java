// fb.core.cmds.UnmuteCommand.java
package fb.core.cmds;

import fb.core.Main;
import fb.core.api.BanAPI;
import fb.core.api.HexAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class UnMuteCommand implements CommandExecutor { // Nazwa klasy UnmuteCommand

    private final Main plugin;
    private final BanAPI muteAPI; // Zmieniamy z BanAPI na MuteAPI i ustawiamy final
    private final HexAPI hexAPI; // Dodajemy HexAPI
    private final String commandName = HexAPI.hex("#0096fcWyciszenia"); // Dodajemy nazwę komendy/modułu dla spójności

    public UnMuteCommand(Main plugin, BanAPI muteAPI) { // Konstruktor przyjmuje MuteAPI
        this.plugin = plugin;
        this.muteAPI = muteAPI;
        this.hexAPI = new HexAPI(); // Inicjalizacja HexAPI
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 1. Sprawdzenie uprawnień
        if (!sender.hasPermission("fb.unmute")) {
            sender.sendMessage(hexAPI.hex("§8[#0096FC⚡§8] §fNie posiadasz uprawnienia (fb.unmute)"));
            return true;
        }

        // 2. Weryfikacja argumentów
        if (args.length != 1) {
            sender.sendMessage(hexAPI.hex("§8[#0096FC⚡§8] §fPoprawne użycie: #0096FC/unmute <gracz>"));
            return true;
        }

        String targetNick = args[0];
        String adminNick = sender.getName();
        if (!(sender instanceof Player)) {
            adminNick = "CONSOLE"; // Jeśli komenda jest wywołana z konsoli
        }

        // Musimy zadeklarować adminNick jako final lub efektywnie finalny, aby użyć go w lambdzie
        String finalAdminNick = adminNick;

        // 3. Sprawdzenie, czy gracz jest w ogóle wyciszony (opcjonalne, ale dobra praktyka)
        // Możemy to zrobić asynchronicznie, tak jak odbanowanie.
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean isMuted = muteAPI.isMutePlayer(targetNick);

            if (!isMuted) {
                // Jeśli gracz nie jest wyciszony, powiadom o tym administratora w głównym wątku
                Bukkit.getScheduler().runTask(plugin, () -> {
                    sender.sendMessage(hexAPI.hex("§8[#0096FC⚡§8] §fGracz #0096FC" + targetNick + " §fnie jest aktualnie wyciszony."));
                });
                return; // Zakończ działanie zadania asynchronicznego
            }

            // 4. Wykonanie odciszenia za pomocą MuteAPI
            boolean success = muteAPI.unMutePlayer(targetNick, finalAdminNick); // Zmieniamy unMutePlayer na unmutePlayer, zgodnie z konwencją nazw

            if (success) {
                // Wysyłamy wiadomość do administratora (z powrotem w wątku głównym, jeśli to gracz)
                Bukkit.getScheduler().runTask(plugin, () -> {
                    sender.sendMessage(hexAPI.hex("§8[#0096FC⚡§8] §fPomyślnie odciszono gracza #0096FC" + targetNick + " §fprzez: #0096FC" + finalAdminNick));

                    // Wysyłanie wiadomości do docelowego gracza, jeśli jest online
                    Player targetPlayer = Bukkit.getPlayer(targetNick);
                    if (targetPlayer != null && targetPlayer.isOnline()) {
                        // Wiadomość dla odciszonego gracza
                        targetPlayer.sendMessage(hexAPI.hex("§8[#0096FC⚡§8] §fZostałeś odciszony przez #0096FC" + finalAdminNick + ". §fMożesz już pisać na czacie!"));
                    }
                });
            } else {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    sender.sendMessage(hexAPI.hex("§8[#0096FC⚡§8] §fNie udało się odciszyć gracza #0096FC" + targetNick + ". §fSprawdź logi serwera."));
                });
            }
        });

        return true; // Zawsze zwracamy true, bo przetwarzamy asynchronicznie
    }
}