package santoro.serverXRay.service;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.UUID;

public class CurrencyService {

    private Economy economy;
    private PlayerPointsAPI playerPointsAPI;

    public ChargeResult chargeIfEnough(Player player, double vaultCost, int playerPointsCost) {
        if (vaultCost > 0 && getEconomy() == null) {
            return ChargeResult.fail("§c未找到可用 Vault 经济系统。");
        }

        if (playerPointsCost > 0 && getPlayerPointsAPI() == null) {
            return ChargeResult.fail("§c未找到可用 PlayerPoints 插件。");
        }

        if (vaultCost > 0 && !economy.has(player, vaultCost)) {
            return ChargeResult.fail("§cVault 余额不足。");
        }

        if (playerPointsCost > 0) {
            int points = playerPointsAPI.look(player.getUniqueId());
            if (points < playerPointsCost) {
                return ChargeResult.fail("§cPlayerPoints 点券不足。");
            }
        }

        boolean vaultDeducted = false;
        if (vaultCost > 0) {
            EconomyResponse response = economy.withdrawPlayer(player, vaultCost);
            if (!response.transactionSuccess()) {
                String reason = response.errorMessage == null || response.errorMessage.isBlank()
                        ? "未知错误"
                        : response.errorMessage;
                return ChargeResult.fail("§cVault 扣费失败: " + reason);
            }
            vaultDeducted = true;
        }

        if (playerPointsCost > 0) {
            UUID uuid = player.getUniqueId();
            boolean success = playerPointsAPI.take(uuid, playerPointsCost);
            if (!success) {
                if (vaultDeducted) {
                    economy.depositPlayer(player, vaultCost);
                }
                return ChargeResult.fail("§cPlayerPoints 扣费失败。");
            }
        }

        return ChargeResult.success();
    }

    private Economy getEconomy() {
        if (economy != null) {
            return economy;
        }

        RegisteredServiceProvider<Economy> registration = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (registration != null) {
            economy = registration.getProvider();
        }
        return economy;
    }

    private PlayerPointsAPI getPlayerPointsAPI() {
        if (playerPointsAPI != null) {
            return playerPointsAPI;
        }

        if (Bukkit.getPluginManager().getPlugin("PlayerPoints") instanceof PlayerPoints playerPoints) {
            playerPointsAPI = playerPoints.getAPI();
        }
        return playerPointsAPI;
    }


    public static class ChargeResult {
        private final boolean success;
        private final String message;

        public ChargeResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static ChargeResult success() {
            return new ChargeResult(true, "");
        }

        public static ChargeResult fail(String message) {
            return new ChargeResult(false, message);
        }

        public String getMessage() {
            return message;
        }

        public boolean isSuccess() {
            return success;
        }
    }
}
