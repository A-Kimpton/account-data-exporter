package io.kimpton.accountdataexporter;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class AccountDataExporterTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(AccountDataExporterPlugin.class);
		RuneLite.main(args);
	}
}
