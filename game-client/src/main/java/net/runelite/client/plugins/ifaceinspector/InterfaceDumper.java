/*
 * Interface Inspector - dev tool for dumping open interface component ids + metadata.
 *
 * Reads the client's own interface model (com.osroyale.RSInterface) directly, since the
 * plugin runs in the same JVM. Produces a human-readable report so content devs can map
 * the new client's component ids to server-side button handling.
 */
package net.runelite.client.plugins.ifaceinspector;

import com.osroyale.RSInterface;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

final class InterfaceDumper
{
	private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private InterfaceDumper()
	{
	}

	/** Result of locating the currently-open interface. */
	static final class OpenRoot
	{
		final int id;
		final String source;

		OpenRoot(int id, String source)
		{
			this.id = id;
			this.source = source;
		}
	}

	/**
	 * Finds the currently-open interface root from the client's public open-interface fields,
	 * in priority order. Returns null if nothing identifiable is open.
	 */
	static OpenRoot resolveOpenRoot()
	{
		final com.osroyale.Client c = com.osroyale.Client.instance;
		if (c == null)
		{
			return null;
		}

		if (c.fullscreenInterfaceID != -1)
		{
			return new OpenRoot(c.fullscreenInterfaceID, "fullscreenInterfaceID");
		}
		if (com.osroyale.Client.openInterfaceID != -1)
		{
			return new OpenRoot(com.osroyale.Client.openInterfaceID, "openInterfaceID");
		}
		if (c.backDialogueId != -1)
		{
			return new OpenRoot(c.backDialogueId, "backDialogueId (chatbox)");
		}
		return null;
	}

	/**
	 * Builds the full dump for the given root interface id.
	 */
	static String dump(int rootId, String source, InterfaceInspectorConfig config)
	{
		final StringBuilder sb = new StringBuilder();
		final RSInterface[] cache = RSInterface.interfaceCache;

		sb.append("=== Interface Dump ===\n");
		sb.append("time:   ").append(LocalDateTime.now().format(TS)).append('\n');
		sb.append("root:   ").append(rootId);
		if (source != null)
		{
			sb.append("  (via ").append(source).append(')');
		}
		sb.append('\n');

		if (cache == null || rootId < 0 || rootId >= cache.length || cache[rootId] == null)
		{
			sb.append("!! No interface loaded at id ").append(rootId).append('\n');
			return sb.toString();
		}

		final RSInterface root = cache[rootId];
		final String title = inferTitle(root, cache);
		if (title != null)
		{
			sb.append("title:  \"").append(title).append("\" (inferred)\n");
		}
		sb.append("type:   ").append(root.type).append(" (").append(typeName(root.type)).append(")\n");
		sb.append("\ncomponents (id | type | clickable | label | details):\n");

		final int[] count = {0};
		final Set<Integer> visited = new HashSet<>();
		appendComponent(sb, cache, rootId, 0, visited, config, count);

		sb.append("\ntotal components listed: ").append(count[0]).append('\n');
		return sb.toString();
	}

	/**
	 * Dumps every valid (non-null) interface in the cache. Each interface is emitted
	 * once: top-level "roots" (interfaces not referenced as a child of any other) are
	 * printed with their full child tree, which together cover every defined interface.
	 */
	static String dumpAll(InterfaceInspectorConfig config)
	{
		final RSInterface[] cache = RSInterface.interfaceCache;
		final StringBuilder sb = new StringBuilder();
		sb.append("=== ALL INTERFACES DUMP ===\n");
		sb.append("time: ").append(LocalDateTime.now().format(TS)).append('\n');

		if (cache == null)
		{
			sb.append("!! interface cache not loaded yet\n");
			return sb.toString();
		}

		// Mark every id that appears as a child of another interface.
		final boolean[] isChild = new boolean[cache.length];
		int defined = 0;
		for (final RSInterface w : cache)
		{
			if (w == null)
			{
				continue;
			}
			defined++;
			if (w.children != null)
			{
				for (final int childId : w.children)
				{
					if (childId >= 0 && childId < cache.length)
					{
						isChild[childId] = true;
					}
				}
			}
		}

		int roots = 0;
		final StringBuilder body = new StringBuilder();
		final int[] count = {0};
		for (int id = 0; id < cache.length; id++)
		{
			if (cache[id] == null || isChild[id])
			{
				continue;
			}
			roots++;
			final RSInterface root = cache[id];
			body.append("\n----- ROOT ").append(id);
			final String title = inferTitle(root, cache);
			if (title != null)
			{
				body.append("  \"").append(title).append('"');
			}
			body.append("  (type ").append(root.type).append('/').append(typeName(root.type)).append(") -----\n");

			final Set<Integer> visited = new HashSet<>();
			appendComponent(body, cache, id, 0, visited, config, count);
		}

		sb.append("cache size:          ").append(cache.length).append('\n');
		sb.append("defined interfaces:  ").append(defined).append('\n');
		sb.append("root interfaces:     ").append(roots).append('\n');
		sb.append("components listed:   ").append(count[0]).append('\n');
		sb.append(body);
		return sb.toString();
	}

