package handlers.usercommandhandlers;

import org.classiclude.gameserver.handler.IVoicedCommandHandler;
import org.classiclude.gameserver.model.actor.Player;

public class GiveAdena implements IVoicedCommandHandler
{
	private static final String[] COMMANDS =
	{
		"adena"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player player, String params)
	{
		if (command.equalsIgnoreCase("adena"))
		{
			try
			{
				// Obtener la cantidad de adena del comando
				String[] parts = params.split(" ");
				long amount = Long.parseLong(parts[0]);
				
				// Limitar cantidad máxima por razones de seguridad
				if ((amount <= 0) || (amount > 999999999))
				{
					player.sendMessage("Por favor, ingresa una cantidad válida de adena.");
					return false;
				}
				
				// Dar adena al jugador
				player.addAdena("CustomCommand", amount, player, true);
				player.sendMessage("Has recibido " + amount + " adena.");
			}
			catch (Exception e)
			{
				player.sendMessage("Uso correcto: .adena [cantidad]");
				return false;
			}
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return COMMANDS;
	}
}
