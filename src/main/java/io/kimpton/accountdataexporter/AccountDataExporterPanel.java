package io.kimpton.accountdataexporter;

import io.kimpton.accountdataexporter.model.AchievementDiaries;
import io.kimpton.accountdataexporter.model.DiaryRegion;
import io.kimpton.accountdataexporter.model.HunterRumours;
import io.kimpton.accountdataexporter.model.RumourAssignment;
import io.kimpton.accountdataexporter.model.SlayerBlockSlot;
import io.kimpton.accountdataexporter.model.SlayerMasterBlocks;
import io.kimpton.accountdataexporter.model.SlayerState;
import io.kimpton.accountdataexporter.model.SlayerTask;
import io.kimpton.accountdataexporter.model.Snapshot;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalTime;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.LinkBrowser;

/** Compact status panel: export destination + last export, the player's slayer
 *  block lists per master (each creature a wiki-linked button; empty and
 *  locked slots shown like the companion web UI), hunter rumours across every
 *  master, and a Buy Me a Coffee button in the top right. Text wraps — the
 *  panel only ever scrolls vertically. */
@Slf4j
class AccountDataExporterPanel extends PluginPanel
{
	private static final String COFFEE_URL = "https://buymeacoffee.com/kimpton";
	private static final String WIKI_SEARCH = "https://oldschool.runescape.wiki/w/Special:Search?search=";

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

		// Header: title left, coffee-mug support button pinned top right.
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

		content.add(sectionTitle("Export"));
		content.add(wrapLabel(exportDirectory.getAbsolutePath(), ColorScheme.MEDIUM_GRAY_COLOR));
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

		content.add(sectionTitle("Slayer blocks"));
		slayerSection.add(smallLabel("Waiting for the first export…", ColorScheme.MEDIUM_GRAY_COLOR));
		content.add(slayerSection);
		content.add(Box.createVerticalStrut(10));
		content.add(sectionTitle("Hunter rumours"));
		rumourSection.add(smallLabel("Waiting for the first export…", ColorScheme.MEDIUM_GRAY_COLOR));
		content.add(rumourSection);

		// Vertical-only scrolling: the wrapper tracks the viewport width so long
		// text wraps instead of pushing content (and the coffee button) off-screen.
		ScrollableColumn north = new ScrollableColumn();
		north.add(content, BorderLayout.NORTH);
		JScrollPane scroll = new JScrollPane(north,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setBorder(null);
		scroll.getVerticalScrollBar().setUnitIncrement(16);
		add(scroll, BorderLayout.CENTER);
	}

	/** Called on the EDT after each snapshot write. */
	void update(Snapshot snapshot)
	{
		accountLabel.setText("Account: " + snapshot.getRsn());
		lastExportLabel.setText("Last export: " + LocalTime.now().withNano(0));
		rebuildSlayer(snapshot);
		rebuildRumours(snapshot.getHunterRumours());
		revalidate();
		repaint();
	}

	private void rebuildSlayer(Snapshot snapshot)
	{
		SlayerState slayer = snapshot.getSlayer();
		slayerSection.removeAll();
		if (slayer == null)
		{
			slayerSection.add(smallLabel("Slayer section is disabled in the config.", ColorScheme.MEDIUM_GRAY_COLOR));
			return;
		}
		SlayerTask task = slayer.getCurrentTask();
		if (task != null && task.isHasTask())
		{
			String line = "Task: " + task.getAmountRemaining() + "× " + orId(task.getName(), task.getTaskId());
			if (task.getAreaName() != null)
			{
				line += " · " + task.getAreaName();
			}
			slayerSection.add(wrapLabel(line, ColorScheme.LIGHT_GRAY_COLOR));
			slayerSection.add(Box.createVerticalStrut(4));
		}

		// Slot gating, like the companion web UI: one block slot per 50 quest
		// points (six slots → 300 QP), the diary slot needs Elite Lumbridge &
		// Draynor. QP is our own export (0.6.1+).
		int qp = snapshot.getQuests() != null ? snapshot.getQuests().getQuestPoints() : -1;
		boolean diaryDone = lumbridgeEliteDone(snapshot.getAchievementDiaries());

		if (slayer.getBlockLists() == null || slayer.getBlockLists().isEmpty())
		{
			slayerSection.add(smallLabel("No block-list data yet.", ColorScheme.MEDIUM_GRAY_COLOR));
			return;
		}
		for (String key : MASTER_KEYS)
		{
			SlayerMasterBlocks blocks = slayer.getBlockLists().get(key);
			if (blocks == null)
			{
				continue;
			}
			String master = key.equals("konar") ? "Konar" : Character.toUpperCase(key.charAt(0)) + key.substring(1);
			slayerSection.add(smallLabel(master, ColorScheme.BRAND_ORANGE));
			addSlotRows(blocks.getSlots(), qp);
			SlayerBlockSlot diary = blocks.getDiarySlot();
			if (diary != null)
			{
				if (diary.isBlocked())
				{
					slayerSection.add(creatureButton(orId(diary.getName(), diary.getTaskId())));
				}
				else if (!diaryDone)
				{
					slayerSection.add(slotNote("🔒 Diary slot — Elite Lumbridge & Draynor"));
				}
				else
				{
					slayerSection.add(slotNote("Diary slot — empty"));
				}
			}
			slayerSection.add(Box.createVerticalStrut(6));
		}
	}

