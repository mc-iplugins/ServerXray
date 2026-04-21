package santoro.serverXRay;

import fr.skytasul.glowingentities.GlowingBlocks;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import santoro.serverXRay.command.ToggleXRayCommand;
import santoro.serverXRay.config.CheckConfig;
import santoro.serverXRay.listener.CheckInteractListener;
import santoro.serverXRay.service.BlockFinderService;
import santoro.serverXRay.service.CurrencyService;
import santoro.serverXRay.service.HighlightService;
import santoro.serverXRay.service.ItemMatcherService;
import santoro.serverXRay.session.SessionManager;
import santoro.serverXRay.util.ConfigUtil;

import java.util.Map;

public class ServerXRay extends JavaPlugin {

    private static ServerXRay instance;
    private SessionManager sessionManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        saveResource("check.yml", false);

        GlowingBlocks glowing = new GlowingBlocks(this);
        Map<Material, ChatColor> highlightConfig = ConfigUtil.loadHighlights();
        BlockFinderService finderService = new BlockFinderService(highlightConfig.keySet());
        HighlightService highlightService = new HighlightService(glowing, highlightConfig);
        sessionManager = new SessionManager(finderService, highlightService);

        CheckConfig checkConfig = new CheckConfig(this);
        CurrencyService currencyService = new CurrencyService();
        ItemMatcherService itemMatcherService = new ItemMatcherService(checkConfig);

        PluginCommand xrayCommand = getCommand("xray");
        if (xrayCommand != null) {
            xrayCommand.setExecutor(new ToggleXRayCommand(sessionManager));
        }

        getServer().getPluginManager().registerEvents(
                new CheckInteractListener(sessionManager, checkConfig, itemMatcherService, currencyService),
                this
        );
    }

    @Override
    public void onDisable() {
        if (sessionManager != null) {
            sessionManager.disableAll();
        }
    }

    public static ServerXRay get() {
        return instance;
    }
}
