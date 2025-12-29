package org.classiclude.gameserver.taskmanager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.World;
import org.classiclude.gameserver.model.vip.VipManager;

public class VipExpirationTaskManager
{
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void start()
    {
        scheduler.scheduleAtFixedRate(() -> 
        {
            for (Player player : World.getInstance().getPlayers())
            {
                if (player == null || player.getVipTier() == 0)
                {
                    continue; 
                }
                VipManager.getInstance().checkVipTierExpiration(player);
            }
        }, 0, 1, TimeUnit.MINUTES); 
    }

    public static void shutdown()
    {
        scheduler.shutdownNow();
    }
}
