package finder;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.mat.query.Column;
import org.eclipse.mat.query.IContextObject;
import org.eclipse.mat.query.IQuery;
import org.eclipse.mat.query.IResult;
import org.eclipse.mat.query.IResultTree;
import org.eclipse.mat.query.ResultMetaData;
import org.eclipse.mat.query.annotations.Category;
import org.eclipse.mat.query.annotations.CommandName;
import org.eclipse.mat.query.annotations.Help;
import org.eclipse.mat.query.annotations.Name;
import org.eclipse.mat.util.IProgressListener;

import finder.util.ReportManager;

@CommandName("AboutFinder")
@Category("Finder")
@Name("11|About Finder")
@Help("About")
public class AboutQuery implements IQuery,IResultTree {

    static class ProductionInfo
    {
        String mProductionNameString = "Finder for Android Memory Analyzer";
        //String mCompanyString = "www.FinderMat.com";
        String mCompanyString = "Tencent.com";
        String mAuthorString = "YunLei-Fu";
        String mThxForString = "Xiaoheng,"+
        		"Millie,Victor,"+
        		"Bess,Yang,"+
        		".etc";
    }
    
    List<ProductionInfo> mInfoList = new ArrayList<AboutQuery.ProductionInfo>();
    
	public AboutQuery() {
		// TODO Auto-generated constructor stub
	}
    
	public IResult execute(IProgressListener listener) throws Exception
    {
	    mInfoList.add(new ProductionInfo());
	    
	    ReportManager.ReportMsg msg = new ReportManager.ReportMsg(ReportManager.FINDER_RPMSG_AboutFinder); 
        ReportManager.getSrvInst().sendRPMsg(msg);
        return this;
    }

    public Column[] getColumns()
    {
        // TODO Auto-generated method stub
        return new Column[] {
                new Column("Production Name", String.class).noTotals(),
                new Column("@Addr", String.class).noTotals(),
                new Column("Primary author", String.class).noTotals(),
                new Column("Thx for", String.class).noTotals()};
    }

    public Object getColumnValue(Object row, int columnIndex)
    {
        // TODO Auto-generated method stub
        AboutQuery.ProductionInfo pi = (AboutQuery.ProductionInfo)row;
        switch (columnIndex)
        {
            case 0:
                return pi.mProductionNameString;
            case 1:
                return pi.mCompanyString;
            case 2:
                return pi.mAuthorString;
            case 3:
                return pi.mThxForString;

            default:
                return "Error";
        }
    }

    public IContextObject getContext(Object row)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public ResultMetaData getResultMetaData()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public List<?> getElements()
    {
        // TODO Auto-generated method stub
        return mInfoList;
    }

    public boolean hasChildren(Object element)
    {
        // TODO Auto-generated method stub
        return false;
    }

    public List<?> getChildren(Object parent)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
}