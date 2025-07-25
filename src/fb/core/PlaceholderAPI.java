package fb.core;

import fb.core.Main;
import fb.core.api.BanAPI;
import fb.core.api.BungeeAPI;
import fb.core.api.HexAPI;
import fb.core.api.RanksAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
// Ważne: Zostaw tylko te importy, które są faktycznie używane i potrzebne do poprawnej sygnatury!
import org.bukkit.OfflinePlayer; // Użyj OfflinePlayer jeśli to jest w sygnaturze
// import org.bukkit.entity.Player; // Odkomentuj i użyj, jeśli w sygnaturze jest Player

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public class PlaceholderAPI extends PlaceholderExpansion {

    private final Main plugin;
    private final RanksAPI ranksAPI;

    public PlaceholderAPI(Main plugin, RanksAPI ranksApi) {
        this.plugin = plugin;
        this.ranksAPI = ranksApi;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "fpcode-essentials";
    }

    @Override
    public @NotNull String getAuthor() {
        return "xSpox_";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return plugin != null && plugin.isEnabled() && ranksAPI != null;
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        // ######################################################################################

        // --- PLACEHOLDERY DLA AKTUALNEGO GRACZA ---
        if (player != null) {

            // %fpcode-survival_rank%
            if (identifier.equalsIgnoreCase("rank")) {
                if (ranksAPI != null) {
                    return ranksAPI.getRank(player.getName());
                }
                return "default";
            }

            // %fpcode-survival_rankprefix%
            if (identifier.equalsIgnoreCase("rankprefix")) {
                if (ranksAPI != null) {
                    String rankName = ranksAPI.getRank(player.getName());
                    return ranksAPI.getRankPrefix(rankName);
                }
                return "";
            }


        }

        return null;
    }
}