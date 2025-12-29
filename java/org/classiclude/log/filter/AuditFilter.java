package org.classiclude.log.filter;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

/**
 * @author zabbix
 */
public class AuditFilter implements Filter
{
	@Override
	public boolean isLoggable(LogRecord record)
	{
		return record.getLoggerName().equalsIgnoreCase("audit");
	}
}
