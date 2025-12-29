package org.classiclude.commons.network.internal.fairness;

import java.util.function.Consumer;

import org.classiclude.commons.network.Client;

/**
 * Manages fair distribution of actions across multiple clients.<br>
 * This controller uses different strategies based on the number of fairness buckets.
 * @author JoeAlisson
 */
public class FairnessController
{
	private FairnessStrategy _strategy;
	
	private FairnessController()
	{
		// Only constructable through the init method.
	}
	
	/**
	 * Initializes and creates a new FairnessController with the specified number of fairness buckets.
	 * @param fairnessBuckets The number of buckets used to distribute fairness.
	 * @return A new instance of FairnessController.
	 */
	public static FairnessController init(int fairnessBuckets)
	{
		final FairnessController controller = new FairnessController();
		if (fairnessBuckets <= 1)
		{
			controller._strategy = new SingleBucketStrategy();
		}
		else
		{
			controller._strategy = new MultiBucketStrategy(fairnessBuckets);
		}
		return controller;
	}
	
	/**
	 * Adds a client to the fairness controller and executes an action for the next fair client.
	 * @param client The client to be managed by the FairnessController.
	 * @param action The action to be executed for the next fair client.
	 */
	public void nextFairAction(Client<?> client, Consumer<Client<?>> action)
	{
		_strategy.doNextAction(client, action);
	}
}
