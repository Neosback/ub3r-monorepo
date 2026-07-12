package net.runelite.client.plugins.hiscore;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class HiscoreResourceTest
{
	@Test
	public void supportedSkillIconsAreBundled()
	{
		List<String> icons = Arrays.asList(
			"combat", "overall", "attack", "hitpoints", "mining", "strength", "agility", "smithing",
			"defence", "herblore", "fishing", "ranged", "thieving", "cooking", "prayer", "crafting",
			"firemaking", "magic", "fletching", "woodcutting", "runecraft", "slayer", "farming");

		for (String icon : icons)
		{
			URL resource = HiscorePanel.class.getResource("/skill_icons_small/" + icon + ".png");
			assertNotNull("missing hiscore icon: " + icon, resource);
		}
	}
}
