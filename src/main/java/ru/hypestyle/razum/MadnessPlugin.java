package ru.hypestyle.razum;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class MadnessPlugin extends JavaPlugin implements Listener {

    private Map<UUID, Integer> sanityLevels;
    private Random random;

    @Override
    public void onEnable() {
        sanityLevels = new HashMap<>();
        random = new Random();
        Bukkit.getPluginManager().registerEvents(this, this);

        // Периодическое снижение рассудка
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    decreaseSanity(player, 1);
                    checkSanityEffects(player);
                }
            }
        }.runTaskTimer(this, 0, 20 * 60); // каждую минуту

        // Рецепт кристалла разума
        ItemStack mindCrystal = new ItemStack(Material.EMERALD);
        ItemMeta meta = mindCrystal.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Кристалл разума");
            mindCrystal.setItemMeta(meta);
        }

        ShapedRecipe recipe = new ShapedRecipe(mindCrystal);
        recipe.shape(" O ", "OEO", " O ");
        recipe.setIngredient('O', Material.OBSIDIAN);
        recipe.setIngredient('E', Material.EMERALD);
        Bukkit.addRecipe(recipe);
    }

    @Override
    public void onDisable() {
        sanityLevels.clear();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        sanityLevels.put(player.getUniqueId(), 100); // Начальный уровень рассудка
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntityType() == EntityType.PLAYER) {
            Player player = (Player) event.getEntity();
            if (event.getDamager().getType() == EntityType.CREEPER || event.getDamager().getType() == EntityType.ZOMBIE) {
                decreaseSanity(player, 5);
                checkSanityEffects(player);
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() instanceof Player) {
            Player player = event.getEntity().getKiller();
            if (event.getEntityType() == EntityType.ZOMBIE || event.getEntityType() == EntityType.CREEPER) {
                increaseSanity(player, 5);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        decreaseSanity(player, 10);
        checkSanityEffects(player);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item != null && item.getType() == Material.EMERALD) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName() && meta.getDisplayName().equals(ChatColor.LIGHT_PURPLE + "Кристалл разума")) {
                int sanity = sanityLevels.getOrDefault(player.getUniqueId(), 100);
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Ваш уровень рассудка: " + sanity + "/100");
            }
        }
    }

    private void decreaseSanity(Player player, int amount) {
        UUID playerId = player.getUniqueId();
        int currentSanity = sanityLevels.getOrDefault(playerId, 100);
        sanityLevels.put(playerId, Math.max(currentSanity - amount, 0));
    }

    private void increaseSanity(Player player, int amount) {
        UUID playerId = player.getUniqueId();
        int currentSanity = sanityLevels.getOrDefault(playerId, 100);
        sanityLevels.put(playerId, Math.min(currentSanity + amount, 100));
    }

    private void checkSanityEffects(Player player) {
        int sanity = sanityLevels.getOrDefault(player.getUniqueId(), 100);

        if (sanity < 50 && sanity >= 30) {
            player.playSound(player.getLocation(), Sound.ENTITY_GHAST_SCREAM, 0.5f, 0.5f);
        }

        if (sanity < 40 && sanity >= 20) {
            if (random.nextInt(100) < 10) {
                player.spawnParticle(Particle.SMOKE_NORMAL, player.getLocation(), 20);
            }
        }

        if (sanity < 30 && sanity >= 10) {
            if (random.nextInt(100) < 15) {
                World world = player.getWorld();
                world.spawnEntity(player.getLocation(), EntityType.ZOMBIE);
            }
        }

        if (sanity < 20 && sanity >= 10) {
            if (random.nextInt(100) < 20) {
                Vector velocity = player.getVelocity();
                velocity.setX(-velocity.getX());
                velocity.setZ(-velocity.getZ());
                player.setVelocity(velocity);
            }
        }

        if (sanity < 15 && sanity >= 5) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 200, 1));
        }

        if (sanity < 10 && sanity >= 5) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, 1));
        }

        if (sanity < 5) {
            if (random.nextInt(100) < 20) {
                Vector randomVector = new Vector(random.nextDouble() - 0.5, 0, random.nextDouble() - 0.5).normalize().multiply(5);
                player.teleport(player.getLocation().add(randomVector));
            }
            player.damage(1);
        }
    }
}
