package fb.core.api;

import fb.core.Main;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class BossBarAPI {

    private final Main plugin;
    private int taskID = -1;
    private BossBar bar;
    private int count = -1; // Inicjalizujemy count tutaj
    private double progress = 1.0;
    private final double time = 1.0 / 30;

    public BossBarAPI(Main m) {
        plugin = m;
        createBar(); // Inicjalizujemy BossBar w konstruktorze
        cast();
    }

    public void addPlayer(Player p) {
        if (bar != null) {
            bar.addPlayer(p);
        }
    }

    public BossBar getBar() {
        return bar;
    }

    public void createBar() {
        bar = Bukkit.createBossBar("§fᴡᴇᴊᴅᴢ ɴᴀ ᴅɪѕᴄᴏʀᴅᴀ §bᴅᴄ.ꜰᴘᴄᴏᴅᴇ.ᴘʟ", BarColor.BLUE, BarStyle.SOLID);
        bar.setVisible(true);
    }

    public void cast() {
        if (bar == null) {
            createBar(); // Upewniamy się, że pasek istnieje
        }
        setTaskID(Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                bar.setProgress(progress);
                switch (count) {
                    case -1:
                        break;
                    case 0:
                        bar.setColor(BarColor.PURPLE);
                        bar.setTitle("§fѕᴛʀᴏɴᴀ ᴡᴡᴡ §bᴡᴡᴡ.ꜰᴘᴄᴏᴅᴇ.ᴘʟ");
                        break;
                    case 1:
                        bar.setColor(BarColor.WHITE);
                        bar.setTitle("§fᴘᴏᴛʀᴢᴇʙᴜᴊᴇѕᴢ ᴘᴏᴍᴏᴄʏ? §b/ʜᴇʟᴘᴏᴘ");
                        break;
                    case 2:
                        bar.setColor(BarColor.PINK);
                        bar.setTitle("§fѕᴇʀᴡᴇʀ ᴊᴇѕᴛ ʜᴏѕᴛᴏᴡᴀɴʏ ᴘʀᴢᴇᴢ ᴇᴋɪᴘᴇ §cꜰᴘᴄᴏᴅᴇ x ᴅᴇᴠᴇʟᴏᴘᴇʀѕ");
                        break;
                    default:
                        bar.setColor(BarColor.BLUE);
                        bar.setTitle("§fᴡᴇᴊᴅᴢ ɴᴀ ᴅɪѕᴄᴏʀᴅᴀ §bᴅᴄ.ꜰᴘᴄᴏᴅᴇ.ᴘʟ");
                        count = -1;
                        break;
                }
                progress = progress - time;
                if (progress <= 0) {
                    count++;
                    progress = 1.0;
                }
            }
        }, 0L, 20L));
    }

    public int getTaskID() {
        return taskID;
    }

    public void setTaskID(int taskID) {
        this.taskID = taskID; // Używamy 'this' aby odwołać się do pola klasy
    }
}