	private void addSlotRows(List<SlayerBlockSlot> slots, int qp)
	{
		if (slots == null)
		{
			return;
		}
		for (SlayerBlockSlot slot : slots)
		{
			if (slot == null)
			{
				continue;
			}
			if (slot.isBlocked())
			{
				slayerSection.add(creatureButton(orId(slot.getName(), slot.getTaskId())));
				continue;
			}
			int need = slot.getSlot() * 50;
			if (qp >= 0 && qp < need)
			{
				slayerSection.add(slotNote("🔒 Slot " + slot.getSlot() + " — " + need + " quest points"));
			}
			else
			{
				slayerSection.add(slotNote("Slot " + slot.getSlot() + " — empty"));
			}
		}
	}

	private void rebuildRumours(HunterRumours rumours)
	{
		rumourSection.removeAll();
		if (rumours == null || !rumours.isAvailable())
		{
			rumourSection.add(wrapLabel("No Hunter Rumours data (needs the Hunter Rumours plugin).", ColorScheme.MEDIUM_GRAY_COLOR));
			return;
		}
		if (rumours.getAssignments() != null)
		{
			for (RumourAssignment a : rumours.getAssignments())
			{
				if (a == null || a.getHunter() == null)
				{
					continue;
				}
				String rumour = a.isKnown() && a.getRumour() != null ? a.getRumour().getName() : "unknown";
				JPanel row = new JPanel(new BorderLayout());
				row.setOpaque(false);
				row.setAlignmentX(Component.LEFT_ALIGNMENT);
				JLabel who = smallLabel((a.isActive() ? "▶ " : "") + a.getHunter().getName(),
					a.isActive() ? ColorScheme.BRAND_ORANGE : ColorScheme.LIGHT_GRAY_COLOR);
				row.add(who, BorderLayout.WEST);
				if (a.isKnown() && a.getRumour() != null)
				{
					row.add(linkButton(rumour, wikiSearchUrl(rumour)), BorderLayout.EAST);
				}
				else
				{
					JLabel unk = smallLabel("unknown", ColorScheme.MEDIUM_GRAY_COLOR);
					unk.setHorizontalAlignment(SwingConstants.RIGHT);
					row.add(unk, BorderLayout.EAST);
				}
				row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
				rumourSection.add(row);
			}
		}
		if (rumours.getCurrentRumour() != null)
		{
			String caught = "Caught: " + rumours.getCaught();
			if (rumours.getPity() != null && rumours.getPity().getCatchesUntilPity() > 0)
			{
				caught += " · " + rumours.getPity().getCatchesUntilPity() + " until pity";
			}
			rumourSection.add(Box.createVerticalStrut(3));
			rumourSection.add(smallLabel(caught, ColorScheme.MEDIUM_GRAY_COLOR));
		}
		if (rumours.isFinished())
		{
			rumourSection.add(smallLabel("✓ Complete — hand it in!", ColorScheme.PROGRESS_COMPLETE_COLOR));
		}
	}

	private static boolean lumbridgeEliteDone(AchievementDiaries diaries)
	{
		if (diaries == null || diaries.getRegions() == null)
		{
			return false;
		}
		for (DiaryRegion r : diaries.getRegions())
		{
			if ("lumbridge_draynor".equals(r.getKey()) || "lumbridge".equals(r.getKey()))
			{
				return r.getElite() != null && r.getElite().isComplete();
			}
		}
		return false;
	}

	/** A blocked creature as its own row: a wiki-linked button. */
	private JButton creatureButton(String name)
	{
		return linkButton("• " + name, wikiSearchUrl(name));
	}

	private JButton linkButton(String text, String url)
	{
		JButton b = new JButton(text);
		b.setFont(FontManager.getRunescapeSmallFont());
		b.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		b.setToolTipText("Open on the OSRS Wiki");
		b.setCursor(new Cursor(Cursor.HAND_CURSOR));
		b.setBorder(BorderFactory.createEmptyBorder(1, 8, 1, 2));
		b.setContentAreaFilled(false);
		b.setFocusPainted(false);
		b.setHorizontalAlignment(SwingConstants.LEFT);
		b.setAlignmentX(Component.LEFT_ALIGNMENT);
		b.setMaximumSize(new Dimension(Integer.MAX_VALUE, b.getPreferredSize().height));
		b.addActionListener(e -> LinkBrowser.browse(url));
		return b;
	}

	private static String wikiSearchUrl(String name)
	{
		try
		{
			return WIKI_SEARCH + URLEncoder.encode(name, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			return WIKI_SEARCH + name.replace(' ', '+');
		}
	}

	private JLabel slotNote(String text)
	{
		JLabel l = smallLabel(text, ColorScheme.MEDIUM_GRAY_COLOR);
		l.setBorder(BorderFactory.createEmptyBorder(1, 8, 1, 0));
		return l;
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

	/** A wrapping multi-line label: fills the panel width, breaks long text. */
	private static JLabel wrapLabel(String text, java.awt.Color color)
	{
		JLabel l = new JLabel("<html>" + text + "</html>");
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

	/** BorderLayout panel that always matches the scroll viewport's width, so
	 *  content can only grow downward (vertical scrolling, wrapped text). */
	private static class ScrollableColumn extends JPanel implements Scrollable
	{
		ScrollableColumn()
		{
			super(new BorderLayout());
			setBackground(ColorScheme.DARK_GRAY_COLOR);
		}

		@Override
		public Dimension getPreferredScrollableViewportSize()
		{
			return getPreferredSize();
		}

		@Override
		public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
		{
			return 16;
		}

		@Override
		public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
		{
			return 64;
		}

		@Override
		public boolean getScrollableTracksViewportWidth()
		{
			return true;
		}

		@Override
		public boolean getScrollableTracksViewportHeight()
		{
			return false;
		}
	}
}
