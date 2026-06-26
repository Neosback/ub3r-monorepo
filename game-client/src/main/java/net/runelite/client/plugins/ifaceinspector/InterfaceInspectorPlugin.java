/*
 * Interface Inspector - dev tool for dumping open interface component ids + metadata.
 *
 * Temp content-dev tool: identifies the new client's interface/button component ids and
 * their human-readable labels so they can be wired up on the server.
 */
package net.runelite.client.plugins.ifaceinspector;

import com.google.inject.Provides;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.input.KeyManager;
import net.runelite.client.util.HotkeyListener;

@Slf4j
@PluginDescriptor(
	name = "Interface Inspector",
	description = "Dump the open interface's component ids + labels to a file (content dev tool)",
	tags = {"dev", "interface", "widget", "button", "id", "inspect"},
	loadWhenOutdated = true
)
public class InterfaceInspectorPlugin extends Plugin
{
	private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private KeyManager keyManager;

	@Inject
	private InterfaceInspectorConfig config;

	private InterfaceInspectorPanel panel;
	private NavigationButton navButton;
	private String lastReport;
	private int lastRootId = -1;

	private final HotkeyListener hotkey = new HotkeyListener(() -> config.dumpHotkey())
	{
		@Override
		public void hotkeyPressed()
		{
			dumpOpen();
		}
	};

	@Provides
	InterfaceInspectorConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(InterfaceInspectorConfig.class);
	}

	@Override
	protected void startUp()
	{
		panel = injector.getInstance(InterfaceInspectorPanel.class);
		panel.init(this);

		navButton = NavigationButton.builder()
			.tooltip("Interface Inspector")
			.icon(createIcon())
			.priority(12)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navButton);
		keyManager.registerKeyListener(hotkey);

		if (config.dumpAllOnStartup())
		{
			dumpAll();
		}
	}

	@Override
	protected void shutDown()
	{
		keyManager.unregisterKeyListener(hotkey);
		clientToolbar.removeNavigation(navButton);
		panel = null;
	}

	/** Dump whatever interface the client currently has open. */
	void dumpOpen()
	{
		final InterfaceDumper.OpenRoot root = InterfaceDumper.resolveOpenRoot();
		if (root == null)
		{
			chat("Interface Inspector: no interface is open.");
			if (panel != null)
			{
				panel.showReport("No interface is open.\n\nOpen an interface (bank, duel, trade...) then press the hotkey or 'Dump open interface'.");
			}
			return;
		}
		final String report = InterfaceDumper.dump(root.id, root.source, config);
		publish(root.id, report);
	}

	/** Dump a specific interface id (from the panel). */
	void dumpId(int id)
	{
		final String report = InterfaceDumper.dump(id, "manual", config);
		publish(id, report);
	}

	/** Dump every valid interface in the cache to a single file. */
	void dumpAll()
	{
		final String report = InterfaceDumper.dumpAll(config);
		lastReport = report;
		lastRootId = -1;
		if (panel != null)
		{
			panel.showReport(report);
		}
		final Path path = writeAllToDesktop(report);
		chat("Interface Inspector: dumped ALL interfaces"
			+ (path != null ? " -> " + path.getFileName() : " (see side panel)"));
	}

	/** Save the last dump to the Desktop on demand (panel button). */
	void saveLast()
	{
		if (lastReport == null)
		{
			chat("Interface Inspector: nothing to save yet.");
			return;
		}
		final Path path = writeToDesktop(lastRootId, lastReport);
		if (path != null)
		{
			chat("Interface Inspector: saved " + path.getFileName());
		}
	}

	private void publish(int rootId, String report)
	{
		lastReport = report;
		lastRootId = rootId;
		if (panel != null)
		{
			panel.showReport(report);
		}
		if (config.saveToDesktop())
		{
			final Path path = writeToDesktop(rootId, report);
			chat("Interface Inspector: dumped interface " + rootId
				+ (path != null ? " -> " + path.getFileName() : ""));
		}
		else
		{
			chat("Interface Inspector: dumped interface " + rootId + " (see side panel)");
		}
	}

	private Path writeToDesktop(int rootId, String report)
	{
		return writeNamed("iface-" + rootId + "-" + LocalDateTime.now().format(FILE_TS) + ".txt", report);
	}

	private Path writeAllToDesktop(String report)
	{
		return writeNamed("iface-ALL-" + LocalDateTime.now().format(FILE_TS) + ".txt", report);
	}

	private Path writeNamed(String fileName, String report)
	{
		try
		{
			final Path desktop = Paths.get(System.getProperty("user.home"), "Desktop");
			Files.createDirectories(desktop);
			final Path file = desktop.resolve(fileName);
			Files.write(file, report.getBytes(java.nio.charset.StandardCharsets.UTF_8));
			return file;
		}
		catch (Exception ex)
		{
			log.warn("Failed to write interface dump", ex);
			chat("Interface Inspector: failed to write file (" + ex.getMessage() + ")");
			return null;
		}
	}

	private void chat(String message)
	{
		try
		{
			if (com.osroyale.Client.instance != null)
			{
				com.osroyale.Client.instance.pushMessage(message, false);
			}
		}
		catch (Exception ignored)
		{
			// chat is best-effort
		}
	}

	private static BufferedImage createIcon()
	{
		final BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = img.createGraphics();
		g.setColor(new Color(255, 220, 120));
		g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
		g.drawString("IF", 1, 13);
		g.dispose();
		return img;
	}
}
