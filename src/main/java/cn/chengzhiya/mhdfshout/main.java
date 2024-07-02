/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  me.clip.placeholderapi.PlaceholderAPI
 *  net.kyori.adventure.platform.bukkit.BukkitAudiences
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.java.JavaPlugin
 *  org.bukkit.plugin.messaging.PluginMessageListener
 */
package cn.chengzhiya.mhdfshout;

import cn.chengzhiya.mhdfshout.api.YamlFileUtil;
import cn.chengzhiya.mhdfshout.command.ShoutReload;
import cn.chengzhiya.mhdfshout.entity.Shout;
import cn.chengzhiya.mhdfshout.listener.PluginMessage;
import cn.chengzhiya.mhdfshout.task.TakeShoutDelay;
import cn.chengzhiya.mhdfshout.util.Util;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;

public final class main
extends JavaPlugin {
    public static main main;
    public static BukkitAudiences adventure;

    public static BukkitAudiences adventure() {
        if (adventure == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return adventure;
    }

    public void onEnable() {
        boolean Stats;
        main = this;
        adventure = BukkitAudiences.create(this);
        File PluginHome = this.getDataFolder();
        File ConfigFile = new File(PluginHome, "config.yml");
        File LangFile = new File(PluginHome, "lang.yml");
        if (!PluginHome.exists() && !(Stats = PluginHome.mkdirs())) {
            cn.chengzhiya.mhdfshout.api.Util.ColorLog("&c[MHDF-PluginAPI]\u63d2\u4ef6\u6570\u636e\u6587\u4ef6\u5939\u521b\u5efa\u5931\u8d25!");
        }
        if (!ConfigFile.exists()) {
            YamlFileUtil.SaveResource(this.getDataFolder().getPath(), "config.yml", "config.yml", true);
        }
        if (!LangFile.exists()) {
            YamlFileUtil.SaveResource(this.getDataFolder().getPath(), "lang.yml", "lang.yml", true);
        }
        Util.registerCommand(this, new ShoutReload(), "\u91cd\u8f7d\u914d\u7f6e", "shoutreload");
        for (String shouts : Objects.requireNonNull(this.getConfig().getConfigurationSection("HornSettings")).getKeys(false)) {
            CommandExecutor commandExecutor = (sender, command, label, args) -> {
                if (sender.hasPermission(Objects.requireNonNull(this.getConfig().getString("HornSettings." + shouts + ".Permission")))) {
                    if (Util.getShoutDelayHashMap().get(sender.getName()) != null && !sender.hasPermission("MHDFShout.Bypass.Delay") && Util.getShoutDelayHashMap().get(sender.getName()) != -1) {
                        sender.sendMessage(Util.i18n("Delay").replaceAll("\\{Delay}", String.valueOf(Util.getShoutDelayHashMap().get(sender.getName()))));
                        return false;
                    }
                    if (args.length != 0) {
                        StringBuilder shoutMessageBuilder = new StringBuilder();
                        for (String messages : args) {
                            shoutMessageBuilder.append(messages);
                            if (messages.equals(args[args.length - 1])) continue;
                            shoutMessageBuilder.append(" ");
                        }
                        if (this.getConfig().getInt("HornSettings." + shouts + ".MaxLength") != -1 && shoutMessageBuilder.toString().length() > this.getConfig().getInt("HornSettings." + shouts + ".MaxLength") && !sender.hasPermission("MHDFShout.Bypass.Length")) {
                            sender.sendMessage(Util.i18n("OutLength").replaceAll("\\{Length}", String.valueOf(this.getConfig().getInt("HornSettings." + shouts + ".MaxLength"))));
                            return false;
                        }
                        if (!sender.hasPermission("MHDFShout.Bypass.BlackWord")) {
                            for (String blackWord : this.getConfig().getStringList("ShoutSettings.BlackWordList")) {
                                if (!shoutMessageBuilder.toString().contains(blackWord)) continue;
                                sender.sendMessage(Util.i18n("BlackWord"));
                                return false;
                            }
                        }
                        String shoutMessage = cn.chengzhiya.mhdfshout.api.Util.ChatColor(PlaceholderAPI.setPlaceholders(sender instanceof Player ? (Player)sender : null, Objects.requireNonNull(this.getConfig().getString("HornSettings." + shouts + ".MessageFormat")))).replaceAll("\\{Message}", this.getConfig().getBoolean("HornSettings." + shouts + ".Color") ? cn.chengzhiya.mhdfshout.api.Util.ChatColor(shoutMessageBuilder.toString()) : shoutMessageBuilder.toString());
                        Shout shout = new Shout(this.getConfig().getString("HornSettings." + shouts + ".BossBarColor"), this.getConfig().getString("HornSettings." + shouts + ".NullBossBarMessage"), shoutMessage, Objects.requireNonNull(this.getConfig().getString("HornSettings." + shouts + ".Sound")), this.getConfig().getInt("HornSettings." + shouts + ".ShowTime"));
                        if (!sender.hasPermission("MHDFShout.Bypass.Delay")) {
                            Util.setDelay(sender.getName(), this.getConfig().getInt("ShoutSettings.Delay"));
                        }
                        if (this.getConfig().getBoolean("HornSettings." + shouts + ".Wait") && Util.getShoutWaitHashMap().get(shouts) != null && !Util.getShoutWaitHashMap().get(shouts).isEmpty()) {
                            sender.sendMessage(Util.i18n("DoneInQueue").replaceAll("\\{Size}", String.valueOf(Util.getShoutWaitHashMap().get(shouts).size())));
                        } else {
                            sender.sendMessage(Util.i18n("Done"));
                        }
                        Util.sendShout(shouts, this.getConfig().getBoolean("HornSettings." + shouts + ".Wait"), shout);
                    } else {
                        sender.sendMessage(Util.i18n("UsageError").replaceAll("\\{Command}", label));
                    }
                } else {
                    sender.sendMessage(Util.i18n("NotPermission"));
                }
                return false;
            };
            for (String commands : this.getConfig().getStringList("HornSettings." + shouts + ".Commands")) {
                Util.registerCommand(this, commandExecutor, shouts, commands);
            }
        }
        new TakeShoutDelay().runTaskTimerAsynchronously(this, 0L, 20L);
        cn.chengzhiya.mhdfshout.api.Util.ColorLog("&f[MHDF-Shout] &d  __  __ _    _ _____  ______    _____ _                 _   ");
        cn.chengzhiya.mhdfshout.api.Util.ColorLog("&f[MHDF-Shout] &d |  \\/  | |  | |  __ \\|  ____|  / ____| |               | |  ");
        cn.chengzhiya.mhdfshout.api.Util.ColorLog("&f[MHDF-Shout] &d | \\  / | |__| | |  | | |__    | (___ | |__   ___  _   _| |_ ");
        cn.chengzhiya.mhdfshout.api.Util.ColorLog("&f[MHDF-Shout] &d | |\\/| |  __  | |  | |  __|    \\___ \\| '_ \\ / _ \\| | | | __|");
        cn.chengzhiya.mhdfshout.api.Util.ColorLog("&f[MHDF-Shout] &d | |  | | |  | | |__| | |       ____) | | | | (_) | |_| | |_ ");
        cn.chengzhiya.mhdfshout.api.Util.ColorLog("&f[MHDF-Shout] &d |_|  |_|_|  |_|_____/|_|      |_____/|_| |_|\\___/ \\__,_|\\__|");
        cn.chengzhiya.mhdfshout.api.Util.ColorLog("&f[MHDF-Shout]");
        if (this.getConfig().getBoolean("BungeeCordMode")) {
            this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
            this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new PluginMessage());
            cn.chengzhiya.mhdfshout.api.Util.ColorLog("&f[MHDF-Shout] &a\u5df2\u8fde\u63a5BC!");
        }
        cn.chengzhiya.mhdfshout.api.Util.ColorLog("&f[MHDF-Shout] &a\u63d2\u4ef6\u52a0\u8f7d\u5b8c\u6210!");
        cn.chengzhiya.mhdfshout.api.Util.ColorLog("&f[MHDF-Shout] &a\u6b22\u8fce\u4f7f\u7528\u68a6\u4e1c\u7cfb\u5217\u63d2\u4ef6 \u4ea4\u6d41\u7fa4\u53f7:129139830");
    }

    public void onDisable() {
        main = null;
        if (adventure != null) {
            adventure.close();
            adventure = null;
        }
        cn.chengzhiya.mhdfshout.api.Util.ColorLog("&f[MHDF-Shout] &d  __  __ _    _ _____  ______    _____ _                 _   ");
        cn.chengzhiya.mhdfshout.api.Util.ColorLog("&f[MHDF-Shout] &d |  \\/  | |  | |  __ \\|  ____|  / ____| |               | |  ");
        cn.chengzhiya.mhdfshout.api.Util.ColorLog("&f[MHDF-Shout] &d | \\  / | |__| | |  | | |__    | (___ | |__   ___  _   _| |_ ");
        cn.chengzhiya.mhdfshout.api.Util.ColorLog("&f[MHDF-Shout] &d | |\\/| |  __  | |  | |  __|    \\___ \\| '_ \\ / _ \\| | | | __|");
        cn.chengzhiya.mhdfshout.api.Util.ColorLog("&f[MHDF-Shout] &d | |  | | |  | | |__| | |       ____) | | | | (_) | |_| | |_ ");
        cn.chengzhiya.mhdfshout.api.Util.ColorLog("&f[MHDF-Shout] &d |_|  |_|_|  |_|_____/|_|      |_____/|_| |_|\\___/ \\__,_|\\__|");
        cn.chengzhiya.mhdfshout.api.Util.ColorLog("&f[MHDF-Shout]");
        cn.chengzhiya.mhdfshout.api.Util.ColorLog("&f[MHDF-Shout] &9\u63d2\u4ef6\u5df2\u5378\u8f7d! \u611f\u8c22\u60a8\u518d\u6b21\u652f\u6301!");
        cn.chengzhiya.mhdfshout.api.Util.ColorLog("&f[MHDF-Shout] &9\u68a6\u4e1c\u7cfb\u5217\u63d2\u4ef6 \u4ea4\u6d41\u7fa4\u53f7:129139830");
    }
}

