package santoro.serverXRay.session;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import santoro.serverXRay.ServerXRay;
import santoro.serverXRay.service.BlockFinderService;
import santoro.serverXRay.service.HighlightService;
import santoro.serverXRay.xray.XRayRenderer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionManager {

    private final BlockFinderService finderService;
    private final HighlightService highlightService;
    private final Map<UUID, Session> sessions = new HashMap<>();

    public SessionManager(BlockFinderService finderService, HighlightService highlightService) {
        this.finderService = finderService;
        this.highlightService = highlightService;
    }

    public boolean isActive(Player player) {
        return sessions.containsKey(player.getUniqueId());
    }

    public void toggle(Player player) {
        if (isActive(player)) {
            disable(player);
        } else {
            enable(player);
        }
    }

    public void enable(Player player) {
        Session session = getOrCreateSession(player);
        cancelAutoDisable(session);
    }

    public void activateTimed(Player player, int durationTicks) {
        if (durationTicks <= 0) {
            return;
        }

        Session session = getOrCreateSession(player);
        scheduleAutoDisable(player, session, durationTicks);
    }

    public void disable(Player player) {
        Session session = sessions.remove(player.getUniqueId());
        if (session == null) {
            return;
        }

        cancelAutoDisable(session);
        session.renderer.stop();
    }

    public void disableAll() {
        for (Session session : sessions.values()) {
            cancelAutoDisable(session);
            session.renderer.stop();
        }
        sessions.clear();
    }

    private Session getOrCreateSession(Player player) {
        Session existing = sessions.get(player.getUniqueId());
        if (existing != null) {
            return existing;
        }

        XRayRenderer renderer = new XRayRenderer(player, finderService, highlightService);
        renderer.start();

        Session session = new Session(renderer);
        sessions.put(player.getUniqueId(), session);
        return session;
    }

    private void scheduleAutoDisable(Player player, Session session, int durationTicks) {
        cancelAutoDisable(session);
        UUID uuid = player.getUniqueId();

        session.autoDisableTask = Bukkit.getScheduler().runTaskLater(ServerXRay.get(), () -> {
            Session current = sessions.get(uuid);
            if (current != session) {
                return;
            }
            disable(player);
        }, durationTicks);
    }

    private void cancelAutoDisable(Session session) {
        if (session.autoDisableTask != null) {
            session.autoDisableTask.cancel();
            session.autoDisableTask = null;
        }
    }

    private static final class Session {

        private final XRayRenderer renderer;
        private BukkitTask autoDisableTask;

        private Session(XRayRenderer renderer) {
            this.renderer = renderer;
        }
    }
}
