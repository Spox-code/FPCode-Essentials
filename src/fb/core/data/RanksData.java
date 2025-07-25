package fb.core.data;

import fb.core.Main;
import fb.core.api.BanAPI;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class RanksData {
    static RanksData instance = new RanksData();
    private MySQL mySQL;
    private Plugin p;
    static Main plugin;
    static BanAPI banAPI;

    public RanksData() {
    }

    public static RanksData getInstance() {
        return instance;
    }

    public void setup(Plugin p) {
        this.p = p;
        plugin = Main.getMain();
        ConfigData config = ConfigData.getInstance();
        config.setup(p); // Upewnij się, że config jest załadowany

        String host = config.getData().getString("mysql.host");
        int port = config.getData().getInt("mysql.port");
        String database = config.getData().getString("mysql.basename");
        String user = config.getData().getString("mysql.user");
        String password = config.getData().getString("mysql.password");

        this.mySQL = new MySQL(plugin);
        try {
            this.mySQL.connect();
            this.mySQL.createTables(); // Utwórz tabele po połączeniu
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się zainicjalizować połączenia z bazą danych dla rang. Plugin może nie działać poprawnie.", e);
            // Tutaj możesz zdecydować, czy wyłączyć plugin, jeśli połączenie z BD jest kluczowe
            // p.getPluginLoader().disablePlugin(p);
        }
    }

    public Connection getConnection() {
        return mySQL.getConnection();
    }

    public void disconnect() {
        if (mySQL != null) {
            mySQL.disconnect();
        }
    }
}