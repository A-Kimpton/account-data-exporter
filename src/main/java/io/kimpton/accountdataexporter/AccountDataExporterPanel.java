package io.kimpton.accountdataexporter;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.GridLayout;
import java.io.File;
import java.time.LocalTime;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.LinkBrowser;

/** Small status panel: export destination + last export, with a Buy Me a
 *  Coffee button in the top right. */
@Slf4j
class AccountDataExporterPanel extends PluginPanel
{
	private static final String COFFEE_URL = "https://buymeacoffee.com/kimpton";

	private final JLabel lastExportLabel = new JLabel("No export yet this session.");
	private final JLabel accountLabel = new JLabel(" ");

	AccountDataExporterPanel(File exportDirectory)
	{
		super(false);
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Header: title on the left, coffee-mug support button top right.
		JPanel header = new JPanel(new BorderLayout());
		header.setOpaque(false);
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
		add(header, BorderLayout.NORTH);

		// Body: where exports go + the freshest export we wrote.
		JPanel body = new JPanel(new GridLayout(0, 1, 0, 6));
		body.setOpaque(false);
		body.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

		JLabel dirTitle = new JLabel("Exports are written to:");
		dirTitle.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		JLabel dirLabel = new JLabel("<html><body style='width:180px'>" + exportDirectory.getAbsolutePath() + "</body></html>");
		dirLabel.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
		body.add(dirTitle);
		body.add(dirLabel);

		JButton open = new JButton("Open export folder");
		open.setFocusPainted(false);
		open.addActionListener(e -> openFolder(exportDirectory));
		body.add(open);

		accountLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		lastExportLabel.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
		lastExportLabel.setHorizontalAlignment(SwingConstants.LEFT);
		body.add(accountLabel);
		body.add(lastExportLabel);

		add(body, BorderLayout.CENTER);
	}

	/** Called (on the EDT) after each snapshot write. */
	void setLastExport(String rsn)
	{
		accountLabel.setText("Account: " + rsn);
		lastExportLabel.setText("Last export: " + LocalTime.now().withNano(0));
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
