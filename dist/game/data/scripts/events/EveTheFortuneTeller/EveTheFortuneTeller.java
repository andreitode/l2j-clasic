/*
<<<<<<< .mine
 * This file is part of the ClassicLude project. This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. This program
 * is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
||||||| .r15319
 * This file is part of the ClassicLude project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
=======
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
>>>>>>> .r15381
 */
/**
 * package events.EveTheFortuneTeller; import org.classiclude.gameserver.enums.LuckyGameType; import org.classiclude.gameserver.model.actor.Npc; import org.classiclude.gameserver.model.actor.Player; import org.classiclude.gameserver.model.quest.LongTimeEvent; import
 * org.classiclude.gameserver.network.serverpackets.luckygame.ExStartLuckyGame; Eve the Fortune Teller Returns<br>
 * Info - http://www.lineage2.com/en/news/events/11182015-eve-the-fortune-teller-returns.php
 * @author Mobius public class EveTheFortuneTeller extends LongTimeEvent { // NPCs private static final int EVE = 31855; // Items private static final int FORTUNE_READING_TICKET = 23767; private static final int LUXURY_FORTUNE_READING_TICKET = 23768; private EveTheFortuneTeller() { addStartNpc(EVE);
 *         addFirstTalkId(EVE); addTalkId(EVE); addSpawnId(EVE); }
 * @Override public String onEvent(String event, Npc npc, Player player) { String htmltext = null; switch (event) { case "31855.htm": case "31855-1.htm": { htmltext = event; break; } case "FortuneReadingGame": { player.sendPacket(new ExStartLuckyGame(LuckyGameType.NORMAL,
 *           player.getInventory().getInventoryItemCount(FORTUNE_READING_TICKET, -1))); break; } case "LuxuryFortuneReadingGame": { player.sendPacket(new ExStartLuckyGame(LuckyGameType.LUXURY, player.getInventory().getInventoryItemCount(LUXURY_FORTUNE_READING_TICKET, -1))); break; } } return
 *           htmltext; }
 * @Override public String onFirstTalk(Npc npc, Player player) { return "31855.htm"; } public static void main(String[] args) { new EveTheFortuneTeller(); } }
 */
package events.EveTheFortuneTeller;

public class EveTheFortuneTeller
{
	
}