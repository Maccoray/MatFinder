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

public class AddSameBytesRuleDlg extends Dialog
{
    @Override
	protected boolean isResizable() {
		// TODO Auto-generated method stub
		return true;
	}

	Text mRL,mDL,mMaxNL,mMinNL,mMCL,mMSL;
    boolean mbGC = false;
    private CloudConfigCenter.AnalyzerConfig.SameBytesRule mSameBytesRule = null;
    
    protected AddSameBytesRuleDlg(Shell parentShell)
    {
        super(parentShell);
        // TODO Auto-generated constructor stub
    }
    
    @Override
    protected Control createDialogArea(Composite parent)
    {
        // TODO Auto-generated method stub
        parent.getShell().setText("Add an Class rule");
        GridLayout rootLayout = new GridLayout();
        rootLayout.numColumns = 2;
        parent.setLayout(rootLayout);

        Label ruleNameLab = new Label(parent,SWT.LEFT);
        ruleNameLab.setText("RuleName:");
        mRL = new Text(parent, SWT.LEFT|SWT.SINGLE);
        
        Label devNameLab = new Label(parent,SWT.LEFT);
        devNameLab.setText("DeviceName:");
        mDL = new Text(parent, SWT.LEFT|SWT.SINGLE);
        
        Label maxSizeLab = new Label(parent,SWT.LEFT);
        maxSizeLab.setText("MaxSize");
        mMaxNL = new Text(parent, SWT.LEFT|SWT.SINGLE);
        mMaxNL.setText("0000");
        
        Label minSizeLab = new Label(parent,SWT.LEFT);
        minSizeLab.setText("MinSize");
        mMinNL = new Text(parent, SWT.LEFT|SWT.SINGLE);
        mMinNL.setText("0000");
        
        Label maxCountLab = new Label(parent,SWT.LEFT);
        maxCountLab.setText("RepTime<");
        mMCL = new Text(parent, SWT.LEFT|SWT.SINGLE);
        mMCL.setText("0000");
        
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
            	mSameBytesRule = new CloudConfigCenter.AnalyzerConfig.SameBytesRule();
                
            	mSameBytesRule.strRuleName = mRL.getText();
            	mSameBytesRule.strDevName = mDL.getText();
            	mSameBytesRule.lMaxSize = Long.valueOf(mMaxNL.getText());
            	mSameBytesRule.lMinSize = Long.valueOf(mMinNL.getText());
                mSameBytesRule.iRepTimes = Integer.valueOf(mMCL.getText());
                mSameBytesRule.bShowGC = mbGC;
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
        return new Point(220, 200);
    }

    @Override
    protected int getShellStyle()
    {
        // TODO Auto-generated method stub
        return SWT.CLOSE|SWT.APPLICATION_MODAL|SWT.RESIZE;
    }
    
    public CloudConfigCenter.AnalyzerConfig.SameBytesRule getSameBytesRule()
    {
        return mSameBytesRule;
    }
}
