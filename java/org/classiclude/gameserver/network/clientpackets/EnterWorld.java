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
package org.classiclude.gameserver.network.clientpackets;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.classiclude.Config;
import org.classiclude.commons.threads.ThreadPool;
import org.classiclude.gameserver.LoginServerThread;
import org.classiclude.gameserver.cache.HtmCache;
import org.classiclude.gameserver.data.sql.AnnouncementsTable;
import org.classiclude.gameserver.data.sql.OfflineTraderTable;
import org.classiclude.gameserver.data.xml.AdminData;
import org.classiclude.gameserver.data.xml.BeautyShopData;
import org.classiclude.gameserver.data.xml.ClanHallData;
import org.classiclude.gameserver.data.xml.EnchantItemGroupsData;
import org.classiclude.gameserver.data.xml.SkillTreeData;
import org.classiclude.gameserver.enums.ChatType;
import org.classiclude.gameserver.enums.IllegalActionPunishmentType;
import org.classiclude.gameserver.enums.PlayerCondOverride;
import org.classiclude.gameserver.enums.SubclassInfoType;
import org.classiclude.gameserver.enums.TeleportWhereType;
import org.classiclude.gameserver.instancemanager.AntiFeedManager;
import org.classiclude.gameserver.instancemanager.CastleManager;
import org.classiclude.gameserver.instancemanager.CursedWeaponsManager;
import org.classiclude.gameserver.instancemanager.DimensionalRiftManager;
import org.classiclude.gameserver.instancemanager.FortManager;
import org.classiclude.gameserver.instancemanager.FortSiegeManager;
import org.classiclude.gameserver.instancemanager.InstanceManager;
import org.classiclude.gameserver.instancemanager.MailManager;
import org.classiclude.gameserver.instancemanager.PcCafePointsManager;
import org.classiclude.gameserver.instancemanager.PetitionManager;
import org.classiclude.gameserver.instancemanager.PunishmentManager;
import org.classiclude.gameserver.instancemanager.ServerRestartManager;
import org.classiclude.gameserver.instancemanager.SiegeManager;
import org.classiclude.gameserver.model.World;
import org.classiclude.gameserver.model.WorldObject;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.actor.appearance.PlayerAppearance;
import org.classiclude.gameserver.model.clan.Clan;
import org.classiclude.gameserver.model.holders.AttendanceInfoHolder;
import org.classiclude.gameserver.model.holders.ClientHardwareInfoHolder;
import org.classiclude.gameserver.model.instancezone.Instance;
import org.classiclude.gameserver.model.item.ItemTemplate;
import org.classiclude.gameserver.model.item.instance.Item;
import org.classiclude.gameserver.model.punishment.PunishmentAffect;
import org.classiclude.gameserver.model.punishment.PunishmentType;
import org.classiclude.gameserver.model.quest.Quest;
import org.classiclude.gameserver.model.residences.ClanHall;
import org.classiclude.gameserver.model.sevensigns.SevenSigns;
import org.classiclude.gameserver.model.siege.Castle;
import org.classiclude.gameserver.model.siege.Fort;
import org.classiclude.gameserver.model.siege.FortSiege;
import org.classiclude.gameserver.model.siege.Siege;
import org.classiclude.gameserver.model.skill.AbnormalVisualEffect;
import org.classiclude.gameserver.model.skill.CommonSkill;
import org.classiclude.gameserver.model.variables.AccountVariables;
import org.classiclude.gameserver.model.variables.PlayerVariables;
import org.classiclude.gameserver.model.zone.ZoneId;
import org.classiclude.gameserver.network.ConnectionState;
import org.classiclude.gameserver.network.Disconnection;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.PacketLogger;
import org.classiclude.gameserver.network.SystemMessageId;
import org.classiclude.gameserver.network.serverpackets.CreatureSay;
import org.classiclude.gameserver.network.serverpackets.Die;
import org.classiclude.gameserver.network.serverpackets.EtcStatusUpdate;
import org.classiclude.gameserver.network.serverpackets.ExAdenaInvenCount;
import org.classiclude.gameserver.network.serverpackets.ExAutoSoulShot;
import org.classiclude.gameserver.network.serverpackets.ExBasicActionList;
import org.classiclude.gameserver.network.serverpackets.ExBeautyItemList;
import org.classiclude.gameserver.network.serverpackets.ExBrPremiumState;
import org.classiclude.gameserver.network.serverpackets.ExGetBookMarkInfoPacket;
import org.classiclude.gameserver.network.serverpackets.ExNoticePostArrived;
import org.classiclude.gameserver.network.serverpackets.ExNotifyPremiumItem;
import org.classiclude.gameserver.network.serverpackets.ExPCCafePointInfo;
import org.classiclude.gameserver.network.serverpackets.ExPledgeCount;
import org.classiclude.gameserver.network.serverpackets.ExQuestItemList;
import org.classiclude.gameserver.network.serverpackets.ExRotation;
import org.classiclude.gameserver.network.serverpackets.ExShowScreenMessage;
import org.classiclude.gameserver.network.serverpackets.ExSubjobInfo;
import org.classiclude.gameserver.network.serverpackets.ExUnReadMailCount;
import org.classiclude.gameserver.network.serverpackets.ExUserInfoEquipSlot;
import org.classiclude.gameserver.network.serverpackets.ExUserInfoInvenWeight;
import org.classiclude.gameserver.network.serverpackets.ExVitalityEffectInfo;
import org.classiclude.gameserver.network.serverpackets.ExVoteSystemInfo;
import org.classiclude.gameserver.network.serverpackets.HennaInfo;
import org.classiclude.gameserver.network.serverpackets.ItemList;
import org.classiclude.gameserver.network.serverpackets.LeaveWorld;
import org.classiclude.gameserver.network.serverpackets.NpcHtmlMessage;
import org.classiclude.gameserver.network.serverpackets.PledgeShowMemberListAll;
import org.classiclude.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import org.classiclude.gameserver.network.serverpackets.PledgeSkillList;
import org.classiclude.gameserver.network.serverpackets.QuestList;
import org.classiclude.gameserver.network.serverpackets.ShortCutInit;
import org.classiclude.gameserver.network.serverpackets.SkillCoolTime;
import org.classiclude.gameserver.network.serverpackets.SkillList;
import org.classiclude.gameserver.network.serverpackets.SystemMessage;
import org.classiclude.gameserver.network.serverpackets.UserInfo;
import org.classiclude.gameserver.network.serverpackets.attendance.ExVipAttendanceItemList;
import org.classiclude.gameserver.network.serverpackets.dailymission.ExConnectedTimeAndGettableReward;
import org.classiclude.gameserver.network.serverpackets.dailymission.ExOneDayReceiveRewardList;
import org.classiclude.gameserver.network.serverpackets.friend.L2FriendList;
import org.classiclude.gameserver.util.BuilderUtil;
import org.classiclude.gameserver.util.Util;

