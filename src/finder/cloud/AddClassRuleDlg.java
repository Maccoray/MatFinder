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

public class AddClassRuleDlg extends Dialog
{

    @Override
	protected boolean isResizable() {
		// TODO Auto-generated method stub
		return true;
	}

	Text mRL,mDL,mCNL,mMCL,mMSL;
    boolean mbGC = false,mbAttributes=false;
    private CloudConfigCenter.AnalyzerConfig.ClassRule mClassRule = null;
    
    protected AddClassRuleDlg(Shell parentShell)
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
        
        Label clsNameLab = new Label(parent,SWT.LEFT);
        clsNameLab.setText("ClassName");
        mCNL = new Text(parent, SWT.LEFT|SWT.SINGLE);
        
        Label maxCountLab = new Label(parent,SWT.LEFT);
        maxCountLab.setText("Count<");
        mMCL = new Text(parent, SWT.LEFT|SWT.SINGLE);
        mMCL.setText("0000");

        Label maxSizeLab = new Label(parent,SWT.LEFT);
        maxSizeLab.setText("HeapSize(kb)<");
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
        
        Label needAttributeLab = new Label(parent,SWT.LEFT);
        needAttributeLab.setText("needAttributes:");
        
        Combo rAttrCombo = new Combo(parent,SWT.READ_ONLY);
        rAttrCombo.setItems(new String[] {"False","True"});
        rAttrCombo.setText(rAttrCombo.getItem(0));
        
        rAttrCombo.addSelectionListener (new SelectionAdapter ()
        {
            public void widgetSelected (SelectionEvent event)
            {
                Combo selComboItem = (Combo)event.widget;
                String selTextString = selComboItem.getText();
                mbAttributes = Boolean.valueOf(selTextString);
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
                mClassRule = new CloudConfigCenter.AnalyzerConfig.ClassRule();
                
                mClassRule.strRuleName = mRL.getText();
                mClassRule.strDevName = mDL.getText();
                mClassRule.strClassName = mCNL.getText();
                mClassRule.iMaxCount = Integer.valueOf(mMCL.getText());
                mClassRule.lMaxSize = Long.valueOf(mMSL.getText());
                mClassRule.bShowGc = mbGC;
                mClassRule.bShowAttributes = mbAttributes;
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
    
    public CloudConfigCenter.AnalyzerConfig.ClassRule getClassRule()
    {
        return mClassRule;
    }
}
