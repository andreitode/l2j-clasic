package org.classiclude.gameserver;

import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.classiclude.Config;
import org.classiclude.commons.database.DatabaseFactory;
import org.classiclude.commons.enums.ServerMode;
import org.classiclude.commons.network.ConnectionBuilder;
import org.classiclude.commons.network.ConnectionHandler;
import org.classiclude.commons.threads.ThreadPool;
import org.classiclude.commons.util.DeadLockDetector;
import org.classiclude.commons.util.PropertiesParser;
import org.classiclude.gameserver.cache.HtmCache;
import org.classiclude.gameserver.data.BotReportTable;
import org.classiclude.gameserver.data.SchemeBufferTable;
import org.classiclude.gameserver.data.sql.AnnouncementsTable;
import org.classiclude.gameserver.data.sql.CharInfoTable;
import org.classiclude.gameserver.data.sql.CharSummonTable;
import org.classiclude.gameserver.data.sql.ClanTable;
import org.classiclude.gameserver.data.sql.CrestTable;
import org.classiclude.gameserver.data.sql.IndividualVoteTable;
import org.classiclude.gameserver.data.sql.OfflineTraderTable;
import org.classiclude.gameserver.data.sql.PartyMatchingHistoryTable;
import org.classiclude.gameserver.data.xml.ActionData;
import org.classiclude.gameserver.data.xml.AdminData;
import org.classiclude.gameserver.data.xml.AppearanceItemData;
import org.classiclude.gameserver.data.xml.ArmorSetData;
import org.classiclude.gameserver.data.xml.AttendanceRewardData;
import org.classiclude.gameserver.data.xml.BeautyShopData;
import org.classiclude.gameserver.data.xml.BuyListData;
import org.classiclude.gameserver.data.xml.CategoryData;
import org.classiclude.gameserver.data.xml.ClanHallData;
import org.classiclude.gameserver.data.xml.ClassListData;
import org.classiclude.gameserver.data.xml.CombinationItemsData;
import org.classiclude.gameserver.data.xml.CubicData;
import org.classiclude.gameserver.data.xml.DailyMissionData;
import org.classiclude.gameserver.data.xml.DoorData;
import org.classiclude.gameserver.data.xml.DynamicExpRateData;
import org.classiclude.gameserver.data.xml.ElementalAttributeData;
import org.classiclude.gameserver.data.xml.EnchantItemData;
import org.classiclude.gameserver.data.xml.EnchantItemGroupsData;
import org.classiclude.gameserver.data.xml.EnchantItemHPBonusData;
import org.classiclude.gameserver.data.xml.EnchantItemOptionsData;
import org.classiclude.gameserver.data.xml.EnchantSkillGroupsData;
import org.classiclude.gameserver.data.xml.ExperienceData;
import org.classiclude.gameserver.data.xml.FakePlayerData;
import org.classiclude.gameserver.data.xml.FenceData;
import org.classiclude.gameserver.data.xml.FishData;
import org.classiclude.gameserver.data.xml.FishingMonstersData;
import org.classiclude.gameserver.data.xml.FishingRodsData;
import org.classiclude.gameserver.data.xml.HennaData;
import org.classiclude.gameserver.data.xml.HitConditionBonusData;
import org.classiclude.gameserver.data.xml.InitialEquipmentData;
import org.classiclude.gameserver.data.xml.InitialShortcutData;
import org.classiclude.gameserver.data.xml.ItemCrystallizationData;
import org.classiclude.gameserver.data.xml.ItemData;
import org.classiclude.gameserver.data.xml.KarmaData;
import org.classiclude.gameserver.data.xml.LevelUpCrystalData;
import org.classiclude.gameserver.data.xml.MultisellData;
import org.classiclude.gameserver.data.xml.NpcData;
import org.classiclude.gameserver.data.xml.NpcNameLocalisationData;
import org.classiclude.gameserver.data.xml.OptionData;
import org.classiclude.gameserver.data.xml.PetDataTable;
import org.classiclude.gameserver.data.xml.PetSkillData;
import org.classiclude.gameserver.data.xml.PlayerTemplateData;
import org.classiclude.gameserver.data.xml.PlayerXpPercentLostData;
import org.classiclude.gameserver.data.xml.PrimeShopData;
import org.classiclude.gameserver.data.xml.RecipeData;
import org.classiclude.gameserver.data.xml.ResidenceFunctionsData;
import org.classiclude.gameserver.data.xml.SayuneData;
import org.classiclude.gameserver.data.xml.SecondaryAuthData;
import org.classiclude.gameserver.data.xml.SendMessageLocalisationData;
import org.classiclude.gameserver.data.xml.ShuttleData;
import org.classiclude.gameserver.data.xml.SiegeScheduleData;
import org.classiclude.gameserver.data.xml.SkillData;
import org.classiclude.gameserver.data.xml.SkillRoutes;
import org.classiclude.gameserver.data.xml.SkillTreeData;
import org.classiclude.gameserver.data.xml.SpawnData;
import org.classiclude.gameserver.data.xml.StaticObjectData;
import org.classiclude.gameserver.data.xml.TeleporterData;
import org.classiclude.gameserver.data.xml.TransformData;
import org.classiclude.gameserver.data.xml.VariationData;
import org.classiclude.gameserver.data.xml.VipData;
import org.classiclude.gameserver.data.xml.VoteSiteData;
import org.classiclude.gameserver.geoengine.GeoEngine;
import org.classiclude.gameserver.handler.ConditionHandler;
import org.classiclude.gameserver.handler.DailyMissionHandler;
import org.classiclude.gameserver.handler.EffectHandler;
import org.classiclude.gameserver.handler.SkillConditionHandler;
import org.classiclude.gameserver.instancemanager.AirShipManager;
import org.classiclude.gameserver.instancemanager.AntiFeedManager;
import org.classiclude.gameserver.instancemanager.BoatManager;
import org.classiclude.gameserver.instancemanager.CaptchaManager;
import org.classiclude.gameserver.instancemanager.CastleManager;
import org.classiclude.gameserver.instancemanager.CastleManorManager;
import org.classiclude.gameserver.instancemanager.ClanHallAuctionManager;
import org.classiclude.gameserver.instancemanager.CursedWeaponsManager;
import org.classiclude.gameserver.instancemanager.CustomMailManager;
import org.classiclude.gameserver.instancemanager.DBSpawnManager;
import org.classiclude.gameserver.instancemanager.DailyResetManager;
import org.classiclude.gameserver.instancemanager.DimensionalRiftManager;
import org.classiclude.gameserver.instancemanager.FakePlayerChatManager;
import org.classiclude.gameserver.instancemanager.FishingChampionshipManager;
import org.classiclude.gameserver.instancemanager.GlobalVariablesManager;
import org.classiclude.gameserver.instancemanager.GrandBossManager;
import org.classiclude.gameserver.instancemanager.IdManager;
import org.classiclude.gameserver.instancemanager.InstanceManager;
import org.classiclude.gameserver.instancemanager.ItemAuctionManager;
import org.classiclude.gameserver.instancemanager.ItemCommissionManager;
import org.classiclude.gameserver.instancemanager.ItemsOnGroundManager;
import org.classiclude.gameserver.instancemanager.MailManager;
import org.classiclude.gameserver.instancemanager.MapRegionManager;
import org.classiclude.gameserver.instancemanager.MatchingRoomManager;
import org.classiclude.gameserver.instancemanager.MentorManager;
import org.classiclude.gameserver.instancemanager.PcCafePointsManager;
import org.classiclude.gameserver.instancemanager.PetitionManager;
import org.classiclude.gameserver.instancemanager.PrecautionaryRestartManager;
import org.classiclude.gameserver.instancemanager.PremiumManager;
import org.classiclude.gameserver.instancemanager.PunishmentManager;
import org.classiclude.gameserver.instancemanager.QuestManager;
import org.classiclude.gameserver.instancemanager.SellBuffsManager;
import org.classiclude.gameserver.instancemanager.ServerRestartManager;
import org.classiclude.gameserver.instancemanager.SiegeGuardManager;
import org.classiclude.gameserver.instancemanager.SiegeManager;
import org.classiclude.gameserver.instancemanager.WalkingManager;
import org.classiclude.gameserver.instancemanager.ZoneManager;
import org.classiclude.gameserver.instancemanager.events.EventDropManager;
import org.classiclude.gameserver.instancemanager.games.LotteryManager;
import org.classiclude.gameserver.instancemanager.games.MonsterRaceManager;
import org.classiclude.gameserver.model.AutoSpawnHandler;
import org.classiclude.gameserver.model.World;
import org.classiclude.gameserver.model.events.EventDispatcher;
import org.classiclude.gameserver.model.events.EventType;
import org.classiclude.gameserver.model.events.impl.OnServerStart;
import org.classiclude.gameserver.model.olympiad.Hero;
import org.classiclude.gameserver.model.olympiad.Olympiad;
import org.classiclude.gameserver.model.sevensigns.SevenSigns;
import org.classiclude.gameserver.model.sevensigns.SevenSignsFestival;
import org.classiclude.gameserver.model.vip.VipManager;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.GamePacketHandler;
import org.classiclude.gameserver.network.NpcStringId;
import org.classiclude.gameserver.network.SystemMessageId;
import org.classiclude.gameserver.scripting.ScriptEngineManager;
import org.classiclude.gameserver.taskmanager.GameTimeTaskManager;
import org.classiclude.gameserver.taskmanager.ItemLifeTimeTaskManager;
import org.classiclude.gameserver.taskmanager.ItemsAutoDestroyTaskManager;
import org.classiclude.gameserver.taskmanager.TaskManager;
import org.classiclude.gameserver.taskmanager.VipExpirationTaskManager;
import org.classiclude.gameserver.ui.Gui;
import org.classiclude.gameserver.util.Broadcast;

