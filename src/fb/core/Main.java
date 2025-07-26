package fb.core;

import fb.core.api.*;
import fb.core.cmds.*;
import fb.core.cmds.TempMuteCommand;
import fb.core.data.ConfigData;
import fb.core.data.MySQL;
import fb.core.data.RanksData;
import fb.core.events.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.ZoneId;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class Main extends JavaPlugin {

    private static Main main; // Statyczna instancja pluginu
    private ConfigData configData;
    private MySQL mySQL;
    private RanksData ranksData;
    private RanksAPI ranksAPI;
    private BanAPI banAPI;
    private TabListAPI tabListAPI; // Dodaj pole dla TabListAPI, jeśli nie jest singletonem używanym statycznie
    // Jeśli Sidebar też nie jest statyczny, dodaj pole i dla niego.

    private final ExecutorService scheduler = Executors.newCachedThreadPool();

    @Override
    public void onEnable() {
        main = this; // Ustaw statyczną instancję

        // 1. Inicjalizacja ConfigData (musi być najpierw, bo inni jej używają)
        configData = ConfigData.getInstance();
        configData.setup(this); // Przekaż instancję Main do setup ConfigData

        // --- Zarządzanie zależnościami: MySQL, BanAPI, RanksAPI ---

        // 2. Inicjalizacja MySQL
        // MySQL potrzebuje Main do logowania i ConfigData do danych połączeniowych.
        // Jeśli konstruktor MySQL pobiera ConfigData przez getInstance(), to configData musi być już zainicjalizowane.
        this.mySQL = new MySQL(this); // Przekazujemy Main do MySQL

        // 3. Połącz z bazą danych i utwórz tabele
        try {
            MySQL.connect(); // Statyczna metoda połączenia
            this.mySQL.createTables(); // Niestatyczna metoda do tworzenia tabel
            getLogger().info("Połączono z bazą danych MySQL i utworzono tabele.");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Nie udało się połączyć z bazą danych lub utworzyć tabel! Plugin zostanie wyłączony.", e);
            getServer().getPluginManager().disablePlugin(this);
            return; // Przerwij dalsze ładowanie, jeśli połączenie z BD się nie udało
        }

        // 4. Inicjalizacja BanAPI
        // BanAPI potrzebuje dostępu do MySQL. Teraz możemy bezpiecznie przekazać zainicjalizowany mySQL.
        this.banAPI = new BanAPI(this);
        getLogger().info("BanAPI zostało zainicjalizowane.");


        // 5. Inicjalizacja RanksData i RanksAPI
        // RanksData i RanksAPI również potrzebują MySQL.
        ranksData = RanksData.getInstance();
        ranksData.setup(this); // Jeśli RanksData potrzebuje Main, przekazujemy je.
        this.ranksAPI = new RanksAPI(this); // RanksAPI potrzebuje Main i MySQL
        getLogger().info("RanksAPI zostało zainicjalizowane.");


        // --- Inicjalizacja pozostałych API/Listenerów/Komend ---

        // Sidebar i TabListAPI - zakładam, że przyjmują Main
        // Jeśli TabListAPI używa statycznych metod (jak pupdate), upewnij się, że ich konstruktor
        // ustawia statyczną instancję lub używa getterów z Main.
        new Sidebar(this, banAPI); // Zakładam, że Sidebar istnieje i przyjmuje Main oraz BanAPI
        this.tabListAPI = new TabListAPI(this); // Jeśli TabListAPI nie jest singletonem, użyj pola

        new BungeeAPI(this); // BungeeAPI
        new BossBarAPI(this);

        // Rejestracja komend - upewnij się, że konstruktory komend są zgodne
        getCommand("rank").setExecutor(new Rank(this)); // Prawdopodobnie RankCmd potrzebuje RanksAPI
        getCommand("configreload").setExecutor(new ConfigReload(this));
        getCommand("tempban").setExecutor(new TempBanCommand(this, banAPI));
        getCommand("unban").setExecutor(new UnBanCommand(this));
        getCommand("tempmute").setExecutor(new TempMuteCommand(this, banAPI));
        getCommand("unmute").setExecutor(new UnMuteCommand(this, banAPI));
        getCommand("helpop").setExecutor(new HelpOP(this));
        getCommand("alertmsg").setExecutor(new Alert(this));
        getCommand("gamemode").setExecutor(new Gamemode(this));
        getCommand("warn").setExecutor(new WarnCommand(this, banAPI));
        getCommand("playerinfo").setExecutor(new PlayerInfo(this));
        getCommand("msg").setExecutor(new Msg(this));
        getCommand("food").setExecutor(new Food(this));
        getCommand("heal").setExecutor(new Heal(this));
        getCommand("chat").setExecutor(new Chat(this));
        getCommand("bctitle").setExecutor(new BCTitle(this));
        getCommand("odbierz").setExecutor(new Odbierz(this));
        getCommand("live").setExecutor(new Live(this));

        // Rejestracja zdarzeń (listenerów)
        getServer().getPluginManager().registerEvents(new PlayerChat(this, banAPI), this);
        getServer().getPluginManager().registerEvents(new PlayerJoin(this), this); // PlayerJoin często potrzebuje Main i RanksAPI
        getServer().getPluginManager().registerEvents(new PlayerQuit(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInventory(ranksAPI), this);
        getServer().getPluginManager().registerEvents(new PlayerLogin(this), this); // PlayerLogin często potrzebuje Main i BanAPI

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");


        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPI(this, ranksAPI).register();
            getLogger().info("Zarejestrowano rozszerzenie PlaceholderAPI dla FPCode-Survival.");
        } else {
            getLogger().warning("PlaceholderAPI nie znaleziono! Niektóre funkcje mogą być niedostępne.");
        }

        // Task do aktualizacji Sidebara / TabListy
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            if (configData.getData().getBoolean("sidebar.enabled", false)) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    Bukkit.getScheduler().runTask(this, () -> {
                        // Jeśli TabListAPI jest polem klasy, wywołaj na nim metodę instancji:
                        if (tabListAPI != null) {
                            tabListAPI.pupdate(p); // Nazwa metody może się różnić (np. updateTabList)
                        } else {
                            // Jeśli TabListAPI jest singletonem, który ma statyczną metodę:
                            // TabListAPI.pupdate(p);
                        }
                    });
                }
            }
        }, 20L, 100L); // Opóźnienie 1s, powtarzaj co 5s

        // Task do odświeżania liczby graczy BungeeCord
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (!Bukkit.getOnlinePlayers().isEmpty()) {
                if (configData.getData().isSet("servers.list")) {
                    for (String serverName : configData.getData().getStringList("servers.list")) {
                        BungeeAPI.requestOnlinePlayers(serverName);
                    }
                }
                BungeeAPI.requestOnlinePlayers("ALL");
            }
        }, 100L, 100L); // Opóźnienie 5s, powtarzaj co 5s
    }

    @Override
    public void onDisable() {
        if (mySQL != null) {
            MySQL.disconnect(); // Użyj statycznej metody disconnect
        }
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown(); // Zamknij ExecutorService
        }
        getLogger().info("FPCode-Essentials wyłączony!");
    }

    // --- Gettery ---
    public static Main getMain() {
        return main;
    }

    public ConfigData getConfigData() {
        return configData;
    }

    public MySQL getMySQL() {
        return mySQL;
    }

    public RanksData getRanksData() {
        return ranksData;
    }

    public RanksAPI getRanksAPI() {
        return ranksAPI;
    }

    public BanAPI getBanAPI(){
        return banAPI;
    }

    public ExecutorService getScheduler() {
        return scheduler;
    }
    // Dodaj inne gettery dla TabListAPI, Sidebar, jeśli masz pola dla nich
    public TabListAPI getTabListAPI() {
        return tabListAPI;
    }
    public ZoneId getServerZoneId() {
        return ZoneId.of("Europe/Warsaw");
    }
}