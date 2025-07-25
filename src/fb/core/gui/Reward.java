package fb.core.gui;

import fb.core.api.BanAPI;
import fb.core.api.HexAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class Reward {

    public static void OpenGUI(Player p){
        Inventory i = Bukkit.createInventory(null, 27, "§8§lNagroda");

        ItemStack grayglass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemStack blueglass = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
        
        for(int x = 0; x<27;x++){
            i.setItem(x, grayglass);
        }
        setItem(new ItemStack(Material.AIR), i, 10, 7);
        i.setItem(1, blueglass);
        i.setItem(7, blueglass);
        i.setItem(9, blueglass);
        i.setItem(17, blueglass);
        i.setItem(19, blueglass);
        i.setItem(25, blueglass);

        i.setItem(13, getitem(BanAPI.getReward(p.getName())));

        p.openInventory(i);
    }
    private static void setItem(ItemStack item, Inventory i, int start, int ilosc){
        int end = start+ilosc;
        for(int x = start; x<end;x++){
            i.setItem(x, item);
        }
    }
    private static ItemStack getitem(String reward){
        if(reward.equals("vip")){
            ItemStack item = new ItemStack(Material.GOLD_INGOT);
            ItemMeta meta = item.getItemMeta();

            meta.setDisplayName(HexAPI.hex("#0096fc§lNAGRODA §6§lVIP"));

            ArrayList<String> lore = new ArrayList<>();

            lore.add("");
            lore.add(HexAPI.hex("  §8--===[ #0096fcFPCode.PL §8]===--"));
            lore.add("");
            lore.add(HexAPI.hex(" §7Gratulacje z zakupu rangi §eVIP§7!"));
            lore.add(HexAPI.hex(" §7Ten token symbolizuje Twoje wsparcie"));
            lore.add(HexAPI.hex(" §7i dostęp do ekskluzywnych przywilejów."));
            lore.add("");
            lore.add(HexAPI.hex(" #0096fc★ §7Twoja Ranga: §eVIP"));
            lore.add("");
            lore.add(HexAPI.hex("§aKliknij LPM aby odebrac"));
            lore.add("");
            lore.add(HexAPI.hex(" §8# Pamiątka z FPCode.PL - Nieprzemienny #"));
            lore.add("");

            meta.setLore(lore);
            item.setItemMeta(meta);
            return item;
        }else if(reward.equals("svip")){
            ItemStack item = new ItemStack(Material.GOLD_BLOCK);
            ItemMeta meta = item.getItemMeta();

            meta.setDisplayName(HexAPI.hex("#0096fc§lNAGRODA §e§lS§6§lVIP"));

            ArrayList<String> lore = new ArrayList<>();

            lore.add("");
            lore.add(HexAPI.hex("  §8--===[ #0096fcFPCode.PL §8]===--"));
            lore.add("");
            lore.add(HexAPI.hex(" §7Gratulacje z zakupu rangi §eSVIP§7!"));
            lore.add(HexAPI.hex(" §7Ten token symbolizuje Twoje wsparcie"));
            lore.add(HexAPI.hex(" §7i dostęp do ekskluzywnych przywilejów."));
            lore.add("");
            lore.add(HexAPI.hex(" #0096fc★ §7Twoja Ranga: §eSVIP"));
            lore.add("");
            lore.add(HexAPI.hex("§aKliknij LPM aby odebrac"));
            lore.add("");
            lore.add(HexAPI.hex(" §8# Pamiątka z FPCode.PL - Nieprzemienny #"));
            lore.add("");

            meta.setLore(lore);
            item.setItemMeta(meta);
            return item;
        }else if(reward.equals("Brak")){
            ItemStack item = new ItemStack(Material.AIR);

            return item;
        }else{
            ItemStack item = new ItemStack(Material.BARRIER);
            ItemMeta meta = item.getItemMeta();

            meta.setDisplayName("§cBLAD");

            ArrayList<String> lore = new ArrayList<>();

            lore.add("§cWystapil blad zglos to do administracji");

            meta.setLore(lore);
            item.setItemMeta(meta);
            return item;
        }
    }
}
