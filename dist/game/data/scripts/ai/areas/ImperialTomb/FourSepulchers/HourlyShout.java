package ai.areas.ImperialTomb.FourSepulchers;

import java.util.Calendar;

import org.classiclude.commons.threads.ThreadPool;
import org.classiclude.gameserver.enums.ChatType;
import org.classiclude.gameserver.model.World;
import org.classiclude.gameserver.model.actor.Npc;
import org.classiclude.gameserver.network.serverpackets.CreatureSay;

public class HourlyShout
{
	private static final int[] NPC_IDS =
	{
		31921,
		31922,
		31923,
		31924
	
	};
	
	private static final String SHOUT_MESSAGE = "You may now enter the Sepulcher.";
	private static final String SHOUT_MESSAGE2 = "If you place your hand on the stone statue in front of each sepulcher, you will be able to enter.";
	private static final int SHOUT_RANGE = 15000;
	
	public static void main(String[] args)
	{
		new HourlyShout();
	}
	
	public HourlyShout()
	{
		scheduleHourlyShout();
	}
	
	private void scheduleHourlyShout()
	{
		long delay = calculateDelayUntilNextShout();
		
		ThreadPool.scheduleAtFixedRate(() ->
		{
			shoutMessage();
		}, delay, 3600000);
	}
	
	private long calculateDelayUntilNextShout()
	{
		Calendar calendar = Calendar.getInstance();
		int currentMinute = calendar.get(Calendar.MINUTE);
		int currentSecond = calendar.get(Calendar.SECOND);
		
		int delayMinutes = ((55 - currentMinute) + 60) % 60;
		int delayMillis = ((delayMinutes * 60) - currentSecond) * 1000;
		
		return delayMillis > 0 ? delayMillis : 0;
	}
	
	private void shoutMessage()
	{
		for (int npcId : NPC_IDS)
		{
			Npc npcInstance = World.getInstance().getNpc(npcId);
			
			if (npcInstance != null)
			{
				CreatureSay shoutPacket = new CreatureSay(npcInstance, ChatType.SHOUT, npcInstance.getName(), SHOUT_MESSAGE);
				CreatureSay shoutPacket1 = new CreatureSay(npcInstance, ChatType.SHOUT, npcInstance.getName(), SHOUT_MESSAGE2);
				World.getInstance().getPlayers().stream().filter(player -> player.isInsideRadius3D(npcInstance, SHOUT_RANGE)).forEach(player -> player.sendPacket(shoutPacket));
				World.getInstance().getPlayers().stream().filter(player -> player.isInsideRadius3D(npcInstance, SHOUT_RANGE)).forEach(player -> player.sendPacket(shoutPacket1));
			}
			else
			{
				System.out.println("NPC con ID " + npcId + " No se encontro para shout de 4s");
			}
		}
	}
}
