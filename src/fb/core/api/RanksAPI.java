package fb.core.api;


import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import fb.core.Main;
import fb.core.data.ConfigData;
import fb.core.data.MySQL;
import fb.core.data.RanksData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class RanksAPI {
    private final RanksData rd; // Zmień na niestatyczne
    private final MySQL mySQL; // Zmień na niestatyczne
    private final Main plugin; // Zmień na niestatyczne i użyj 'plugin'
    static BanAPI banAPI;

    public RanksAPI(Main plugin) { // Konstruktor przyjmuje Main
        this.plugin = plugin; // Przypisz plugin

        // Zainicjalizuj ConfigData, jeśli jeszcze nie jest
        ConfigData.initialize(plugin); // Upewnij się, że jest zainicjalizowane

        // Teraz możesz bezpiecznie tworzyć instancje
        this.rd = RanksData.getInstance(); // Powinien już być zainicjalizowany przez Main
        this.mySQL = new MySQL(plugin); // Przekaż plugin do MySQL

        try {
            MySQL.connect(); // Metoda connect powinna być statyczna lub wywołana na instancji
            mySQL.createTables(); // Metoda createTables powinna być wywołana na instancji
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Nie udało się zainicjalizować połączenia z bazą danych dla rang. Plugin może nie działać poprawnie.", e);
        }
    }

    public void setRank(Player cel, String rank) {
        mySQL.setPlayerRank(cel.getName(), rank);
        TabListAPI.pupdate(cel);
    }

    public void setDefaultRank(String rank) {
        setDefaultRank(rank);
    }

    public void addRankPermission(String rank, String permission) {
        mySQL.addRankPermission(rank, permission);
    }

    public void removeRankPermission(String rank, String permission) {
        mySQL.removeRankPermission(rank,permission);
    }

    public void setRankPrefix(String rank, String prefix) {
        mySQL.setRankPrefix(rank, prefix);
    }

    public void setRankSuffix(String rank, String suffix){
        mySQL.setRankSuffix(rank, suffix);
    }

    public String getRank(String playername) {
        String rank = mySQL.getPlayerRank(playername);
        if(rank == null){
            rank = getDefaultRank();
        }
        return rank;
    }


    public boolean hasPermission(Player p, String permission) {
        boolean solutin = false;
        String rank = this.getRank(p.getName());
        List<String> perms = mySQL.getRankPermissions(rank);
        solutin = perms.contains(permission);
        if(p.isOp()){
            solutin = true;
        }
        return solutin;
    }

    public String getRankPrefix(String rank) {
        String prefix = mySQL.getRankPrefix(rank);
        String x = HexAPI.hex(prefix);
        return x;
    }

    public String getRankSuffix(String rank) {
        String suffix = mySQL.getRankSuffix(rank);
        String x = HexAPI.hex(suffix);
        return x;
    }

    public String getDefaultRank() {
        String defaultRank = mySQL.getDefaultRank();
        return defaultRank;
    }

    public void createRank(String rank, String prefix) {
        mySQL.createRank(rank, prefix, "&f");
    }

    public void reloadConfig() {

    }


    public List<String> getRanks() {
        List<String> list = mySQL.getRanks();
        return list;
    }

    public int getRankWeight(String rankName) {
        return mySQL.getRankWeight(rankName);
    }
    public void setWeight(String rank, int weight){
        mySQL.setRankWeight(rank, weight);
    }
}