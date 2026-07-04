package io.kimpton.accountdataexporter;

import io.kimpton.accountdataexporter.model.HunterRumours;
import io.kimpton.accountdataexporter.model.SlayerBlockSlot;
import io.kimpton.accountdataexporter.model.SlayerMasterBlocks;
import io.kimpton.accountdataexporter.model.SlayerState;
import io.kimpton.accountdataexporter.model.SlayerTask;
import io.kimpton.accountdataexporter.model.Snapshot;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.io.File;
import java.time.LocalTime;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.LinkBrowser;

/** Compact status panel: export destination + last export, the player's slayer
 *  block lists per master, the active hunter rumour, and a Buy Me a Coffee
 *  button in the top right. Content hugs the top (no stretched rows) and
 *  scrolls when it outgrows the sidebar. */
@Slf4j
class AccountDataExporterPanel extends PluginPanel
{
	private static final String COFFEE_URL = "https://buymeacoffee.com/kimpton";

	private static final String[] MASTER_KEYS = {
		"turael", "mazchna", "vannaka", "chaeldar", "konar", "nieve", "duradel", "krystilia",
	};

	private final JLabel accountLabel = smallLabel("Account: —", ColorScheme.LIGHT_GRAY_COLOR);
	private final JLabel lastExportLabel = smallLabel("No export yet this session.", ColorScheme.MEDIUM_GRAY_COLOR);
	private final JPanel slayerSection = column();
	private final JPanel rumourSection = column();

	AccountDataExporterPanel(File exportDirectory)
	{
		super(false);
		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel content = column();
		content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		content.setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Header: title left, coffee-mug support button top right.
		JPanel header = new JPanel(new BorderLayout());
		header.setOpaque(false);
		header.setAlignmentX(Component.LEFT_ALIGNMENT);
		JLabel title = new JLabel("Account Data Exporter");
		title.setForeground(ColorScheme.BRAND_ORANGE);
		header.add(title, BorderLayout.WEST);
		JButton coffee = new JButton(new ImageIcon(ImageUtil.loadImageResource(AccountDataExporterPanel.class, "coffee.png")));
		coffee.setToolTipText("Enjoying the plugin? Buy me a coffee");
		coffee.setCursor(new Cursor(Cursor.HAND_CURSOR));
		coffee.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		coffee.setContentAreaFilled(false);
		coffee.setFocusPainted(false);
		coffee.addActionListener(e -> LinkBrowser.browse(COFFEE_URL));
		header.add(coffee, BorderLayout.EAST);
		content.add(header);
		content.add(Box.createVerticalStrut(10));

		// Export status.
		content.add(sectionTitle("Export"));
		JLabel dirLabel = smallLabel("<html><body style='width:190px'>"
			+ exportDirectory.getAbsolutePath() + "</body></html>", ColorScheme.MEDIUM_GRAY_COLOR);
		content.add(dirLabel);
		content.add(Box.createVerticalStrut(4));
		JButton open = new JButton("Open export folder");
		open.setFocusPainted(false);
		open.setFont(FontManager.getRunescapeSmallFont());
		open.setAlignmentX(Component.LEFT_ALIGNMENT);
		open.addActionListener(e -> openFolder(exportDirectory));
		content.add(open);
		content.add(Box.createVerticalStrut(4));
		content.add(accountLabel);
		content.add(lastExportLabel);
		content.add(Box.createVerticalStrut(10));

		// Dynamic sections, filled in after each export.
		content.add(sectionTitle("Slayer blocks"));
		slayerSection.add(smallLabel("Waiting for the first export…", ColorScheme.MEDIUM_GRAY_COLOR));
		content.add(slayerSection);
		content.add(Box.createVerticalStrut(10));
		content.add(sectionTitle("Hunter rumour"));
		rumourSection.add(smallLabel("Waiting for the first export…", ColorScheme.MEDIUM_GRAY_COLOR));
		content.add(rumourSection);

		// Anchor the column to the top so nothing stretches, and scroll overflow.
		JPanel north = new JPanel(new BorderLayout());
		north.setBackground(ColorScheme.DARK_GRAY_COLOR);
		north.add(content, BorderLayout.NORTH);
		JScrollPane scroll = new JScrollPane(north);
		scroll.setBorder(null);
		scroll.getVerticalScrollBar().setUnitIncrement(16);
		add(scroll, BorderLayout.CENTER);
	}

	/** Called on the EDT after each snapshot write. */
	void update(Snapshot snapshot)
	{
		accountLabel.setText("Account: " + snapshot.getRsn());
		lastExportLabel.setText("Last export: " + LocalTime.now().withNano(0));
		rebuildSlayer(snapshot.getSlayer());
		rebuildRumour(snapshot.getHunterRumours());
		revalidate();
		repaint();
	}

