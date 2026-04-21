package santoro.serverXRay.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import santoro.serverXRay.ServerXRay;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class CheckConfig {

    private final ServerXRay plugin;
    private FileConfiguration config;

    public CheckConfig(ServerXRay plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "check.yml");
        if (!file.exists()) {
            plugin.saveResource("check.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public int getDurationTicks() {
        return Math.max(1, config.getInt("check.duration", 200));
    }

    public List<String> getLoreKeywords() {
        return config.getStringList("check.lore-keywords")
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public double getVaultCost() {
        return Math.max(0D, config.getDouble("check.cost.vault", 0D));
    }

    public int getPlayerPointsCost() {
        return Math.max(0, config.getInt("check.cost.playerpoints", 0));
    }
}
