/*
 * Interface Inspector - dev tool for dumping open interface component ids + metadata.
 */
package net.runelite.client.plugins.ifaceinspector;

import java.awt.event.KeyEvent;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;
import net.runelite.client.config.ModifierlessKeybind;

@ConfigGroup(InterfaceInspectorConfig.GROUP)
public interface InterfaceInspectorConfig extends Config
{
	String GROUP = "ifaceinspector";

	@ConfigItem(
		keyName = "dumpHotkey",
		name = "Dump hotkey",
		description = "Press to dump the currently-open interface to a file on your Desktop.",
		position = 0
	)
	default Keybind dumpHotkey()
	{
		return new ModifierlessKeybind(KeyEvent.VK_F8, 0);
	}

	@ConfigItem(
		keyName = "clickableOnly",
		name = "Clickable only",
		description = "Only list components that look clickable (buttons), skipping pure layout/text.",
		position = 1
	)
	default boolean clickableOnly()
	{
		return false;
	}

	@ConfigItem(
		keyName = "includeText",
		name = "Include text components",
		description = "Include text labels (helps identify what a button does by the text next to it).",
		position = 2
	)
	default boolean includeText()
	{
		return true;
	}

	@ConfigItem(
		keyName = "saveToDesktop",
		name = "Save to Desktop",
		description = "Write each dump to a .txt on your Desktop (also previewed in the side panel).",
		position = 3
	)
	default boolean saveToDesktop()
	{
		return true;
	}

	@ConfigItem(
		keyName = "dumpAllOnStartup",
		name = "Dump ALL on launch",
		description = "Automatically dump every interface to the Desktop when the client starts.",
		position = 4
	)
	default boolean dumpAllOnStartup()
	{
		return false;
	}
}
