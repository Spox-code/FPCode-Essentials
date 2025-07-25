package fb.core.data;

import fb.core.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigData {
    static ConfigData instance = new ConfigData();
    Plugin p;
    FileConfiguration data;
    public static File rfile;

    public ConfigData() {
    }

    public static ConfigData getInstance() {
        return instance;
    }
    public static void initialize(Main plugin) {
        if (instance == null) {
            instance = new ConfigData();
        }
    }

    public void setup(Plugin p) {
        this.p = p; // Przypisz plugin do pola instancji

        if (!p.getDataFolder().exists()) {
            p.getDataFolder().mkdir();
        }

        File path = p.getDataFolder(); // Lepszy sposób na uzyskanie katalogu danych pluginu
        rfile = new File(path, "config.yml");

        if (!rfile.exists()) {
            try {
                path.mkdirs(); // Upewnij się, że katalogi istnieją
                rfile.createNewFile();
                // Jeśli plik nie istnieje, ustaw domyślne wartości
                setDefaultConfigValues();
                saveData();
            } catch (IOException var4) {
                p.getLogger().severe("Nie udalo sie stworzyc pliku config.yml: " + var4.getMessage());
            }
        } else {
            // Jeśli plik istnieje, wczytaj go i uzupełnij brakujące sekcje
            this.data = YamlConfiguration.loadConfiguration(rfile);
            checkAndSetMissingConfigValues();
            saveData(); // Zapisz ewentualne nowe wartości
        }

        this.data = YamlConfiguration.loadConfiguration(rfile); // Ponowne wczytanie po ewentualnych zmianach
    }

    // Nowa metoda do ustawiania domyślnych wartości początkowych
    private void setDefaultConfigValues() {
        // MySQL
        data.set("mysql.host", "localhost");
        data.set("mysql.port", 3306);
        data.set("mysql.user", "root");
        data.set("mysql.password", "");
        data.set("mysql.basename", "fpcodemc");

        // Messages
        data.set("messages.join", "&fGracz &a{player} &fdolaczyl/a na serwer");
        data.set("messages.quit", "&fGracz &c{player} &fwyszedl/a z serwera");

        // Tablist
        data.set("tablist.header", "header");
        data.set("tablist.footer", "footer");

        // Servers
        ArrayList<String> servers = new ArrayList<>();
        servers.add("lobby");
        servers.add("survival");
        data.set("servers.list", servers);
        data.set("servers.this", "lobby");

        // Sidebar - NOWA SEKCJA
        data.set("sidebar.enabled", true);
        data.set("sidebar.title", "&6&lTwójSerwer");
        List<String> defaultLines = Arrays.asList(
                " ",
                "&7Witaj, &f{player}",
                "&7Ranga: &f{rank_prefix}{rank_name}", // Placeholder dla rangi
                "&7Online: &a{online_players}/{max_players}", // Placeholder dla graczy online
                " ",
                "&7Twoje saldo: &a${balance}", // Przykładowy placeholder na saldo
                " ",
                "&7Strona: &fwww.twojserwer.pl",
                "&7Discord: &fdiscord.twojserwer.pl"
        );
        data.set("sidebar.lines", defaultLines);
    }

    // Nowa metoda do sprawdzania i ustawiania brakujących wartości, jeśli plik już istnieje
    private void checkAndSetMissingConfigValues() {
        // Sprawdź i ustaw wartości MySQL, jeśli ich brakuje
        if (!data.contains("mysql.host")) data.set("mysql.host", "localhost");
        if (!data.contains("mysql.port")) data.set("mysql.port", 3306);
        if (!data.contains("mysql.user")) data.set("mysql.user", "root");
        if (!data.contains("mysql.password")) data.set("mysql.password", "");
        if (!data.contains("mysql.basename")) data.set("mysql.basename", "fpcodemc");

        // Sprawdź i ustaw wartości wiadomości
        if (!data.contains("messages.join")) data.set("messages.join", "&fGracz &a{player} &fdolaczyl/a na serwer");
        if (!data.contains("messages.quit")) data.set("messages.quit", "&fGracz &c{player} &fwyszedl/a z serwera");

        // Sprawdź i ustaw wartości tablisty
        ArrayList<String> tab = new ArrayList<>();
        tab.add("tablist");
        if (!data.contains("tablist.header")) data.set("tablist.header", tab);
        if (!data.contains("tablist.footer")) data.set("tablist.footer", tab);

        // Sprawdź i ustaw wartości serwerów
        if (!data.contains("servers.list")) {
            ArrayList<String> servers = new ArrayList<>();
            servers.add("lobby");
            servers.add("survival");
            data.set("servers.list", servers);
        }
        if (!data.contains("servers.this")) data.set("servers.this", "lobby");

        // Sprawdź i ustaw wartości sidebara - NOWA SEKCJA
        if (!data.contains("sidebar.enabled")) data.set("sidebar.enabled", true);
        if (!data.contains("sidebar.title")) data.set("sidebar.title", "&6&lTwójSerwer");
        if (!data.contains("sidebar.lines")) {
            List<String> defaultLines = Arrays.asList(
                    " ",
                    "&7Witaj, &f{player}",
                    "&7Ranga: &f{rank_prefix}{rank_name}",
                    "&7Online: &a{online_players}/{max_players}",
                    " ",
                    "&7Twoje saldo: &a${balance}",
                    " ",
                    "&7Strona: &fwww.twojserwer.pl",
                    "&7Discord: &fdiscord.twojserwer.pl"
            );
            data.set("sidebar.lines", defaultLines);
        }
    }

    public FileConfiguration getData() {
        return this.data;
    }

    public void saveData() {
        try {
            this.data.save(rfile);
        } catch (IOException var2) {
            p.getLogger().severe("Nie udalo sie zapisac pliku config.yml: " + var2.getMessage()); // Użyj loggera pluginu
        }
    }

    public void reloadData() {
        this.data = YamlConfiguration.loadConfiguration(rfile);
        checkAndSetMissingConfigValues(); // Zawsze uzupełniaj brakujące wartości po przeładowaniu
        saveData();
    }
}