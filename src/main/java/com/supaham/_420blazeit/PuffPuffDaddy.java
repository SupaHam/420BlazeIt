package com.supaham._420blazeit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class PuffPuffDaddy extends JavaPlugin implements Listener {

    private final Set<Player> smokers = new HashSet<>();
    private final Set<Player> dontShow = new HashSet<>();
    private int interval, particleAmount, radius;
    private float speed, distanceFromMouth, offset;
    private boolean smokeInRain;
    private FuckingSmokers task;

    @Override
    public void onEnable() {
        reload();
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void smokersSpreadingTheirAidsOnTheServer(PlayerJoinEvent event) {
        if (event.getPlayer().hasPermission("420blazeit.onjoin")) {
            this.smokers.add(event.getPlayer());
        }
    }

    @EventHandler
    public void smokersPussyingOut(PlayerQuitEvent event) {
        this.smokers.remove(event.getPlayer());
        this.dontShow.remove(event.getPlayer());
    }

    private void reload() {
        int oldInterval = interval;
        reloadConfig();
        FileConfiguration config = getConfig();
        config.addDefault("smoke-interval", 3);
        config.addDefault("particle-speed", 0.01);
        config.addDefault("particle-amount", 3);
        config.addDefault("particle-radius", 32);
        config.addDefault("particle-dist-from-mouth", 0.4);
        config.addDefault("smoke-in-rain", false);
        config.addDefault("offset-from-mouth", 30.0);
        config.options().copyDefaults(true);
        saveConfig();
        this.interval = config.getInt("smoke-interval");
        if (this.interval < 0) {
            this.interval = 0;
        }
        this.particleAmount = config.getInt("particle-amount");
        this.radius = config.getInt("particle-radius");
        this.speed = (float) config.getDouble("particle-speed");
        this.distanceFromMouth = (float) config.getDouble("particle-dist-from-mouth");
        this.smokeInRain = config.getBoolean("smoke-in-rain");
        this.offset = (float) config.getDouble("offset-from-mouth");

        if (this.interval != oldInterval || this.task == null) {
            if (this.task != null) {
                this.task.cancel();
                this.task = null;
            }
            (this.task = new FuckingSmokers()).runTaskTimer(this, 0, interval);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("420blazeit")) {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("reload")) {
                    if (!sender.hasPermission("420blazeit.reload")) {
                        sender.sendMessage(ChatColor.RED + "No permissions, bruuuh.");
                        return true;
                    }
                    reload();
                    sender.sendMessage(ChatColor.YELLOW + "You successfully blazed the config.");
                    return true;
                } else if (args[0].equalsIgnoreCase("with")) {
                    if (args.length <= 1) {
                        sender.sendMessage(ChatColor.RED + "Who do you want to blaze it with?");
                        return true;
                    }
                    Player player = Bukkit.getPlayer(args[1]);
                    if (player == null) {
                        sender.sendMessage(ChatColor.RED + args[1] + " is not available to blaze it with you.");
                        return true;
                    }
                    this.smokers.add(player);
                    sender.sendMessage(ChatColor.YELLOW + "You successfully 420blazedit with " 
                            + args[1] + ".");
                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED + "Unknown subcommand. Available subcommands: " 
                            + "reload, with.");
                    return true;
                }
            } else {
                if (sender instanceof Player) {
                    if (!sender.hasPermission("420blazeit.smoke")) {
                        sender.sendMessage(ChatColor.RED + "You're not cool enough to blaze.");
                        return true;
                    }
                    this.smokers.add(((Player) sender));
                    this.dontShow.remove(sender);
                    sender.sendMessage(ChatColor.GREEN + "420blazeit.");
                    return true;
                }
            }
        } else if (label.equalsIgnoreCase("iquit")) {
            if (sender instanceof Player) {
                if (!sender.hasPermission("420blazeit.quit")) {
                    sender.sendMessage(ChatColor.RED + "Once you smoke, you can't go broke, or something like that...");
                    return true;
                }
                if (args.length > 0) {
                    this.dontShow.add(((Player) sender));
                    sender.sendMessage(ChatColor.GRAY + "You can only lie to yourself for so long...");
                } else {
                    this.smokers.remove(sender);
                    this.dontShow.remove(sender);
                    sender.sendMessage(ChatColor.GREEN + "You did it, congratulations! How many years did you waste " +
                            "in Narcotics Anonymous?");
                }
                return true;
            }
        }
        sender.sendMessage(ChatColor.RED + "You're a computer, you can't blaze it.");
        return true;
    }

    public class FuckingSmokers extends BukkitRunnable {

        @Override
        public void run() {
            for (Player smoker : smokers) {
                if ((!smokeInRain && smoker.getWorld().hasStorm())) {
                    continue;
                }
                double distance = distanceFromMouth;
                Location player_loc = smoker.getLocation();
                double rot_x = ((player_loc.getYaw() + 90.0F + offset) % 360.0F);
                double rot_y = player_loc.getPitch() * -1.0F;
                double h_length = distance * Math.cos(Math.toRadians(rot_y));
                double yOff = distance * Math.sin(Math.toRadians(rot_y));
                double xOff = h_length * Math.cos(Math.toRadians(rot_x));
                double zOff = h_length * Math.sin(Math.toRadians(rot_x));
                Location loc = new Location(smoker.getWorld(), xOff + player_loc.getX(),
                        yOff + player_loc.getY() + 1.5D, zOff + player_loc.getZ());
                for (Player player : smoker.getWorld().getPlayers()) {
                    if (!player.equals(smoker) || !dontShow.contains(smoker)) {
                        player.spigot().playEffect(loc, Effect.PARTICLE_SMOKE, 0, 0, 0, 0, 0,
                                speed, particleAmount, radius);
                    }
                }
            }
        }
    }
}
