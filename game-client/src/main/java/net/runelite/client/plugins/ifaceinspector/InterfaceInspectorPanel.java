/*
 * Interface Inspector - dev tool for dumping open interface component ids + metadata.
 */
package net.runelite.client.plugins.ifaceinspector;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

class InterfaceInspectorPanel extends PluginPanel
{
	private final JTextArea output = new JTextArea();
	private final JTextField idField = new JTextField();
	private InterfaceInspectorPlugin plugin;

	void init(InterfaceInspectorPlugin plugin)
	{
		this.plugin = plugin;

		setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		setLayout(new BorderLayout(0, 6));
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		final JPanel controls = new JPanel(new BorderLayout(0, 4));
		controls.setBackground(ColorScheme.DARK_GRAY_COLOR);

		final JPanel topButtons = new JPanel(new java.awt.GridLayout(1, 2, 4, 0));
		topButtons.setBackground(ColorScheme.DARK_GRAY_COLOR);
		final JButton dumpOpen = new JButton("Dump open");
		dumpOpen.addActionListener(e -> plugin.dumpOpen());
		final JButton dumpAll = new JButton("Dump ALL");
		dumpAll.setToolTipText("Dump every valid interface in the cache to one file");
		dumpAll.addActionListener(e -> plugin.dumpAll());
		topButtons.add(dumpOpen);
		topButtons.add(dumpAll);
		controls.add(topButtons, BorderLayout.NORTH);

		final JPanel idRow = new JPanel(new BorderLayout(4, 0));
		idRow.setBackground(ColorScheme.DARK_GRAY_COLOR);
		idField.setToolTipText("Dump a specific interface id");
		idRow.add(new JLabel("id:"), BorderLayout.WEST);
		idRow.add(idField, BorderLayout.CENTER);
		final JButton dumpId = new JButton("Dump");
		dumpId.addActionListener(e -> dumpSpecific());
		idField.addActionListener(e -> dumpSpecific());
		idRow.add(dumpId, BorderLayout.EAST);
		controls.add(idRow, BorderLayout.CENTER);

		final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
		buttons.setBackground(ColorScheme.DARK_GRAY_COLOR);
		final JButton save = new JButton("Save .txt");
		save.addActionListener(e -> plugin.saveLast());
		final JButton copy = new JButton("Copy");
		copy.addActionListener(e -> copyToClipboard());
		buttons.add(save);
		buttons.add(copy);
		controls.add(buttons, BorderLayout.SOUTH);

		add(controls, BorderLayout.NORTH);

		output.setEditable(false);
		output.setLineWrap(false);
		output.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 11));
		output.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		output.setForeground(Color.WHITE);
		final JScrollPane scroll = new JScrollPane(output);
		scroll.setPreferredSize(new Dimension(0, 480));
		add(scroll, BorderLayout.CENTER);
	}

	private void dumpSpecific()
	{
		final String text = idField.getText().trim();
		if (text.isEmpty())
		{
			return;
		}
		try
		{
			plugin.dumpId(Integer.parseInt(text));
		}
		catch (NumberFormatException ex)
		{
			showReport("Enter a numeric interface id.");
		}
	}

	private void copyToClipboard()
	{
		try
		{
			java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
				.setContents(new java.awt.datatransfer.StringSelection(output.getText()), null);
		}
		catch (Exception ignored)
		{
			// clipboard is best-effort
		}
	}

	void showReport(String report)
	{
		output.setText(report);
		output.setCaretPosition(0);
	}
}
