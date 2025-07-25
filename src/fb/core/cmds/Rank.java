package fb.core.cmds;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fb.core.Main;
import fb.core.api.HexAPI;
import fb.core.api.RanksAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

public class Rank implements CommandExecutor, TabExecutor {
    Main plugin;
    static RanksAPI ra;
    static HexAPI h;
    private final String name = HexAPI.hex("#0096fcRanks");

    public Rank(Main m) {
        this.plugin = m;
        ra = new RanksAPI(plugin);
        h = new HexAPI();
    }
    public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
        if (sender instanceof Player p) {
            if (ra.hasPermission(p, "fb.rank")) {
                HexAPI var10001;
                String rank;
                if (args.length == 3) {
                    if (args[0].equalsIgnoreCase("set")) {
                        rank = args[1];
                        Player cel = Bukkit.getPlayerExact(args[2]);
                        ra.setRank(cel, rank);
                        var10001 = h;
                        p.sendMessage(HexAPI.hex("§8[#0096FC⚡§8] §fUstawiles range #0096FC" + rank + " §fdla gracza #0096FC" + cel.getName()));
                        cel.sendTitle(this.name, HexAPI.hex("§fPosiadasz nowa range " + ra.getRankPrefix(rank)));
                    } else if (args[0].equalsIgnoreCase("setdefault")) {
                        if (args[1].equalsIgnoreCase("rank")) {
                            rank = args[2];
                            ra.setDefaultRank(rank);
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("§8[#0096FC⚡§8] §fUstawiles domysna range na #0096FC" + rank));
                        } else {
                            p.sendMessage("");
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("§8[#0096FC☆§8]--------" + this.name + "§8--------[#0096FC☆§8]"));
                            p.sendMessage("");
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("#0096FC/rank §fautor"));
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("#0096FC/rank §freload"));
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("#0096FC/rank check §f<gracz>"));
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("#0096FC/rank set §f<ranga> <gracz>"));
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("#0096FC/rank setdefault rank §f<ranga>"));
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("#0096FC/rank group add §f<ranga> <uprawnienie>"));
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("#0096FC/rank group remove §f<ranga> <uprawnienie>"));
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("#0096FC/rank group prefix §f<ranga> <prefix>"));
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("#0096FC/rank group suffix §f<ranga> <suffix>"));
                            p.sendMessage(HexAPI.hex("#0096FC/rank group weight §f<ranga> <weight>"));
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("#0096FC/rank group create §f<ranga> <prefix>"));
                            p.sendMessage("");
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("§8[#0096FC☆§8]--------" + this.name + "§8--------[#0096FC☆§8]"));
                            p.sendMessage("");
                        }
                    } else {
                        p.sendMessage("");
                        var10001 = h;
                        p.sendMessage(HexAPI.hex("§8[#0096FC☆§8]--------" + this.name + "§8--------[#0096FC☆§8]"));
                        p.sendMessage("");
                        var10001 = h;
                        p.sendMessage(HexAPI.hex("#0096FC/rank §fautor"));
                        var10001 = h;
                        p.sendMessage(HexAPI.hex("#0096FC/rank §freload"));
                        var10001 = h;
                        p.sendMessage(HexAPI.hex("#0096FC/rank check §f<gracz>"));
                        var10001 = h;
                        p.sendMessage(HexAPI.hex("#0096FC/rank set §f<ranga> <gracz>"));
                        var10001 = h;
                        p.sendMessage(HexAPI.hex("#0096FC/rank setdefault rank §f<ranga>"));
                        var10001 = h;
                        p.sendMessage(HexAPI.hex("#0096FC/rank group add §f<ranga> <uprawnienie>"));
                        var10001 = h;
                        p.sendMessage(HexAPI.hex("#0096FC/rank group remove §f<ranga> <uprawnienie>"));
                        var10001 = h;
                        p.sendMessage(HexAPI.hex("#0096FC/rank group prefix §f<ranga> <prefix>"));
                        var10001 = h;
                        p.sendMessage(HexAPI.hex("#0096FC/rank group suffix §f<ranga> <suffix>"));
                        p.sendMessage(HexAPI.hex("#0096FC/rank group weight §f<ranga> <weight>"));
                        var10001 = h;
                        p.sendMessage(HexAPI.hex("#0096FC/rank group create §f<ranga> <prefix>"));
                        p.sendMessage("");
                        var10001 = h;
                        p.sendMessage(HexAPI.hex("§8[#0096FC☆§8]--------" + this.name + "§8--------[#0096FC☆§8]"));
                        p.sendMessage("");
                    }
                } else {
                    String prefix;
                    if (args.length == 2) {
                        if (args[0].equalsIgnoreCase("check")) {
                            Player cel = Bukkit.getPlayerExact(args[1]);
                            prefix = ra.getRank(cel.getName());
                            var10001 = h;
                            String var10 = cel.getName();
                            p.sendMessage(HexAPI.hex("§8[#0096FC⚡§8] §fRanga gracza #0096FC" + var10 + " §fto #0096FC" + prefix));
                        } else {
                            p.sendMessage("");
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("§8[#0096FC☆§8]--------" + this.name + "§8--------[#0096FC☆§8]"));
                            p.sendMessage("");
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("#0096FC/rank §fautor"));
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("#0096FC/rank §freload"));
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("#0096FC/rank check §f<gracz>"));
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("#0096FC/rank set §f<ranga> <gracz>"));
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("#0096FC/rank setdefault rank §f<ranga>"));
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("#0096FC/rank group add §f<ranga> <uprawnienie>"));
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("#0096FC/rank group remove §f<ranga> <uprawnienie>"));
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("#0096FC/rank group prefix §f<ranga> <prefix>"));
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("#0096FC/rank group suffix §f<ranga> <suffix>"));
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("#0096FC/rank group weight §f<ranga> <weight>"));
                            p.sendMessage(HexAPI.hex("#0096FC/rank group create §f<ranga> <prefix>"));
                            p.sendMessage("");
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("§8[#0096FC☆§8]--------" + this.name + "§8--------[#0096FC☆§8]"));
                            p.sendMessage("");
                        }
                    } else if (args.length == 1) {
                        if (args[0].equalsIgnoreCase("autor")) {
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("§8[#0096FC⚡§8] §fAutorem pluginu jest #0096FCxSpox_"));
                        } else if (args[0].equalsIgnoreCase("reload")) {
                            ra.reloadConfig();
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("§8[#0096FC⚡§8] §fPrzeladowano #0096FCpermissions.yml"));
                        } else {
                            p.sendMessage("");
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("§8[#0096FC☆§8]--------" + this.name + "§8--------[#0096FC☆§8]"));
                            p.sendMessage("");
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("#0096FC/rank §fautor"));
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("#0096FC/rank §freload"));
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("#0096FC/rank check §f<gracz>"));
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("#0096FC/rank set §f<ranga> <gracz>"));
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("#0096FC/rank setdefault rank §f<ranga>"));
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("#0096FC/rank group add §f<ranga> <uprawnienie>"));
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("#0096FC/rank group remove §f<ranga> <uprawnienie>"));
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("#0096FC/rank group prefix §f<ranga> <prefix>"));
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("#0096FC/rank group suffix §f<ranga> <suffix>"));
                            p.sendMessage(HexAPI.hex("#0096FC/rank group weight §f<ranga> <weight>"));
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("#0096FC/rank group create §f<ranga> <prefix>"));
                            p.sendMessage("");
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("§8[#0096FC☆§8]--------" + this.name + "§8--------[#0096FC☆§8]"));
                            p.sendMessage("");
                        }
                    } else if (args.length == 4) {
                        if (args[0].equalsIgnoreCase("group")) {
                            if (args[1].equalsIgnoreCase("add")) {
                                rank = args[2];
                                prefix = args[3];
                                ra.addRankPermission(rank, prefix);
                                var10001 = h;
                                p.sendMessage(HexAPI.hex("§8[#0096FC⚡§8] §fDodales uprawnienie #0096FC" + prefix + " §fdo rangi #0096FC" + rank));
                            } else if (args[1].equalsIgnoreCase("remove")) {
                                rank = args[2];
                                prefix = args[3];
                                ra.removeRankPermission(rank, prefix);
                                var10001 = h;
                                p.sendMessage(HexAPI.hex("§8[#0096FC⚡§8] §fUsunoles uprawnienie #0096FC" + prefix + " §fz rangi #0096FC" + rank));
                            } else {
                                HexAPI var10002;
                                if (args[1].equalsIgnoreCase("prefix")) {
                                    rank = args[2];
                                    prefix = args[3].replace("&", "§");
                                    ra.setRankPrefix(rank, prefix);
                                    var10001 = h;
                                    var10002 = h;
                                    p.sendMessage(HexAPI.hex("§8[#0096FC⚡§8] §fUstawiles prefix rangi #0096FC" + rank + " §fna #0096FC" + HexAPI.hex(prefix)));
                                } else if (args[1].equalsIgnoreCase("suffix")) {
                                    rank = args[2];
                                    prefix = args[3].replace("&", "§");
                                    ra.setRankSuffix(rank, prefix);
                                    var10001 = h;
                                    var10002 = h;
                                    p.sendMessage(HexAPI.hex("§8[#0096FC⚡§8] §fUstawiles suffix rangi #0096FC" + rank + " §fna #0096FC" + HexAPI.hex(prefix) + "text"));
                                } else if (args[1].equalsIgnoreCase("weight")) {
                                    rank = args[2];
                                    int weight = Integer.parseInt(args[3]);
                                    ra.setWeight(rank, weight);
                                    var10001 = h;
                                    var10002 = h;
                                    p.sendMessage(HexAPI.hex("§8[#0096FC⚡§8] §fUstawiles weight rangi #0096FC" + rank + " §fna #0096FC" + weight));
                                } else if (args[1].equalsIgnoreCase("create")) {
                                    rank = args[2];
                                    prefix = args[3].replace("&", "§");
                                    ra.createRank(rank, prefix);
                                    var10001 = h;
                                    var10002 = h;
                                    p.sendMessage(HexAPI.hex("§8[#0096FC⚡§8] §fStworzyles range #0096FC" + rank + " §fz prefixem #0096FC" + HexAPI.hex(prefix)));
                                } else {
                                    p.sendMessage("");
                                    var10001 = h;
                                    p.sendMessage(HexAPI.hex("§8[#0096FC☆§8]--------" + this.name + "§8--------[#0096FC☆§8]"));
                                    p.sendMessage("");
                                    var10001 = h;
                                    p.sendMessage(HexAPI.hex("#0096FC/rank §fautor"));
                                    var10001 = h;
                                    p.sendMessage(HexAPI.hex("#0096FC/rank §freload"));
                                    var10001 = h;
                                    p.sendMessage(HexAPI.hex("#0096FC/rank check §f<gracz>"));
                                    var10001 = h;
                                    p.sendMessage(HexAPI.hex("#0096FC/rank set §f<ranga> <gracz>"));
                                    var10001 = h;
                                    p.sendMessage(HexAPI.hex("#0096FC/rank setdefault rank §f<ranga>"));
                                    var10001 = h;
                                    p.sendMessage(HexAPI.hex("#0096FC/rank group add §f<ranga> <uprawnienie>"));
                                    var10001 = h;
                                    p.sendMessage(HexAPI.hex("#0096FC/rank group remove §f<ranga> <uprawnienie>"));
                                    var10001 = h;
                                    p.sendMessage(HexAPI.hex("#0096FC/rank group prefix §f<ranga> <prefix>"));
                                    var10001 = h;
                                    p.sendMessage(HexAPI.hex("#0096FC/rank group suffix §f<ranga> <suffix>"));
                                    p.sendMessage(HexAPI.hex("#0096FC/rank group weight §f<ranga> <weight>"));
                                    var10001 = h;
                                    p.sendMessage(HexAPI.hex("#0096FC/rank group create §f<ranga> <prefix>"));
                                    p.sendMessage("");
                                    var10001 = h;
                                    p.sendMessage(HexAPI.hex("§8[#0096FC☆§8]--------" + this.name + "§8--------[#0096FC☆§8]"));
                                    p.sendMessage("");
                                }
                            }
                        } else {
                            p.sendMessage("");
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("§8[#0096FC☆§8]--------" + this.name + "§8--------[#0096FC☆§8]"));
                            p.sendMessage("");
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("#0096FC/rank §fautor"));
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("#0096FC/rank §freload"));
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("#0096FC/rank check §f<gracz>"));
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("#0096FC/rank set §f<ranga> <gracz>"));
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("#0096FC/rank setdefault rank §f<ranga>"));
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("#0096FC/rank group add §f<ranga> <uprawnienie>"));
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("#0096FC/rank group remove §f<ranga> <uprawnienie>"));
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("#0096FC/rank group prefix §f<ranga> <prefix>"));
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("#0096FC/rank group suffix §f<ranga> <suffix>"));
                            p.sendMessage(HexAPI.hex("#0096FC/rank group weight §f<ranga> <weight>"));
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("#0096FC/rank group create §f<ranga> <prefix>"));
                            p.sendMessage("");
                            var10001 = h;
                            p.sendMessage(HexAPI.hex("§8[#0096FC☆§8]--------" + this.name + "§8--------[#0096FC☆§8]"));
                            p.sendMessage("");
                        }
                    } else {
                        p.sendMessage("");
                        var10001 = h;
                        p.sendMessage(HexAPI.hex("§8[#0096FC☆§8]--------" + this.name + "§8--------[#0096FC☆§8]"));
                        p.sendMessage("");
                        var10001 = h;
                        p.sendMessage(HexAPI.hex("#0096FC/rank §fautor"));
                        var10001 = h;
                        p.sendMessage(HexAPI.hex("#0096FC/rank §freload"));
                        var10001 = h;
                        p.sendMessage(HexAPI.hex("#0096FC/rank check §f<gracz>"));
                        var10001 = h;
                        p.sendMessage(HexAPI.hex("#0096FC/rank set §f<ranga> <gracz>"));
                        var10001 = h;
                        p.sendMessage(HexAPI.hex("#0096FC/rank setdefault rank §f<ranga>"));
                        var10001 = h;
                        p.sendMessage(HexAPI.hex("#0096FC/rank group add §f<ranga> <uprawnienie>"));
                        var10001 = h;
                        p.sendMessage(HexAPI.hex("#0096FC/rank group remove §f<ranga> <uprawnienie>"));
                        var10001 = h;
                        p.sendMessage(HexAPI.hex("#0096FC/rank group prefix §f<ranga> <prefix>"));
                        var10001 = h;
                        p.sendMessage(HexAPI.hex("#0096FC/rank group suffix §f<ranga> <suffix>"));
                        p.sendMessage(HexAPI.hex("#0096FC/rank group weight §f<ranga> <weight>"));
                        var10001 = h;
                        p.sendMessage(HexAPI.hex("#0096FC/rank group create §f<ranga> <prefix>"));
                        p.sendMessage("");
                        var10001 = h;
                        p.sendMessage(HexAPI.hex("§8[#0096FC☆§8]--------" + this.name + "§8--------[#0096FC☆§8]"));
                        p.sendMessage("");
                    }
                }
            } else {
                p.sendMessage(HexAPI.hex("§cNie posiadasz uprawnienia (fb.rank)"));
            }
        } else {
            sender.sendMessage("§cKomenda tylko dla graczy");
        }

        return false;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player p) {
            if (!ra.hasPermission(p, "fb.rank")) {
                return null;
            } else {
                List<String> tab = new ArrayList();
                if (args.length == 1) {
                    tab.add("autor");
                    tab.add("reload");
                    tab.add("check");
                    tab.add("set");
                    tab.add("setdefault");
                    tab.add("group");
                } else {
                    int i;
                    String s1;
                    if (args.length == 2) {
                        if (args[0].equalsIgnoreCase("set")) {
                            for(i = 0; i < ra.getRanks().size(); ++i) {
                                s1 = (String)ra.getRanks().get(i);
                                tab.add(s1);
                            }
                        } else if (args[0].equalsIgnoreCase("setdefault")) {
                            tab.add("rank");
                        } else if (args[0].equalsIgnoreCase("group")) {
                            tab.add("add");
                            tab.add("remove");
                            tab.add("prefix");
                            tab.add("suffix");
                            tab.add("create");
                            tab.add("weight");
                        }
                    } else {
                        Iterator var9;
                        Player ps;
                        if (args.length == 3) {
                            if (args[0].equalsIgnoreCase("setdefault")) {
                                if (args[1].equalsIgnoreCase("rank")) {
                                    for(i = 0; i < ra.getRanks().size(); ++i) {
                                        s1 = (String)ra.getRanks().get(i);
                                        tab.add(s1);
                                    }
                                }
                            } else if (args[0].equalsIgnoreCase("group")) {
                                for(i = 0; i < ra.getRanks().size(); ++i) {
                                    s1 = (String)ra.getRanks().get(i);
                                    tab.add(s1);
                                }
                            } else if (args[0].equalsIgnoreCase("set")) {
                                var9 = Bukkit.getOnlinePlayers().iterator();

                                while(var9.hasNext()) {
                                    ps = (Player)var9.next();
                                    tab.add(ps.getName());
                                }
                            }
                        } else if (args.length == 4) {
                            if (!args[1].equalsIgnoreCase("add") && !args[1].equalsIgnoreCase("remove")) {
                                if (!args[1].equalsIgnoreCase("create") && !args[1].equalsIgnoreCase("prefix")) {
                                    if (args[1].equalsIgnoreCase("suffix")) {
                                        tab.add("<suffix>");
                                    } else if (args[0].equalsIgnoreCase("set")) {
                                        var9 = Bukkit.getOnlinePlayers().iterator();

                                        while(var9.hasNext()) {
                                            ps = (Player)var9.next();
                                            tab.add(ps.getName());
                                        }
                                    }
                                } else {
                                    tab.add("<prefix>");
                                }
                            } else {
                                tab.add("<uprawnienie>");
                            }
                            if(args[1].equalsIgnoreCase("weight")){
                                tab.add("<1-500>");
                            }
                        }
                    }
                }

                return tab;
            }
        } else {
            return null;
        }
    }
}
