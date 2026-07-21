/*
 * Copyright (c) 2026, Antigravity
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

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("bosstimer")
public interface BossTimersConfig extends Config
{
	@ConfigItem(
		keyName = "showDad",
		name = "Dad",
		description = "Show spawn timer for Dad",
		position = 1
	)
	default boolean showDad()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showKbd",
		name = "King Black Dragon",
		description = "Show spawn timer for King Black Dragon",
		position = 2
	)
	default boolean showKbd()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showKq",
		name = "Kalphite Queen",
		description = "Show spawn timer for Kalphite Queen",
		position = 3
	)
	default boolean showKq()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showKk",
		name = "Kalphite King",
		description = "Show spawn timer for Kalphite King",
		position = 4
	)
	default boolean showKk()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showGreaterDemon",
		name = "Greater Demon",
		description = "Show spawn timer for Greater Demon",
		position = 5
	)
	default boolean showGreaterDemon()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showBlackDemon",
		name = "Black Demon",
		description = "Show spawn timer for Black Demon",
		position = 6
	)
	default boolean showBlackDemon()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showGreenDragon",
		name = "Green Dragon",
		description = "Show spawn timer for Green Dragon",
		position = 7
	)
	default boolean showGreenDragon()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showBlackDragon",
		name = "Black Dragon",
		description = "Show spawn timer for Black Dragon",
		position = 8
	)
	default boolean showBlackDragon()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showIceQueen",
		name = "Ice Queen",
		description = "Show spawn timer for Ice Queen",
		position = 9
	)
	default boolean showIceQueen()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showJungleDemon",
		name = "Jungle Demon",
		description = "Show spawn timer for Jungle Demon",
		position = 10
	)
	default boolean showJungleDemon()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showBlackKnightTitan",
		name = "Black Knight Titan",
		description = "Show spawn timer for Black Knight Titan",
		position = 11
	)
	default boolean showBlackKnightTitan()
	{
		return true;
	}
}
