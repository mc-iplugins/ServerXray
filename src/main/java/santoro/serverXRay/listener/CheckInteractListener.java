package santoro.serverXRay.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import santoro.serverXRay.config.CheckConfig;
import santoro.serverXRay.service.CurrencyService;
import santoro.serverXRay.service.ItemMatcherService;
import santoro.serverXRay.session.SessionManager;

public class CheckInteractListener implements Listener {

    private final SessionManager sessionManager;
    private final CheckConfig checkConfig;
    private final ItemMatcherService itemMatcherService;
    private final CurrencyService currencyService;

    public CheckInteractListener(SessionManager sessionManager,
                                 CheckConfig checkConfig,
                                 ItemMatcherService itemMatcherService,
                                 CurrencyService currencyService) {
        this.sessionManager = sessionManager;
        this.checkConfig = checkConfig;
        this.itemMatcherService = itemMatcherService;
        this.currencyService = currencyService;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCheckInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.isCancelled() && action == Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.isSneaking()) {
            return;
        }

        if (!player.hasPermission("xray.use")) {
            return;
        }

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand == null || mainHand.getType() == Material.AIR) {
            return;
        }

        if (!itemMatcherService.isTargetItem(mainHand)) {
            return;
        }

        if (sessionManager.isActive(player)) {
            player.sendMessage("§c你当前已经处于透视状态，请等待结束后再使用。");
            return;
        }

        CurrencyService.ChargeResult charge = currencyService.chargeIfEnough(
                player,
                checkConfig.getVaultCost(),
                checkConfig.getPlayerPointsCost()
        );

        if (!charge.isSuccess()) {
            player.sendMessage(charge.getMessage());
            return;
        }

        int durationTicks = checkConfig.getDurationTicks();
        sessionManager.activateTimed(player, durationTicks);
        player.sendMessage("§a透视已开启，将在 §e" + (durationTicks / 20.0D) + " §a秒后关闭。");
    }
}