/**
 * Enter World Packet Handler
 * <p>
 * <p>
 * 0000: 03
 * <p>
 * packet format rev87 bddddbdcccccccccccccccccccc
 * <p>
 */
public class EnterWorld extends ClientPacket
{
	private static final Map<String, ClientHardwareInfoHolder> TRACE_HWINFO = new ConcurrentHashMap<>();
	private static final String VAR_CUSTOM_TITLE_ASSIGNED = "customTitleAssigned";
	private final int[][] _tracert = new int[5][4];
	
	@Override
	protected void readImpl()
	{
		for (int i = 0; i < 5; i++)
		{
			for (int o = 0; o < 4; o++)
			{
				_tracert[i][o] = readUnsignedByte();
			}
		}
		readInt(); // Unknown Value
		readInt(); // Unknown Value
		readInt(); // Unknown Value
		readInt(); // Unknown Value
		readBytes(64); // Unknown Byte Array
		readInt(); // Unknown Value
	}
	
	@Override
	protected void runImpl()
	{
		final GameClient client = getClient();
		final Player player = client.getPlayer();
		if (player == null)
		{
			PacketLogger.warning("EnterWorld failed! player returned 'null'.");
			Disconnection.of(client).defaultSequence(LeaveWorld.STATIC_PACKET);
			return;
		}
		
		client.setConnectionState(ConnectionState.IN_GAME);
		
		final String[] adress = new String[5];
		for (int i = 0; i < 5; i++)
		{
			adress[i] = _tracert[i][0] + "." + _tracert[i][1] + "." + _tracert[i][2] + "." + _tracert[i][3];
		}
		
		LoginServerThread.getInstance().sendClientTracert(player.getAccountName(), adress);
		client.setClientTracert(_tracert);
		
		player.sendPacket(new UserInfo(player));
		
		// Restore to instanced area if enabled.
		final PlayerVariables vars = player.getVariables();
		if (Config.RESTORE_PLAYER_INSTANCE)
		{
			final Instance instance = InstanceManager.getInstance().getPlayerInstance(player, false);
			if ((instance != null) && (instance.getId() == vars.getInt(PlayerVariables.INSTANCE_RESTORE, 0)))
			{
				player.setInstance(instance);
			}
			vars.remove(PlayerVariables.INSTANCE_RESTORE);
		}
		
		if (!player.isGM())
		{
			player.updatePvpTitleAndColor(false);
		}
		// Apply special GM properties to the GM when entering
		else
		{
			gmStartupProcess:
			{
				if (Config.GM_STARTUP_BUILDER_HIDE && AdminData.getInstance().hasAccess("admin_hide", player.getAccessLevel()))
				{
					BuilderUtil.setHiding(player, true);
					BuilderUtil.sendSysMessage(player, "hide is default for builder.");
					BuilderUtil.sendSysMessage(player, "FriendAddOff is default for builder.");
					BuilderUtil.sendSysMessage(player, "whisperoff is default for builder.");
					
					// It isn't recommend to use the below custom L2J GMStartup functions together with retail-like GMStartupBuilderHide, so breaking the process at that stage.
					break gmStartupProcess;
				}
				
				if (Config.GM_STARTUP_INVULNERABLE && AdminData.getInstance().hasAccess("admin_invul", player.getAccessLevel()))
				{
					player.setInvul(true);
				}
				
				if (Config.GM_STARTUP_INVISIBLE && AdminData.getInstance().hasAccess("admin_invisible", player.getAccessLevel()))
				{
					player.setInvisible(true);
					player.getEffectList().startAbnormalVisualEffect(AbnormalVisualEffect.STEALTH);
				}
				
				if (Config.GM_STARTUP_SILENCE && AdminData.getInstance().hasAccess("admin_silence", player.getAccessLevel()))
				{
					player.setSilenceMode(true);
				}
				
				if (Config.GM_STARTUP_DIET_MODE && AdminData.getInstance().hasAccess("admin_diet", player.getAccessLevel()))
				{
					player.setDietMode(true);
					player.refreshOverloaded(true);
				}
			}
			
			if (Config.GM_STARTUP_AUTO_LIST && AdminData.getInstance().hasAccess("admin_gmliston", player.getAccessLevel()))
			{
				AdminData.getInstance().addGm(player, false);
			}
			else
			{
				AdminData.getInstance().addGm(player, true);
			}
			
			if (Config.GM_GIVE_SPECIAL_SKILLS)
			{
				SkillTreeData.getInstance().addSkills(player, false);
			}
			
			if (Config.GM_GIVE_SPECIAL_AURA_SKILLS)
			{
				SkillTreeData.getInstance().addSkills(player, true);
			}
		}
		
		// Set dead status if applies
		if (player.getCurrentHp() < 0.5)
		{
			player.setDead(true);
		}
		
		if (Config.CUSTOM_TITLE_ON)
		{
			boolean alreadyAssigned = player.getVariables().getBoolean(VAR_CUSTOM_TITLE_ASSIGNED, false);
			if (!alreadyAssigned)
			{
				player.setTitle(Config.CUSTOM_TITLE);
				player.getVariables().set(VAR_CUSTOM_TITLE_ASSIGNED, true);
				player.getVariables().storeMe();
			}
		}
		
		boolean showClanNotice = false;
		
		// Clan related checks are here
		final Clan clan = player.getClan();
		if (clan != null)
		{
			notifyClanMembers(player);
			notifySponsorOrApprentice(player);
			
			for (Siege siege : SiegeManager.getInstance().getSieges())
			{
				if (!siege.isInProgress())
				{
					continue;
				}
				
				if (siege.checkIsAttacker(clan))
				{
					player.setSiegeState((byte) 1);
					player.setSiegeSide(siege.getCastle().getResidenceId());
				}
				else if (siege.checkIsDefender(clan))
				{
					player.setSiegeState((byte) 2);
					player.setSiegeSide(siege.getCastle().getResidenceId());
				}
			}
			
			for (FortSiege siege : FortSiegeManager.getInstance().getSieges())
			{
				if (!siege.isInProgress())
				{
					continue;
				}
				
				if (siege.checkIsAttacker(clan))
				{
					player.setSiegeState((byte) 1);
					player.setSiegeSide(siege.getFort().getResidenceId());
				}
				else if (siege.checkIsDefender(clan))
				{
					player.setSiegeState((byte) 2);
					player.setSiegeSide(siege.getFort().getResidenceId());
				}
			}
			
			// Residential skills support
			if (clan.getCastleId() > 0)
			{
				final Castle castle = CastleManager.getInstance().getCastleByOwner(clan);
				if (castle != null)
				{
					castle.giveResidentialSkills(player);
				}
			}
			
			if (clan.getFortId() > 0)
			{
				final Fort fort = FortManager.getInstance().getFortByOwner(clan);
				if (fort != null)
				{
					fort.giveResidentialSkills(player);
				}
			}
			
			showClanNotice = clan.isNoticeEnabled();
		}
		
		// Updating Seal of Strife Buff/Debuff
		if (SevenSigns.getInstance().isSealValidationPeriod() && (SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE) != SevenSigns.CABAL_NULL))
		{
			final int cabal = SevenSigns.getInstance().getPlayerCabal(player.getObjectId());
			if (cabal != SevenSigns.CABAL_NULL)
			{
				if (cabal == SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE))
				{
					player.addSkill(CommonSkill.THE_VICTOR_OF_WAR.getSkill());
				}
				else
				{
					player.addSkill(CommonSkill.THE_VANQUISHED_OF_WAR.getSkill());
				}
			}
		}
		else
		{
			player.removeSkill(CommonSkill.THE_VICTOR_OF_WAR.getSkill());
			player.removeSkill(CommonSkill.THE_VANQUISHED_OF_WAR.getSkill());
		}
		
		if (Config.ENABLE_VITALITY)
		{
			player.sendPacket(new ExVitalityEffectInfo(player));
		}
		
		// Send Macro List
		player.getMacros().sendAllMacros();
		
		// Send Teleport Bookmark List
		player.sendPacket(new ExGetBookMarkInfoPacket(player));
		
		// Send Item List
		player.sendPacket(new ItemList(player, false));
		
		// Send Quest Item List
		player.sendPacket(new ExQuestItemList(player));
		
		// Send Shortcuts
		player.sendPacket(new ShortCutInit(player));
		
		// Send Action list
		player.sendPacket(ExBasicActionList.STATIC_PACKET);
		
		// Send blank skill list
		player.sendPacket(new SkillList());
		
		// Send GG check
		// player.queryGameGuard();
		
		// Send Dye Information
		player.sendPacket(new HennaInfo(player));
		
		// Send Skill list
		player.sendSkillList();
		
		// Send EtcStatusUpdate
		player.sendPacket(new EtcStatusUpdate(player));
		
		// Clan packets
		if (clan != null)
		{
			clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(player));
			PledgeShowMemberListAll.sendAllTo(player);
			clan.broadcastToOnlineMembers(new ExPledgeCount(clan));
			player.sendPacket(new PledgeSkillList(clan));
			final ClanHall ch = ClanHallData.getInstance().getClanHallByClan(clan);
			if ((ch != null) && (ch.getCostFailDay() > 0))
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW);
				sm.addInt(ch.getLease());
				player.sendPacket(sm);
			}
		}
		else
		{
			// player.sendPacket(ExPledgeWaitingListAlarm.STATIC_PACKET);
		}
		
		// Send SubClass Info
		player.sendPacket(new ExSubjobInfo(player, SubclassInfoType.NO_CHANGES));
		
		// Send Inventory Info
		player.sendPacket(new ExUserInfoInvenWeight(player));
		
		// Send Adena / Inventory Count Info
		player.sendPacket(new ExAdenaInvenCount(player));
		
		// Send Equipped Items
		player.sendPacket(new ExUserInfoEquipSlot(player));
		
		// Send VIP/Premium Info
		player.sendPacket(new ExBrPremiumState(player));
		
		// Send Unread Mail Count
		if (MailManager.getInstance().hasUnreadPost(player))
		{
			player.sendPacket(new ExUnReadMailCount(player));
		}
		
		// Faction System
		if (Config.FACTION_SYSTEM_ENABLED)
		{
			if (player.isGood())
			{
				final PlayerAppearance appearance = player.getAppearance();
				appearance.setNameColor(Config.FACTION_GOOD_NAME_COLOR);
				appearance.setTitleColor(Config.FACTION_GOOD_NAME_COLOR);
				player.sendMessage("Welcome " + player.getName() + ", you are fighting for the " + Config.FACTION_GOOD_TEAM_NAME + " faction.");
				player.sendPacket(new ExShowScreenMessage("Welcome " + player.getName() + ", you are fighting for the " + Config.FACTION_GOOD_TEAM_NAME + " faction.", 10000));
			}
			else if (player.isEvil())
			{
				final PlayerAppearance appearance = player.getAppearance();
				appearance.setNameColor(Config.FACTION_EVIL_NAME_COLOR);
				appearance.setTitleColor(Config.FACTION_EVIL_NAME_COLOR);
				player.sendMessage("Welcome " + player.getName() + ", you are fighting for the " + Config.FACTION_EVIL_TEAM_NAME + " faction.");
				player.sendPacket(new ExShowScreenMessage("Welcome " + player.getName() + ", you are fighting for the " + Config.FACTION_EVIL_TEAM_NAME + " faction.", 10000));
			}
		}
		
		Quest.playerEnter(player);
		
		// Send Quest List
		player.sendPacket(new QuestList(player));
		if (Config.PLAYER_SPAWN_PROTECTION > 0)
		{
			player.setSpawnProtection(true);
		}
		
		player.spawnMe(player.getX(), player.getY(), player.getZ());
		player.sendPacket(new ExRotation(player.getObjectId(), player.getHeading()));
		
		if (player.isCursedWeaponEquipped())
		{
			CursedWeaponsManager.getInstance().getCursedWeapon(player.getCursedWeaponEquippedId()).cursedOnLogin();
		}
		
		if (Config.PC_CAFE_ENABLED)
		{
			if (player.getPcCafePoints() > 0)
			{
				player.sendPacket(new ExPCCafePointInfo(player.getPcCafePoints(), 0, 1));
			}
			else
			{
				player.sendPacket(new ExPCCafePointInfo());
			}
		}
		
		// Expand Skill
		player.sendStorageMaxCount();
		
		// Friend list
		player.sendPacket(new L2FriendList(player));
		SystemMessage sm = new SystemMessage(SystemMessageId.YOUR_FRIEND_S1_JUST_LOGGED_IN);
		sm.addString(player.getName());
		for (int id : player.getFriendList())
		{
			final WorldObject obj = World.getInstance().findObject(id);
			if (obj != null)
			{
				obj.sendPacket(sm);
			}
		}
		
		player.sendPacket(SystemMessageId.WELCOME_TO_THE_WORLD_OF_LINEAGE_II);
		
		SevenSigns.getInstance().sendCurrentPeriodMsg(player);
		AnnouncementsTable.getInstance().showAnnouncements(player);
		
		if ((Config.SERVER_RESTART_SCHEDULE_ENABLED) && (Config.SERVER_RESTART_SCHEDULE_MESSAGE))
		{
			player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, "[SERVER]", "Next restart is scheduled at " + ServerRestartManager.getInstance().getNextRestartTime() + "."));
		}
		
		if (showClanNotice)
		{
			final NpcHtmlMessage notice = new NpcHtmlMessage();
			notice.setFile(player, "data/html/clanNotice.htm");
			notice.replace("%clan_name%", player.getClan().getName());
			notice.replace("%notice_text%", player.getClan().getNotice().replaceAll("(\r\n|\n)", "<br>"));
			notice.disableValidation();
			player.sendPacket(notice);
		}
		else if (Config.SERVER_NEWS)
		{
			final String serverNews = HtmCache.getInstance().getHtm(player, "data/html/servnews.htm");
			if (serverNews != null)
			{
				player.sendPacket(new NpcHtmlMessage(serverNews));
			}
		}
		
		if (Config.PETITIONING_ALLOWED)
		{
			PetitionManager.getInstance().checkPetitionMessages(player);
		}
		
		if (player.isAlikeDead()) // dead or fake dead
		{
			// no broadcast needed since the player will already spawn dead to others
			player.sendPacket(new Die(player));
		}
		
		player.onPlayerEnter();
		
		player.sendPacket(new SkillCoolTime(player));
		player.sendPacket(new ExVoteSystemInfo(player));
		for (Item item : player.getInventory().getItems())
		{
			if (item.isTimeLimitedItem())
			{
				item.scheduleLifeTimeTask();
			}
			if (item.isShadowItem() && item.isEquipped())
			{
				item.decreaseMana(false);
			}
		}
		
		for (Item whItem : player.getWarehouse().getItems())
		{
			if (whItem.isTimeLimitedItem())
			{
				whItem.scheduleLifeTimeTask();
			}
		}
		
		if (DimensionalRiftManager.getInstance().checkIfInRiftZone(player.getX(), player.getY(), player.getZ(), false))
		{
			DimensionalRiftManager.getInstance().teleportToWaitingRoom(player);
		}
		
		if (player.getClanJoinExpiryTime() > System.currentTimeMillis())
		{
			player.sendPacket(SystemMessageId.YOU_HAVE_RECENTLY_BEEN_DISMISSED_FROM_A_CLAN_YOU_ARE_NOT_ALLOWED_TO_JOIN_ANOTHER_CLAN_FOR_24_HOURS);
		}
		
		// remove combat flag before teleporting
		if (player.getInventory().getItemByItemId(9819) != null)
		{
			final Fort fort = FortManager.getInstance().getFort(player);
			if (fort != null)
			{
				FortSiegeManager.getInstance().dropCombatFlag(player, fort.getResidenceId());
			}
			else
			{
				final int slot = player.getInventory().getSlotFromItem(player.getInventory().getItemByItemId(9819));
				player.getInventory().unEquipItemInBodySlot(slot);
				player.destroyItem("CombatFlag", player.getInventory().getItemByItemId(9819), null, true);
			}
		}
		
		// Attacker or spectator logging in to a siege zone.
		// Actually should be checked for inside castle only?
		if (!player.canOverrideCond(PlayerCondOverride.ZONE_CONDITIONS) && player.isInsideZone(ZoneId.SIEGE) && (!player.isInSiege() || (player.getSiegeState() < 2)))
		{
			player.teleToLocation(TeleportWhereType.TOWN);
		}
		
		// Over-enchant protection.
		if (Config.OVER_ENCHANT_PROTECTION && !player.isGM())
		{
			boolean punish = false;
			for (Item item : player.getInventory().getItems())
			{
				if (item.isEquipable() //
					&& ((item.isWeapon() && (item.getEnchantLevel() > EnchantItemGroupsData.getInstance().getMaxWeaponEnchant())) //
						|| ((item.getTemplate().getType2() == ItemTemplate.TYPE2_ACCESSORY) && (item.getEnchantLevel() > EnchantItemGroupsData.getInstance().getMaxAccessoryEnchant())) //
						|| (item.isArmor() && (item.getTemplate().getType2() != ItemTemplate.TYPE2_ACCESSORY) && (item.getEnchantLevel() > EnchantItemGroupsData.getInstance().getMaxArmorEnchant()))))
				{
					PacketLogger.info("Over-enchanted (+" + item.getEnchantLevel() + ") " + item + " has been removed from " + player);
					player.getInventory().destroyItem("Over-enchant protection", item, player, null);
					punish = true;
				}
			}
			if (punish && (Config.OVER_ENCHANT_PUNISHMENT != IllegalActionPunishmentType.NONE))
			{
				player.sendMessage("[Server]: You have over-enchanted items!");
				player.sendMessage("[Server]: Respect our server rules.");
				player.sendPacket(new ExShowScreenMessage("You have over-enchanted items!", 6000));
				Util.handleIllegalPlayerAction(player, player.getName() + " has over-enchanted items.", Config.OVER_ENCHANT_PUNISHMENT);
			}
		}
		
		// Remove demonic weapon if character is not cursed weapon equipped.
		if ((player.getInventory().getItemByItemId(8190) != null) && !player.isCursedWeaponEquipped())
		{
			player.destroyItem("Zariche", player.getInventory().getItemByItemId(8190), null, true);
		}
		if ((player.getInventory().getItemByItemId(8689) != null) && !player.isCursedWeaponEquipped())
		{
			player.destroyItem("Akamanah", player.getInventory().getItemByItemId(8689), null, true);
		}
		
		if (Config.ALLOW_MAIL)
		{
			if (MailManager.getInstance().hasUnreadPost(player))
			{
				player.sendPacket(ExNoticePostArrived.valueOf(false));
			}
		}
		
		if (Config.WELCOME_MESSAGE_ENABLED)
		{
			player.sendPacket(new ExShowScreenMessage(Config.WELCOME_MESSAGE_TEXT, Config.WELCOME_MESSAGE_TIME));
		}
		
		if (!player.getPremiumItemList().isEmpty())
		{
			player.sendPacket(ExNotifyPremiumItem.STATIC_PACKET);
		}
		
		if ((Config.OFFLINE_TRADE_ENABLE || Config.OFFLINE_CRAFT_ENABLE) && Config.STORE_OFFLINE_TRADE_IN_REALTIME)
		{
			OfflineTraderTable.getInstance().onTransaction(player, true, false);
		}
		
		// Check if expoff is enabled.
		if (vars.getBoolean("EXPOFF", false))
		{
			player.disableExpGain();
			player.sendMessage("Experience gain is disabled.");
		}
		
		player.broadcastUserInfo();
		
		if (BeautyShopData.getInstance().hasBeautyData(player.getRace(), player.getAppearance().getSexType()))
		{
			player.sendPacket(new ExBeautyItemList(player));
		}
		
		// Disabled to give a more Classic feeling.
		// if (Config.ENABLE_WORLD_CHAT)
		// {
		// player.sendPacket(new ExWorldChatCnt(player));
		// }
		player.sendPacket(new ExConnectedTimeAndGettableReward(player));
		player.sendPacket(new ExOneDayReceiveRewardList(player, true));
		
		// Handle soulshots, disable all on EnterWorld
		player.sendPacket(new ExAutoSoulShot(0, true, 0));
		player.sendPacket(new ExAutoSoulShot(0, true, 1));
		player.sendPacket(new ExAutoSoulShot(0, true, 2));
		player.sendPacket(new ExAutoSoulShot(0, true, 3));
		
		// Fix for equipped item skills
		if (!player.getEffectList().getCurrentAbnormalVisualEffects().isEmpty())
		{
			player.updateAbnormalVisualEffects();
		}
		
		if (Config.ENABLE_ATTENDANCE_REWARDS)
		{
			ThreadPool.schedule(() ->
			{
				// Check if player can receive reward today.
				final AttendanceInfoHolder attendanceInfo = player.getAttendanceInfo();
				if (attendanceInfo.isRewardAvailable())
				{
					final int lastRewardIndex = attendanceInfo.getRewardIndex() + 1;
					player.sendPacket(new ExShowScreenMessage("Your attendance day " + lastRewardIndex + " reward is ready.", ExShowScreenMessage.TOP_CENTER, 7000, 0, true, true));
					player.sendMessage("Your attendance day " + lastRewardIndex + " reward is ready.");
					player.sendMessage("Click on General Menu -> Attendance Check.");
					if (Config.ATTENDANCE_POPUP_WINDOW)
					{
						player.sendPacket(new ExVipAttendanceItemList(player));
					}
				}
			}, Config.ATTENDANCE_REWARD_DELAY * 60 * 1000);
			
			if (Config.ATTENDANCE_POPUP_START)
			{
				player.sendPacket(new ExVipAttendanceItemList(player));
			}
		}
		
		// Delayed HWID checks.
		if (Config.HARDWARE_INFO_ENABLED)
		{
			ThreadPool.schedule(() ->
			{
				// Generate trace string.
				final StringBuilder sb = new StringBuilder();
				for (int[] i : _tracert)
				{
					for (int j : i)
					{
						sb.append(j);
						sb.append(".");
					}
				}
				final String trace = sb.toString();
				
				// Get hardware info from client.
				ClientHardwareInfoHolder hwInfo = client.getHardwareInfo();
				if (hwInfo != null)
				{
					hwInfo.store(player);
					TRACE_HWINFO.put(trace, hwInfo);
				}
				else
				{
					// Get hardware info from stored tracert map.
					hwInfo = TRACE_HWINFO.get(trace);
					if (hwInfo != null)
					{
						hwInfo.store(player);
						client.setHardwareInfo(hwInfo);
					}
					// Get hardware info from account variables.
					else
					{
						final String storedInfo = player.getAccountVariables().getString(AccountVariables.HWID, "");
						if (!storedInfo.isEmpty())
						{
							hwInfo = new ClientHardwareInfoHolder(storedInfo);
							TRACE_HWINFO.put(trace, hwInfo);
							client.setHardwareInfo(hwInfo);
						}
					}
				}
				
				// Banned?
				if ((hwInfo != null) && PunishmentManager.getInstance().hasPunishment(hwInfo.getMacAddress(), PunishmentAffect.HWID, PunishmentType.BAN))
				{
					Disconnection.of(client).defaultSequence(LeaveWorld.STATIC_PACKET);
					return;
				}
				
				// Check max players.
				if (Config.KICK_MISSING_HWID && (hwInfo == null))
				{
					Disconnection.of(client).defaultSequence(LeaveWorld.STATIC_PACKET);
				}
				else if (Config.MAX_PLAYERS_PER_HWID > 0)
				{
					int count = 0;
					for (Player plr : World.getInstance().getPlayers())
					{
						if (plr.isOnlineInt() == 1)
						{
							final ClientHardwareInfoHolder hwi = plr.getClient().getHardwareInfo();
							if ((hwi != null) && hwi.equals(hwInfo))
							{
								count++;
							}
						}
					}
					if (count > Config.MAX_PLAYERS_PER_HWID)
					{
						Disconnection.of(client).defaultSequence(LeaveWorld.STATIC_PACKET);
					}
				}
			}, 5000);
		}
		
		// Chat banned icon.
		ThreadPool.schedule(() ->
		{
			if (player.isChatBanned())
			{
				player.getEffectList().startAbnormalVisualEffect(AbnormalVisualEffect.NO_CHAT);
			}
		}, 5500);
		
		AntiFeedManager.getInstance().removePlayer(AntiFeedManager.OFFLINE_PLAY, player);
		
		// EnterWorld has finished.
		player.setEnteredWorld();
		
		if ((player.hasPremiumStatus() || !Config.PC_CAFE_ONLY_PREMIUM) && Config.PC_CAFE_RETAIL_LIKE)
		{
			PcCafePointsManager.getInstance().run(player);
		}
	}
	
	/**
	 * @param player
	 */
	private void notifyClanMembers(Player player)
	{
		final Clan clan = player.getClan();
		if (clan != null)
		{
			clan.getClanMember(player.getObjectId()).setPlayer(player);
			
			final SystemMessage msg = new SystemMessage(SystemMessageId.CLAN_MEMBER_S1_HAS_LOGGED_INTO_GAME);
			msg.addString(player.getName());
			clan.broadcastToOtherOnlineMembers(msg, player);
			clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(player), player);
		}
	}
	
	/**
	 * @param player
	 */
	private void notifySponsorOrApprentice(Player player)
	{
		if (player.getSponsor() != 0)
		{
			final Player sponsor = World.getInstance().getPlayer(player.getSponsor());
			if (sponsor != null)
			{
				final SystemMessage msg = new SystemMessage(SystemMessageId.YOUR_APPRENTICE_S1_HAS_LOGGED_IN);
				msg.addString(player.getName());
				sponsor.sendPacket(msg);
			}
		}
		else if (player.getApprentice() != 0)
		{
			final Player apprentice = World.getInstance().getPlayer(player.getApprentice());
			if (apprentice != null)
			{
				final SystemMessage msg = new SystemMessage(SystemMessageId.YOUR_SPONSOR_C1_HAS_LOGGED_IN);
				msg.addString(player.getName());
				apprentice.sendPacket(msg);
			}
		}
	}
}
