package fb.core.api;

import fb.core.Main;
import fb.core.data.MySQL;

import java.sql.Timestamp;
import java.util.List;

public class BanAPI {

    static MySQL mySQL;
    static Main plugin;

    public BanAPI(Main m){
        plugin = m;
        mySQL = new MySQL(plugin);
    }

    public static void BanPlayer(String nick, String reason, String admin, Timestamp endTime){
        mySQL.banPlayer(nick, reason, admin, endTime);
    }
    public static boolean isPlayerBanned(String nick){
        return mySQL.isPlayerBanned(nick);
    }
    public static void unbanPlayer(String nick, String admin){
        mySQL.unbanPlayer(nick, admin);
    }
    public static String getBanReason(String nick){
        return mySQL.getBanReason(nick);
    }
    public static Timestamp getBanEndTime(String nick){
        return mySQL.getBanEndTime(nick);
    }
    public static String getBanAdmin(String nick){
        return mySQL.getBanAdmin(nick);
    }
    public static boolean mutePlayer(String nick, String reason, String admin, Timestamp endTime){
        return mySQL.mutePlayer(nick, reason, admin, endTime);
    }
    public static boolean isMutePlayer(String nick){
        return mySQL.isPlayerMuted(nick);
    }
    public static boolean unMutePlayer(String nick, String admin){
        return mySQL.unmutePlayer(nick, admin);
    }
    public static String getMuteReason(String nick){
        return mySQL.getMuteReason(nick);
    }
    public static Timestamp getMuteTime(String nick){
        return mySQL.getMuteEndTime(nick);
    }
    public static String getMuteAdmin(String nick){
        return mySQL.getMuteAdmin(nick);
    }
    public static boolean WarnPLayer(String playername, String powod){
        return mySQL.addWarn(playername, powod);
    }
    public static List<Object[]> getWarnsPlayer(String player){
        return mySQL.getPlayerWarns(player);
    }
    public static int getCountWarn(String nick){
        return mySQL.getCountWarn(nick);
    }
    public static void setupPlayerSurvival(String playername){
        mySQL.setStatsSurvivalPlayer(playername);
    }
    public static int getPlayerStatMoney(String playername ){
        return mySQL.getSurvivalMoney(playername);
    }
    public static int getPlayerStatDeaths(String playername ){
        return mySQL.getSurvivalDeaths(playername);
    }
    public static int getPlayerStatKills(String playername ){
        return mySQL.getSurvivalKills(playername);
    }
    public static void setSurvivalMoney(String playername, int amount){
        mySQL.setSurvivalMoney(playername, amount);
    }
    public static void setSurvivalKills(String playername, int amount){
        mySQL.setSurvivalKills(playername, amount);
    }
    public static void setSurvivalDeaths(String playername, int amount){
        mySQL.setSurvivalDeaths(playername, amount);
    }
    public static void addMoney(String playername, int amount){
        setSurvivalMoney(playername, getPlayerStatMoney(playername)+amount);
    }
    public static void addKills(String playername, int amount){
        setSurvivalKills(playername, getPlayerStatKills(playername)+amount);
    }
    public static void addDeaths(String playername, int amount){
        setSurvivalDeaths(playername, getPlayerStatDeaths(playername)+amount);
    }
    public static void takeMoney(String playername, int amount){
        setSurvivalMoney(playername, getPlayerStatMoney(playername)-amount);
    }
    public static void takeKills(String playername, int amount){
        setSurvivalKills(playername, getPlayerStatKills(playername)-amount);
    }
    public static void takeDeaths(String playername, int amount){
        setSurvivalDeaths(playername, getPlayerStatDeaths(playername)-amount);
    }
    public static boolean hasPlayerStats(String playername){
        return mySQL.hasPlayerStat(playername);
    }
    public static String getTopKillsName(int rank){
        return mySQL.getTopKillsPlayerName(rank);
    }
    public static int getTopKilsAmount(int rank){
        return mySQL.getTopKillsAmount(rank);
    }
    public static String getTopDeathsName(int rank){
        return mySQL.getTopDeathsPlayerName(rank);
    }
    public static int getTopDeathsAmount(int rank){
        return mySQL.getTopDeathsAmount(rank);
    }
    public static int getPlayersCountSurvival(){
        return mySQL.getPlayersCount();
    }
    public static String getReward(String playername){
        return mySQL.getPlayerReward(playername);
    }
    public static void removeReward(String playername, String reward){
        mySQL.removeReward(playername, reward);
    }
}