package finder;

import java.util.ArrayList;
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
import org.eclipse.mat.query.annotations.Argument.Advice;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.util.IProgressListener;

import finder.util.ReportManager;
import finder.util.UtilClass;


@CommandName("3E3Dump")
@Category("Finder")
@Name("7|3E3Dump")
@Help("1E-1D,2E-2D,3E-3D,Please take operation in 3D")
public class ThreeExecutionThreeDumpQuery implements IQuery,IResultTree
{
	@Argument
	public ISnapshot mThirdSnapshot;
	
	@Argument(advice = Advice.SECONDARY_SNAPSHOT,isMandatory = true, flag = "2S")
	@Help("Select old Snapshot,2d")
	public ISnapshot mSecondSnapshot;
	
	@Argument(advice = Advice.SECONDARY_SNAPSHOT,isMandatory = true, flag = "1S")
	@Help("Select oldder Snapshot,1d")
	public ISnapshot mFirstSnapshot;
	
	@Argument(isMandatory = false,flag = "f")
	@Help("Filter Contain str")
	public String mStrFilter = "";

	
	static class LeakClassInfo
	{
		int iID;
		String clsNameString;
		int iFirstIncObjCount;
		int iSecondIncObjCount;
		List<Integer> incObjList = null;
		
		public LeakClassInfo(int iID,String strName,int iFirst,int iSecond,List<Integer> incList)
		{
			this.iID = iID; 
			clsNameString = strName;
			iFirstIncObjCount = iFirst;
			iSecondIncObjCount = iSecond;
			incObjList = incList;
		}
	}
	
	private List<LeakClassInfo> leakClasses = new ArrayList<LeakClassInfo>();
	
	public ThreeExecutionThreeDumpQuery()
	{
		// TODO Auto-generated constructor stub
	}

	private void whichObjIsInc(UtilClass.SnapshotClassInfo tcinfo,
			UtilClass.SnapshotClassInfo scinfo,UtilClass.SnapshotClassInfo fcinfo)
	{
		//详细判断那个对象是新增的
		List<Integer> incObjLst = new ArrayList<Integer>();
		for(UtilClass.SnapshotClassInfo.SnapshotObjInfo objInfo3:tcinfo.objs)
		{
			boolean bIsInc = true;
			for(UtilClass.SnapshotClassInfo.SnapshotObjInfo objInfo2:scinfo.objs)
			{
				if(objInfo2.laddr == objInfo3.laddr)
				{
					bIsInc = false;
					break;
				}
			}
			if(bIsInc)
			{
				incObjLst.add(objInfo3.iID);
			}
		}
		
		leakClasses.add(new LeakClassInfo(tcinfo.iID,tcinfo.strName,
				scinfo.objs.size()-fcinfo.objs.size(),
				tcinfo.objs.size()-scinfo.objs.size(),
				incObjLst));
	}
	
	
	public IResult execute(IProgressListener arg0) throws Exception
	{
		// TODO Auto-generated method stub
		List<UtilClass.SnapshotClassInfo> thirdClasses = UtilClass.getSnapshotClasses(mThirdSnapshot);
		List<UtilClass.SnapshotClassInfo> secondClasses = UtilClass.getSnapshotClasses(mSecondSnapshot);
		List<UtilClass.SnapshotClassInfo> firstClasses = UtilClass.getSnapshotClasses(mFirstSnapshot);
		
		
		for(UtilClass.SnapshotClassInfo fcinfo:firstClasses)
		{
			for(UtilClass.SnapshotClassInfo scinfo:secondClasses)
			{
				//如果找到一个增加的项
				if(scinfo.strName.equals(fcinfo.strName)&&scinfo.objs.size() > fcinfo.objs.size())
				{
					for(UtilClass.SnapshotClassInfo tcinfo:thirdClasses)
					{
						if (tcinfo.strName.equals(scinfo.strName) && tcinfo.objs.size()>scinfo.objs.size())
						{
							whichObjIsInc(tcinfo,scinfo,fcinfo);
						}
					}
				}
			}
		}
        ReportManager.ReportMsg msg = new ReportManager.ReportMsg(ReportManager.FINDER_RPMSG_3DumpsCmp); 
        ReportManager.getSrvInst().sendRPMsg(msg);
		return this;
	}

	public Object getColumnValue(Object row, int columnIndex)
	{
		// TODO Auto-generated method stub
		if(row instanceof LeakClassInfo)
		{
			switch (columnIndex)
			{
				case 0:
				{
					return ((LeakClassInfo)row).clsNameString;
				}
	
				case 1:
				{
					return ((LeakClassInfo)row).iFirstIncObjCount;
				}
				
				case 2:
				{
					return ((LeakClassInfo)row).iSecondIncObjCount;
				}
			}
		}
		else {
			IObject obj = null;
			try
			{
				obj = mThirdSnapshot.getObject((Integer)row);
			} catch (SnapshotException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(obj != null)
			{
				switch (columnIndex)
				{
					case 0:
					{
						return obj.getDisplayName();
					}
					case 1:
					{
						return 0;
					}
					case 2:
					{
						return 1;
					}
				}
			}
			else {
				return null;
			}
		}
		return null;
	}

	public Column[] getColumns()
	{
		// TODO Auto-generated method stub
        return new Column[] { new Column("Class&ObjectName",String.class), //类&实例名称
                new Column("FirstInc", int.class),//首次执行增加
                new Column("SecondInc",int.class)//二次执行增加
                };//占用内存
	}

	public IContextObject getContext(final Object row)
	{
		// TODO Auto-generated method stub
		if(row instanceof LeakClassInfo)
		{
			return new IContextObject()
	        {
	            public int getObjectId()
	            {
	                return ((LeakClassInfo)row).iID;
	            }
	        };
		}
		else
		{
			return new IContextObject()
	        {
	            public int getObjectId()
	            {
	                return (Integer)row;
	            }
	        };
		}
	}

	public ResultMetaData getResultMetaData()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public List<?> getChildren(Object parent)
	{
		// TODO Auto-generated method stub
		return ((LeakClassInfo)parent).incObjList;
	}

	public List<?> getElements()
	{
		// TODO Auto-generated method stub
		return leakClasses;
	}

	public boolean hasChildren(Object element)
	{
		// TODO Auto-generated method stub
		return (element instanceof LeakClassInfo);
	}

}
