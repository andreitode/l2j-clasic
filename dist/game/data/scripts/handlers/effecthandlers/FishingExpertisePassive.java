
package handlers.effecthandlers;

import org.classiclude.gameserver.model.StatSet;
import org.classiclude.gameserver.model.stats.Stat;

/**
 * @author Sdw
 */
public class FishingExpertisePassive extends AbstractStatEffect
{
	public FishingExpertisePassive(StatSet params)
	{
		super(params, Stat.FISHING_EXPERTISE);
	}
}