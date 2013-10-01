package com.grendelscan.tests.testModules.informationLeakage;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.grendelscan.data.findings.Finding;
import com.grendelscan.data.findings.FindingSeverity;
import com.grendelscan.logging.Log;
import com.grendelscan.requester.TransactionSource;
import com.grendelscan.requester.http.transactions.StandardHttpTransaction;
import com.grendelscan.requester.http.transactions.UnrequestableTransaction;
import com.grendelscan.scan.InterruptedScanException;
import com.grendelscan.scan.Scan;
import com.grendelscan.tests.testModuleUtils.TestModuleGUIPath;
import com.grendelscan.tests.testModules.TestModule;
import com.grendelscan.tests.testTypes.ByHostTest;
import com.grendelscan.utils.HtmlUtils;
import com.grendelscan.utils.HttpUtils;

public class RobotsTxt extends TestModule implements ByHostTest
{

	public RobotsTxt()
	{
		requestOptions.followRedirects = true;
		requestOptions.testRedirectTransactions = true;
	}
	
	@Override
	public String getDescription()
	{
		return "Looks for a robots.txt file. If one is found, all paths are requested, and " +
				"disallowed paths (or similar directives) are documented in the scan report.";
	}

	@Override
	public TestModuleGUIPath getGUIDisplayPath()
	{
		return TestModuleGUIPath.INFORMATION_LEAKAGE;
	}

	@Override
	public String getName()
	{
		return "Robots.txt";
	}

	@Override
	public boolean isExperimental()
	{
		return false;
	}

	public void logFindings(String robotsUri, Set<String> uris)
	{
		String title = "Blocked paths in robots.txt";
		String briefDescription = "Blocked paths were discovered in robots.txt";
		String longDescription =
				"Blocked paths were discovered in robots.txt, which is primarily used for controlling search engine access. The "
						+
						"paths are listed below:<br>";
			longDescription += "<br>" + HtmlUtils.makeLink(robotsUri) + ":<br>";
		for (String path : uris)
		{
			longDescription += "&nbsp;&nbsp;&nbsp;&nbsp;" + path + "<br>";
		}
		
		String impact =
				"Robots.txt should not be used to hide sensitive areas of a website. Not only is it ineffective, it provides information "
						+
						"to potential attackers.";
		String recomendations =
				"Confirm that robots.txt is only being used to optimize search engines, and is not being used as a security "
						+
						"control.";
		String references =
				HtmlUtils.makeLink("http://www.robotstxt.org/") + "<br>"
						+ HtmlUtils.makeLink("http://en.wikipedia.org/wiki/Robots_exclusion_standard");

		Finding event =
				new Finding(null, getName(), FindingSeverity.INFO, robotsUri,
						title, briefDescription, longDescription, impact, recomendations, references);
		Scan.getInstance().getFindings().addFinding(event);
	}

	@Override
	public void testByServer(int transactionID, int testJobId) throws InterruptedScanException
	{
		StandardHttpTransaction originalTransaction = Scan.getInstance().getTransactionRecord().getTransaction(transactionID);
		handlePause_isRunning();
		try
		{
			StandardHttpTransaction robotGet = originalTransaction.cloneFullRequest(TransactionSource.MISC_TEST, testJobId);
			robotGet.getRequestWrapper().setURI("/robots.txt", true);
			robotGet.setRequestOptions(requestOptions);
			robotGet.execute();
			if (HttpUtils.fileExists(robotGet.getLogicalResponseCode()))
			{
				String robotsTxt = new String(robotGet.getResponseWrapper().getBody());
				String lines[] = robotsTxt.split("[\r\n]+");
				Set<String> uris = new HashSet<String>();
				Pattern p = Pattern.compile("(disallow|allow):\\s+(\\S+)", Pattern.CASE_INSENSITIVE);
				for (String line : lines)
				{
					Matcher m = p.matcher(line);
					if (m.find())
					{
						String directive = m.group(1).toLowerCase();
						String path = m.group(2);
						if (!directive.equals("user-agent"))
						{
							try
							{
								StandardHttpTransaction disallowGet = originalTransaction.cloneFullRequest(TransactionSource.MISC_TEST, testJobId);
								disallowGet.getRequestWrapper().setURI(path, true);
								disallowGet.setRequestOptions(requestOptions);
								disallowGet.execute();
							}
							catch (UnrequestableTransaction e)
							{
								Log.warn(getName() + " request unrequestable (" + e.toString() + ")", e);
							}
							if (isBlockDirective(directive))
							{
								uris.add(path);
							}
						}
					}
				}
				if (uris.size() > 0)
				{
					logFindings(robotGet.getRequestWrapper().getAbsoluteUriString(), uris);
				}
			}
		}
		catch (UnrequestableTransaction e)
		{
			Log.debug(getName() + " request unrequestable (" + e.toString() + ")", e);
		}

	}

	private boolean isBlockDirective(String directive)
	{
		boolean block = false;
		if (directive.equals("noindex")
				|| directive.equals("nofollow")
				|| directive.equals("noarchive")
				|| directive.equals("opreview")
				|| directive.equals("nosnippet")
				|| directive.equals("disallow"))
		{
			block = true;
		}

		return block;
	}

}