	private void rebuildSlayer(SlayerState slayer)
	{
		slayerSection.removeAll();
		if (slayer == null)
		{
			slayerSection.add(smallLabel("Slayer section is disabled in the config.", ColorScheme.MEDIUM_GRAY_COLOR));
			return;
		}
		SlayerTask task = slayer.getCurrentTask();
		if (task != null && task.isHasTask())
		{
			String line = task.getAmountRemaining() + "× " + orId(task.getName(), task.getTaskId());
			if (task.getAreaName() != null)
			{
				line += " · " + task.getAreaName();
			}
			slayerSection.add(smallLabel("Task: " + line, ColorScheme.LIGHT_GRAY_COLOR));
			slayerSection.add(Box.createVerticalStrut(4));
		}
		boolean any = false;
		if (slayer.getBlockLists() != null)
		{
			for (String key : MASTER_KEYS)
			{
				SlayerMasterBlocks blocks = slayer.getBlockLists().get(key);
				if (blocks == null)
				{
					continue;
				}
				StringBuilder names = new StringBuilder();
				appendBlocked(names, blocks.getSlots());
				if (blocks.getDiarySlot() != null && blocks.getDiarySlot().isBlocked())
				{
					if (names.length() > 0)
					{
						names.append(", ");
					}
					names.append(orId(blocks.getDiarySlot().getName(), blocks.getDiarySlot().getTaskId()));
				}
				if (names.length() == 0)
				{
					continue;
				}
				any = true;
				String master = key.equals("konar") ? "Konar" : Character.toUpperCase(key.charAt(0)) + key.substring(1);
				slayerSection.add(smallLabel(master + ":", ColorScheme.BRAND_ORANGE));
				JLabel list = smallLabel("<html><body style='width:190px'>" + names + "</body></html>", ColorScheme.LIGHT_GRAY_COLOR);
				list.setBorder(BorderFactory.createEmptyBorder(0, 8, 4, 0));
				slayerSection.add(list);
			}
		}
		if (!any)
		{
			slayerSection.add(smallLabel("No blocked tasks recorded.", ColorScheme.MEDIUM_GRAY_COLOR));
		}
	}

	private static void appendBlocked(StringBuilder out, java.util.List<SlayerBlockSlot> slots)
	{
		if (slots == null)
		{
			return;
		}
		for (SlayerBlockSlot slot : slots)
		{
			if (slot == null || !slot.isBlocked())
			{
				continue;
			}
			if (out.length() > 0)
			{
				out.append(", ");
			}
			out.append(orId(slot.getName(), slot.getTaskId()));
		}
	}

	private void rebuildRumour(HunterRumours rumours)
	{
		rumourSection.removeAll();
		if (rumours == null || !rumours.isAvailable())
		{
			rumourSection.add(smallLabel("No Hunter Rumours data (needs the Hunter Rumours plugin).", ColorScheme.MEDIUM_GRAY_COLOR));
			return;
		}
		if (rumours.getActiveHunter() != null)
		{
			rumourSection.add(smallLabel("Hunter: " + rumours.getActiveHunter().getName()
				+ " (" + rumours.getActiveHunter().getTier() + ")", ColorScheme.LIGHT_GRAY_COLOR));
		}
		if (rumours.getCurrentRumour() != null)
		{
			rumourSection.add(smallLabel("Rumour: " + rumours.getCurrentRumour().getName(), ColorScheme.LIGHT_GRAY_COLOR));
			String caught = "Caught: " + rumours.getCaught();
			if (rumours.getPity() != null && rumours.getPity().getCatchesUntilPity() > 0)
			{
				caught += " · " + rumours.getPity().getCatchesUntilPity() + " until pity";
			}
			rumourSection.add(smallLabel(caught, ColorScheme.MEDIUM_GRAY_COLOR));
		}
		if (rumours.isFinished())
		{
			rumourSection.add(smallLabel("✓ Complete — hand it in!", ColorScheme.PROGRESS_COMPLETE_COLOR));
		}
		if (rumours.getActiveHunter() == null && rumours.getCurrentRumour() == null)
		{
			rumourSection.add(smallLabel("No active rumour.", ColorScheme.MEDIUM_GRAY_COLOR));
		}
	}

	private static String orId(String name, int id)
	{
		return name != null && !name.isEmpty() ? name : "Task #" + id;
	}

	private static JPanel column()
	{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setOpaque(false);
		p.setAlignmentX(Component.LEFT_ALIGNMENT);
		return p;
	}

	private static JLabel smallLabel(String text, java.awt.Color color)
	{
		JLabel l = new JLabel(text);
		l.setFont(FontManager.getRunescapeSmallFont());
		l.setForeground(color);
		l.setAlignmentX(Component.LEFT_ALIGNMENT);
		return l;
	}

	private static JLabel sectionTitle(String text)
	{
		JLabel l = new JLabel(text.toUpperCase());
		l.setFont(FontManager.getRunescapeSmallFont());
		l.setForeground(ColorScheme.BRAND_ORANGE);
		l.setBorder(BorderFactory.createEmptyBorder(0, 0, 3, 0));
		l.setAlignmentX(Component.LEFT_ALIGNMENT);
		return l;
	}

	private static void openFolder(File dir)
	{
		try
		{
			if (Desktop.isDesktopSupported())
			{
				Desktop.getDesktop().open(dir);
			}
		}
		catch (Exception e)
		{
			log.debug("Could not open export folder", e);
		}
	}
}
