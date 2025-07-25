package fb.core.events;

import fb.core.api.BanAPI;
import fb.core.api.HexAPI;
import fb.core.api.RanksAPI;
import fb.core.gui.Reward;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class PlayerInventory implements Listener {

    static RanksAPI ra;

    public PlayerInventory(RanksAPI ra){
        PlayerInventory.ra = ra;
    }

    @EventHandler
    public void PlayerInventory(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        Inventory clickedInventory = e.getClickedInventory();


        if (ChatColor.stripColor(e.getView().getTitle()).equalsIgnoreCase("Nagroda") && clickedInventory != null && clickedInventory.equals(e.getInventory())) {
            e.setCancelled(true);

            switch (e.getCurrentItem().getType()) {
                case GOLD_INGOT:
                    p.closeInventory();
                    ra.setRank(p, "vip");
                    BanAPI.removeReward(p.getName(), "vip");
                    p.sendTitle(HexAPI.hex("#0096fc§lNAGRODA"), HexAPI.hex("§fOdebrales nagrode §6§lVIP"));
                    break;
                case GOLD_BLOCK:
                    p.closeInventory();
                    ra.setRank(p, "svip");
                    BanAPI.removeReward(p.getName(), "svip");
                    p.sendTitle(HexAPI.hex("#0096fc§lNAGRODA"), HexAPI.hex("§fOdebrales nagrode §e§lS§6§lVIP"));
                    break;

                default:
                    Reward.OpenGUI(p);
                    break;
            }
        }else{
            return;
        }
    }
}