	private static void appendComponent(
		StringBuilder sb,
		RSInterface[] cache,
		int id,
		int depth,
		Set<Integer> visited,
		InterfaceInspectorConfig config,
		int[] count)
	{
		if (id < 0 || id >= cache.length || cache[id] == null || !visited.add(id))
		{
			return;
		}

		final RSInterface w = cache[id];
		final boolean clickable = isClickable(w);
		final boolean isText = w.type == 4;

		final boolean skip = (config.clickableOnly() && !clickable)
			|| (!config.includeText() && isText && !clickable);

		if (!skip)
		{
			count[0]++;
			indent(sb, depth);
			sb.append('[').append(id).append("] ")
				.append("type=").append(w.type).append('(').append(typeName(w.type)).append(')');
			if (clickable)
			{
				sb.append(" CLICK(action=").append(w.atActionType).append(')');
			}
			sb.append(" \"").append(label(w)).append('"');

			final String details = details(w);
			if (!details.isEmpty())
			{
				sb.append("  ").append(details);
			}
			sb.append('\n');
		}

		if (w.children != null)
		{
			for (int childId : w.children)
			{
				appendComponent(sb, cache, childId, depth + 1, visited, config, count);
			}
		}
	}

	private static boolean isClickable(RSInterface w)
	{
		return w.atActionType != 0 || (w.actions != null && w.actions.length > 0);
	}

	/** Best human-readable description of a component. */
	private static String label(RSInterface w)
	{
		String s = firstNonBlank(w.tooltip, w.enabledMessage, w.disabledMessage);
		if (s == null && w.actions != null)
		{
			for (String a : w.actions)
			{
				if (notBlank(a))
				{
					s = a;
					break;
				}
			}
		}
		if (s == null)
		{
			s = firstNonBlank(w.selectedActionName, w.spellName);
		}
		if (s == null)
		{
			return "(unnamed)";
		}
		return stripTags(s);
	}

	private static String details(RSInterface w)
	{
		final StringBuilder d = new StringBuilder();
		if (w.actions != null && w.actions.length > 0)
		{
			d.append("actions=").append(joinActions(w.actions));
		}
		if (notBlank(w.disabledMessage) || notBlank(w.enabledMessage))
		{
			append(d, "text=\"" + stripTags(firstNonBlank(w.disabledMessage, w.enabledMessage)) + "\"");
		}
		if (w.scripts != null && w.scripts.length > 0)
		{
			append(d, "config-script=Y");
		}
		if (w.contentType != 0)
		{
			append(d, "content=" + w.contentType);
		}
		append(d, "pos=(" + columnX(w) + "," + columnY(w) + ")");
		return d.toString();
	}

	private static int columnX(RSInterface w)
	{
		return w.childX != null && w.childX.length > 0 ? w.childX[0] : 0;
	}

	private static int columnY(RSInterface w)
	{
		return w.childY != null && w.childY.length > 0 ? w.childY[0] : 0;
	}

	/** Inferred interface name: text of the first non-blank TEXT child. */
	private static String inferTitle(RSInterface root, RSInterface[] cache)
	{
		if (root.children == null)
		{
			return null;
		}
		for (int childId : root.children)
		{
			if (childId < 0 || childId >= cache.length || cache[childId] == null)
			{
				continue;
			}
			final RSInterface c = cache[childId];
			if (c.type == 4)
			{
				final String t = firstNonBlank(c.disabledMessage, c.enabledMessage);
				if (t != null)
				{
					return stripTags(t);
				}
			}
		}
		return null;
	}

	private static String typeName(int type)
	{
		switch (type)
		{
			case 0: return "container";
			case 1: return "model-list";
			case 2: return "inventory";
			case 3: return "rectangle";
			case 4: return "text";
			case 5: return "sprite";
			case 6: return "model";
			case 7: return "item-list";
			case 8: return "tooltip/hover";
			case 9: return "line";
			default: return "?";
		}
	}

	private static String joinActions(String[] actions)
	{
		final StringBuilder b = new StringBuilder("[");
		boolean first = true;
		for (String a : actions)
		{
			if (!notBlank(a))
			{
				continue;
			}
			if (!first)
			{
				b.append(", ");
			}
			b.append(stripTags(a));
			first = false;
		}
		return b.append(']').toString();
	}

	private static void indent(StringBuilder sb, int depth)
	{
		for (int i = 0; i < depth; i++)
		{
			sb.append("  ");
		}
	}

	private static void append(StringBuilder d, String part)
	{
		if (d.length() > 0)
		{
			d.append(" | ");
		}
		d.append(part);
	}

	private static String firstNonBlank(String... values)
	{
		for (String v : values)
		{
			if (notBlank(v))
			{
				return v;
			}
		}
		return null;
	}

	private static boolean notBlank(String s)
	{
		return s != null && !s.trim().isEmpty() && !"null".equalsIgnoreCase(s.trim());
	}

	private static String stripTags(String s)
	{
		if (s == null)
		{
			return "";
		}
		return s.replaceAll("<[^>]*>", "").trim();
	}
}
