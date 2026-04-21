package santoro.serverXRay.service;

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadableNBT;
import de.tr7zw.changeme.nbtapi.iface.ReadableNBTList;
import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import santoro.serverXRay.config.CheckConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ItemMatcherService {

    private final CheckConfig checkConfig;

    public ItemMatcherService(CheckConfig checkConfig) {
        this.checkConfig = checkConfig;
    }

    public boolean isTargetItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }

        List<String> keywords = checkConfig.getLoreKeywords();
        if (keywords.isEmpty()) {
            return false;
        }

        List<String> loreLines = new ArrayList<>();
        loreLines.addAll(readLoreFromMeta(item));

        if (loreLines.isEmpty() || isMythicItem(item)) {
            loreLines.addAll(readLoreFromNbt(item));
        }

        if (loreLines.isEmpty()) {
            return false;
        }

        List<String> normalizedKeywords = keywords.stream()
                .map(this::normalize)
                .filter(s -> !s.isEmpty())
                .toList();

        for (String lore : loreLines) {
            String normalizedLore = normalize(lore);
            for (String keyword : normalizedKeywords) {
                if (normalizedLore.contains(keyword)) {
                    return true;
                }
            }
        }

        return false;
    }

    private List<String> readLoreFromMeta(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) {
            return List.of();
        }

        List<String> lore = meta.getLore();
        return lore == null ? List.of() : lore;
    }

    private List<String> readLoreFromNbt(ItemStack item) {
        List<String> lines = new ArrayList<>();

        NBT.get(item, nbt -> {
            ReadableNBT display = nbt.getCompound("display");
            if (display != null) {
                ReadableNBTList<String> lore = display.getStringList("Lore");
                if (lore != null && !lore.isEmpty()) {
                    lines.addAll(lore.toListCopy());
                }
            }

            ReadableNBT components = nbt.getCompound("components");
            if (components != null) {
                ReadableNBT loreData = components.getCompound("minecraft:lore");
                if (loreData != null) {
                    ReadableNBTList<String> loreLines = loreData.getStringList("lines");
                    if (loreLines != null && !loreLines.isEmpty()) {
                        lines.addAll(loreLines.toListCopy());
                    }
                }
            }
        });

        return lines;
    }

    private boolean isMythicItem(ItemStack item) {
        if (!Bukkit.getPluginManager().isPluginEnabled("MythicMobs")) {
            return false;
        }

        try {
            return MythicBukkit.inst().getItemManager().isMythicItem(item);
        } catch (Throwable ignored) {
            return false;
        }
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        String stripped = ChatColor.stripColor(text);
        String value = stripped == null ? text : stripped;
        return value.toLowerCase(Locale.ROOT);
    }
}
