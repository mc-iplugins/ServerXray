package santoro.serverXRay.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

public final class ActionBarUtil {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

    private ActionBarUtil() {
    }

    public static void send(Player player, String message) {
        Component component = LEGACY_SERIALIZER.deserialize(message == null ? "" : message);
        player.sendActionBar(component);
    }
}
