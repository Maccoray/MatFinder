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
import org.eclipse.mat.snapshot.ClassHistogramRecord;
import org.eclipse.mat.snapshot.Histogram;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.IPrimitiveArray;
import org.eclipse.mat.util.IProgressListener;

import finder.util.ReportManager;
import finder.util.UtilClass;

@CommandName("TopClass")
@Category("Finder")
@Name("2|Top Classes")
@Help("Find Top Classes")
public class TopClassQuery implements IQuery,IResultTree {

	public TopClassQuery() {
		// TODO Auto-generated constructor stub
	}
	
	@Argument
	public ISnapshot mOpenSnapshot;
	
	@Argument(isMandatory = false, flag = "f")
	@Help("Filter Contain str")
	public String mStringBaseClassName = "";

	public static class BaseInfo
	{
		boolean mbIsObj = false;
		BaseInfo(boolean bIsObj)
		{
			mbIsObj = bIsObj;
		}
		boolean isObj()
		{
			return mbIsObj;
		}
	}
	
	public static class TopClassObj extends BaseInfo
	{
		int mid = 0;
		TopClassObj(int id,boolean bIsObj)
		{
			super(bIsObj);
			mid = id;
		}
	}
	
	public static class TopClassInfo extends BaseInfo
	{
		String mStrClassName;
		long mlHeapSize;
		float mfPersent;
		int miClassID;
		List<TopClassObj> mObjs;
		TopClassInfo(boolean bIsObj)
		{
			super(bIsObj);
			mObjs = new ArrayList<TopClassObj>();
		}
		
		void setInfo(int iCID,String strName,long lUHSize,float fPersent)
		{
			this.mStrClassName = strName;
			this.mlHeapSize = lUHSize;
			this.mfPersent = fPersent;
			this.miClassID = iCID;
		}
		
		void pushObj(TopClassObj tobj)
		{
			if (mObjs != null) {
				mObjs.add(tobj);
			}
		}
	}
	List<TopClassInfo> mTopClassInfos = new ArrayList<TopClassInfo>();
	
	public IResult execute(IProgressListener listener) throws Exception 
	{
		// TODO Auto-generated method stub
        Histogram histogram = mOpenSnapshot.getHistogram(listener);
        if (listener.isCanceled())
            throw new IProgressListener.OperationCanceledException();
        Collection<IClass> classes;
        classes = mOpenSnapshot.getClasses();
        
        //ClassHistogramRecord[] classRecords = histogram.getClassHistogramRecords().toArray(new ClassHistogramRecord[0]);
        
        //Arrays.sort(classRecords, Histogram.reverseComparator(Histogram.COMPARATOR_FOR_RETAINEDHEAPSIZE));
        
        for(IClass clsHis : classes )
        {
        	//IClass clsHis = (IClass) mOpenSnapshot.getObject(rd.getClassId());
        	
        	String strNameString = clsHis.getName();
        	
        	if(mStringBaseClassName != null)
        	{
        		if(!strNameString.contains(mStringBaseClassName))
        		{
            		continue;
            	}
        	}
        	
        	long lClassRetainedSize = 0;
        	long lUsedHeapSize = mOpenSnapshot.getSnapshotInfo().getUsedHeapSize();

        	TopClassInfo tci = new TopClassInfo(false);

    		//获取类实例列表
    		int[] objectIds = clsHis.getObjectIds();
    		if (objectIds != null && objectIds.length > 0)
    		{
    			if(strNameString.contains("java.lang.ref.FinalizerReference"))
    			{
    				continue;
    			}
			    for (int id : objectIds)
			    {
			    	if (listener.isCanceled())
						break;
			    	IObject objC = mOpenSnapshot.getObject(id);
			    	
			    	long objSize = objC.getUsedHeapSize();
			    	
			    	if( strNameString.contains("byte[]") &&
							!strNameString.contains("byte[][]") )
					{
						//miniDump中的byte是被置换成空的
						IObject bytesObjOrg = mOpenSnapshot.getObject(id);
			            IPrimitiveArray arr = (IPrimitiveArray) bytesObjOrg;
			            byte[] value = (byte[]) arr.getValueArray(0, arr.getLength());
			            if (UtilClass.isAMiniBytes(value))
			            {
			            	objSize = UtilClass.getAMiniBytesSize(value);
			            }
					}
			    	lClassRetainedSize += objSize;
			    	
			    	TopClassObj tobj = new TopClassObj(id, true);
			    	tci.pushObj(tobj);
			    }
        	
			    float fPersent = (float)lClassRetainedSize/lUsedHeapSize;
				tci.setInfo(clsHis.getObjectId(), clsHis.getDisplayName(), lClassRetainedSize, fPersent);
	    		mTopClassInfos.add(tci);
    		}

        }
        
		ReportManager.ReportMsg msg = new ReportManager.ReportMsg(ReportManager.FINDER_RPMSG_Top); 
		ReportManager.getSrvInst().sendRPMsg(msg);
        return this;
	}

