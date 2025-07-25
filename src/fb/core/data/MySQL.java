package fb.core.data;

import fb.core.Main;
import fb.core.api.BanAPI; // Zostawiono, choć nadal nieużywane w tym fragmencie. Prawdopodobnie będzie używane w BanAPI samej.

import java.sql.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList; // Dodane dla przykładów metod CRUD
import java.util.List; // Dodane dla przykładów metod CRUD
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class MySQL {

    private static Connection connection;
    private static String dbHost; // Zmieniłem nazwy na dbHost, dbPort itd. dla jasności
    private static String dbDatabase;
    private static String dbUsername;
    private static String dbPassword;
    private static int dbPort;
    private static Main plugin; // Pole, które później zmieniłem na niestatyczne
    static ConfigData configData; // To było pole statyczne, które nie było inicjalizowane w bezpieczny sposób
    public static final UUID CONSOLE_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private final ZoneId displayZoneId; // NOWE POLE
    private final DateTimeFormatter dateFormatter; // NOWE POLE


    public MySQL(Main mainPlugin) { // Zmieniam nazwę parametru na mainPlugin, aby uniknąć konfliktu z polem 'plugin'
        MySQL.plugin = mainPlugin; // Przypisz instancję Main do statycznego pola
        MySQL.configData = ConfigData.getInstance(); // Upewnij się, że ConfigData jest zainicjalizowane przed użyciem

        // === KLUCZOWA ZMIANA: Przypisuj do STATYCZNYCH pól klasy ===
        MySQL.dbHost = configData.getData().getString("mysql.host", "localhost"); // Użyj wartości domyślnych
        MySQL.dbPort = configData.getData().getInt("mysql.port", 3306);
        MySQL.dbDatabase = configData.getData().getString("mysql.basename", "fpcodemc");
        MySQL.dbUsername = configData.getData().getString("mysql.user", "filip");
        MySQL.dbPassword = configData.getData().getString("mysql.password", "");

        // Debugowanie: Upewnij się, że wartości zostały przypisane
        // --- NOWY KOD: Pobieranie strefy czasowej z Main ---
        this.displayZoneId = plugin.getServerZoneId();
        this.dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        // --- KONIEC NOWEGO KODU ---
    }



    public Connection getConnection() {
        return connection;
    }

    /**
     * Nawiązuje połączenie z bazą danych MySQL.
     * Używa drivera com.mysql.cj.jdbc.Driver (nowego) i ustawia strefę czasową na UTC.
     * @throws SQLException Jeśli wystąpi błąd podczas łączenia z bazą danych.
     */
    public static void connect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return;
        }

        // Dodatkowe sprawdzenie, czy statyczne pola zostały zainicjalizowane
        if (dbHost == null || dbDatabase == null || dbUsername == null || dbPassword == null) {
            if (plugin != null) { // Użyj plugin.getLogger() jeśli plugin jest już zainicjalizowany
                plugin.getLogger().log(Level.SEVERE, "Błąd inicjalizacji danych połączenia z bazą danych. Host, baza danych, użytkownik lub hasło są nullem.");
            } else {
                System.err.println("Błąd: Dane połączenia z bazą danych są nullem i plugin nie jest jeszcze zainicjalizowany.");
            }
            throw new SQLException("Dane połączenia z bazą danych nie zostały zainicjalizowane.");
        }


        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbDatabase + "?autoReconnect=true&useSSL=false&serverTimezone=UTC", dbUsername, dbPassword);
            if (plugin != null) {
                plugin.getLogger().info("Połączono z bazą danych MySQL!");
            } else {
                System.out.println("Połączono z bazą danych MySQL!");
            }
        } catch (ClassNotFoundException e) {
            if (plugin != null) {
                plugin.getLogger().log(Level.SEVERE, "Sterownik JDBC MySQL nie został znaleziony! Upewnij się, że masz dodane odpowiednie zależności (np. mysql-connector-j) do projektu.", e);
            } else {
                System.err.println("Błąd: Sterownik JDBC MySQL nie został znaleziony!");
            }
        } catch (SQLException e) {
            if (plugin != null) {
                plugin.getLogger().log(Level.SEVERE, "Nie udało się połączyć z bazą danych MySQL! Sprawdź dane logowania w config.yml: " + e.getMessage(), e);
            } else {
                System.err.println("Błąd: Nie udało się połączyć z bazą danych MySQL! " + e.getMessage());
            }
            throw e;
        }
    }

    /**
     * Rozłącza z bazą danych MySQL.
     * Ta metoda jest statyczna, ponieważ operuje na statycznym połączeniu.
     */
    public static void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                Main.getMain().getLogger().info("Rozłączono z bazą danych MySQL.");
            } catch (SQLException e) {
                Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się rozłączyć z bazą danych MySQL!", e);
            } finally {
                connection = null; // Upewnij się, że połączenie jest ustawione na null po zamknięciu
            }
        }
    }

    /**
     * Tworzy wszystkie wymagane tabele: ranks, player_ranks, rank_permissions, bans.
     * Zapewnia również migrację schematu dla tabeli `bans` poprzez dodawanie brakujących kolumn.
     */
    public void createTables() {
        if (connection == null) {
            Main.getMain().getLogger().severe("Brak połączenia z bazą danych. Nie można utworzyć tabel.");
            return;
        }

        // Tabela ranks
        try (PreparedStatement ps = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS ranks (" +
                        "rank_name VARCHAR(64) PRIMARY KEY," +
                        "prefix VARCHAR(255) NOT NULL," +
                        "suffix VARCHAR(255) DEFAULT ''," +
                        "weight int(11)" +
                        ");"
        )) {
            ps.executeUpdate();
            Main.getMain().getLogger().info("Utworzono tabelę 'ranks' (jeśli nie istniała).");
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się utworzyć tabeli 'ranks'!", e);
        }

        // Tabela player_ranks
        try (PreparedStatement ps = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS player_ranks (" +
                        "nick VARCHAR(36) PRIMARY KEY," +
                        "rank_name VARCHAR(64) NOT NULL," +
                        "FOREIGN KEY (rank_name) REFERENCES ranks(rank_name) ON DELETE CASCADE" +
                        ");"
        )) {
            ps.executeUpdate();
            Main.getMain().getLogger().info("Utworzono tabelę 'player_ranks' (jeśli nie istniała).");
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się utworzyć tabeli 'player_ranks'!", e);
        }

        // Tabela rank_permissions
        try (PreparedStatement ps = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS rank_permissions (" +
                        "rank_name VARCHAR(64) NOT NULL," +
                        "permission VARCHAR(255) NOT NULL," +
                        "PRIMARY KEY (rank_name, permission)," +
                        "FOREIGN KEY (rank_name) REFERENCES ranks(rank_name) ON DELETE CASCADE" +
                        ");"
        )) {
            ps.executeUpdate();
            Main.getMain().getLogger().info("Utworzono tabelę 'rank_permissions' (jeśli nie istniała).");
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się utworzyć tabeli 'rank_permissions'!", e);
        }

        String createBansTable = "CREATE TABLE IF NOT EXISTS bans (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "nick VARCHAR(16) NOT NULL," +
                "reason VARCHAR(255) NOT NULL," +
                "admin VARCHAR(16) NOT NULL," +
                "start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," + // Czas rozpoczęcia bana
                "end_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +                              // Czas zakończenia bana
                "unban BOOLEAN DEFAULT FALSE," +                     // Flaga czy ban jest aktywny (0=aktywny, 1=odbany)
                "unbanby VARCHAR(16) DEFAULT NULL" +                 // Kto odbanował
                ");";
        try (PreparedStatement ps = connection.prepareStatement(createBansTable)) {
            ps.executeUpdate();
            Main.getMain().getLogger().info("Upewniono się, że tabela 'bans' istnieje.");
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się utworzyć tabeli 'bans'", e);
        }

        String createMuteTable = "CREATE TABLE IF NOT EXISTS mutes (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "nick VARCHAR(16) NOT NULL," +
                "reason VARCHAR(255) NOT NULL," +
                "admin VARCHAR(16) NOT NULL," +
                "start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," + // Czas rozpoczęcia bana
                "end_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +                              // Czas zakończenia bana
                "unmute BOOLEAN DEFAULT FALSE," +                     // Flaga czy ban jest aktywny (0=aktywny, 1=odbany)
                "unmuteby VARCHAR(16) DEFAULT NULL" +                 // Kto odbanował
                ");";
        try (PreparedStatement ps = connection.prepareStatement(createMuteTable)) {
            ps.executeUpdate();
            Main.getMain().getLogger().info("Upewniono się, że tabela 'bans' istnieje.");
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się utworzyć tabeli 'bans'", e);
        }

        String createWarnTable = "CREATE TABLE IF NOT EXISTS warns (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "nick VARCHAR(16) NOT NULL," +
                "time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "reason VARCHAR(255) NOT NULL" +
                ");";
        try (PreparedStatement ps = connection.prepareStatement(createWarnTable)) {
            ps.executeUpdate();
            Main.getMain().getLogger().info("Upewniono się, że tabela 'bans' istnieje.");
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się utworzyć tabeli 'bans'", e);
        }

        String createSurvivalTable = "CREATE TABLE IF NOT EXISTS survival (" +
                "nick VARCHAR(16) NOT NULL," +
                "kills int(11) DEFAULT 0," +
                "deaths int(11) DEFAULT 0," +
                "money int(11) DEFAULT 100" +
                ");";
        try (PreparedStatement ps = connection.prepareStatement(createSurvivalTable)) {
            ps.executeUpdate();
            Main.getMain().getLogger().info("Upewniono się, że tabela 'survival' istnieje.");
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się utworzyć tabeli 'survival'", e);
        }

        String createRward = "CREATE TABLE IF NOT EXISTS rewards (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "nick VARCHAR(16) NOT NULL," +
                "namereward varchar(255)" +
                ");";
        try (PreparedStatement ps = connection.prepareStatement(createRward)) {
            ps.executeUpdate();
            Main.getMain().getLogger().info("Upewniono się, że tabela 'rewards' istnieje.");
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się utworzyć tabeli 'rewards'", e);
        }




        // Ustawienie domyślnej rangi (jeśli nie istnieje)
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT IGNORE INTO ranks (rank_name, prefix, suffix) VALUES (?, ?, ?)"
        )) {
            ps.setString(1, "default"); // Ustaw domyślną nazwę rangi
            ps.setString(2, "&7");     // Ustaw domyślny prefix
            ps.setString(3, "");       // Ustaw domyślny suffix
            ps.executeUpdate();
            Main.getMain().getLogger().info("Ustawiono domyślną rangę 'default' (jeśli nie istniała).");
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się ustawić domyślnej rangi!", e);
        }
    }

    /**
     * Metoda pomocnicza do dodawania brakujących kolumn do tabeli.
     * @param tableName Nazwa tabeli.
     * @param columnName Nazwa kolumny do dodania.
     * @param columnDefinition Definicja kolumny (np. "VARCHAR(36) NULL").
     */
    private void addMissingColumn(String tableName, String columnName, String columnDefinition) {
        try {
            ResultSet rs = connection.getMetaData().getColumns(null, null, tableName, columnName);
            if (!rs.next()) { // Kolumna nie istnieje
                try (PreparedStatement ps = connection.prepareStatement(
                        "ALTER TABLE `" + tableName + "` ADD COLUMN `" + columnName + "` " + columnDefinition + ";"
                )) {
                    ps.executeUpdate();
                    Main.getMain().getLogger().info("Dodano brakującą kolumnę '" + columnName + "' do tabeli '" + tableName + "'.");
                }
            }
        } catch (SQLException e) {
            // Nie logujemy jako SEVERE, bo to może być oczekiwana sytuacja dla istniejących baz.
            // Lub kolumna jest już Primary Key, co uniemożliwia proste dodanie.
            Main.getMain().getLogger().log(Level.WARNING, "Nie udało się sprawdzić/dodać kolumny " + columnName + " do tabeli " + tableName + ": " + e.getMessage());
        }
    }


    /**
     * Zapewnia, że połączenie z bazą danych jest aktywne.
     * @return true jeśli połączenie jest aktywne lub zostało nawiązane; false w przeciwnym razie.
     */
    private static boolean ensureConnection() {
        if (connection == null) {
            try {
                connect();
            } catch (SQLException e) {
                Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się ponownie połączyć z bazą danych.", e);
                return false;
            }
        }
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Błąd podczas sprawdzania statusu połączenia.", e);
            return false;
        }
    }

    // --- Metody dotyczące rang ---

    /**
     * Tworzy nową rangę w bazie danych.
     * @param rankName Nazwa rangi.
     * @param prefix Prefix rangi.
     * @param suffix Suffix rangi.
     * @return true, jeśli ranga została pomyślnie utworzona; false w przeciwnym razie (np. ranga już istnieje).
     */
    public boolean createRank(String rankName, String prefix, String suffix) {
        if (!ensureConnection()) return false;
        String sql = "INSERT INTO ranks (rank_name, prefix, suffix) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, rankName);
            ps.setString(2, prefix);
            ps.setString(3, suffix);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (e.getSQLState().startsWith("23")) { // SQLState 23xxx dla duplicate entry (ranga już istnieje)
                Main.getMain().getLogger().warning("Próba utworzenia istniejącej rangi: " + rankName);
            } else {
                Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się utworzyć rangi " + rankName, e);
            }
            return false;
        }
    }
    public boolean addWarn(String name, String powod) {
        if (!ensureConnection()) return false;
        String sql = "INSERT INTO warns (nick, reason, time) VALUES (?, ?, NOW())";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, powod);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (e.getSQLState().startsWith("23")) { // SQLState 23xxx dla duplicate entry (ranga już istnieje)
                Main.getMain().getLogger().warning("Warn dla " + name);
            } else {
                Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się utworzyć Warna dla " + name, e);
            }
            return false;
        }
    }
    public int getCountWarn(String nick) {
        if (!ensureConnection()) return 0;
        int weight = 0;
        String sql = "SELECT count(*) as ilosc FROM warns WHERE nick = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nick);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    weight = rs.getInt("ilosc");
                }
            }
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się pobrać warnow dla " + nick, e);
        }
        return weight;
    }
    public List<Object[]> getPlayerWarns(String playerName) {
        List<Object[]> playerWarns = new ArrayList<>();
        if (!ensureConnection()) {
            Main.getMain().getLogger().warning("Brak połączenia z bazą danych podczas pobierania ostrzeżeń dla gracza " + playerName);
            return playerWarns;
        }

        String sql = "SELECT nick, reason, time FROM warns WHERE nick = ? ORDER BY time DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String nick = rs.getString("nick");
                    String reason = rs.getString("reason");
                    Timestamp timestampFromDb = rs.getTimestamp("time");
                    long time = (timestampFromDb != null) ? timestampFromDb.getTime() : -1L;

                    playerWarns.add(new Object[]{nick, reason, time});
                }
            }
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Błąd podczas pobierania ostrzeżeń dla gracza " + playerName + ": " + e.getMessage(), e);
        }
        return playerWarns;
    }

    /**
     * Usuwa rangę z bazy danych. Spowoduje to również usunięcie powiązanych player_ranks i rank_permissions
     * z powodu ustawień ON DELETE CASCADE.
     * @param rankName Nazwa rangi do usunięcia.
     * @return true, jeśli ranga została pomyślnie usunięta; false w przeciwnym razie.
     */
    public boolean deleteRank(String rankName) {
        if (!ensureConnection()) return false;
        String sql = "DELETE FROM ranks WHERE rank_name = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, rankName);
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się usunąć rangi " + rankName, e);
            return false;
        }
    }

    public boolean hasPlayerStat(String playername) {
        if (!ensureConnection()) return false; // Upewnij się, że połączenie z bazą danych jest aktywne

        String sql = "SELECT nick FROM survival WHERE nick = ?"; // Wystarczy wybrać jeden z istniejących pól, np. nick

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playername);

            // === KLUCZOWA ZMIANA TUTAJ: Używamy executeQuery() zamiast executeUpdate() ===
            try (ResultSet rs = ps.executeQuery()) {
                // Jeśli rs.next() zwróci true, oznacza to, że znaleziono co najmniej jeden wiersz
                return rs.next();
            }
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Wystąpił błąd podczas sprawdzania statystyk gracza " + playername, e);
            return false;
        }
    }

    /**
     * Pobiera listę wszystkich nazw rang z bazy danych.
     * @return Lista nazw rang (String).
     */
    public List<String> getRanks() {
        List<String> ranks = new ArrayList<>();
        if (!ensureConnection()) return ranks;
        String sql = "SELECT rank_name FROM ranks";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ranks.add(rs.getString("rank_name"));
            }
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się pobrać listy rang.", e);
        }
        return ranks;
    }
    public int getRankWeight(String rankName) {
        if (!ensureConnection()) return 1;
        int weight = 0;
        String sql = "SELECT weight FROM ranks WHERE rank_name = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, rankName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    weight = rs.getInt("weight");
                }
            }
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się pobrać prefixu dla rangi " + rankName, e);
        }
        return weight;
    }
    public int getPlayersCount() {
        if (!ensureConnection()) return 1;
        int weight = 0;
        String sql = "SELECT count(*) as count from survival";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    weight = rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się pobrać graczy", e);
        }
        return weight;
    }
    public boolean setRankWeight(String rankName, int weight) {
        if (!ensureConnection()) return false;
        String sql = "UPDATE `ranks` SET `weight`=? WHERE `rank_name`=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, weight);
            ps.setString(2, rankName);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się ustawić weight dla rangi " + rankName, e);
            return false;
        }
    }

    /**
     * Ustawia rangę dla gracza. Jeśli gracz już ma rangę, zostaje ona zaktualizowana.
     * @param nick nick gracza.
     * @param rankName Nazwa rangi do ustawienia.
     * @return true, jeśli ranga została pomyślnie ustawiona; false w przeciwnym razie.
     */
    public boolean setPlayerRank(String nick, String rankName) {
        if (!ensureConnection()) return false;
        String sql = "INSERT INTO player_ranks (nick, rank_name) VALUES (?, ?) ON DUPLICATE KEY UPDATE rank_name = VALUES(rank_name)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nick.toString());
            ps.setString(2, rankName);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się ustawić rangi dla gracza " + nick + ": " + rankName, e);
            return false;
        }
    }
    public boolean setStatsSurvivalPlayer(String nick) {
        if (!ensureConnection()) return false;
        String sql = "INSERT INTO survival (nick, kills, deaths, money) VALUES (?, 0, 0, 100) ";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nick.toString());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się ustawić statystyk dla gracza " + nick, e);
            return false;
        }
    }

    /**
     * Pobiera rangę gracza na podstawie jego UUID.
     * @param nick gracza.
     * @return Nazwa rangi gracza (String), lub null, jeśli nie znaleziono rangi dla gracza.
     */
    public String getPlayerRank(String nick) {
        if (!ensureConnection()) return null;
        String sql = "SELECT rank_name FROM player_ranks WHERE nick = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nick.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("rank_name");
                }
            }
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się pobrać rangi dla gracza " + nick, e);
        }
        return null;
    }
    public String getPlayerReward(String nick) {
        if (!ensureConnection()) return "Brak";
        String sql = "select namereward from rewards where nick = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nick.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("namereward");
                }
            }
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się pobrać nagrody dla gracza " + nick, e);
        }
        return "Brak";
    }


    public int getSurvivalMoney(String nick) {
        if (!ensureConnection()) return 0;
        String sql = "SELECT money FROM survival WHERE nick = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nick.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("money");
                }
            }
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się pobrać statystyki dla gracza " + nick, e);
        }
        return 0;
    }
    public int getSurvivalKills(String nick) {
        if (!ensureConnection()) return 0;
        String sql = "SELECT kills FROM survival WHERE nick = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nick.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("kills");
                }
            }
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się pobrać statystyki dla gracza " + nick, e);
        }
        return 0;
    }
    public String getTopKillsPlayerName(int rank) {
        if (!ensureConnection()) return "-";

        // Używamy LIMIT i OFFSET do pobrania konkretnej pozycji
        // OFFSET to (rank - 1), ponieważ bazy danych liczą od 0
        String sql = "SELECT nick FROM survival ORDER BY kills DESC, nick ASC LIMIT 1 OFFSET ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, rank - 1); // np. dla rank=1, offset=0; dla rank=2, offset=1
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nick");
                }
            }
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się pobrać gracza z topki zabójstw (pozycja: " + rank + ")", e);
        }
        return "-"; // Zwróć "N/A", jeśli nie znaleziono gracza na tej pozycji lub wystąpił błąd
    }
    public int getTopKillsAmount(int rank) {
        if (!ensureConnection()) return 0;

        String sql = "SELECT kills FROM survival ORDER BY kills DESC, nick ASC LIMIT 1 OFFSET ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, rank - 1);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("kills");
                }
            }
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się pobrać ilości zabójstw z topki (pozycja: " + rank + ")", e);
        }
        return 0; // Zwróć 0, jeśli nie znaleziono lub wystąpił błąd
    }
    public String getTopDeathsPlayerName(int rank) {
        if (!ensureConnection()) return "-";

        // Dla śmierci możesz chcieć posortować rosnąco (najmniej śmierci na górze),
        // albo malejąco (najwięcej śmierci na górze) - zdecyduj, jak chcesz.
        // Tutaj sortujemy malejąco (najwięcej śmierci na górze topki).
        String sql = "SELECT nick FROM survival ORDER BY deaths DESC, nick ASC LIMIT 1 OFFSET ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, rank - 1);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nick");
                }
            }
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się pobrać gracza z topki śmierci (pozycja: " + rank + ")", e);
        }
        return "-";
    }
    public int getTopDeathsAmount(int rank) {
        if (!ensureConnection()) return 0;

        String sql = "SELECT deaths FROM survival ORDER BY deaths DESC, nick ASC LIMIT 1 OFFSET ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, rank - 1);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("deaths");
                }
            }
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się pobrać ilości śmierci z topki (pozycja: " + rank + ")", e);
        }
        return 0;
    }
    public int getSurvivalDeaths(String nick) {
        if (!ensureConnection()) return 0;
        String sql = "SELECT deaths FROM survival WHERE nick = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nick.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("deaths");
                }
            }
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się pobrać statystyki dla gracza " + nick, e);
        }
        return 0;
    }

    /**
     * Pobiera domyślną rangę z bazy danych. Jeśli nie ma rangi o nazwie 'default', zwraca "default".
     * @return Nazwa domyślnej rangi.
     */
    public String getDefaultRank() {
        if (!ensureConnection()) return "default";
        String sql = "SELECT rank_name FROM ranks WHERE rank_name = 'default'";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getString("rank_name");
            }
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.WARNING, "Nie udało się pobrać domyślnej rangi 'default' z bazy danych, używam fallbacku.", e);
        }
        return "default";
    }

    /**
     * Dodaje uprawnienie do rangi.
     * @param rankName Nazwa rangi.
     * @param permission Uprawnienie do dodania.
     * @return true, jeśli uprawnienie zostało pomyślnie dodane; false w przeciwnym razie (np. uprawnienie już istnieje).
     */
    public boolean addRankPermission(String rankName, String permission) {
        if (!ensureConnection()) return false;
        String sql = "INSERT INTO rank_permissions (rank_name, permission) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, rankName);
            ps.setString(2, permission);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (e.getSQLState().startsWith("23")) {
                Main.getMain().getLogger().warning("Uprawnienie '" + permission + "' już istnieje dla rangi '" + rankName + "'.");
            } else {
                Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się dodać uprawnienia " + permission + " do rangi " + rankName, e);
            }
            return false;
        }
    }

    /**
     * Usuwa uprawnienie z rangi.
     * @param rankName Nazwa rangi.
     * @param permission Uprawnienie do usunięcia.
     * @return true, jeśli uprawnienie zostało pomyślnie usunięte; false w przeciwnym razie.
     */
    public boolean removeRankPermission(String rankName, String permission) {
        if (!ensureConnection()) return false;
        String sql = "DELETE FROM rank_permissions WHERE rank_name = ? AND permission = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, rankName);
            ps.setString(2, permission);
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się usunąć uprawnienia " + permission + " z rangi " + rankName, e);
            return false;
        }
    }
    public boolean removeReward(String playername, String reward) {
        if (!ensureConnection()) return false;
        String sql = "delete from rewards where nick = ? and namereward = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playername);
            ps.setString(2, reward);
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się usunąć nagrody " + reward + " dla gracza " + playername, e);
            return false;
        }
    }

    /**
     * Pobiera listę uprawnień dla danej rangi.
     * @param rankName Nazwa rangi.
     * @return Lista uprawnień (String) dla danej rangi.
     */
    public List<String> getRankPermissions(String rankName) {
        List<String> permissions = new ArrayList<>();
        if (!ensureConnection()) return permissions;
        String sql = "SELECT permission FROM rank_permissions WHERE rank_name = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, rankName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    permissions.add(rs.getString("permission"));
                }
            }
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się pobrać uprawnień dla rangi " + rankName, e);
        }
        return permissions;
    }

    /**
     * Ustawia prefix dla rangi.
     * @param rankName Nazwa rangi.
     * @param prefix Nowy prefix.
     * @return true, jeśli prefix został pomyślnie ustawiony; false w przeciwnym razie.
     */
    public boolean setRankPrefix(String rankName, String prefix) {
        if (!ensureConnection()) return false;
        String sql = "UPDATE `ranks` SET `prefix`=? WHERE `rank_name`=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, prefix);
            ps.setString(2, rankName);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się ustawić prefixu dla rangi " + rankName, e);
            return false;
        }
    }

    public boolean setSurvivalMoney(String playername, int money) {
        if (!ensureConnection()) return false;
        String sql = "UPDATE `survival` SET `money`=? WHERE `nick`=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, money);
            ps.setString(2, playername);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się ustawić statystyki dla " + playername, e);
            return false;
        }
    }
    public boolean setSurvivalDeaths(String playername, int deaths) {
        if (!ensureConnection()) return false;
        String sql = "UPDATE `survival` SET `deaths`=? WHERE `nick`=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, deaths);
            ps.setString(2, playername);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się ustawić statystyki dla " + playername, e);
            return false;
        }
    }
    public boolean setSurvivalKills(String playername, int kills) {
        if (!ensureConnection()) return false;
        String sql = "UPDATE `survival` SET `kills`=? WHERE `nick`=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, kills);
            ps.setString(2, playername);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się ustawić statystyki dla " + playername, e);
            return false;
        }
    }

    /**
     * Pobiera prefix rangi.
     * @param rankName Nazwa rangi.
     * @return Prefix rangi (String), lub null, jeśli ranga nie istnieje.
     */
    public String getRankPrefix(String rankName) {
        if (!ensureConnection()) return null;
        String prefix = null;
        String sql = "SELECT prefix FROM ranks WHERE rank_name = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, rankName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    prefix = rs.getString("prefix");
                }
            }
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się pobrać prefixu dla rangi " + rankName, e);
        }
        return prefix;
    }

    /**
     * Ustawia suffix dla rangi.
     * @param rankName Nazwa rangi.
     * @param suffix Nowy suffix.
     * @return true, jeśli suffix został pomyślnie ustawiony; false w przeciwnym razie.
     */
    public boolean setRankSuffix(String rankName, String suffix) {
        if (!ensureConnection()) return false;
        String sql = "UPDATE `ranks` SET `suffix`=? WHERE `rank_name`=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, suffix);
            ps.setString(2, rankName);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się ustawić suffixu dla rangi " + rankName, e);
            return false;
        }
    }

    /**
     * Pobiera suffix rangi.
     * @param rankName Nazwa rangi.
     * @return Suffix rangi (String), lub null, jeśli ranga nie istnieje.
     */
    public String getRankSuffix(String rankName) {
        if (!ensureConnection()) return null;
        String suffix = null;
        String sql = "SELECT suffix FROM ranks WHERE rank_name = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, rankName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    suffix = rs.getString("suffix");
                }
            }
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się pobrać suffixu dla rangi " + rankName, e);
        }
        return suffix;
    }

    /**
     * Dodaje wpis o banie do bazy danych.
     * @param nick Nick gracza, który ma zostać zbanowany.
     * @param reason Powód bana.
     * @param admin Nick administratora, który nałożył bana.
     * @param endTime Czas zakończenia bana (Timestamp).
     * @return true, jeśli ban został pomyślnie dodany; false w przeciwnym razie.
     */
    public boolean banPlayer(String nick, String reason, String admin, Timestamp endTime) {
        if (!ensureConnection()) return false;

        // Upewnij się, że gracz nie jest już zbanowany aktywnym banem
        if (isPlayerBanned(nick)) {
            Main.getMain().getLogger().warning("Gracz '" + nick + "' jest już zbanowany. Ban nie został nałożony ponownie.");
            return false;
        }

        String sql = "INSERT INTO bans (nick, reason, admin, start_time, end_time, unban) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nick);
            ps.setString(2, reason);
            ps.setString(3, admin);
            ps.setTimestamp(4, new Timestamp(System.currentTimeMillis())); // Czas rozpoczęcia bana
            ps.setTimestamp(5, endTime);
            ps.setBoolean(6, false);
            ps.executeUpdate();

            Instant instant = endTime.toInstant();
            ZonedDateTime zonedEndTime = instant.atZone(displayZoneId);
            String formattedEndTime = zonedEndTime.format(dateFormatter);

            Main.getMain().getLogger().info("Zbanowano gracza '" + nick + "' do " + formattedEndTime + " z powodu: " + reason + " przez: " + admin);
            return true;
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się zbanować gracza '" + nick + "'", e);
            return false;
        }
    }

    /**
     * Sprawdza, czy gracz jest aktualnie zbanowany.
     * @param nick Nick gracza do sprawdzenia.
     * @return true, jeśli gracz jest zbanowany i jego ban nie wygasł oraz nie został odbanowany; false w przeciwnym razie.
     */
    public boolean isPlayerBanned(String nick) {
        if (!ensureConnection()) return false;
        // POBIERZ AKTUALNY CZAS Z JAVY I PRZEKAŻ GO DO ZAPYTANIA
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        String sql = "SELECT COUNT(*) FROM bans WHERE nick = ? AND unban = 0 AND (end_time IS NULL OR end_time > ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nick);
            ps.setTimestamp(2, currentTime); // Użyj czasu z Javy zamiast NOW()
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się sprawdzić statusu bana dla gracza '" + nick + "'", e);
        }
        return false;
    }

    /**
     * Usuwa bana dla danego gracza, oznaczając go jako nieaktywny w bazie danych.
     * @param nick Nick gracza, którego ban ma zostać usunięty.
     * @param admin Nick administratora, który odbanował gracza.
     * @return true, jeśli ban został pomyślnie usunięty; false w przeciwnym razie.
     */
    public boolean unbanPlayer(String nick, String admin) {
        if (!ensureConnection()) return false;
        String sql = "UPDATE `bans` SET `unban`='1',`unbanby`=? WHERE nick = ? AND unban = 0 ORDER BY start_time DESC LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, admin);
            ps.setString(2, nick);
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                Main.getMain().getLogger().info("Odbanowano gracza '" + nick + "' przez '" + admin + "'.");
            }
            return affectedRows > 0;
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się odbanować gracza '" + nick + "'", e);
            return false;
        }
    }

    /**
     * Pobiera powód aktualnie aktywnego bana dla danego gracza.
     * @param nick Nick gracza.
     * @return Powód bana, lub null, jeśli gracz nie jest aktywnie zbanowany.
     */
    public String getBanReason(String nick) {
        if (!ensureConnection()) return null;
        // POBIERZ AKTUALNY CZAS Z JAVY I PRZEKAŻ GO DO ZAPYTANIA
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        String sql = "SELECT reason FROM bans WHERE nick = ? AND unban = 0 AND (end_time IS NULL OR end_time > ?) ORDER BY start_time DESC LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nick);
            ps.setTimestamp(2, currentTime); // Użyj czasu z Javy zamiast NOW()
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("reason");
                }
            }
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się pobrać powodu bana dla gracza '" + nick + "'", e);
        }
        return null;
    }

    /**
     * Pobiera nick administratora, który nałożył aktualnie aktywny ban dla danego gracza.
     * @param nick Nick gracza.
     * @return Nick administratora, lub null, jeśli gracz nie jest aktywnie zbanowany.
     */
    public String getBanAdmin(String nick) {
        if (!ensureConnection()) return null;
        // POBIERZ AKTUALNY CZAS Z JAVY I PRZEKAŻ GO DO ZAPYTANIA
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        String sql = "SELECT admin FROM bans WHERE nick = ? AND unban = 0 AND (end_time IS NULL OR end_time > ?) ORDER BY start_time DESC LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nick);
            ps.setTimestamp(2, currentTime); // Użyj czasu z Javy zamiast NOW()
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("admin");
                }
            }
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się pobrać administratora bana dla gracza '" + nick + "'", e);
        }
        return null;
    }

    /**
     * Pobiera czas zakończenia aktualnie aktywnego bana dla danego gracza.
     * @param nick Nick gracza.
     * @return Timestamp czasu zakończenia bana, lub null, jeśli gracz nie jest aktywnie zbanowany.
     */
    public Timestamp getBanEndTime(String nick) {
        if (!ensureConnection()) return null;
        // POBIERZ AKTUALNY CZAS Z JAVY I PRZEKAŻ GO DO ZAPYTANIA
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        String sql = "SELECT end_time FROM bans WHERE nick = ? AND unban = 0 AND (end_time IS NULL OR end_time > ?) ORDER BY start_time DESC LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nick);
            ps.setTimestamp(2, currentTime); // Użyj czasu z Javy zamiast NOW()
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getTimestamp("end_time");
                }
            }
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się pobrać czasu zakończenia bana dla gracza '" + nick + "'", e);
        }
        return null;
    }

    /**
     * Dodaje wpis o wyciszeniu do bazy danych.
     * @param nick Nick gracza, który ma zostać wyciszony.
     * @param reason Powód wyciszenia.
     * @param admin Nick administratora, który nałożył wyciszenie.
     * @param endTime Czas zakończenia wyciszenia (Timestamp). Ustaw na null dla permanentnego.
     * @return true, jeśli wyciszenie zostało pomyślnie dodane; false w przeciwnym razie.
     */
    public boolean mutePlayer(String nick, String reason, String admin, Timestamp endTime) {
        if (!ensureConnection()) return false;

        // Upewnij się, że gracz nie jest już wyciszony aktywnym mutem
        if (isPlayerMuted(nick)) {
            Main.getMain().getLogger().warning("Gracz '" + nick + "' jest już wyciszony. Wyciszenie nie zostało nałożone ponownie.");
            return false;
        }

        String sql = "INSERT INTO mutes (nick, reason, admin, start_time, end_time, unmute) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nick);
            ps.setString(2, reason);
            ps.setString(3, admin);
            ps.setTimestamp(4, new Timestamp(System.currentTimeMillis())); // Czas rozpoczęcia muta
            ps.setTimestamp(5, endTime); // Czas zakończenia muta (może być null)
            ps.setBoolean(6, false); // Ustawiamy mute jako aktywny (nieodmutowany)
            ps.executeUpdate();

            String formattedEndTime;
            if (endTime == null) {
                formattedEndTime = "PERMANENTNIE";
            } else {
                Instant instant = endTime.toInstant();
                ZonedDateTime zonedEndTime = instant.atZone(displayZoneId);
                formattedEndTime = zonedEndTime.format(dateFormatter);
            }

            Main.getMain().getLogger().info("Wyciszono gracza '" + nick + "' do " + formattedEndTime + " z powodu: " + reason + " przez: " + admin);
            return true;
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się wyciszyć gracza '" + nick + "'", e);
            return false;
        }
    }

    /**
     * Sprawdza, czy gracz jest aktualnie wyciszony.
     * @param nick Nick gracza do sprawdzenia.
     * @return true, jeśli gracz jest wyciszony i jego wyciszenie nie wygasło oraz nie zostało odmutowane; false w przeciwnym razie.
     */
    public boolean isPlayerMuted(String nick) {
        if (!ensureConnection()) return false;
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        // Kwerenda sprawdza, czy istnieje mute dla danego nicku, który nie został odmutowany (unmute=0)
        // ORAZ jego czas zakończenia jest w przyszłości (end_time > ?) LUB jest to mute permanentny (end_time IS NULL).
        String sql = "SELECT COUNT(*) FROM mutes WHERE nick = ? AND unmute = 0 AND (end_time IS NULL OR end_time > ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nick);
            ps.setTimestamp(2, currentTime); // Użyj czasu z Javy zamiast NOW()
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się sprawdzić statusu muta dla gracza '" + nick + "'", e);
        }
        return false;
    }

    /**
     * Usuwa wyciszenie dla danego gracza, oznaczając je jako nieaktywne w bazie danych.
     * @param nick Nick gracza, którego wyciszenie ma zostać usunięte.
     * @param admin Nick administratora, który odmutował gracza.
     * @return true, jeśli wyciszenie zostało pomyślnie usunięte; false w przeciwnym razie.
     */
    public boolean unmutePlayer(String nick, String admin) {
        if (!ensureConnection()) return false;
        // Aktualizujemy najbardziej aktualne aktywne wyciszenie (unmute=0) dla danego gracza.
        // Ustawiamy flagę 'unmute' na '1' i dodajemy informację, kto odmutował.
        String sql = "UPDATE `mutes` SET `unmute`='1',`unmuteby`=? WHERE nick = ? AND unmute = 0 ORDER BY start_time DESC LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, admin);
            ps.setString(2, nick);
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                Main.getMain().getLogger().info("Odmutowano gracza '" + nick + "' przez '" + admin + "'.");
            }
            return affectedRows > 0;
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się odmutować gracza '" + nick + "'", e);
            return false;
        }
    }

    /**
     * Pobiera powód aktualnie aktywnego wyciszenia dla danego gracza.
     * @param nick Nick gracza.
     * @return Powód wyciszenia, lub null, jeśli gracz nie jest aktywnie wyciszony.
     */
    public String getMuteReason(String nick) {
        if (!ensureConnection()) return null;
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        String sql = "SELECT reason FROM mutes WHERE nick = ? AND unmute = 0 AND (end_time IS NULL OR end_time > ?) ORDER BY start_time DESC LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nick);
            ps.setTimestamp(2, currentTime);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("reason");
                }
            }
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się pobrać powodu muta dla gracza '" + nick + "'", e);
        }
        return null;
    }

    /**
     * Pobiera nick administratora, który nałożył aktualnie aktywne wyciszenie dla danego gracza.
     * @param nick Nick gracza.
     * @return Nick administratora, lub null, jeśli gracz nie jest aktywnie wyciszony.
     */
    public String getMuteAdmin(String nick) {
        if (!ensureConnection()) return null;
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        String sql = "SELECT admin FROM mutes WHERE nick = ? AND unmute = 0 AND (end_time IS NULL OR end_time > ?) ORDER BY start_time DESC LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nick);
            ps.setTimestamp(2, currentTime);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("admin");
                }
            }
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się pobrać administratora muta dla gracza '" + nick + "'", e);
        }
        return null;
    }

    /**
     * Pobiera czas zakończenia aktualnie aktywnego wyciszenia dla danego gracza.
     * @param nick Nick gracza.
     * @return Timestamp czasu zakończenia wyciszenia, lub null, jeśli gracz nie jest aktywnie wyciszony.
     */
    public Timestamp getMuteEndTime(String nick) {
        if (!ensureConnection()) return null;
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        String sql = "SELECT end_time FROM mutes WHERE nick = ? AND unmute = 0 AND (end_time IS NULL OR end_time > ?) ORDER BY start_time DESC LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nick);
            ps.setTimestamp(2, currentTime);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getTimestamp("end_time");
                }
            }
        } catch (SQLException e) {
            Main.getMain().getLogger().log(Level.SEVERE, "Nie udało się pobrać czasu zakończenia muta dla gracza '" + nick + "'", e);
        }
        return null;
    }


}