/*
 * Copyright (c) 2017, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.hiscore;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.swing.SwingUtilities;
import lombok.Getter;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.IconID;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.MenuEntryTwo;
import net.runelite.api.Player;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.widgets.WidgetID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.hiscore.HiscoreEndpoint;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;

@PluginDescriptor(
	name = "HiScore",
	description = "Enable the HiScore panel and an optional Lookup option on players",
	tags = {"panel", "players"},
	loadWhenOutdated = true
)
public class HiscorePlugin extends Plugin
{
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HiscorePlugin.class);
	private static final String LOOKUP = "Lookup";
	private static final Pattern BOUNTY_PATTERN = Pattern.compile("You have been assigned a new target: <col=[0-9a-f]+>(.*)</col>");

	// A short-lived cache of player indexes to names to allow lookup of players who have despawned prior to a lookup
	// being executed via a "Lookup" menu option click.
	private final Map<Integer, String> playerIndexName = new HashMap<>();

	@Inject
	private Client client;

	@Inject
	private Provider<MenuManager> menuManager;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private HiscoreConfig config;

	private NavigationButton navButton;
	private HiscorePanel hiscorePanel;

	@Getter
	private HiscoreEndpoint localHiscoreEndpoint;

	@Provides
	HiscoreConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(HiscoreConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		hiscorePanel = injector.getInstance(HiscorePanel.class);

		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "normal.png");

		navButton = NavigationButton.builder()
			.tooltip("Hiscore")
			.icon(icon)
			.priority(5)
			.panel(hiscorePanel)
			.build();

		clientToolbar.addNavigation(navButton);

		if (config.playerOption())
		{
			menuManager.get().addPlayerMenuItem(LOOKUP);
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		hiscorePanel.shutdown();
		clientToolbar.removeNavigation(navButton);
		playerIndexName.clear();

		menuManager.get().removePlayerMenuItem(LOOKUP);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals("hiscore"))
		{
			menuManager.get().removePlayerMenuItem(LOOKUP);

			if (config.playerOption())
			{
				menuManager.get().addPlayerMenuItem(LOOKUP);
			}
		}
	}

	@Subscribe
	private void onMenuOpened(MenuOpened event)
	{
		playerIndexName.clear();
		for (MenuEntryTwo entry : event.getMenuEntries())
		{
			if (MenuAction.of(entry.getType()) != MenuAction.RUNELITE_PLAYER || !entry.getOption().equals(LOOKUP))
			{
				continue;
			}

			int index = (int) entry.getIdentifier();
			if (index < 0 || index >= client.getCachedPlayers().length)
			{
				continue;
			}
			
			String name = null;
			final Player player = client.getCachedPlayers()[index];
			if (player != null)
			{
				name = player.getName();
			}
			else
			{
				String entryTarget = entry.getTarget();
				if (entryTarget != null && !entryTarget.isEmpty())
				{
					String cleaned = Text.removeTags(entryTarget);
					cleaned = cleaned.replace('\u00A0', ' ');
					int parenIndex = cleaned.indexOf(" (");
					if (parenIndex != -1)
					{
						cleaned = cleaned.substring(0, parenIndex);
					}
					name = cleaned.trim();
				}
			}

			if (name != null && !name.isEmpty())
			{
				playerIndexName.put((int) entry.getIdentifier(), name);
			}
		}
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if ((event.getType() != MenuAction.CC_OP.getId() && event.getType() != MenuAction.CC_OP_LOW_PRIORITY.getId()) || !config.menuOption())
		{
			return;
		}

		final String option = event.getOption();
		final int componentId = event.getActionParam1();
		final int groupId = componentId >>> 16;

		if (groupId == WidgetID.FRIENDS_LIST_GROUP_ID && option.equals("Delete")
			|| groupId == WidgetID.FRIENDS_CHAT_GROUP_ID && (option.equals("Add ignore") || option.equals("Remove friend"))
			|| groupId == WidgetID.CHATBOX_GROUP_ID && (option.equals("Add ignore") || option.equals("Message"))
			|| groupId == WidgetID.IGNORE_LIST_GROUP_ID && option.equals("Delete")
			|| groupId == WidgetID.PRIVATE_CHAT && (option.equals("Add ignore") || option.equals("Message"))
		)
		{
			client.createMenuEntry(-2)
				.setOption(LOOKUP)
				.setTarget(event.getTarget())
				.setType(MenuAction.RUNELITE)
				.setIdentifier(event.getIdentifier())
				.onClick(e ->
				{
					final HiscoreEndpoint chatMessageEndpoint = findHiscoreEndpointFromPlayerName(e.getTarget());
					HiscoreEndpoint endpoint = HiscoreEndpoint.NORMAL;

					if (chatMessageEndpoint != HiscoreEndpoint.NORMAL || endpoint == HiscoreEndpoint.LEAGUE)
					{
						// Determine proper endpoint from player name (eg. ironman or normal endpoint)
						// Also assume normal hiscore endpoint for chat message w/o league icon received on league world
						endpoint = chatMessageEndpoint;
					}

					String target = Text.removeTags(e.getTarget());
					lookupPlayer(target, endpoint);
				});
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (event.getMenuAction() == MenuAction.RUNELITE_PLAYER && event.getMenuOption().equals(LOOKUP))
		{
			Player player = null;
			int index = (int) event.getId();
			if (index >= 0 && index < client.getCachedPlayers().length)
			{
				player = client.getCachedPlayers()[index];
			}
			String target = null;
			if (player != null)
			{
				target = player.getName();
			}
			if (target == null || target.isEmpty())
			{
				target = playerIndexName.get((int) event.getId());
			}
			if (target == null || target.isEmpty())
			{
				String menuTarget = event.getMenuTarget();
				if (menuTarget != null && !menuTarget.isEmpty())
				{
					String cleaned = Text.removeTags(menuTarget);
					cleaned = cleaned.replace('\u00A0', ' ');
					int parenIndex = cleaned.indexOf(" (");
					if (parenIndex != -1)
					{
						cleaned = cleaned.substring(0, parenIndex);
					}
					target = cleaned.trim();
				}
			}
			playerIndexName.clear();

			lookupPlayer(target, getWorldEndpoint());
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.GAMEMESSAGE || !config.bountylookup())
		{
			return;
		}

		String message = event.getMessage();
		Matcher m = BOUNTY_PATTERN.matcher(message);
		if (m.matches())
		{
			lookupPlayer(m.group(1), HiscoreEndpoint.NORMAL);
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		localHiscoreEndpoint = findHiscoreEndpointFromLocalPlayer();
	}

	void lookupPlayer(String playerName, HiscoreEndpoint endpoint)
	{
		if (com.google.common.base.Strings.isNullOrEmpty(playerName))
		{
			log.warn("Cannot lookup player: name is empty");
			return;
		}
		SwingUtilities.invokeLater(() ->
		{
			clientToolbar.openPanel(navButton);
			hiscorePanel.lookup(playerName, endpoint);
		});
	}

	HiscoreEndpoint getWorldEndpoint()
	{
		return HiscoreEndpoint.NORMAL;
	}

	private HiscoreEndpoint findHiscoreEndpointFromLocalPlayer()
	{
		final HiscoreEndpoint profile = getWorldEndpoint();
		if (profile != HiscoreEndpoint.NORMAL)
		{
			return profile;
		}
		return HiscoreEndpoint.NORMAL;
	}

	private static HiscoreEndpoint findHiscoreEndpointFromPlayerName(String name)
	{
		if (name.contains(IconID.IRONMAN.toString()))
		{
			return HiscoreEndpoint.IRONMAN;
		}
		if (name.contains(IconID.ULTIMATE_IRONMAN.toString()))
		{
			return HiscoreEndpoint.ULTIMATE_IRONMAN;
		}
		if (name.contains(IconID.HARDCORE_IRONMAN.toString()))
		{
			return HiscoreEndpoint.HARDCORE_IRONMAN;
		}
		if (name.contains(IconID.LEAGUE.toString()))
		{
			return HiscoreEndpoint.LEAGUE;
		}
		return HiscoreEndpoint.NORMAL;
	}
}
