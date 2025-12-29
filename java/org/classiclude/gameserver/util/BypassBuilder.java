package org.classiclude.gameserver.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author UnAfraid
 */
public class BypassBuilder
{
	private final String _bypass;
	private final List<BypassParam> _params = new ArrayList<>();
	
	public BypassBuilder(String bypass)
	{
		_bypass = bypass;
	}
	
	public void addParam(BypassParam param)
	{
		Objects.requireNonNull(param, "param cannot be null!");
		_params.add(param);
	}
	
	public void addParam(String name, String separator, Object value)
	{
		Objects.requireNonNull(name, "name cannot be null!");
		addParam(new BypassParam(name, Optional.ofNullable(separator), Optional.ofNullable(value)));
	}
	
	public void addParam(String name, Object value)
	{
		addParam(name, "=", value);
	}
	
	public void addParam(String name)
	{
		addParam(name, null, null);
	}
	
	public StringBuilder toStringBuilder()
	{
		final StringBuilder sb = new StringBuilder(_bypass);
		for (BypassParam param : _params)
		{
			sb.append(" ").append(param.getName().trim());
			if (param.getSeparator().isPresent() && param.getValue().isPresent())
			{
				sb.append(param.getSeparator().get().trim());
				final Object value = param.getValue().get();
				if (value instanceof String)
				{
					sb.append('"');
				}
				sb.append(String.valueOf(value).trim());
				if (value instanceof String)
				{
					sb.append('"');
				}
			}
		}
		return sb;
	}
	
	@Override
	public String toString()
	{
		return toStringBuilder().toString();
	}
	
	private static class BypassParam
	{
		private final String _name;
		private final Optional<String> _separator;
		private final Optional<Object> _value;
		
		public BypassParam(String name, Optional<String> separator, Optional<Object> value)
		{
			_name = name;
			_separator = separator;
			_value = value;
		}
		
		public String getName()
		{
			return _name;
		}
		
		public Optional<String> getSeparator()
		{
			return _separator;
		}
		
		public Optional<Object> getValue()
		{
			return _value;
		}
	}
}
