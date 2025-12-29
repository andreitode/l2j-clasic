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
package org.classiclude.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.List;

import org.classiclude.gameserver.enums.MacroType;
import org.classiclude.gameserver.model.Macro;
import org.classiclude.gameserver.model.MacroCmd;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.network.SystemMessageId;

/**
 * @author Mobius
 */
public class RequestMakeMacro extends ClientPacket
{
	private Macro _macro;
	private int _commandsLength = 0;
	
	private static final int MAX_MACRO_LENGTH = 12;
	
	@Override
	protected void readImpl()
	{
		final int id = readInt();
		final String name = readString();
		final String desc = readString();
		final String acronym = readString();
		final int icon = readInt();
		int count = readByte();
		if (count > MAX_MACRO_LENGTH)
		{
			count = MAX_MACRO_LENGTH;
		}
		
		final List<MacroCmd> commands = new ArrayList<>(count);
		for (int i = 0; i < count; i++)
		{
			final int entry = readByte();
			final int type = readByte(); // 1 = skill, 3 = action, 4 = shortcut
			final int d1 = readInt(); // skill or page number for shortcuts
			final int d2 = readByte();
			final String command = readString();
			_commandsLength += command.length();
			commands.add(new MacroCmd(entry, MacroType.values()[(type < 1) || (type > 6) ? 0 : type], d1, d2, command));
		}
		_macro = new Macro(id, icon, name, desc, acronym, commands);
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getPlayer();
		if (player == null)
		{
			return;
		}
		if (_commandsLength > 255)
		{
			// Invalid macro. Refer to the Help file for instructions.
			player.sendPacket(SystemMessageId.INVALID_MACRO_REFER_TO_THE_HELP_FILE_FOR_INSTRUCTIONS);
			return;
		}
		if (player.getMacros().getAllMacroses().size() > 48)
		{
			// You may create up to 48 macros.
			player.sendPacket(SystemMessageId.YOU_MAY_CREATE_UP_TO_48_MACROS);
			return;
		}
		if (_macro.getName().isEmpty())
		{
			// Enter the name of the macro.
			player.sendPacket(SystemMessageId.ENTER_THE_NAME_OF_THE_MACRO);
			return;
		}
		if (_macro.getDescr().length() > 32)
		{
			// Macro descriptions may contain up to 32 characters.
			player.sendPacket(SystemMessageId.MACRO_DESCRIPTIONS_MAY_CONTAIN_UP_TO_32_CHARACTERS);
			return;
		}
		player.registerMacro(_macro);
	}
}
