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

import com.google.common.collect.ImmutableMap;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

@Getter
enum Boss
{
	DAD(4130, "Dad", 180, ChronoUnit.SECONDS, 8136),
	KING_BLACK_DRAGON(239, "King Black Dragon", 180, ChronoUnit.SECONDS, 12653),
	KALPHITE_QUEEN(4303, "Kalphite Queen", 250, ChronoUnit.SECONDS, 12647),
	KALPHITE_KING(4304, "Kalphite King", 250, ChronoUnit.SECONDS, 13489),
	GREATER_DEMON(-1, "Greater Demon", 35, ChronoUnit.SECONDS, 13501),
	BLACK_DEMON(-1, "Black Demon", 90, ChronoUnit.SECONDS, 13501),
	GREEN_DRAGON(-1, "Green Dragon", 40, ChronoUnit.SECONDS, 1725),
	BLACK_DRAGON(-1, "Black Dragon", 40, ChronoUnit.SECONDS, 1747),
	ICE_QUEEN(-1, "Ice Queen", 180, ChronoUnit.SECONDS, 1583),
	JUNGLE_DEMON(-1, "Jungle Demon", 180, ChronoUnit.SECONDS, 13501),
	BLACK_KNIGHT_TITAN(4067, "Black Knight Titan", 180, ChronoUnit.SECONDS, 11678),
	;

	private static final Map<Integer, Boss> bosses;
	private static final Map<String, Boss> bossesByName;

	private final int id;
	private final String name;
	private final Duration spawnTime;
	private final int itemSpriteId;
	private final boolean ignoreDead;

	static
	{
		ImmutableMap.Builder<Integer, Boss> builder = new ImmutableMap.Builder<>();
		Map<String, Boss> nameMap = new HashMap<>();

		for (Boss boss : values())
		{
			if (boss.getId() != -1)
			{
				builder.put(boss.getId(), boss);
			}
			nameMap.put(boss.getName().toLowerCase(), boss);
		}

		bosses = builder.build();
		bossesByName = nameMap;
	}

	Boss(int id, String name, long period, TemporalUnit unit, int itemSpriteId)
	{
		this(id, name, period, unit, itemSpriteId, false);
	}

	Boss(int id, String name, long period, TemporalUnit unit, int itemSpriteId, boolean ignoreDead)
	{
		this.id = id;
		this.name = name;
		this.spawnTime = Duration.of(period, unit);
		this.itemSpriteId = itemSpriteId;
		this.ignoreDead = ignoreDead;
	}

	static Boss find(int id)
	{
		return bosses.get(id);
	}

	static Boss findByName(String name)
	{
		return bossesByName.get(name.toLowerCase());
	}
}
