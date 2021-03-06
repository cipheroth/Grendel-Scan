package com.grendelscan.ui.http.transactionDisplay;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.grendelscan.commons.StringUtils;
import com.grendelscan.commons.http.HttpConstants;
import com.grendelscan.commons.http.HttpFormatException;
import com.grendelscan.commons.http.URIStringUtils;
import com.grendelscan.commons.http.transactions.StandardHttpTransaction;
import com.grendelscan.commons.http.transactions.TransactionSource;
import com.grendelscan.commons.http.wrappers.HttpRequestWrapper;
import com.grendelscan.ui.GuiUtils;
import com.grendelscan.ui.customControls.basic.GComposite;
import com.grendelscan.ui.customControls.basic.GGroup;
import com.grendelscan.ui.customControls.basic.GLabel;
import com.grendelscan.ui.customControls.basic.GText;
import com.grendelscan.ui.http.transactionDisplay.parsedEntityComposites.UrlEncodedViewComposite;

/**
 * This code was edited or generated using CloudGarden's Jigloo SWT/Swing GUI Builder, which is free for non-commercial use. If Jigloo is being used commercially (ie, by a corporation, company or
 * business for any purpose whatever) then you should purchase a license for each developer using Jigloo. Please visit www.cloudgarden.com for details. Use of Jigloo implies acceptance of these
 * licensing terms. A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
public class ParsedRequestComposite extends ScrolledComposite
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ParsedRequestComposite.class);

    private GLabel methodLabel;
    private GLabel urlLabel;
    private ParsedBodyComposite requestBodyComposite;
    private GGroup requestBodyGroup;
    private ParsedHttpHeaderComposite httpRequestHeaderList;
    private GGroup httpHeadersGroup;
    private UrlEncodedViewComposite urlQueryParameterList;
    private GGroup urlQueryParameterGroup;
    private GText httpVersionTextBox;
    private GText urlTextBox;
    private GText methodTextBox;
    private GLabel httpVersionLabel;
    private final boolean editable;
    private GComposite requestLineComposite;
    protected SashForm mainSash;
    protected static int[] weights;

    static private Pattern httpVersionPattern = Pattern.compile("^(\\w+)/(\\d+)\\.(\\d+)$");

    public ParsedRequestComposite(final com.grendelscan.ui.customControls.basic.GComposite parent, final int style, final boolean editable)
    {
        super(parent, style | SWT.V_SCROLL | SWT.H_SCROLL);
        this.editable = editable;

        initGUI();
    }

    public void clearData()
    {
        httpVersionTextBox.setText("");
        urlTextBox.setText("");
        methodTextBox.setText("");
        httpRequestHeaderList.clearData();
        urlQueryParameterList.clearData();
        requestBodyComposite.clearData();
    }

    public byte[] getBody()
    {
        return requestBodyComposite.getBody();
    }

    public byte[] getBytes()
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try
        {
            out.write(getMethod().getBytes());
            out.write(' ');
            out.write(getUri().getBytes());
            out.write(' ');
            out.write(getHttpVersion().getBytes());
            out.write(HttpConstants.CRLF_BYTES);
            for (Header header : getHeaders())
            {
                out.write(header.getName().getBytes());
                out.write(':');
                out.write(' ');
                out.write(header.getValue().getBytes());
                out.write(HttpConstants.CRLF_BYTES);
            }
            out.write(HttpConstants.CRLF_BYTES);
            out.write(getBody());
        }
        catch (IOException e)
        {
            LOGGER.error("Problem getting bytes for request: " + e.toString(), e);
        }
        return out.toByteArray();
    }

    public Header[] getHeaders()
    {
        NameValuePair[] rawHeaders = httpRequestHeaderList.getNameValuePairs();
        Header[] headers = new Header[rawHeaders.length];
        int index = 0;
        for (NameValuePair rawHeader : rawHeaders)
        {
            headers[index++] = new BasicHeader(rawHeader.getName(), rawHeader.getValue());
        }
        return headers;
    }

    public String getHttpVersion()
    {
        return httpVersionTextBox.getText();
    }

    public String getMethod()
    {
        return methodTextBox.getText();
    }

    public String getUri()
    {
        String uri = urlTextBox.getText();

        if (urlQueryParameterList.getItemCount() > 0)
        {
            uri += "?" + URIStringUtils.urlEncode(urlQueryParameterList.getNameValuePairs());
        }
        return uri;
    }

    private void initGUI()
    {
        addDisposeListener(new DisposeListener()
        {
            @Override
            public void widgetDisposed(@SuppressWarnings("unused") final DisposeEvent arg0)
            {
                weights = mainSash.getWeights();
            }
        });
        FillLayout thisLayout = new FillLayout(org.eclipse.swt.SWT.HORIZONTAL);
        setLayout(thisLayout);
        setExpandHorizontal(true);
        setExpandVertical(true);
        mainSash = new SashForm(this, SWT.VERTICAL | SWT.V_SCROLL | SWT.NONE);
        mainSash.setBackground(GuiUtils.getColor(255, 255, 128));

        setContent(mainSash);
        {
            requestLineComposite = new GComposite(mainSash, SWT.NONE);
            FormLayout requestLineCompositeLayout = new FormLayout();
            requestLineComposite.setLayout(requestLineCompositeLayout);
            {
                methodLabel = new GLabel(requestLineComposite, SWT.NONE);
                FormData methodLabelLData = new FormData();
                methodLabelLData.width = 58;
                methodLabelLData.height = 15;
                methodLabelLData.left = new FormAttachment(0, 1000, 5);
                methodLabelLData.top = new FormAttachment(0, 1000, 0);
                methodLabel.setLayoutData(methodLabelLData);
                methodLabel.setText("Method");
            }
            {
                FormData methodTextBoxLData = new FormData();
                methodTextBoxLData.width = 71;
                methodTextBoxLData.height = 19;
                methodTextBoxLData.left = new FormAttachment(0, 1000, 5);
                methodTextBoxLData.top = new FormAttachment(0, 1000, 15);
                methodTextBox = new GText(requestLineComposite, SWT.BORDER);
                methodTextBox.setEditable(editable);
                methodTextBox.setLayoutData(methodTextBoxLData);
            }
            {
                urlLabel = new GLabel(requestLineComposite, SWT.NONE);
                FormData urlLabelLData = new FormData();
                urlLabelLData.width = 50;
                urlLabelLData.height = 15;
                urlLabelLData.left = new FormAttachment(0, 1000, 115);
                urlLabelLData.top = new FormAttachment(0, 1000, 0);
                urlLabel.setLayoutData(urlLabelLData);
                urlLabel.setText("URL");
            }

            {
                FormData urlTextBoxLData = new FormData();
                urlTextBoxLData.width = 537;
                urlTextBoxLData.height = 19;
                urlTextBoxLData.left = new FormAttachment(0, 1000, 115);
                urlTextBoxLData.top = new FormAttachment(0, 1000, 15);
                urlTextBoxLData.right = new FormAttachment(1000, 1000, -111);
                urlTextBox = new GText(requestLineComposite, SWT.BORDER);
                urlTextBox.setEditable(editable);
                urlTextBox.setLayoutData(urlTextBoxLData);
            }
            {
                httpVersionLabel = new GLabel(requestLineComposite, SWT.NONE);
                FormData httpVersionLabelLData = new FormData();
                httpVersionLabelLData.width = 93;
                httpVersionLabelLData.height = 15;
                httpVersionLabelLData.top = new FormAttachment(0, 1000, 0);
                httpVersionLabelLData.right = new FormAttachment(1000, 1000, 0);
                httpVersionLabel.setLayoutData(httpVersionLabelLData);
                httpVersionLabel.setText("HTTP Version");
            }
            {
                FormData httpVersionTextBoxLData = new FormData();
                httpVersionTextBoxLData.width = 75;
                httpVersionTextBoxLData.height = 19;
                httpVersionTextBoxLData.top = new FormAttachment(0, 1000, 15);
                httpVersionTextBoxLData.right = new FormAttachment(1000, 1000, -10);
                httpVersionTextBox = new GText(requestLineComposite, SWT.BORDER);
                httpVersionTextBox.setEditable(editable);
                httpVersionTextBox.setLayoutData(httpVersionTextBoxLData);
            }
            {
                urlQueryParameterGroup = new GGroup(requestLineComposite, SWT.NONE);
                FillLayout urlQueryParameterGroupLayout = new FillLayout(org.eclipse.swt.SWT.HORIZONTAL);
                urlQueryParameterGroup.setLayout(urlQueryParameterGroupLayout);
                FormData urlQueryParameterGroupLData = new FormData();
                urlQueryParameterGroupLData.width = 545;
                urlQueryParameterGroupLData.height = 100;
                urlQueryParameterGroupLData.left = new FormAttachment(0, 1000, 115);
                urlQueryParameterGroupLData.top = new FormAttachment(0, 1000, 40);
                urlQueryParameterGroupLData.right = new FormAttachment(1000, 1000, -109);
                urlQueryParameterGroupLData.bottom = new FormAttachment(1000, 1000, 0);
                urlQueryParameterGroup.setLayoutData(urlQueryParameterGroupLData);
                urlQueryParameterGroup.setText("URL query parameters");
                {
                    urlQueryParameterList = new UrlEncodedViewComposite(urlQueryParameterGroup, SWT.NONE, 100, editable);
                }
            }
        }

        {
            httpHeadersGroup = new GGroup(mainSash, SWT.NONE);
            FillLayout httpHeadersGroupLayout = new FillLayout(org.eclipse.swt.SWT.HORIZONTAL);
            httpHeadersGroup.setLayout(httpHeadersGroupLayout);
            FormData httpHeadersGroupLData = new FormData();
            httpHeadersGroupLData.width = 742;
            httpHeadersGroupLData.height = 113;
            httpHeadersGroupLData.left = new FormAttachment(0, 1000, 12);
            httpHeadersGroupLData.top = new FormAttachment(213, 1000, 0);
            httpHeadersGroupLData.right = new FormAttachment(1000, 1000, -8);
            httpHeadersGroupLData.bottom = new FormAttachment(496, 1000, 0);
            httpHeadersGroup.setLayoutData(httpHeadersGroupLData);
            httpHeadersGroup.setText("HTTP headers");
            {
                httpRequestHeaderList = new ParsedHttpHeaderComposite(httpHeadersGroup, SWT.NONE, 200, editable, true);
            }
        }

        {
            requestBodyGroup = new GGroup(mainSash, SWT.NONE);
            FillLayout requestBodyGroupLayout = new FillLayout(org.eclipse.swt.SWT.HORIZONTAL);
            requestBodyGroup.setLayout(requestBodyGroupLayout);
            FormData requestBodyGroupLData = new FormData();
            requestBodyGroupLData.width = 751;
            requestBodyGroupLData.height = 295;
            requestBodyGroup.setLayoutData(requestBodyGroupLData);
            requestBodyGroup.setText("Request body");

            requestBodyComposite = new ParsedBodyComposite(requestBodyGroup, SWT.NONE, editable);
        }

        if (weights == null)
        {
            weights = new int[] { 60, 100, 200 };
        }
        mainSash.setWeights(weights);

        this.layout();

    }

    public StandardHttpTransaction makeHttpRequest(final TransactionSource source) throws HttpFormatException
    {
        String uri = getUri();

        // String host;
        // String scheme;
        // int port;
        // try
        // {
        // host = URIStringUtils.getHost(uri);
        // if (host.equals(""))
        // {
        // host = httpRequestHeaderList.getFirstValue("Host", true);
        // if (host == null)
        // {
        // throw new HttpFormatException("Host header is required with a relative URL.");
        // }
        // }
        // scheme = URIStringUtils.getScheme(uri).toLowerCase();
        // if (scheme.equals(""))
        // {
        // scheme = "http";
        // }
        //
        // port = URIStringUtils.getPort(uri);
        // }
        // catch (URISyntaxException e)
        // {
        // IllegalStateException ise = new IllegalStateException("Really, really weird problem with uri parsing", e);
        // LOGGER.error(e.toString(), e);
        // throw ise;
        // }
        // if (port == 0)
        // {
        // if (scheme.equals("https"))
        // {
        // port = 443;
        // }
        // else
        // {
        // port = 80;
        // }
        // }

        String version = getHttpVersion().toUpperCase();
        Matcher m = httpVersionPattern.matcher(version);
        int major, minor;
        String protocol = "";
        if (m.find())
        {
            protocol = m.group(1);
            major = Integer.valueOf(m.group(2));
            minor = Integer.valueOf(m.group(3));
        }
        else
        {
            throw new HttpFormatException("Invalid protocol version format. It should look something like \"HTTP/1.0\".");
        }

        byte[] body = getBody();

        StandardHttpTransaction transaction = new StandardHttpTransaction(source, -1);
        transaction.getRequestWrapper().setURI(uri, true);
        transaction.getRequestWrapper().setBody(body);
        transaction.getRequestWrapper().setVersion(protocol, major, minor);
        transaction.getRequestWrapper().setMethod(getMethod());
        // transaction.getRequestWrapper().setNetworkHost(host);
        // transaction.getRequestWrapper().setNetworkPort(port);

        for (Header header : getHeaders())
        {
            transaction.getRequestWrapper().getHeaders().addHeader(header);
        }

        return transaction;
    }

    public void switchToParsed()
    {
        requestBodyComposite.switchToParsed();
    }

    @Override
    public String toString()
    {
        return new String(getBytes());
    }

    public void updateData(final HttpRequestWrapper request) throws URISyntaxException
    {
        if (request == null)
        {
            clearData();
            return;
        }
        httpRequestHeaderList.updateHeaderEncodedData(request.getHeaders().getReadOnlyHeaderArray());
        methodTextBox.setText(request.getMethod());
        urlTextBox.setText(URIStringUtils.getFileUri(request.getURI()));
        httpVersionTextBox.setText(request.getVersion().toString());
        urlQueryParameterList.updateData(URIStringUtils.getQuery(request.getURI()).getBytes(StringUtils.getDefaultCharset()));
        requestBodyComposite.updateData(request.getBody(), request.getHeaders().getMimeType());
        this.layout();
    }
}
