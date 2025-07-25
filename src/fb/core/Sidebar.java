package fb.core;

import fb.core.api.BanAPI;
import fb.core.api.BungeeAPI;
import fb.core.api.HexAPI;
import fb.core.api.RanksAPI;
import fb.core.data.ConfigData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.*;
import java.util.logging.Level; // Upewnij się, że to jest zaimportowane

public class Sidebar {

    private static Main plugin = null;
    private static RanksAPI ranksAPI = null;

    // Mapa do przechowywania aktualnych tekstów dla linii scorebordu dla każdego gracza.
    private static final Map<Player, Map<String, String>> playerLineCache = new HashMap<>();

    public Sidebar(Main plugin, BanAPI banAPI) {
        Sidebar.plugin = plugin;
        ranksAPI = new RanksAPI(plugin);
    }

    public static void updateScoreboard(Player p) {
        if (!ConfigData.getInstance().getData().getBoolean("sidebar.enabled", false)) {
            // Jeśli sidebar wyłączony, ustaw domyślny scoreboard (bez sidebara)
            p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard()); // Ustawia główny scoreboard
            playerLineCache.remove(p);
            return;
        }

        // Używamy scoreboarda gracza do sidebara.
        // Jeśli gracz ma domyślny scoreboard, utwórz nowy, aby uniknąć problemów z obj sidebar
        Scoreboard board = p.getScoreboard();
        if (board == null || board == Bukkit.getScoreboardManager().getMainScoreboard() || board.getObjective("sidebar") == null) {
            board = Bukkit.getScoreboardManager().getNewScoreboard();
            p.setScoreboard(board);
        }

        Objective obj = board.getObjective("sidebar");
        if (obj == null) {
            obj = board.registerNewObjective("sidebar", "dummy", "");
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        String title = ConfigData.getInstance().getData().getString("sidebar.title", "&6&lTwójSerwer");
        obj.setDisplayName(HexAPI.hex(title));

        List<String> lines = ConfigData.getInstance().getData().getStringList("sidebar.lines");
        Map<String, String> currentPlayerCache = playerLineCache.computeIfAbsent(p, k -> new HashMap<>());

        Set<String> currentConfigEntries = new HashSet<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int score = lines.size() - i;

            String processedLine = replacePlaceholders(p, line);
            String entryIdentifier = "sidebar_entry_" + score;
            currentConfigEntries.add(entryIdentifier);

            String scoreboardEntry = ChatColor.values()[i % ChatColor.values().length].toString() + ChatColor.RESET;
            if (i > ChatColor.values().length) {
                scoreboardEntry += ChatColor.values()[(i / ChatColor.values().length) % ChatColor.values().length].toString();
            }

            Team team = board.getTeam(entryIdentifier);
            if (team == null) {
                team = board.registerNewTeam(entryIdentifier);
            }

            if (!team.hasEntry(scoreboardEntry)) {
                team.addEntry(scoreboardEntry);
            }

            if (!currentPlayerCache.containsKey(entryIdentifier) || !currentPlayerCache.get(entryIdentifier).equals(processedLine)) {
                currentPlayerCache.put(entryIdentifier, processedLine);

                team.setPrefix(HexAPI.hex(processedLine));
                team.setSuffix("");

                if (obj.getScore(scoreboardEntry).getScore() != score) {
                    obj.getScore(scoreboardEntry).setScore(score);
                }
            }
        }

        Set<String> entriesToRemoveFromCache = new HashSet<>();
        for (Map.Entry<String, String> cacheEntry : currentPlayerCache.entrySet()) {
            String cachedEntryIdentifier = cacheEntry.getKey();
            if (!currentConfigEntries.contains(cachedEntryIdentifier)) {
                Team team = board.getTeam(cachedEntryIdentifier);
                if (team != null) {
                    for (String entryInTeam : team.getEntries()) {
                        board.resetScores(entryInTeam);
                    }
                    team.unregister();
                }
                entriesToRemoveFromCache.add(cachedEntryIdentifier);
            }
        }
        currentPlayerCache.keySet().removeAll(entriesToRemoveFromCache);

