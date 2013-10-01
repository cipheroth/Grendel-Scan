package com.grendelscan.tests.testModules.spidering;

import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;

import org.apache.http.client.HttpClient;

import com.grendelscan.logging.Log;
import com.grendelscan.requester.TransactionSource;
import com.grendelscan.requester.http.factories.NonScanClientFactory;
import com.grendelscan.requester.http.transactions.NonScanHttpTransaction;
import com.grendelscan.requester.http.transactions.StandardHttpTransaction;
import com.grendelscan.scan.InterruptedScanException;
import com.grendelscan.scan.Scan;
import com.grendelscan.tests.libraries.spidering.SpiderConfig;
import com.grendelscan.tests.libraries.spidering.searchEngines.Google;
import com.grendelscan.tests.libraries.spidering.searchEngines.Live;
import com.grendelscan.tests.libraries.spidering.searchEngines.SearchEngine;
import com.grendelscan.tests.libraries.spidering.searchEngines.Yahoo;
import com.grendelscan.tests.testModuleUtils.TestModuleGUIPath;
import com.grendelscan.tests.testModuleUtils.settings.IntegerOption;
import com.grendelscan.tests.testModuleUtils.settings.MultiSelectOptionGroup;
import com.grendelscan.tests.testModuleUtils.settings.SelectableOption;
import com.grendelscan.tests.testModules.TestModule;
import com.grendelscan.tests.testTypes.ByHostTest;
import com.grendelscan.utils.StringUtils;

public class SearchEngineRecon extends TestModule implements ByHostTest
{



	private IntegerOption				delayOption;
	private SelectableOption			googleSearchOption;
	private SelectableOption			liveSearchOption;


	private IntegerOption				maxResultsOption;


	private SelectableOption			yahooSearchOption;

	public SearchEngineRecon()
	{
		addConfigurationOption(SpiderConfig.ignoredParameters);
		addConfigurationOption(SpiderConfig.spiderStyle);
		MultiSelectOptionGroup engines =
				new MultiSelectOptionGroup("Search engines", "Select which search engines to use.", null);
		liveSearchOption = new SelectableOption("Live.com", true, "Search live.com", null);
		googleSearchOption = new SelectableOption("Google.com", false, "Search google.com", null);
		yahooSearchOption = new SelectableOption("Yahoo.com", false, "Search yahoo.com", null);
		engines.addOption(liveSearchOption);
		engines.addOption(googleSearchOption);
		engines.addOption(yahooSearchOption);
		maxResultsOption =
				new IntegerOption("Max results", 500,
						"Stop searching after this number of URLs has been discovered from the search engines", null);
		delayOption =
				new IntegerOption(
						"Request delay (ms)",
						1000,
						"The minimum number of milliseconds between search engine requests. Note that each request to the search engine will result in many URLs from the target website.", null);

		addConfigurationOption(engines);
		addConfigurationOption(maxResultsOption);
		addConfigurationOption(delayOption);
	}


	@Override
	public String getDescription()
	{
		return "Using the \"site:\" query command with the targeted websites, this module will " +
				"attempt to discover content on the selected search engines. Since search " +
				"engines will index pages that other sites link to, this can technique can " +
				"reveal content that standard spidering won't discover. Obviously this is only " +
				"useful if the sites are present on the Internet.\n" +
				"\n" +
				"Be careful in decreasing the delay between search engine requests. Submitting " +
				"too many requests can result in your IP address being temporarily blocked by " +
				"the search engine.\n" +
				"\n" +
				"Live.com is the default, because they seem to have a larger database than " +
				"the other two. Also note that Yahoo.com will only return the first 1000 " +
				"records, although they can be fetched in plain text format with a single " +
				"request.";
	}

	@Override
	public TestModuleGUIPath getGUIDisplayPath()
	{
		return TestModuleGUIPath.SPIDER;
	}

	@Override
	public String getName()
	{
		return "Search engine recon";
	}


	@Override
	public boolean isExperimental()
	{
		return false;
	}

	@Override
	public void testByServer(int transactionID, int testJobId) throws InterruptedScanException
	{
		StandardHttpTransaction transaction = Scan.getInstance().getTransactionRecord().getTransaction(transactionID);
		String host = transaction.getRequestWrapper().getHost();

		HttpClient client = NonScanClientFactory.getClient();
		int totalRequests = 0;
		if (googleSearchOption.isSelected())
		{
			totalRequests = search(new Google(), host, client, totalRequests, testJobId);
		}

		if (liveSearchOption.isSelected())
		{
			totalRequests = search(new Live(), host, client, totalRequests, testJobId);
		}

		if (yahooSearchOption.isSelected())
		{
			totalRequests = search(new Yahoo(), host, client, totalRequests, testJobId);
		}
	}

	private int search(SearchEngine searchEngine, String host, HttpClient client, int currentCount, int testJobId) throws InterruptedScanException
	{
		int startLocation = 1;
		long lastRequest = 0;
		while (currentCount++ <= maxResultsOption.getValue())
		{
			handlePause_isRunning();
			String query = searchEngine.getQueryURI(host, startLocation);
			try
			{
				NonScanHttpTransaction queryTransaction = new NonScanHttpTransaction("GET", query);
				searchEngine.updateTransaction(queryTransaction);
				long pause = lastRequest - new Date().getTime() + delayOption.getValue();
				if (pause > 0)
				{
					Log.debug(getName() + " (" + getClass().getName() + ") is pausing for " + pause + " ms");
//					synchronized (this)
					{
						try
						{
							Thread.sleep(pause);
						}
						catch (InterruptedException e)
						{
							throw new InterruptedScanException(e);
						}
					}
				}
				handlePause_isRunning();
				queryTransaction.execute(client);
				lastRequest = new Date().getTime();
				String body = new String(queryTransaction.getResponseBody(), StringUtils.getDefaultCharset());
				Matcher m = searchEngine.getFindResultPattern().matcher(body);
				while (currentCount <= maxResultsOption.getValue() && m.find())
				{
					handlePause_isRunning();
					String uri = m.group(1);
					StandardHttpTransaction spiderTransaction = new StandardHttpTransaction(TransactionSource.SPIDER, testJobId);
					spiderTransaction.getRequestWrapper().setURI(uri, true);
					currentCount++;
					Scan.getInstance().getRequesterQueue().addTransaction(spiderTransaction);
				}
				if (searchEngine.isMoreResults(body))
				{
					startLocation += searchEngine.getQueryIncrement();
					continue;
				}
			}
			catch (IOException e)
			{
				Log.error("Problem sending request for search engine recon: " + e.toString(), e);
			}
			break;
		}
		return currentCount;
	}

}