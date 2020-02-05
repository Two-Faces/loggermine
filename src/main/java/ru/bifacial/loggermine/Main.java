package ru.bifacial.loggermine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
    public void onEnable() {
        if (!(new File(this.getDataFolder(), "config.yml")).exists()) {
            this.createConfig();
        }

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    public void onDisable() {
        Bukkit.getLogger().info("[LoggerMine]Disable");
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("loggermine")) {
            return true;
        } else if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Неизвестная команда");
            return true;
        } else if (!args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(ChatColor.RED + "Неизвестная команда");
            return true;
        } else if (!sender.hasPermission("loggermine.reload")) {
            sender.sendMessage(ChatColor.RED + "У вас недостаточно прав");
            return true;
        } else {
            this.reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "Плагин перезапущен");
            return true;
        }
    }

    public void createConfig() {
        this.getConfig().options().header("Plugin by Newmine Team");
        if (!this.getConfig().isBoolean("LogCommand")) {
            this.getConfig().set("LogCommand", true);
        }

        if (!this.getConfig().isBoolean("LogCommandOneFile")) {
            this.getConfig().set("LogCommandOneFile", false);
        }

        if (!this.getConfig().isBoolean("LogKillerUser")) {
            this.getConfig().set("LogKillerUser", true);
        }

        this.saveConfig();
    }

    public String Cords(Player player) {
        Location cords = player.getLocation();
        return Math.ceil(cords.getX()) + "|" + Math.ceil(cords.getY()) + "|" + Math.ceil(cords.getZ());
    }

    public String World(Player player) {
        return player.getWorld().getName();
    }

    public String Line(Player player, String message) {
        return this.World(player) + "," + this.Cords(player) + "," + this.DateTime("dd-MM-yyyy kk:mm:ss") + "," + this.Login(player) + "," + message;
    }

    public String Login(Player player) {
        return player.getDisplayName();
    }

    public String DateTime(String format) {
        return (new SimpleDateFormat(format)).format(new GregorianCalendar().getTime());
    }

    public File Filed(String path, String name) throws Exception {
        File dir = new File(path);
        dir.mkdirs();
        File f = new File(path + name + ".txt");
        if (!f.exists()) {
            f.createNewFile();
        }

        return f;
    }

    public void Logs(File file, String value) throws Exception {
        BufferedWriter buffer = new BufferedWriter(new FileWriter(file, true));
        buffer.write(value);
        buffer.newLine();
        buffer.close();
    }

    @EventHandler(
            priority = EventPriority.HIGH
    )
    public void Save(AsyncPlayerChatEvent e) throws Exception {
        Player player = e.getPlayer();
        if (!e.isCancelled()) {
            File file = this.Filed("LoggerMine/logs/chat/", this.DateTime("dd-MM-yyyy"));
            this.Logs(file, this.Line(player, e.getMessage()));
        }
    }

    @EventHandler(
            priority = EventPriority.LOWEST
    )
    public void SaveKiller(PlayerDeathEvent e) throws Exception {
        if (this.getConfig().getBoolean("LogKillerUser") && e.getEntity() != null) {
            Player player = e.getEntity().getPlayer();
            Player killer = e.getEntity().getKiller();

            double damage = killer.getLastDamageCause().getFinalDamage();
            File file = this.Filed("LoggerMine/logs/killers/", "list");

            this.Logs(file, this.World(player) + "," + this.Cords(player) + "," + this.DateTime("dd-MM-yyyy kk:mm:ss") + "," + damage + "," + player.getDisplayName() + ":" + player.getLevel() + "," + killer.getDisplayName());
        }

    }

    @EventHandler(
            priority = EventPriority.HIGH
    )
    public void SaveCommand(PlayerCommandPreprocessEvent e) throws Exception {
        Player player = e.getPlayer();
        if (this.getConfig().getBoolean("LogCommand") && !e.isCancelled()) {
            boolean setting = this.getConfig().getBoolean("LogCommandOneFile");

            String path = setting ? "LoggerMine/logs/commands/" : "LoggerMine/logs/commands/players/";
            String name = setting ? this.DateTime("dd-MM-yyyy") : player.getDisplayName();

            File file = this.Filed(path, name);
            this.Logs(file, this.Line(player, e.getMessage()));
        }

    }
}