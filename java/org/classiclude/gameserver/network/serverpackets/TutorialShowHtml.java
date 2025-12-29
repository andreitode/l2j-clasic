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
package org.classiclude.gameserver.network.serverpackets;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.enums.HtmlActionScope;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;

/**
 * TutorialShowHtml server packet implementation.
 * @author Mobius
 */
public class TutorialShowHtml extends AbstractHtmlPacket
{
	// TODO: Enum
	public static final int NORMAL_WINDOW = 1;
	public static final int LARGE_WINDOW = 2;
	
	private final int _type;
	
	public TutorialShowHtml(String html)
	{
		super(html);
		_type = NORMAL_WINDOW;
	}
	
	public TutorialShowHtml(int npcObjId, String html, int type)
	{
		super(npcObjId, html);
		_type = type;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.TUTORIAL_SHOW_HTML.writeId(this, buffer);
		buffer.writeInt(_type);
		buffer.writeString(getHtml());
	}
	
	@Override
	public HtmlActionScope getScope()
	{
		return HtmlActionScope.TUTORIAL_HTML;
	}
}
