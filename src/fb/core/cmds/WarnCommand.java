package fb.core.cmds;

import fb.core.Main;
import fb.core.api.BanAPI;
import fb.core.api.HexAPI;
import fb.core.data.MySQL;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.OfflinePlayer; // Dodane do pobierania UUID offline graczy
import java.util.UUID; // Dodane do pracy z UUID

import static org.bukkit.Bukkit.getServer;

public class WarnCommand implements CommandExecutor {

    private final Main plugin;
    // Zmieniono na niestatyczne i final - lepsza praktyka
    // W Main musisz przekazywać instancję BanAPI do konstruktora WarnCommand.
    private final BanAPI banAPI;
    private final HexAPI hexAPI;

    // Konstruktor dostosowany do przyjmowania instancji Main i BanAPI
    public WarnCommand(Main plugin, BanAPI banAPI) {
        this.plugin = plugin;
        this.banAPI = banAPI;
        this.hexAPI = new HexAPI();
        // Usunięto ZoneId i DateTimeFormatter, bo nie są potrzebne do ostrzeżeń bez czasu
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Uprawnienie dla komendy warn
        if (!sender.hasPermission("fb.warn")) {
            sender.sendMessage(hexAPI.hex("§cNie posiadasz uprawnienia (fb.warn)"));
            return true;
        }

        // Sprawdzenie składni komendy
        if (args.length < 2) { // Oczekujemy <gracz> <powód>
            sender.sendMessage(hexAPI.hex("§8[#0096FC⚡§8] §fUżycie: #0096FC/warn <gracz> <powód>"));
            return true;
        }

        String targetName = args[0];
        StringBuilder reasonBuilder = new StringBuilder();
        // Powód zaczyna się od drugiego argumentu (indeks 1)
        for (int i = 1; i < args.length; i++) {
            reasonBuilder.append(args[i]).append(" ");
        }
        String reason = reasonBuilder.toString().trim();

        // Nie można ostrzec samego siebie
        if (sender instanceof Player && sender.getName().equalsIgnoreCase(targetName)) {
            sender.sendMessage(hexAPI.hex("§cNie możesz ostrzec samego siebie!"));
            return true;
        }

        // Nie można ostrzec konsoli (technicznie)
        if (targetName.equalsIgnoreCase("console") || targetName.equalsIgnoreCase(MySQL.CONSOLE_UUID.toString())) {
            sender.sendMessage(hexAPI.hex("§cNie możesz ostrzec konsoli!"));
            return true;
        }

        // Sprawdzamy, czy gracz istnieje offline
        OfflinePlayer targetOfflinePlayer = Bukkit.getOfflinePlayer(targetName);
        if (targetOfflinePlayer == null || targetOfflinePlayer.getUniqueId() == null) {
            sender.sendMessage(hexAPI.hex("§cGracz o nazwie §f" + targetName + " §cnie został znaleziony."));
            return true;
        }
        // Pobieramy UUID gracza docelowego
        UUID targetUUID = targetOfflinePlayer.getUniqueId();

        // Pobieramy nazwę admina/konsoli
        String adminName = (sender instanceof Player) ? sender.getName() : "CONSOLE";

        // === Wywołanie metody WarnPLayer z BanAPI ===
        // Zgodnie z Twoim BanAPI, WarnPLayer przyjmuje String nazwy gracza i String powodu.
        // Jeśli chcesz używać UUID w WarnPLayer, musiałbyś zmienić sygnaturę tej metody w BanAPI.
        boolean success = BanAPI.WarnPLayer(targetName, reason);

        if (success) {
            // Wiadomość dla wykonującego komendę
            sender.sendMessage(hexAPI.hex("§8[#0096FC⚡§8] §fOstrzeżono gracza #0096FC" + targetName + " §fz powodu: #0096FC" + reason + "§f."));

            // Wiadomość dla ostrzeżonego gracza, jeśli jest online
            Player targetPlayer = Bukkit.getPlayerExact(targetName);
            if (targetPlayer != null && targetPlayer.isOnline()) {
                targetPlayer.sendMessage("");
                targetPlayer.sendMessage(hexAPI.hex("   #0096FCFPCode.PL §7- §fZostałeś ostrzeżony!"));
                targetPlayer.sendMessage(hexAPI.hex("   §fPowód: #0096FC" + reason));
                targetPlayer.sendMessage(hexAPI.hex("   §fAdmin: #0096FC" + adminName));
                targetPlayer.sendMessage("");
            }
            if(BanAPI.getCountWarn(targetName) == 2){
                BanAPI.unMutePlayer(targetName, "CONSOLE");
                getServer().dispatchCommand(getServer().getConsoleSender(), "tempmute " + targetName + " 1h Ostrzeżenia");
            }else if(BanAPI.getCountWarn(targetName) == 3){
                BanAPI.unMutePlayer(targetName, "CONSOLE");
                getServer().dispatchCommand(getServer().getConsoleSender(), "tempmute " + targetName + " 1d Ostrzeżenia");
            }else if(BanAPI.getCountWarn(targetName) == 4){
                BanAPI.unMutePlayer(targetName, "CONSOLE");
                getServer().dispatchCommand(getServer().getConsoleSender(), "tempmute " + targetName + " 7d Ostrzeżenia");
            }else if(BanAPI.getCountWarn(targetName) == 5){
                BanAPI.unbanPlayer(targetName, "CONSOLE");
                getServer().dispatchCommand(getServer().getConsoleSender(), "tempban " + targetName + " 1d Ostrzeżenia");
            }else if(BanAPI.getCountWarn(targetName) == 6){
                BanAPI.unbanPlayer(targetName, "CONSOLE");
                getServer().dispatchCommand(getServer().getConsoleSender(), "tempmute " + targetName + " 365d Ostrzeżenia");
            }

            // Wiadomość dla innych administratorów (opcjonalnie, jeśli chcesz powiadomienia)
            for (Player ps : Bukkit.getOnlinePlayers()) {
                if (ps.hasPermission("fb.warn.notify")) { // Uprawnienie do odbierania powiadomień o ostrzeżeniach
                    if (ps != sender) { // Nie wysyłaj do samego siebie, jeśli to admin
                        ps.sendMessage(hexAPI.hex("§8[#0096FC⚡§8] §fGracz #0096FC" + targetName + " §fzostał ostrzeżony przez #0096FC" + adminName + " §fz powodu: #0096FC" + reason));
                    }
                }
            }

        } else {
            // Wiadomość o błędzie
            sender.sendMessage(hexAPI.hex("§cWystąpił błąd podczas dodawania ostrzeżenia dla gracza §f" + targetName + "§c."));
        }

        return true;
    }
}