package org.classiclude.gameserver.util;

import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.classiclude.gameserver.model.StatSet;

/**
 * @author UnAfraid
 */
public class BypassParser extends StatSet
{
	private static final String ALLOWED_CHARS = "a-zA-Z0-9-_`!@#%^&*()\\[\\]|\\\\/";
	private static final Pattern PATTERN = Pattern.compile(String.format("([%s]*)=('([%s ]*)'|[%s]*)", ALLOWED_CHARS, ALLOWED_CHARS, ALLOWED_CHARS));
	
	public BypassParser(String bypass)
	{
		super(LinkedHashMap::new);
		process(bypass);
	}
	
	private void process(String bypass)
	{
		final Matcher regexMatcher = PATTERN.matcher(bypass);
		while (regexMatcher.find())
		{
			final String name = regexMatcher.group(1);
			final String escapedValue = regexMatcher.group(2).trim();
			final String unescapedValue = regexMatcher.group(3);
			set(name, unescapedValue != null ? unescapedValue.trim() : escapedValue);
		}
	}
}