	public Object getColumnValue(Object row, int columnIndex)
	{
		// TODO Auto-generated method stub
		
		BaseInfo ele = (BaseInfo) row;
		TopClassInfo tci = null;
		TopClassObj tco = null;
		
    	if (!ele.isObj())
    	{
    		tci = (TopClassInfo)ele;
    		
		}
    	else
    	{
			tco = (TopClassObj)ele;
			
		}
    	
        switch (columnIndex)
        {
        case 0:
        	try
        	{
	        	if (tci != null)
	        	{
	        		return tci.mStrClassName;
				}
	        	else if(tco != null)
	        	{
					return mOpenSnapshot.getObject(tco.mid).getDisplayName();
	        	}
	        	else
	        	{
					return "null";
				}
        	}
        	catch (SnapshotException e)
        	{
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "null";
			}
        case 1:
        	try
        	{
	        	if (tci != null)
	        	{
					IClass cls = (IClass) mOpenSnapshot.getObject(tci.miClassID);
					int[] objIDs = cls.getObjectIds();
					return objIDs.length;
				}
	        	else if(tco != null)
	        	{
	        		return 1;
	        	}
	        	else
	        	{
					return 1;
				}
			}
        	catch (SnapshotException e)
			{
				e.printStackTrace();
			}
        case 2:
        	try {
	        	if (tci != null)
	        	{
	        		return tci.mlHeapSize;
				}
	        	else if(tco != null)
	        	{
	        		IObject objTmpIObject = mOpenSnapshot.getObject(tco.mid);
	        		String strNameString = objTmpIObject.getClazz().getName();
	        		if( strNameString.contains("byte[]") &&
							!strNameString.contains("byte[][]") )
					{
						//miniDump中的byte是被置换成空的
						IObject bytesObjOrg = mOpenSnapshot.getObject(tco.mid);
			            IPrimitiveArray arr = (IPrimitiveArray) bytesObjOrg;
			            byte[] value = (byte[]) arr.getValueArray(0, arr.getLength());
			            if (UtilClass.isAMiniBytes(value))
			            {
			            	long lminiDumpSize = UtilClass.getAMiniBytesSize(value);
			            	System.out.println("Bytes[]'s lenth is "+String.valueOf(lminiDumpSize));
			            	return lminiDumpSize;
			            }
			            else {
							return objTmpIObject.getRetainedHeapSize();
						}
					}
					return objTmpIObject.getUsedHeapSize();
	        	}
	        	else
	        	{
					return 0;
				}
			}
        	catch (SnapshotException e)
        	{
				// TODO Auto-generated catch block
				e.printStackTrace();
				return 0;
			}
        case 3:
        	if (tci != null)
        	{
        		return tci.mfPersent;
			}
        	else
        	{
				return 0;
			}
        }
        return null;
	}

	public Column[] getColumns()
	{
		// TODO Auto-generated method stub
        return new Column[] { new Column("ClassName",String.class), //类名称
        		new Column("ObjectCount",String.class), //类名称
                new Column("HeapSize", Long.class),
                new Column("Percent",float.class).noTotals()};
	}

	public IContextObject getContext(final Object row)
	{

    	
		// TODO Auto-generated method stub
        return new IContextObject()
        {
            public int getObjectId()
            {
        		BaseInfo ele = (BaseInfo) row;
        		TopClassInfo tci = null;
        		TopClassObj tco = null;
        		
            	if (!ele.isObj())
            	{
            		tci = (TopClassInfo)ele;
            		return tci.miClassID;
            		
        		}
            	else
            	{
        			tco = (TopClassObj)ele;
        			return tco.mid;
        			
        		}
            }
        };
	}

    
	public ResultMetaData getResultMetaData()
	{
		// TODO Auto-generated method stub
        return null;
	}

	public List<?> getChildren(Object parent)
	{
		// TODO Auto-generated method stub
		TopClassInfo row = (TopClassInfo)parent;
		return row.mObjs;
	}

	public List<?> getElements()
	{
		// TODO Auto-generated method stub
		return mTopClassInfos;
	}

	public boolean hasChildren(Object element)
	{
		// TODO Auto-generated method stub
		BaseInfo ele = (BaseInfo) element;
		TopClassInfo tci = null;
		
    	if (!ele.isObj())
    	{
    		tci = (TopClassInfo)ele;
    		if (tci.mObjs.size() > 0)
    		{
    			return true;
    		}
    		else
    		{
    			return false;
    		}
    		
		}
    	else
    	{
			return false;
			
		}

	}
}
