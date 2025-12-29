package org.classiclude.log.formatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import org.classiclude.commons.util.StringUtil;

/**
 * @version $Revision: 1.1.4.1 $ $Date: 2005/03/27 15:30:08 $
 */
public class FileLogFormatter extends Formatter
{
	private static final String TAB = "\t";
	private final SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss,SSS");
	
	@Override
	public String format(LogRecord record)
	{
		// Java 1.8
		// return StringUtil.concat(dateFmt.format(new Date(record.getMillis())), TAB, record.getLevel().getName(), TAB, String.valueOf(record.getThreadID()), TAB, record.getLoggerName(), TAB, record.getMessage(), System.lineSeparator());
		// Java 16
		return StringUtil.concat(dateFmt.format(new Date(record.getMillis())), TAB, record.getLevel().getName(), TAB, String.valueOf(record.getLongThreadID()), TAB, record.getLoggerName(), TAB, record.getMessage(), System.lineSeparator());
	}
}
