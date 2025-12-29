/*
 * Copyright (c) 2013 classiclude
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ai.others.Mammons.PriestOfMammon;

import org.classiclude.Config;
import org.classiclude.gameserver.model.Location;
import org.classiclude.gameserver.model.actor.Npc;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.util.Broadcast;

import ai.AbstractNpcAI;

/**
 * @author Mobius, Minzee
 */
public class PriestOfMammon extends AbstractNpcAI
{
	// NPC
	private static final int PRIEST = 33511;
	// Locations
	private static final Location[] LOCATIONS =
	{
		new Location(146882, 29665, -2264, 0), // Aden
		new Location(81284, 150155, -3528, 891), // Giran
		new Location(42784, -41236, -2192, 37972), // Rune
	};
	// Misc
	private static final int TELEPORT_DELAY = 1800000; // 30 minutes
	private static Npc _lastSpawn;
	
	private PriestOfMammon()
	{
		addFirstTalkId(PRIEST);
		onEvent("RESPAWN_PRIEST", null, null);
		startQuestTimer("RESPAWN_PRIEST", TELEPORT_DELAY, null, null, true);
	}
	
	@Override
	public String onEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		switch (event)
		{
			case "31113.html":
			case "31113-01.html":
			case "31113-02.html":
			{
				htmltext = event;
				break;
			}
			case "RESPAWN_PRIEST":
			{
				if (_lastSpawn != null)
				{
					_lastSpawn.deleteMe();
				}
				_lastSpawn = addSpawn(PRIEST, getRandomEntry(LOCATIONS), false, TELEPORT_DELAY);
				if (Config.ANNOUNCE_MAMMON_SPAWN)
				{
					Broadcast.toAllOnlinePlayers("Priest of Mammon has been spawned in Town of " + _lastSpawn.getCastle().getName() + ".", false);
				}
				break;
			}
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new PriestOfMammon();
	}
}
