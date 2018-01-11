package finder.cloud;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class CannotDeleteRuleDlg extends Dialog
{
    Label mMsgLab;
    protected CannotDeleteRuleDlg(Shell parentShell)
    {
        super(parentShell);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected Control createDialogArea(Composite parent)
    {
        // TODO Auto-generated method stub
        mMsgLab = new Label(parent,SWT.LEFT);
        mMsgLab.setText("The root Rule cannot delete.");
        return super.createDialogArea(parent);
    }
    
    @Override
    protected int getShellStyle()
    {
        // TODO Auto-generated method stub
        return SWT.CLOSE|SWT.APPLICATION_MODAL;
    }
    
}
