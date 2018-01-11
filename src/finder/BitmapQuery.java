package finder;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.query.Column;
import org.eclipse.mat.query.IContextObject;
import org.eclipse.mat.query.IIconProvider;
import org.eclipse.mat.query.IQuery;
import org.eclipse.mat.query.IResult;
import org.eclipse.mat.query.IResultTree;
import org.eclipse.mat.query.ResultMetaData;
import org.eclipse.mat.query.annotations.Argument;
import org.eclipse.mat.query.annotations.Category;
import org.eclipse.mat.query.annotations.CommandName;
import org.eclipse.mat.query.annotations.Help;
import org.eclipse.mat.query.annotations.Name;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.IInstance;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.query.Icons;
import org.eclipse.mat.util.IProgressListener;

import finder.util.ReportManager;
import finder.util.UtilClass;


@CommandName("Bitmap")
@Category("Finder")
@Name("4|Bitmap")
@Help("Bitmap check")
public class BitmapQuery implements IQuery,IResultTree,IIconProvider {

	@Argument
	public ISnapshot mOpenSnapshot;

	Logger mLog = Logger.getLogger(BitmapQuery.class.toString());
	public BitmapQuery() {
		// TODO Auto-generated constructor stub
	}
	
	List<UtilClass.BitmapInfo> mBitmapLst = new ArrayList<UtilClass.BitmapInfo>();
	
	public IResult execute(IProgressListener listener) throws Exception
	{
		// TODO Auto-generated method stub
		Collection<IClass> classes = mOpenSnapshot.getClasses();
		Iterator<IClass> iter = classes.iterator();
		while(iter.hasNext()) {
			IClass classfit = iter.next();
			
        	String strNameString = classfit.getName();
        	
        	if (strNameString == null)
        	{
        		continue;
			}
        	
        	//¹ýÂË
        	if(!strNameString.equals("android.graphics.Bitmap"))
        	{
        		continue;
        	}
        	else
        	{
        		int[] objectIds = classfit.getObjectIds();
        		for (int i = 0; i < objectIds.length; i++)
        		{
        			IObject obj = mOpenSnapshot.getObject(objectIds[i]);
        			if (obj == null || !(obj instanceof IInstance))
        			{
        				continue;
        			}

        			UtilClass.BitmapInfo btInfo = UtilClass.getBitmapInfo(obj);
        			if(btInfo != null)
        			{
        				mBitmapLst.add(btInfo);
        			}

        		}
        	}
		}
		
		ReportManager.ReportMsg msg = new ReportManager.ReportMsg(ReportManager.FINDER_RPMSG_Bitmap); 
		ReportManager.getSrvInst().sendRPMsg(msg);
		return this;
	}

	public Column[] getColumns() {
		// TODO Auto-generated method stub
		return new Column[] {new Column("BitmapName", String.class),
                new Column("Width", Integer.class).noTotals(),
                new Column("Higtht", Integer.class).noTotals(),
                new Column("Size", long.class)/*,
                new Column("ARGB8888", Integer.class).noTotals()*/};
	}

	public Object getColumnValue(Object row, int columnIndex) {
		// TODO Auto-generated method stub
		UtilClass.BitmapInfo bn = (UtilClass.BitmapInfo)row;
		IObject obj = null;
		try {
			obj = mOpenSnapshot.getObject(bn.mObjID);
		} catch (SnapshotException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		switch (columnIndex) {
		case 0:
			if (obj != null) {
				return obj.getDisplayName();
			}
			break;
		case 1:
			return bn.mWidth;
		case 2:
			return bn.mHeight;
		case 3:
			return bn.mSize;
		/*case 4:
			if (bn.mHeight == -1 || bn.mWidth == -1)
			{
				return -1;
			}
			else
			{
				return (bn.mSize/(bn.mHeight*bn.mWidth)==4)?1:0;
			}*/
		default:
			return "";
		}
		return null;
	}

	public IContextObject getContext(final Object row) {
		// TODO Auto-generated method stub
        return new IContextObject()
        {
            public int getObjectId()
            {
                return ((UtilClass.BitmapInfo) row).mObjID;
            }
        };
	}

	public ResultMetaData getResultMetaData() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<?> getElements() {
		// TODO Auto-generated method stub
		return mBitmapLst;
	}

	public boolean hasChildren(Object element) {
		// TODO Auto-generated method stub
		return false;
	}

	public List<?> getChildren(Object parent) {
		// TODO Auto-generated method stub
		return null;
	}

	public URL getIcon(Object row) {
		// TODO Auto-generated method stub
		return Icons.forObject(mOpenSnapshot, ((UtilClass.BitmapInfo) row).mObjID);
		
	}

	
}
