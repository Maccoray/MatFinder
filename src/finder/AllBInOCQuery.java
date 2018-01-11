package finder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.query.Column;
import org.eclipse.mat.query.IContextObject;
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
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.util.IProgressListener;

import finder.util.ReportManager;
import finder.util.UtilClass;

@CommandName("FinderABIOCache")
@Category("Finder")
@Name("9|All Bitmap in one CACHE")
@Help("All bitmap should be in caches which developer master know.")
public class AllBInOCQuery implements IQuery,IResultTree
{
    @Argument
    public ISnapshot mOpenSnapshot;
    
    @Argument(isMandatory = true)
    @Help("Bitmap caches name.InputMethodManager is needed for android.")
    public List<String> filterNodeName = Arrays.asList( //
                    new String[] {"android.view.inputmethod.InputMethodManager",
                            "android.view.View","android.graphics.Paint",
                            "android.content.res.Resources", });
    
    List<UtilClass.BitmapInfo> mBitmapLst = new ArrayList<UtilClass.BitmapInfo>();
    
    public AllBInOCQuery()
    {
        // TODO Auto-generated constructor stub
    }

    @Override
    public IResult execute(IProgressListener listener) throws Exception
    {
        // TODO Auto-generated method stub
        Collection<IClass> bitmapClasses = mOpenSnapshot.getClassesByName("android.graphics.Bitmap", false);
        if(bitmapClasses.size() == 0)
        {
            System.out.println("AllBInOCQuery:No android.graphics.Bitmap has been loaded.");
            return null;
        }
        //ArrayInt result = new ArrayInt();
        
        for(IClass cls:bitmapClasses)
        {
            //包含所有过滤字的子类
            List<UtilClass.SnapshotClassInfo.SnapshotObjInfo> noCachebitmapInfos = UtilClass.classNoHaveGCNode(cls,filterNodeName,true,listener);
            for(UtilClass.SnapshotClassInfo.SnapshotObjInfo bt:noCachebitmapInfos)
            {
                //违规存放的图片
                //result.add(bt.iID);
                UtilClass.BitmapInfo btInfo = UtilClass.getBitmapInfo(mOpenSnapshot.getObject(bt.iID));
                if(btInfo != null)
                {
                    mBitmapLst.add(btInfo);
                }
            }
        }
        
        ReportManager.ReportMsg msg = new ReportManager.ReportMsg(ReportManager.FINDER_RPMSG_AllBInOC); 
        ReportManager.getSrvInst().sendRPMsg(msg);
        
        //return new ObjectListResult.Outbound(mOpenSnapshot, result.toArray());
        return this;
    }

    @Override
    public Object getColumnValue(Object row, int columnIndex)
    {
        // TODO Auto-generated method stub
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
        default:
            return "";
        }
        return null;
    }

    @Override
    public Column[] getColumns()
    {
        // TODO Auto-generated method stub
        return new Column[] {new Column("Bitmap", String.class),
                new Column("Width", Integer.class).noTotals(),
                new Column("Higtht", Integer.class).noTotals(),
                new Column("AllocationSize", long.class)};
    }

    @Override
    public IContextObject getContext(final Object row)
    {
        // TODO Auto-generated method stub
        return new IContextObject()
        {
            public int getObjectId()
            {
                return ((UtilClass.BitmapInfo) row).mObjID;
            }
        };
    }

    @Override
    public ResultMetaData getResultMetaData()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<?> getChildren(Object parent)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<?> getElements()
    {
        // TODO Auto-generated method stub
        return mBitmapLst;
    }

    @Override
    public boolean hasChildren(Object element)
    {
        // TODO Auto-generated method stub
        return false;
    }

}