public class GameServer
{
	private static final Logger LOGGER = Logger.getLogger(GameServer.class.getName());
	
	private final DeadLockDetector _deadDetectThread;
	private static GameServer INSTANCE;
	public static final Calendar dateTimeServerStarted = Calendar.getInstance();
	
	public long getUsedMemoryMB()
	{
		return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576;
	}
	
	public DeadLockDetector getDeadLockDetectorThread()
	{
		return _deadDetectThread;
	}
	
	public GameServer() throws Exception
	{
		final long serverLoadStart = System.currentTimeMillis();
		
		// GUI
		final PropertiesParser interfaceConfig = new PropertiesParser(Config.INTERFACE_CONFIG_FILE);
		Config.ENABLE_GUI = interfaceConfig.getBoolean("EnableGUI", true);
		if (Config.ENABLE_GUI && !GraphicsEnvironment.isHeadless())
		{
			Config.DARK_THEME = interfaceConfig.getBoolean("DarkTheme", true);
			System.out.println("GameServer: Running in GUI mode.");
			new Gui();
		}
		
		// Create log folder
		final File logFolder = new File(".", "log");
		logFolder.mkdir();
		
		// Create input stream for log file -- or store file data into memory
		try (InputStream is = new FileInputStream(new File("./log.cfg")))
		{
			LogManager.getLogManager().readConfiguration(is);
		}
		
		// Initialize config
		Config.load(ServerMode.GAME);
		
		printSection("Database");
		DatabaseFactory.init();
		
		printSection("ThreadPool");
		ThreadPool.init();
		
		// Start game time task manager early
		GameTimeTaskManager.getInstance();
		
		printSection("IdManager");
		IdManager.getInstance();
		
		printSection("Scripting Engine");
		EventDispatcher.getInstance();
		ScriptEngineManager.getInstance();
		
		printSection("Skills");
		SkillConditionHandler.getInstance().executeScript();
		EffectHandler.getInstance().executeScript();
		EnchantSkillGroupsData.getInstance();
		SkillTreeData.getInstance();
		SkillData.getInstance();
		PetSkillData.getInstance();
		
		printSection("World");
		World.getInstance();
		MapRegionManager.getInstance();
		ZoneManager.getInstance();
		GrandBossManager.getInstance().initZones();
		DoorData.getInstance();
		FenceData.getInstance();
		AnnouncementsTable.getInstance();
		GlobalVariablesManager.getInstance();
		
		printSection("Data");
		ActionData.getInstance();
		CategoryData.getInstance();
		DynamicExpRateData.getInstance();
		SecondaryAuthData.getInstance();
		CombinationItemsData.getInstance();
		SayuneData.getInstance();
		// ClanRewardData.getInstance();
		DailyMissionHandler.getInstance().executeScript();
		DailyMissionData.getInstance();
		SkillRoutes.getInstance();
		
		printSection("Vote System");
		if (Config.CUSTOM_VOTE_ENABLE)
		{
			VoteSiteData.getInstance();
			IndividualVoteTable.getInstance();
		}
		
		printSection("Items");
		ConditionHandler.getInstance().executeScript();
		ItemData.getInstance();
		EnchantItemGroupsData.getInstance();
		EnchantItemData.getInstance();
		EnchantItemOptionsData.getInstance();
		ElementalAttributeData.getInstance();
		ItemCrystallizationData.getInstance();
		OptionData.getInstance();
		VariationData.getInstance();
		EnchantItemHPBonusData.getInstance();
		BuyListData.getInstance();
		MultisellData.getInstance();
		RecipeData.getInstance();
		ArmorSetData.getInstance();
		FishData.getInstance();
		FishingMonstersData.getInstance();
		FishingRodsData.getInstance();
		HennaData.getInstance();
		PrimeShopData.getInstance();
		PcCafePointsManager.getInstance();
		AppearanceItemData.getInstance();
		ItemCommissionManager.getInstance();
		// LuckyGameData.getInstance();
		AttendanceRewardData.getInstance();
		VipData.getInstance();
		ItemLifeTimeTaskManager.getInstance();
		
		printSection("Characters");
		ClassListData.getInstance();
		InitialEquipmentData.getInstance();
		InitialShortcutData.getInstance();
		ExperienceData.getInstance();
		PlayerXpPercentLostData.getInstance();
		KarmaData.getInstance();
		HitConditionBonusData.getInstance();
		PlayerTemplateData.getInstance();
		CharInfoTable.getInstance();
		PartyMatchingHistoryTable.getInstance();
		AdminData.getInstance();
		PetDataTable.getInstance();
		CubicData.getInstance();
		CharSummonTable.getInstance().init();
		CaptchaManager.getInstance();
		BeautyShopData.getInstance();
		MentorManager.getInstance();
		VipManager.getInstance();
		
		if (Config.PREMIUM_SYSTEM_ENABLED)
		{
			LOGGER.info("PremiumManager: Premium system is enabled.");
			PremiumManager.getInstance();
		}
		
		if (Config.ALT_FISH_CHAMPIONSHIP_ENABLED)
		{
			FishingChampionshipManager.getInstance();
		}
		
		printSection("Clans");
		ClanTable.getInstance();
		ResidenceFunctionsData.getInstance();
		ClanHallData.getInstance();
		ClanHallAuctionManager.getInstance();
		// ClanEntryManager.getInstance();
		
		printSection("Geodata");
		GeoEngine.getInstance();
		
		printSection("NPCs");
		NpcData.getInstance();
		LevelUpCrystalData.getInstance();
		FakePlayerData.getInstance();
		FakePlayerChatManager.getInstance();
		SpawnData.getInstance();
		WalkingManager.getInstance();
		StaticObjectData.getInstance();
		ItemAuctionManager.getInstance();
		CastleManager.getInstance().loadInstances();
		SchemeBufferTable.getInstance();
		
		EventDropManager.getInstance();
		
		printSection("Instance");
		InstanceManager.getInstance();
		VipExpirationTaskManager.start();
		
		printSection("Olympiad");
		Olympiad.getInstance();
		Hero.getInstance();
		
		// Call to load caches
		printSection("Cache");
		HtmCache.getInstance();
		CrestTable.getInstance();
		TeleporterData.getInstance();
		MatchingRoomManager.getInstance();
		PetitionManager.getInstance();
		CursedWeaponsManager.getInstance();
		TransformData.getInstance();
		BotReportTable.getInstance();
		if (Config.SELLBUFF_ENABLED)
		{
			SellBuffsManager.getInstance();
		}
		if (Config.MULTILANG_ENABLE)
		{
			SystemMessageId.loadLocalisations();
			NpcStringId.loadLocalisations();
			SendMessageLocalisationData.getInstance();
			NpcNameLocalisationData.getInstance();
		}
		
		printSection("Scripts");
		QuestManager.getInstance();
		BoatManager.getInstance();
		AirShipManager.getInstance();
		ShuttleData.getInstance();
		
		try
		{
			LOGGER.info(getClass().getSimpleName() + ": Loading server scripts:");
			ScriptEngineManager.getInstance().executeScript(ScriptEngineManager.MASTER_HANDLER_FILE);
			ScriptEngineManager.getInstance().executeScriptList();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Failed to execute script list!", e);
		}
		
		SpawnData.getInstance().init();
		DimensionalRiftManager.getInstance();
		DBSpawnManager.getInstance();
		
		printSection("Siege");
		SiegeManager.getInstance().getSieges();
		CastleManager.getInstance().activateInstances();
		// No fortresses
		// FortManager.getInstance().loadInstances();
		// FortManager.getInstance().activateInstances();
		// FortSiegeManager.getInstance();
		SiegeScheduleData.getInstance();
		CastleManorManager.getInstance();
		SiegeGuardManager.getInstance();
		QuestManager.getInstance().report();
		if (Config.SAVE_DROPPED_ITEM)
		{
			ItemsOnGroundManager.getInstance();
		}
		if ((Config.AUTODESTROY_ITEM_AFTER > 0) || (Config.HERB_AUTO_DESTROY_TIME > 0))
		{
			ItemsAutoDestroyTaskManager.getInstance();
		}
		MonsterRaceManager.getInstance();
		LotteryManager.getInstance();
		TaskManager.getInstance();
		DailyResetManager.getInstance();
		AntiFeedManager.getInstance().registerEvent(AntiFeedManager.GAME_ID);
		SevenSigns.getInstance().spawnSevenSignsNPC();
		SevenSignsFestival.getInstance();
		AutoSpawnHandler.getInstance();
		LOGGER.info("AutoSpawnHandler: Loaded " + AutoSpawnHandler.getInstance().size() + " handlers in total.");
		if (Config.ENABLE_OFFLINE_PLAY_COMMAND)
		{
			AntiFeedManager.getInstance().registerEvent(AntiFeedManager.OFFLINE_PLAY);
		}
		if (Config.ALLOW_MAIL)
		{
			MailManager.getInstance();
		}
		if (Config.CUSTOM_MAIL_MANAGER_ENABLED)
		{
			CustomMailManager.getInstance();
		}
		if (EventDispatcher.getInstance().hasListener(EventType.ON_SERVER_START))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnServerStart());
		}
		PunishmentManager.getInstance();
		
		Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());
		LOGGER.info("IdManager: Free ObjectID's remaining: " + IdManager.getInstance().size());
		
		if ((Config.OFFLINE_TRADE_ENABLE || Config.OFFLINE_CRAFT_ENABLE) && Config.RESTORE_OFFLINERS)
		{
			OfflineTraderTable.getInstance().restoreOfflineTraders();
		}
		if (Config.SERVER_RESTART_SCHEDULE_ENABLED)
		{
			ServerRestartManager.getInstance();
		}
		if (Config.PRECAUTIONARY_RESTART_ENABLED)
		{
			PrecautionaryRestartManager.getInstance();
		}
		if (Config.DEADLOCK_DETECTOR)
		{
			_deadDetectThread = new DeadLockDetector(Duration.ofSeconds(Config.DEADLOCK_CHECK_INTERVAL), () ->
			{
				if (Config.RESTART_ON_DEADLOCK)
				{
					Broadcast.toAllOnlinePlayers("Server has stability issues - restarting now.");
					Shutdown.getInstance().startShutdown(null, 60, true);
				}
			});
			_deadDetectThread.setDaemon(true);
			_deadDetectThread.start();
		}
		else
		{
			_deadDetectThread = null;
		}
		
		System.gc();
		final long totalMem = Runtime.getRuntime().maxMemory() / 1048576;
		LOGGER.info(getClass().getSimpleName() + ": Started, using " + getUsedMemoryMB() + " of " + totalMem + " MB total memory.");
		LOGGER.info(getClass().getSimpleName() + ": Maximum number of connected players is " + Config.MAXIMUM_ONLINE_USERS + ".");
		LOGGER.info(getClass().getSimpleName() + ": Server loaded in " + ((System.currentTimeMillis() - serverLoadStart) / 1000) + " seconds.");
		
		final ConnectionHandler<GameClient> connectionHandler = new ConnectionBuilder<>(new InetSocketAddress(Config.PORT_GAME), GameClient::new, new GamePacketHandler(), ThreadPool::execute).build();
		connectionHandler.start();
		
		LoginServerThread.getInstance().start();
		
		Toolkit.getDefaultToolkit().beep();
	}
	
	public long getStartedTime()
	{
		return ManagementFactory.getRuntimeMXBean().getStartTime();
	}
	
	public String getUptime()
	{
		final long uptime = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
		final long hours = uptime / 3600;
		final long mins = (uptime - (hours * 3600)) / 60;
		final long secs = ((uptime - (hours * 3600)) - (mins * 60));
		if (hours > 0)
		{
			return hours + "hrs " + mins + "mins " + secs + "secs";
		}
		return mins + "mins " + secs + "secs";
	}
	
	public static void main(String[] args) throws Exception
	{
		INSTANCE = new GameServer();
	}
	
	private void printSection(String section)
	{
		String s = "=[ " + section + " ]";
		while (s.length() < 61)
		{
			s = "-" + s;
		}
		LOGGER.info(s);
	}
	
	public static GameServer getInstance()
	{
		return INSTANCE;
	}
}
