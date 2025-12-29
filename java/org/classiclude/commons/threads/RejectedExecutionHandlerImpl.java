package org.classiclude.commons.threads;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Logger;

/**
 * @author NB4L1
 */
public class RejectedExecutionHandlerImpl implements RejectedExecutionHandler
{
	private static final Logger LOGGER = Logger.getLogger(RejectedExecutionHandlerImpl.class.getName());
	
	@Override
	public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor)
	{
		if (executor.isShutdown())
		{
			return;
		}
		
		LOGGER.warning(runnable.getClass().getSimpleName() + System.lineSeparator() + runnable + " from " + executor + " " + new RejectedExecutionException());
		
		if (Thread.currentThread().getPriority() > Thread.NORM_PRIORITY)
		{
			new Thread(runnable).start();
		}
		else
		{
			runnable.run();
		}
	}
}
