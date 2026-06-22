// Copyright (c) 2026, Antony Kimpton. Licensed under the BSD 2-Clause License (see LICENSE).
package io.kimpton.accountdataexporter.exporters;

import io.kimpton.accountdataexporter.model.GeOffer;
import io.kimpton.accountdataexporter.model.GrandExchange;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GrandExchangeOffer;
import net.runelite.client.game.ItemManager;

/** Builds the Grand Exchange section, including a rough estimate of the gp value tied up in
 *  active offers (cash already spent plus the market value of items still to be received). */
public class GrandExchangeBuilder
{
	@Inject
	private Client client;

	@Inject
	private ItemManager itemManager;

	public GrandExchange build()
	{
		GrandExchangeOffer[] offers = client.getGrandExchangeOffers();
		boolean loaded = offers != null;

		int activeCount = 0;
		long listedValueEstimate = 0;
		long accountValueEstimate = 0;
		List<GeOffer> out = new ArrayList<>();

		if (offers != null)
		{
			for (int slot = 0; slot < offers.length; slot++)
			{
				GrandExchangeOffer offer = offers[slot];
				if (offer == null || offer.getState() == null || "EMPTY".equals(offer.getState().name()))
				{
					continue;
				}

				int itemId = offer.getItemId();
				int listedPrice = offer.getPrice();
				int marketPrice = itemId > 0 ? itemManager.getItemPrice(itemId) : 0;
				int totalQuantity = offer.getTotalQuantity();
				int completedQuantity = offer.getQuantitySold();
				int remainingQuantity = Math.max(0, totalQuantity - completedQuantity);
				int spent = offer.getSpent();
				String state = offer.getState().name();

				long offerListed = (long) listedPrice * totalQuantity;
				long offerAccountValue = estimateAccountValue(
					state, marketPrice, listedPrice, totalQuantity, completedQuantity, remainingQuantity, spent);

				activeCount++;
				listedValueEstimate += offerListed;
				accountValueEstimate += offerAccountValue;

				out.add(new GeOffer(
					slot, state, itemId,
					itemId > 0 ? itemManager.getItemComposition(itemId).getName() : "",
					listedPrice, marketPrice, totalQuantity, completedQuantity, remainingQuantity, spent,
					offerListed, offerAccountValue));
			}
		}

		return new GrandExchange(loaded, activeCount, listedValueEstimate, accountValueEstimate, out);
	}

	private long estimateAccountValue(
		String state, int marketPrice, int listedPrice,
		int totalQuantity, int completedQuantity, int remainingQuantity, int spent)
	{
		if (state != null && state.contains("SELL"))
		{
			return (long) spent + ((long) marketPrice * remainingQuantity);
		}
		if (state != null && state.contains("BUY"))
		{
			return ((long) marketPrice * completedQuantity) + ((long) listedPrice * remainingQuantity);
		}
		return (long) marketPrice * totalQuantity;
	}
}
