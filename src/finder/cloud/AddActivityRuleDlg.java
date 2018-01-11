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


public class AddActivityRuleDlg extends Dialog
{
    Text mRL,mAL,mDL,mCL;
    boolean mbGC = false;
    boolean mbAttributes = false;
    //mbWhite = false;
    
    private CloudConfigCenter.AnalyzerConfig.ActivityRule mActivityRule = null;    
    
    public AddActivityRuleDlg (Shell parent) {
            super(parent); 
    }

    @Override
    protected Control createDialogArea(Composite parent)
    {
        // TODO Auto-generated method stub
        parent.getShell().setText("Add an activity rule");;
        GridLayout rootLayout = new GridLayout();
        rootLayout.numColumns = 2;
        parent.setLayout(rootLayout);

        Label ruleNameLab = new Label(parent,SWT.LEFT);
        ruleNameLab.setText("RuleName:");
        mRL = new Text(parent, SWT.LEFT|SWT.SINGLE);
        
        Label activityNameLab = new Label(parent,SWT.LEFT);
        activityNameLab.setText("ActivityName:");
        mAL = new Text(parent, SWT.LEFT|SWT.SINGLE);
        
        Label devNameLab = new Label(parent,SWT.LEFT);
        devNameLab.setText("DeviceName:");
        mDL = new Text(parent, SWT.LEFT|SWT.SINGLE);
        
        Label countLab = new Label(parent,SWT.LEFT);
        countLab.setText("MaxCount:");
        mCL = new Text(parent, SWT.LEFT|SWT.SINGLE);
        mCL.setText("00");
        
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
        
        Label needShowAttributes = new Label(parent,SWT.LEFT);
        needShowAttributes.setText("needAttributes:");
        
        Combo rAttriCombo = new Combo(parent,SWT.READ_ONLY);
        rAttriCombo.setItems(new String[] {"False","True"});
        rAttriCombo.setText(rGCCombo.getItem(0));
        
        rAttriCombo.addSelectionListener (new SelectionAdapter ()
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
                mActivityRule = new CloudConfigCenter.AnalyzerConfig.ActivityRule();
                mActivityRule.activityName = mAL.getText();
                mActivityRule.strRuleName = mRL.getText();
                mActivityRule.strDevName = mDL.getText();
                mActivityRule.iMaxCount = Integer.valueOf(mCL.getText());
                mActivityRule.bShowGc = mbGC;
                mActivityRule.bShowAttributes = mbAttributes;
                close();
            }
        });
        
        
        return parent;
    }

    public CloudConfigCenter.AnalyzerConfig.ActivityRule getActivityRule()
    {
        return mActivityRule;
    }
    
    @Override
    protected Point getInitialSize()
    {
        // TODO Auto-generated method stub
        return new Point(180, 230);
    }

    @Override
    protected int getShellStyle()
    {
        // TODO Auto-generated method stub
        return SWT.CLOSE|SWT.APPLICATION_MODAL|SWT.RESIZE;
    }

    @Override
    protected Control createButtonBar(Composite parent)
    {
        // TODO Auto-generated method stub
        return null;
    }
}
