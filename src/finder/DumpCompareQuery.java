package finder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

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
import org.eclipse.mat.query.annotations.Name;
import org.eclipse.mat.query.annotations.Argument.Advice;
import org.eclipse.mat.query.annotations.Help;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.util.IProgressListener;

import finder.util.ReportManager;


@CommandName("CmpDump")
@Category("Finder")
@Name("3|Compare")
@Help("Compare two dump")
public class DumpCompareQuery implements IQuery, IResultTree {
	
	@Argument
	public ISnapshot mCurrentSnapshot;
	
	@Argument(advice = Advice.SECONDARY_SNAPSHOT,isMandatory = true, flag = "s")
	@Help("Select old Snapshot")
	public ISnapshot mOldSnapshot;
	
	@Argument(isMandatory = false,flag = "f")
	@Help("Filter Contain str")
	public String mStrFilter = "";
	
	Logger mLog = Logger.getLogger(DumpCompareQuery.class.toString());
	
	static class BaseDiffInfo{
		public boolean mbClass; //不同的对象是否是class
		public boolean mbIncreased; //对象是否是新增的(新增=true 非新增=false)
		public BaseDiffInfo(boolean bClass,boolean bIncreased) {
			// TODO Auto-generated constructor stub
			mbClass = bClass;
			mbIncreased = bIncreased;
		}
	}
	
	//实例数据结构
	static class DiffObjectInfo extends BaseDiffInfo{
		public DiffObjectInfo(int iObjectID,boolean bIncreased) {
			// TODO Auto-generated constructor stub
			super(false,bIncreased);
			miObjectID = iObjectID;
		}
		public int miObjectID;//Object ID
	}
	
	//类数据结构
	static class DiffClassInfo extends BaseDiffInfo{
		public DiffClassInfo(int iClassID,boolean bIncreased) {
			// TODO Auto-generated constructor stub
			super(true,bIncreased);
			miClassID = iClassID;
		}
		public int miClassID;//Class ID
		
		List<DiffObjectInfo> mDiffObjsList = new ArrayList<DumpCompareQuery.DiffObjectInfo>();//存储那些不同的对象们
	}
	
	//最终全局应该返回给界面的数据结构
	private List<DiffClassInfo> mDiffClassInfoForShowLst;
	
	static class SnapshotCmpClassInfo{
		public SnapshotCmpClassInfo(int iID) {
			// TODO Auto-generated constructor stub
			miClassID = iID;
			mObjectsMap.clear();
		}
		public int miClassID;//最基础的Class id
		
		//实例列表 key-Addr value id
		public Map<Long, Integer> mObjectsMap = new HashMap<Long, Integer>();
		
		public void addObject(long lAddr,int iObjID) {
			mObjectsMap.put(lAddr, iObjID);
		}
	}
	
