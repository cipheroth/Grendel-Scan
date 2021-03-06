/**
 * 
 */
package com.grendelscan.ui.statusComposites.FindingsTable;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

import com.grendelscan.scan.Scan;
import com.grendelscan.scan.data.findings.Finding;
import com.grendelscan.ui.customControls.basic.GComposite;
import com.grendelscan.ui.customControls.basic.GTableItem;
import com.grendelscan.ui.customControls.dataTable.AbstractDataTable;

/**
 * This code was edited or generated using CloudGarden's Jigloo SWT/Swing GUI Builder, which is free for non-commercial use. If Jigloo is being used commercially (ie, by a corporation, company or
 * business for any purpose whatever) then you should purchase a license for each developer using Jigloo. Please visit www.cloudgarden.com for details. Use of Jigloo implies acceptance of these
 * licensing terms. A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
public class FindingsTable extends AbstractDataTable<FindingsTableColumns, Finding>
{

    /**
     * @param parent
     * @param style
     * @param dataProvider
     * @param comparator
     */
    public FindingsTable(final GComposite parent, final int style)
    {
        super(parent, style, Scan.getInstance().getFindings(), new FindingsTableSorter());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.grendelscan.ui.customControls.dataTable.AbstractDataTable#createColumns()
     */
    @Override
    protected void createColumns()
    {
        addColumn("ID", 50, FindingsTableColumns.ID).setLabelProvider(new ColumnLabelProvider()
        {
            @Override
            public String getText(final Object element)
            {
                Finding f = (Finding) element;
                return f.getId() + "";
            }
        });
        addColumn("Severity", 90, FindingsTableColumns.SEVERITY).setLabelProvider(new ColumnLabelProvider()
        {
            @Override
            public String getText(final Object element)
            {
                Finding f = (Finding) element;
                return f.getSeverity().toString();
            }
        });

        addColumn("Title", 200, FindingsTableColumns.TITLE).setLabelProvider(new ColumnLabelProvider()
        {
            @Override
            public String getText(final Object element)
            {
                Finding f = (Finding) element;
                return f.getTitle();
            }
        });

        addColumn("URL", 300, FindingsTableColumns.URL).setLabelProvider(new ColumnLabelProvider()
        {
            @Override
            public String getText(final Object element)
            {
                Finding f = (Finding) element;
                return f.getUrl();
            }
        });

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.grendelscan.ui.customControls.dataTable.AbstractDataTable#initGUIDone(int)
     */
    @Override
    protected int initGUIBottomControls(final int top)
    {
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.grendelscan.ui.customControls.dataTable.AbstractDataTable#initGUIStart(int)
     */
    @Override
    protected int initGUIStart(final int top)
    {
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.grendelscan.ui.customControls.dataTable.AbstractDataTable#getContextMenuHandler()
     */
    @Override
    protected MenuDetectListener makeContextMenuHandler()
    {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.grendelscan.ui.customControls.dataTable.AbstractDataTable#getMouseAdapter()
     */
    @Override
    protected MouseAdapter makeMouseAdapter()
    {

        return new MouseAdapter()
        {
            @Override
            public void mouseDoubleClick(@SuppressWarnings("unused") final MouseEvent evt)
            {
                GTableItem selection[] = tableViewer.getTable().getSelection();
                if (selection.length != 1)
                {
                    return;
                }
                int id = Integer.valueOf(selection[0].getText());
                FindingComposite.displayFinding(Scan.getInstance().getFindings().get(id));
            }
        };
    }

}
