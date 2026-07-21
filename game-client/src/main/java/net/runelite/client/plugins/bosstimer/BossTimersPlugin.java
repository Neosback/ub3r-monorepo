/*
 * Copyright (c) 2016-2017, Cameron Moberg <Moberg@tuta.io>
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
package net.runelite.client.plugins.bosstimer;

import com.google.inject.Provides;
import java.time.Instant;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NPC;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.NpcDespawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

@PluginDescriptor(
	name = "Boss Timers",
	description = "Show boss spawn timer overlays",
	tags = {"combat", "pve", "overlay", "spawn"}
)
@Slf4j
public class BossTimersPlugin extends Plugin
{
	@Inject
	private OverlayManager overlayManager;

	@Inject
	private BossTimerOverlay overlay;

	@Inject
	private BossTimersConfig config;

	@Provides
	BossTimersConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BossTimersConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		NPC npc = npcDespawned.getNpc();
		Boss boss = Boss.find(npc.getId());

		if (boss != null && (boss.isIgnoreDead() || npc.isDead()))
		{
			createTimer(npc, boss);
		}
	}

	private void createTimer(NPC npc, Boss boss)
	{
		if (!showTimerForBoss(boss))
		{
			return;
		}

		log.debug("Creating spawn timer for {} ({})", npc.getName(), boss.getSpawnTime());
		overlay.setTimer(boss, Instant.now().plus(boss.getSpawnTime()));
	}

	@Subscribe
	public void onChatMessage(ChatMessage ev)
	{
		String message = ev.getMessage();
		if (message.contains(" has been slain by "))
		{
			int index = message.indexOf(" has been slain by ");
			if (index > 0)
			{
				String bossName = message.substring(0, index).trim();
				bossName = Text.removeTags(bossName);
				// This server prefixes broadcast/system messages with a literal "System"
				// label (baked into the text via color tags, not a separate sender field),
				// so after tag-stripping this still reads e.g. "System Dad" instead of "Dad".
				bossName = bossName.replaceFirst("(?i)^system\\s+", "").trim();

				Boss boss = Boss.findByName(bossName);
				if (boss != null && showTimerForBoss(boss))
				{
					log.debug("Creating spawn timer for boss (from chat) {} ({})", boss.getName(), boss.getSpawnTime());
					overlay.setTimer(boss, Instant.now().plus(boss.getSpawnTime()));
				}
			}
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("bosstimer"))
		{
			return;
		}

		for (Boss boss : Boss.values())
		{
			if (!showTimerForBoss(boss))
			{
				overlay.clearTimer(boss);
			}
		}
	}

	private boolean showTimerForBoss(Boss boss)
	{
		switch (boss)
		{
			case DAD:
				return config.showDad();
			case KING_BLACK_DRAGON:
				return config.showKbd();
			case KALPHITE_QUEEN:
				return config.showKq();
			case KALPHITE_KING:
				return config.showKk();
			case GREATER_DEMON:
				return config.showGreaterDemon();
			case BLACK_DEMON:
				return config.showBlackDemon();
			case GREEN_DRAGON:
				return config.showGreenDragon();
			case BLACK_DRAGON:
				return config.showBlackDragon();
			case ICE_QUEEN:
				return config.showIceQueen();
			case JUNGLE_DEMON:
				return config.showJungleDemon();
			case BLACK_KNIGHT_TITAN:
				return config.showBlackKnightTitan();
			default:
				return true;
		}
	}
}