	//返回一个快照中所需要对比的类内容，key-className value-CmpClassInfo
	//参数：进程，过滤字，快照
	private Map<String, SnapshotCmpClassInfo> getSnapshotClassObj(IProgressListener listener,String strFilter,ISnapshot shot){
		
		Map<String, SnapshotCmpClassInfo> retMap = new HashMap<String, SnapshotCmpClassInfo>();
		try {
			Collection<IClass> classes = shot.getClasses();
			Iterator<IClass> iter = classes.iterator();
			while(iter.hasNext()) {
				IClass classfit = iter.next();
				
	        	String strNameString = classfit.getName();
	        	
	        	if (strNameString == null)
	        	{
	        		continue;
				}
	        	
	        	//过滤
	        	if(!strNameString.contains(strFilter))
	        	{
	        		continue;
	        	}
	        	else
	        	{
	        		//找到需要对比的class后
	        		SnapshotCmpClassInfo clsinfoClassInfo = new SnapshotCmpClassInfo(classfit.getObjectId());
	        		
	        		//获取类实例列表
	        		int[] objectIds = classfit.getObjectIds();
	        		if (objectIds != null && objectIds.length > 0) {
					    for (int id : objectIds){
					    	if (listener.isCanceled())
								break;

					    	long lAddr = shot.mapIdToAddress(id);
					    	clsinfoClassInfo.addObject(lAddr, id);
					    }
					    
					    //返回所需对比类列表
					    retMap.put(strNameString, clsinfoClassInfo);
					}	
	        	}
			}
		} catch (SnapshotException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return retMap;
	}
	
	//返回两个类的实例区别
	//参数，第一个类，第二个类，是否是增加
	private List<DiffObjectInfo> getClassDiffObjs(SnapshotCmpClassInfo firstClass,SnapshotCmpClassInfo secendClass,boolean bIncrease) {
		List<DiffObjectInfo> lstDiffObj = new ArrayList<DiffObjectInfo>();
		
		Set<Map.Entry<Long,Integer>> setFirstObj = firstClass.mObjectsMap.entrySet();
		Iterator<Map.Entry<Long,Integer>> itFirstObj = setFirstObj.iterator();
		
		while (itFirstObj.hasNext()){
			Map.Entry<Long,Integer> entryFirstObject = itFirstObj.next();
			long lFirstObjAddr = entryFirstObject.getKey();
			
			Integer iObjId = secendClass.mObjectsMap.get(lFirstObjAddr);
			if (iObjId != null) {
				continue;
			}
			else{
				DiffObjectInfo doi = new DiffObjectInfo(entryFirstObject.getValue(), bIncrease);
				lstDiffObj.add(doi);
			}
		}
		
		return lstDiffObj;
	}
	
	//对比两个快照的类数据结构，找寻不同，返回展示的类数据结构
	private List<DiffClassInfo> cmpDiffSnapshot(Map<String, SnapshotCmpClassInfo> mapOld,Map<String, SnapshotCmpClassInfo> mapCurrent) {
		//key-ClassID,value-DiffObjsList
		List<DiffClassInfo> retList = new ArrayList<DiffClassInfo>();
		
		//以老的为准
		Set<Map.Entry<String, SnapshotCmpClassInfo>> setOldClass = mapOld.entrySet();
		Iterator<Map.Entry<String, SnapshotCmpClassInfo>> itOldClass = setOldClass.iterator(); 
		while (itOldClass.hasNext()) {
			Map.Entry<String, SnapshotCmpClassInfo> entryOldClass = (Map.Entry<String, SnapshotCmpClassInfo>) itOldClass.next();
			String oldClassNameStr = entryOldClass.getKey();
			
			SnapshotCmpClassInfo currentClassInfo = mapCurrent.get(oldClassNameStr);
			
			if(currentClassInfo != null){
				SnapshotCmpClassInfo oldClassInfo = entryOldClass.getValue();
				//找到减少的
				List<DiffObjectInfo> lstReduceObj = getClassDiffObjs(oldClassInfo,currentClassInfo,false);
				//找增加的
				List<DiffObjectInfo> lstIncreasedObjInfos = getClassDiffObjs(currentClassInfo, oldClassInfo, true);

				//这里的true或者false是指使用current snapshot能不能找到，而不是新增的概念
				DiffClassInfo diffClassInfo = new DiffClassInfo(currentClassInfo.miClassID, true);
				diffClassInfo.mDiffObjsList.addAll(lstReduceObj);
				diffClassInfo.mDiffObjsList.addAll(lstIncreasedObjInfos);
				
				retList.add(diffClassInfo);
			}else{
				SnapshotCmpClassInfo oldClassInfo1 = entryOldClass.getValue();
				//如果在当前dump中没有找到这个类，说明整个类都被销毁了，类中的所有对象都是减少的
				DiffClassInfo diffClassInfo1 = new DiffClassInfo(oldClassInfo1.miClassID, false);

				SnapshotCmpClassInfo currentClassInfo1 = new SnapshotCmpClassInfo(oldClassInfo1.miClassID);//根本就没有，这里就不要添加数据了
				diffClassInfo1.mDiffObjsList = getClassDiffObjs(oldClassInfo1,currentClassInfo1,false);
				
				retList.add(diffClassInfo1);
			}
		}
		
		//补充一遍新增的类
		Set<Map.Entry<String, SnapshotCmpClassInfo>> setCurrentClass = mapCurrent.entrySet();
		Iterator<Map.Entry<String, SnapshotCmpClassInfo>> itCurrentClass = setCurrentClass.iterator();
		
		while (itCurrentClass.hasNext()) {
			Map.Entry<String, SnapshotCmpClassInfo> entryCurrentClass = (Map.Entry<String, SnapshotCmpClassInfo>) itCurrentClass.next();
			String currentClassNameStr = entryCurrentClass.getKey();
			SnapshotCmpClassInfo oldClassInfo = mapOld.get(currentClassNameStr);
			
			if (oldClassInfo == null) {
				//在老表中找不到这个类，说明整个类都是新加的
				SnapshotCmpClassInfo currentClassInfo2 = entryCurrentClass.getValue();
				DiffClassInfo diffClassInfo2 = new DiffClassInfo(currentClassInfo2.miClassID, true);
				SnapshotCmpClassInfo oldClassInfo2 = new SnapshotCmpClassInfo(currentClassInfo2.miClassID);
				diffClassInfo2.mDiffObjsList = getClassDiffObjs(currentClassInfo2,oldClassInfo2,true);
				
				retList.add(diffClassInfo2);
			}
		}
		
		return retList;
	}
	
	public DumpCompareQuery() {
		// TODO Auto-generated constructor stub
		/*if(mISnapshots.length > 2){
			mCurrentSnapshot = mISnapshots[0];
			mOldSnapshot = mISnapshots[1];
		}*/
	}

	public Object getColumnValue(Object row, int columnIndex) {
		// TODO Auto-generated method stub
		BaseDiffInfo bdi = (BaseDiffInfo)row;
		IClass cls = null;
		IObject obj = null;
		
		try {
			if (bdi.mbClass) {
				DiffClassInfo dci = (DiffClassInfo)row;
				if (dci.mbIncreased) {
					cls = (IClass)mCurrentSnapshot.getObject(dci.miClassID);
				}else {
					//当前类中根本没有这个cls，所以不用获取了
					cls = null;
				}
			}else{
				DiffObjectInfo doi = (DiffObjectInfo)row;
				if (doi.mbIncreased) {
					obj = (IObject)mCurrentSnapshot.getObject(doi.miObjectID);
				}else {
					obj = (IObject)mOldSnapshot.getObject(doi.miObjectID);
				}
			}
		} catch (SnapshotException e) {
			e.printStackTrace();
		}
		
		switch (columnIndex) {
		case 0:
			if (cls != null) {
				return cls.getName();
			}
			else if(obj != null){
				return obj.getDisplayName();
			}else {
				DiffClassInfo dci = (DiffClassInfo)row;
				try {
					//纯粹减少，且减到当前dump中已经没有的情况
					cls = (IClass)mOldSnapshot.getObject(dci.miClassID);
					return cls.getName();
				} catch (SnapshotException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return e.toString();
				}
			}
		
		case 1:
			if (bdi.mbClass){
				DiffClassInfo dci = (DiffClassInfo)row;
				return dci.mDiffObjsList.size();
			}else{
				DiffObjectInfo doi = (DiffObjectInfo)row;
				return doi.mbIncreased ? 1 : (-1);
			}
			
		case 2:
			int iNewCount = 0;
			if (bdi.mbClass){
				DiffClassInfo dci = (DiffClassInfo)row;
				for(DiffObjectInfo objInfo:dci.mDiffObjsList)
				{
					iNewCount += objInfo.mbIncreased?1:0;
				}
				return iNewCount;
			}else{
				DiffObjectInfo doi = (DiffObjectInfo)row;
				return doi.mbIncreased ? 1 : 0;
			}
			
		case 3:
			int iGcCount = 0;
			if (bdi.mbClass){
				DiffClassInfo dci = (DiffClassInfo)row;
				for(DiffObjectInfo objInfo:dci.mDiffObjsList)
				{
					iGcCount += (!objInfo.mbIncreased)?1:0;
				}
				return iGcCount;
			}else{
				DiffObjectInfo doi = (DiffObjectInfo)row;
				return doi.mbIncreased ? 0 : 1;
			}
		case 4:
			if (cls != null)
			{
				try {
					return cls.getObjectIds().length;
				} catch (SnapshotException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return 0;
				}
			}
			else if(obj != null){
				return 1;
			}else {
				return 0;
			}
		case 5:
			if (cls != null) {
				int iClassRetainedSize = 0;
				DiffClassInfo dci = (DiffClassInfo)row;
				for(DiffObjectInfo objInfo:dci.mDiffObjsList)
				{
					if(objInfo.mbIncreased)
					{
						try {
							iClassRetainedSize += mCurrentSnapshot.getObject(objInfo.miObjectID).getRetainedHeapSize();
						} catch (SnapshotException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else 
					{
						try {
							iClassRetainedSize -= mOldSnapshot.getObject(objInfo.miObjectID).getRetainedHeapSize();
						} catch (SnapshotException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				return iClassRetainedSize;
			}
			else if(obj != null){
				return obj.getRetainedHeapSize();
			}else {
				return 0;
			}
			
		default:
			break;
		}
		return null;
	}

	public Column[] getColumns() {
		// TODO Auto-generated method stub
        return new Column[] { new Column("Class&ObjectName",String.class), //类&实例名称
                new Column("ChangedObjectsCount", int.class),//实例个数
                new Column("NewCount",int.class),//new的实例个数
                new Column("GCCount",int.class),//GC的实例个数
                new Column("CurrentCount",int.class),//GC的实例个数
                new Column("Incr Retained", long.class)};//占用内存
	}

	public IContextObject getContext(Object row) {
		// TODO Auto-generated method stub
		BaseDiffInfo bdi = (BaseDiffInfo)row;
		if (bdi.mbClass) {
			final DiffClassInfo dci = (DiffClassInfo)row;
			if (dci.mbIncreased) {//只返回当前快照的上下文，之后打开的不要给上下文
				return new IContextObject()
		        {
		            public int getObjectId()
		            {
		                return dci.miClassID;
		            }
		        };
			}
		}else{
			final DiffObjectInfo doi = (DiffObjectInfo)row;
			if (doi.mbIncreased) {
					return new IContextObject()
			        {
			            public int getObjectId()
			            {
			                return doi.miObjectID;
			            }
			        };
			}
		}
		return null;
	}

	public ResultMetaData getResultMetaData() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<?> getChildren(Object parent) {
		// TODO Auto-generated method stub
		return ((DiffClassInfo)parent).mDiffObjsList;
	}

	public List<?> getElements() {
		// TODO Auto-generated method stub
		return mDiffClassInfoForShowLst;
	}

	public boolean hasChildren(Object element) {
		// TODO Auto-generated method stub
		BaseDiffInfo bdi = (BaseDiffInfo) element;
		if (bdi.mbClass) {
			return ((DiffClassInfo)element).mDiffObjsList.size() != 0;
		}else {
			return false;
		}
		
	}

	public IResult execute(IProgressListener listener) throws Exception {
		// TODO Auto-generated method stub
		if (mCurrentSnapshot != null && mOldSnapshot != null)
		{
			Map<String, SnapshotCmpClassInfo> mapCurrentSnapshotClassObj = getSnapshotClassObj(listener, mStrFilter, mCurrentSnapshot);
			Map<String, SnapshotCmpClassInfo> mapOldSnapshotClassObj = getSnapshotClassObj(listener, mStrFilter, mOldSnapshot);
			
			if (mapCurrentSnapshotClassObj.size() > 0 && mapOldSnapshotClassObj.size() > 0)
			{
				mDiffClassInfoForShowLst = cmpDiffSnapshot(mapOldSnapshotClassObj,mapCurrentSnapshotClassObj);
			}
			else
			{
				return null;
			}
			
			ReportManager.ReportMsg msg = new ReportManager.ReportMsg(ReportManager.FINDER_RPMSG_Cmp); 
			ReportManager.getSrvInst().sendRPMsg(msg);
			
			return this;

		}
		else
		{
			return null;
		}
		
	}

}
