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

public class AddBitmapRuleDlg extends Dialog
{
    Text mRL,mDL,mWL,mHL;
    boolean mbGC = false;
    private CloudConfigCenter.AnalyzerConfig.BitmapRule mbtRule = null;
    
    protected AddBitmapRuleDlg(Shell parentShell)
    {
        super(parentShell);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected Control createDialogArea(Composite parent)
    {
        // TODO Auto-generated method stub
        parent.getShell().setText("Add an Bitmap rule");;
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
        widthLab.setText("BitmapWidth<");
        mWL = new Text(parent, SWT.LEFT|SWT.SINGLE);
        mWL.setText("0000");
        
        Label heightLab = new Label(parent,SWT.LEFT);
        heightLab.setText("BitmapHeight<");
        mHL = new Text(parent, SWT.LEFT|SWT.SINGLE);
        mHL.setText("0000");

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
                mbtRule = new CloudConfigCenter.AnalyzerConfig.BitmapRule();
                
                mbtRule.strRuleName = mRL.getText();
                mbtRule.strDevName = mDL.getText();
                mbtRule.iMaxWidth = Integer.valueOf(mWL.getText());
                mbtRule.iMaxHeight = Integer.valueOf(mHL.getText());
                mbtRule.bShowGc = mbGC;
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
    
    public CloudConfigCenter.AnalyzerConfig.BitmapRule getBitmapRule()
    {
        return mbtRule;
    }
}
