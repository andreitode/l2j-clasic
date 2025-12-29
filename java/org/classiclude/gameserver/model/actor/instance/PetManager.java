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
package org.classiclude.gameserver.model.actor.instance;

import org.classiclude.gameserver.enums.InstanceType;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.actor.templates.NpcTemplate;
import org.classiclude.gameserver.network.serverpackets.NpcHtmlMessage;
import org.classiclude.gameserver.util.Evolve;

public class PetManager extends Merchant
{
	public PetManager(NpcTemplate template)
	{
		super(template);
		setInstanceType(InstanceType.PetManager);
	}
	
	@Override
	public String getHtmlPath(int npcId, int value, Player player)
	{
		String pom = "";
		if (value == 0)
		{
			pom = Integer.toString(npcId);
		}
		else
		{
			pom = npcId + "-" + value;
		}
		return "data/html/petmanager/" + pom + ".htm";
	}
	
	@Override
	public void showChatWindow(Player player)
	{
		String filename = "data/html/petmanager/" + getId() + ".htm";
		if ((getId() == 36478) && player.hasSummon())
		{
			filename = "data/html/petmanager/restore-unsummonpet.htm";
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(player, filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (command.startsWith("exchange"))
		{
			final String[] params = command.split(" ");
			final int val = Integer.parseInt(params[1]);
			switch (val)
			{
				case 1:
				{
					exchange(player, 7585, 6650);
					break;
				}
				case 2:
				{
					exchange(player, 7583, 6648);
					break;
				}
				case 3:
				{
					exchange(player, 7584, 6649);
					break;
				}
			}
		}
		else if (command.startsWith("evolve"))
		{
			final String[] params = command.split(" ");
			final int val = Integer.parseInt(params[1]);
			boolean ok = false;
			switch (val)
			{
				// Info evolve(player, "curent pet summon item", "new pet summon item", "level required to evolve")
				// To ignore evolve just put value 0 where do you like example: evolve(player, 0, 9882, 55);
				case 1:
				{
					ok = Evolve.doEvolve(player, this, 2375, 9882, 55);
					break;
				}
				case 2:
				{
					ok = Evolve.doEvolve(player, this, 9882, 10426, 70);
					break;
				}
				case 3:
				{
					ok = Evolve.doEvolve(player, this, 6648, 10311, 55);
					break;
				}
				case 4:
				{
					ok = Evolve.doEvolve(player, this, 6650, 10313, 55);
					break;
				}
				case 5:
				{
					ok = Evolve.doEvolve(player, this, 6649, 10312, 55);
					break;
				}
			}
			if (!ok)
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile(player, "data/html/petmanager/evolve_no.htm");
				player.sendPacket(html);
			}
		}
		else if (command.startsWith("restore"))
		{
			final String[] params = command.split(" ");
			final int val = Integer.parseInt(params[1]);
			boolean ok = false;
			switch (val)
			{
				// Info evolve(player, "curent pet summon item", "new pet summon item", "level required to evolve")
				case 1:
				{
					ok = Evolve.doRestore(player, this, 10307, 9882, 55);
					break;
				}
				case 2:
				{
					ok = Evolve.doRestore(player, this, 10611, 10426, 70);
					break;
				}
				case 3:
				{
					ok = Evolve.doRestore(player, this, 10308, 4422, 55);
					break;
				}
				case 4:
				{
					ok = Evolve.doRestore(player, this, 10309, 4423, 55);
					break;
				}
				case 5:
				{
					ok = Evolve.doRestore(player, this, 10310, 4424, 55);
					break;
				}
			}
			if (!ok)
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile(player, "data/html/petmanager/restore_no.htm");
				player.sendPacket(html);
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	public void exchange(Player player, int itemIdtake, int itemIdgive)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		if (player.destroyItemByItemId("Consume", itemIdtake, 1, this, true))
		{
			player.addItem("", itemIdgive, 1, this, true);
			html.setFile(player, "data/html/petmanager/" + getId() + ".htm");
			player.sendPacket(html);
		}
		else
		{
			html.setFile(player, "data/html/petmanager/exchange_no.htm");
			player.sendPacket(html);
		}
	}
}
