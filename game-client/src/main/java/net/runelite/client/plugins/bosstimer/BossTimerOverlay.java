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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

/**
 * Plain text respawn-timer panel: boss name + "M:SS" countdown, no icons.
 * Replaces the old {@code InfoBoxManager}/{@code RespawnTimer} icon-based rendering.
 */
class BossTimerOverlay extends OverlayPanel
{
	private static final Color NAME_COLOR = new Color(255, 208, 87);
	private static final Color TIME_COLOR = Color.WHITE;
	private static final Color TIME_SOON_COLOR = new Color(255, 90, 90);
	private static final long SOON_THRESHOLD_SECONDS = 10;

	private final Map<Boss, Instant> timers = new LinkedHashMap<>();

	@Inject
	private BossTimerOverlay(BossTimersPlugin plugin)
	{
		super(plugin);
		setPosition(OverlayPosition.TOP_RIGHT);
	}

	void setTimer(Boss boss, Instant respawnAt)
	{
		timers.put(boss, respawnAt);
	}

	void clearTimer(Boss boss)
	{
		timers.remove(boss);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		final Instant now = Instant.now();
		final Iterator<Map.Entry<Boss, Instant>> it = timers.entrySet().iterator();
		while (it.hasNext())
		{
			if (!now.isBefore(it.next().getValue()))
			{
				it.remove();
			}
		}

		if (timers.isEmpty())
		{
			return null;
		}

		panelComponent.getChildren().add(TitleComponent.builder()
			.text("Boss Timers")
			.color(Color.WHITE)
			.build());

		for (Map.Entry<Boss, Instant> entry : timers.entrySet())
		{
			final long secondsLeft = Math.max(0, Duration.between(now, entry.getValue()).getSeconds());
			final String time = String.format("%d:%02d", secondsLeft / 60, secondsLeft % 60);

			panelComponent.getChildren().add(LineComponent.builder()
				.left(entry.getKey().getName())
				.leftColor(NAME_COLOR)
				.leftFont(FontManager.getRunescapeBoldFont())
				.right(time)
				.rightColor(secondsLeft <= SOON_THRESHOLD_SECONDS ? TIME_SOON_COLOR : TIME_COLOR)
				.rightFont(FontManager.getRunescapeFont())
				.build());
		}

		return super.render(graphics);
	}
}
