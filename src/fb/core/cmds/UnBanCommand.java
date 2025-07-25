package fb.core.cmds;

import fb.core.Main;
import fb.core.api.BanAPI;
import fb.core.api.HexAPI; // Dodaj import dla HexAPI
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.logging.Level;

public class UnBanCommand implements CommandExecutor {

    private final Main plugin;
    private final HexAPI hexAPI; // Dodajemy instancję HexAPI

    public UnBanCommand(Main plugin) { // Zmieniony konstruktor
        this.plugin = plugin;
        this.hexAPI = new HexAPI(); // Inicjalizacja HexAPI
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Sprawdź uprawnienia do użycia komendy
        if (!sender.hasPermission("fb.unban")) {
            sender.sendMessage(hexAPI.hex("§cNie posiadasz uprawnienia (fb.unban)"));
            return true;
        }

        // Sprawdź, czy liczba argumentów jest poprawna
        if (args.length != 1) {
            sender.sendMessage(hexAPI.hex("§8[#0096FC⚡§8] §fUżycie: #0096FC/unban <gracz>"));
            return true;
        }

        String targetName = args[0];
        String adminName = (sender instanceof Player) ? sender.getName() : "CONSOLE";

        // Sprawdź, czy gracz jest zbanowany
        if (!BanAPI.isPlayerBanned(targetName)) {
            sender.sendMessage(hexAPI.hex("§cGracz §f" + targetName + " §cnie jest zbanowany."));
            return true;
        }

        // Wywołaj BanAPI do odbanowania gracza
        BanAPI.unbanPlayer(targetName, adminName); // Używamy zaktualizowanej metody z adminem
        sender.sendMessage(hexAPI.hex("§8[#0096FC⚡§8] §fOdbanowano gracza #0096FC" + targetName));

        // Powiadomienie gracza, jeśli jest online (opcjonalnie)
        Player targetPlayer = Bukkit.getPlayerExact(targetName);
        if (targetPlayer != null && targetPlayer.isOnline()) {
            targetPlayer.sendMessage(hexAPI.hex("§aZostałeś odbanowany z serwera!"));
            targetPlayer.sendMessage(hexAPI.hex("§aPrzez: §f" + adminName));
        }

        return true;
    }
}