        // --- ZMIENIONE WYWOŁANIE AKTUALIZACJI NAMETAGÓW ---
        // Teraz używamy GŁÓWNEGO scoreboarda serwera
        updatePlayerNametag(p, Bukkit.getScoreboardManager().getMainScoreboard());
    }


    private static String replacePlaceholders(Player p, String line) {
        String processedLine = line;

        processedLine = processedLine.replace("{player}", p.getName());
        processedLine = processedLine.replace("{rank_name}", ranksAPI.getRank(p.getName()));
        processedLine = processedLine.replace("{rank_prefix}", ranksAPI.getRankPrefix(ranksAPI.getRank(p.getName())));
        processedLine = processedLine.replace("{online_players}", String.valueOf(Bukkit.getOnlinePlayers().size()));
        processedLine = processedLine.replace("{max_players}", String.valueOf(Bukkit.getMaxPlayers()));
        processedLine = processedLine.replace("{kills}", String.valueOf(BanAPI.getPlayerStatKills(p.getName())));
        processedLine = processedLine.replace("{deaths}", String.valueOf(BanAPI.getPlayerStatDeaths(p.getName())));
        processedLine = processedLine.replace("{money}", String.valueOf(BanAPI.getPlayerStatMoney(p.getName())));

        processedLine = processedLine.replace("{bungee_online_all}", String.valueOf(BungeeAPI.getCachedOnlinePlayers("ALL")));

        ConfigData cd = ConfigData.getInstance();
        if (cd != null && cd.getData().isSet("servers.list")) {
            for (String serverName : cd.getData().getStringList("servers.list")) {
                processedLine = processedLine.replace("{bungee_online_" + serverName.toLowerCase() + "}", String.valueOf(BungeeAPI.getCachedOnlinePlayers(serverName)));
            }
        }


        return processedLine;
    }

    public static void removeScoreboard(Player p) {
        // Usuwamy indywidualny scoreboard gracza, przywracając mu główny scoreboard serwera
        p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        playerLineCache.remove(p);
    }

    /**
     * Aktualizuje nametag gracza (prefix w grze i w tabliście) na podstawie jego rangi.
     * Wykorzystuje system Teamów Scoreboarda.
     * @param p Gracz, dla którego aktualizujemy nametag.
     * @param globalBoard GLÓWNY Scoreboard serwera, na którym będą zarządzane teamy nametagów.
     */
    public static void updatePlayerNametag(Player p, Scoreboard globalBoard) {
        String rankName = ranksAPI.getRank(p.getName());
        String rankPrefix = ranksAPI.getRankPrefix(rankName);

        int rankWeight = ranksAPI.getRankWeight(rankName);
        String teamName = String.format("%02d_%s", rankWeight, rankName.toLowerCase());

        // Logging dla debugowania
        //plugin.getLogger().log(Level.INFO, "Nazwa druzyny (globalna): " + teamName);


        if (teamName.length() > 16) {
            teamName = teamName.substring(0, 16);
            plugin.getLogger().log(Level.WARNING, "Nazwa teamu '" + teamName + "' została obcięta do 16 znaków.");
        }

        // Usuń gracza ze wszystkich istniejących teamów NA GŁÓWNYM SCOREBOARDZIE.
        // Ważne, aby operować na globalBoard, a nie na board z updateScoreboard.
        for (Team existingTeam : globalBoard.getTeams()) {
            if (existingTeam.hasEntry(p.getName())) {
                existingTeam.removeEntry(p.getName());
                // plugin.getLogger().log(Level.INFO, "Usunieto gracza " + p.getName() + " z teamu " + existingTeam.getName());
                break; // Gracz może należeć tylko do jednego teamu
            }
        }

        Team team = globalBoard.getTeam(teamName);
        if (team == null) {
            team = globalBoard.registerNewTeam(teamName);
            // Ustaw opcje teamu
            team.setAllowFriendlyFire(false);
            team.setCanSeeFriendlyInvisibles(true);
            team.setNameTagVisibility(NameTagVisibility.ALWAYS);

            try {
                team.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
            } catch (NoSuchMethodError e) {
                plugin.getLogger().warning("Wersja serwera nie obsługuje Team.Option.NAME_TAG_VISIBILITY. Nametagi mogą być niewidoczne.");
            }
        }

        String finalPrefix = HexAPI.hex(rankPrefix);
        team.setPrefix(finalPrefix);
        team.setSuffix(HexAPI.hex("")); // Upewnij się, że suffix jest pusty, jeśli go nie używasz

        // Logging dla debugowania
        //plugin.getLogger().log(Level.INFO, "Prefix druzyny (globalna): " + team.getPrefix());

        // Dodaj gracza do teamu
        if (!team.hasEntry(p.getName())) {
            team.addEntry(p.getName());
        }
        //plugin.getLogger().log(Level.INFO, "Czy druzyna (globalna): " + team.getName() + " ma " + p.getName() + " w skladzie " + team.hasEntry(p.getName()));
    }

    /**
     * Ta metoda powinna być wywoływana TYLKO gdy ktoś zmienia rangę lub przy starcie pluginu.
     * Nie w pętli odświeżania sidebara!
     */
    public static void updateAllPlayersNametags() {
        // Ta metoda jest przeznaczona do użycia tylko, gdy np. gracz dostaje nową rangę.
        // Jeśli jest wywoływana w cyklicznym tasku, lepiej robić to przez updateScoreboard,
        // który już wywołuje updatePlayerNametag dla każdego gracza.
        // Wywołujemy ją raz dla wszystkich graczy przy starcie pluginu.
        Scoreboard globalBoard = Bukkit.getScoreboardManager().getMainScoreboard();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            updatePlayerNametag(onlinePlayer, globalBoard);
        }
    }
}