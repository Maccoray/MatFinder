package finder.cloud;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import finder.util.CloudConfigCenter;

public class AddGCWhiteNodeDlg extends Dialog
{
    Text mRL,mDL,mClassL,mValue;
    
    private CloudConfigCenter.AnalyzerConfig.GCWhiteNode mGCWN = null;
    
    protected AddGCWhiteNodeDlg(Shell parentShell)
    {
        super(parentShell);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected Control createDialogArea(Composite parent)
    {
        // TODO Auto-generated method stub
        parent.getShell().setText("Add a GC-White-Node");;
        GridLayout rootLayout = new GridLayout();
        rootLayout.numColumns = 2;
        parent.setLayout(rootLayout);
        
        Label ruleNameLab = new Label(parent,SWT.LEFT);
        ruleNameLab.setText("RuleName:");
        mRL = new Text(parent, SWT.LEFT|SWT.SINGLE);
        
        Label devNameLab = new Label(parent,SWT.LEFT);
        devNameLab.setText("DeviceName:");
        mDL = new Text(parent, SWT.LEFT|SWT.SINGLE);
        
        Label widthLab = new Label(parent,SWT.LEFT);
        widthLab.setText("GCNodeClassName:");
        mClassL = new Text(parent, SWT.LEFT|SWT.SINGLE);
        
        Label valueLab = new Label(parent,SWT.LEFT);
        valueLab.setText("GCNodeValue:");
        mValue = new Text(parent, SWT.LEFT|SWT.SINGLE);
        
        Button button = new Button(parent, SWT.PUSH);
        button.setLayoutData(new GridData(SWT.END, SWT.CENTER, false,
                false));
        
        button.setText("OK");
        button.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mGCWN = new CloudConfigCenter.AnalyzerConfig.GCWhiteNode();
                
                mGCWN.strRuleName = mRL.getText();
                mGCWN.strDevName = mDL.getText();
                mGCWN.strNodeClassName = mClassL.getText();
                mGCWN.strNodeValue = mValue.getText();
                
                close();
            }
        });
        
        return parent;
    }
    
    @Override
    protected Point getInitialSize()
    {
        // TODO Auto-generated method stub
        return new Point(250, 150);
    }
    
    @Override
    protected int getShellStyle()
    {
        // TODO Auto-generated method stub
        return SWT.CLOSE|SWT.APPLICATION_MODAL;
    }
    
    @Override
    protected Control createButtonBar(Composite parent)
    {
        // TODO Auto-generated method stub
        return null;
    }
    public CloudConfigCenter.AnalyzerConfig.GCWhiteNode getGCWhiteNode()
    {
        return mGCWN;
    }

}
