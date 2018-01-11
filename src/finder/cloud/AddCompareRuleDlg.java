package finder.cloud;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import finder.util.CloudConfigCenter;

public class AddCompareRuleDlg extends Dialog
{
    Text mRL,mDL,mCNL,mMCL,mMSL;
    boolean mbGC = false;
    private CloudConfigCenter.AnalyzerConfig.CompareRule mCmpRule = null;
    
    protected AddCompareRuleDlg(Shell parentShell)
    {
        super(parentShell);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected Control createDialogArea(Composite parent)
    {
        // TODO Auto-generated method stub
        parent.getShell().setText("Add an Cmpare rule");
        GridLayout rootLayout = new GridLayout();
        rootLayout.numColumns = 2;
        parent.setLayout(rootLayout);

        Label ruleNameLab = new Label(parent,SWT.LEFT);
        ruleNameLab.setText("RuleName:");
        mRL = new Text(parent, SWT.LEFT|SWT.SINGLE);
        
        Label devNameLab = new Label(parent,SWT.LEFT);
        devNameLab.setText("DeviceName:");
        mDL = new Text(parent, SWT.LEFT|SWT.SINGLE);
        
        Label clsNameLab = new Label(parent,SWT.LEFT);
        clsNameLab.setText("ClassName");
        mCNL = new Text(parent, SWT.LEFT|SWT.SINGLE);
        
        Label maxCountLab = new Label(parent,SWT.LEFT);
        maxCountLab.setText("IncCount<");
        mMCL = new Text(parent, SWT.LEFT|SWT.SINGLE);
        mMCL.setText("0000");

        Label maxSizeLab = new Label(parent,SWT.LEFT);
        maxSizeLab.setText("IncHeapSize(kb)<");
        mMSL = new Text(parent, SWT.LEFT|SWT.SINGLE);
        mMSL.setText("00000000");
        
        Label needGCPathLab = new Label(parent,SWT.LEFT);
        needGCPathLab.setText("needGCPath:");
        
        Combo rGCCombo = new Combo(parent,SWT.READ_ONLY);
        rGCCombo.setItems(new String[] {"False","True"});
        rGCCombo.setText(rGCCombo.getItem(0));
        
        rGCCombo.addSelectionListener (new SelectionAdapter ()
        {
            public void widgetSelected (SelectionEvent event)
            {
                Combo selComboItem = (Combo)event.widget;
                String selTextString = selComboItem.getText();
                mbGC = Boolean.valueOf(selTextString);
            }
        });
        
        Button button = new Button(parent, SWT.PUSH);
        button.setLayoutData(new GridData(SWT.END, SWT.CENTER, false,
                false));
        button.setText("OK");
        button.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mCmpRule = new CloudConfigCenter.AnalyzerConfig.CompareRule();
                
                mCmpRule.strRuleName = mRL.getText();
                mCmpRule.strDevName = mDL.getText();
                mCmpRule.strClassName = mCNL.getText();
                mCmpRule.iMaxIncCount = Integer.valueOf(mMCL.getText());
                mCmpRule.lMaxIncSize = Long.valueOf(mMSL.getText());
                mCmpRule.bShowGc = mbGC;
                close();
            }
        });
        return parent;
    }

    @Override
    protected Control createButtonBar(Composite parent)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Point getInitialSize()
    {
        // TODO Auto-generated method stub
        return new Point(180, 200);
    }

    @Override
    protected int getShellStyle()
    {
        // TODO Auto-generated method stub
        return SWT.CLOSE|SWT.APPLICATION_MODAL;
    }
    
    public CloudConfigCenter.AnalyzerConfig.CompareRule getCompareRule()
    {
        return mCmpRule;
    }
}
