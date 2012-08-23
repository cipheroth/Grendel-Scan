/**
 * 
 */
package com.grendelscan.requester.http.dataHandling.containers;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.NotImplementedException;

import com.grendelscan.logging.Log;
import com.grendelscan.requester.http.dataHandling.data.AbstractData;
import com.grendelscan.requester.http.dataHandling.data.ByteData;
import com.grendelscan.requester.http.dataHandling.data.Data;
import com.grendelscan.requester.http.dataHandling.references.DataReference;
import com.grendelscan.requester.http.dataHandling.references.FilenameComponentDataReference;
import com.grendelscan.utils.StringUtils;

/**
 * @author david
 *
 */
public class UrlFileNameDataContainer extends AbstractData implements DataContainer<FilenameComponentDataReference>
{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	private Data name;
	private Data extension;

	/**
	 * @param parent
	 * @param reference
	 * @param transactionId
	 */
	public UrlFileNameDataContainer(DataContainer<?> parent, DataReference reference, int transactionId, String filename)
	{
		super(parent, reference, transactionId);
		setData(filename);
	}

	/* (non-Javadoc)
	 * @see com.grendelscan.requester.http.dataHandling.containers.DataContainer#getChild(com.grendelscan.requester.http.dataHandling.references.DataReference)
	 */
	@Override
	public Data getChild(FilenameComponentDataReference reference)
	{
		if (reference.isName())
			return name;
		return extension;
	}


	/* (non-Javadoc)
	 * @see com.grendelscan.requester.http.dataHandling.containers.DataContainer#replaceChild(com.grendelscan.requester.http.dataHandling.references.DataReference, com.grendelscan.requester.http.dataHandling.data.Data)
	 */
	@Override
	public void replaceChild(FilenameComponentDataReference reference, Data child)
	{
		if (reference.isName())
		{
			name = child;
		}
		else
		{
			extension = child;
		}
	}


	private void setData(String filename)
	{
		int dot = filename.lastIndexOf('.');
		if (dot < 0)
		{
			name = new ByteData(this, filename.getBytes(), new FilenameComponentDataReference(true), getTransactionId());
//			extension = new ByteData(this, new byte[0], new FilenameComponentDataReference(true), getTransactionId());
			extension = null;
		}
		else
		{
			name = new ByteData(this, filename.substring(0, dot).getBytes(), new FilenameComponentDataReference(true), getTransactionId());
			extension = new ByteData(this, filename.substring(dot + 1, dot).getBytes(), new FilenameComponentDataReference(false), getTransactionId());
		}

	}

//	/* (non-Javadoc)
//	 * @see com.grendelscan.requester.http.dataHandling.data.Data#clone(com.grendelscan.requester.http.transactions.StandardHttpTransaction)
//	 */
//	@Override
//	public UrlFileNameDataContainer clone(TransactionContainer transaction)
//	{
//		DataContainer<?> parentClone = (DataContainer<?>) DataContainerUtils.resolveReferenceChain(transaction, getParent().getReferenceChain());
//		return new UrlFileNameDataContainer(parentClone, getReference().clone(), transaction.getTransactionId(), new String(getBytes()));
//	}

	/* (non-Javadoc)
	 * @see com.grendelscan.requester.http.dataHandling.containers.DataContainer#getChildUnsafeType(com.grendelscan.requester.http.dataHandling.references.DataReference)
	 */
	@Override
	public Data getChildUnsafeType(DataReference reference)
	{
		return getChild((FilenameComponentDataReference) reference);
	}

	/* (non-Javadoc)
	 * @see com.grendelscan.requester.http.dataHandling.data.Data#writeBytes(java.io.OutputStream)
	 */
	@Override
	public void writeBytes(OutputStream out)
	{
		try
		{
			name.writeBytes(out);
			
			if (extension != null)
			{
				out.write('.');
				extension.writeBytes(out);
			}
		}
		catch (IOException e)
		{
			Log.error("Problem writing filename: " + e.toString(), e);
		}
		
	}

	/* (non-Javadoc)
	 * @see com.grendelscan.requester.http.dataHandling.containers.DataContainer#getDataChildren()
	 */
	@Override
	public Data[] getDataChildren()
	{
		return new Data[] {name, extension};
	}

	/* (non-Javadoc)
	 * @see com.grendelscan.requester.http.dataHandling.containers.DataContainer#removeChild(com.grendelscan.requester.http.dataHandling.data.Data)
	 */
	@Override
	public void removeChild(Data child)
	{
		throw new NotImplementedException("Remove child makes no sense for a file name container");
	}

	/* (non-Javadoc)
	 * @see com.grendelscan.requester.http.dataHandling.data.Data#debugString()
	 */
	@Override
	public String debugString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("UrlFileNameDataContainer\n");
		sb.append(StringUtils.indentLines(childrenDebugString(), 1));
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see com.grendelscan.requester.http.dataHandling.containers.DataContainer#childrenDebugString()
	 */
	@Override
	public String childrenDebugString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Name:\n");
		sb.append(StringUtils.indentLines(name.debugString(), 1));
		sb.append("Extension:\n");
		sb.append(StringUtils.indentLines(extension.debugString(), 1));
		return sb.toString();
	}

}
