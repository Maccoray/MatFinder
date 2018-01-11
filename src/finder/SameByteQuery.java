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
import org.eclipse.mat.query.annotations.Help;
import org.eclipse.mat.query.annotations.Name;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.IPrimitiveArray;
import org.eclipse.mat.util.IProgressListener;

import finder.util.ReportManager;
import finder.util.UtilClass;

@CommandName("FinderSameBytes")
@Category("Finder")
@Name("5|Same Bytes")
@Help("Find same byte[]s by given size")
public class SameByteQuery implements IQuery,IResultTree {
	
	static public class NameValuePair{
		public String mName;
		public long mAddr;
		public int mID;
		NameValuePair(String n,long dr,int id){
			this.mName = n;
			this.mAddr = dr;
			this.mID = id;
		}
	}

	//在列表中显示tree型
	static public class SameByteTree{
		public int mRootID;
		public List<Integer> mChildrenID = new ArrayList<Integer>();
		
		SameByteTree(int iRootID){
			mRootID = iRootID;
			mChildrenID.clear();
		}
		
		void putAChildID(int iChildID){
			mChildrenID.add(iChildID);
		}
	}
	
	
	public Map<Integer, SameByteTree> mRetTreeMap = new HashMap<Integer, SameByteQuery.SameByteTree>();
	
	Logger mLog = Logger.getLogger(SameByteQuery.class.toString());

	@Argument
	public ISnapshot mOpenSnapshot;
	
	@Argument
	@Help("Minimum array size for finding.0 for all!")
	public int mMinArraySize = 10240;
	public SameByteQuery() {
		// TODO Auto-generated constructor stub
	}
	
	public IResult execute(IProgressListener listener) throws Exception {
		

		Collection<IClass> classes = mOpenSnapshot.getClasses();
		if(classes.isEmpty()){
			return null;
		}
		
		HashMap<String, NameValuePair> bytesValueMap =new HashMap<String, NameValuePair>();
		
		//ArrayInt result = new ArrayInt();
		
		Iterator<IClass> iter = classes.iterator();
		ClassLoop : while(iter.hasNext()) {
			
			if (listener.isCanceled())
				break ClassLoop;
			
			IClass classfint = iter.next();
			String strClassNameString = classfint.getDisplayName();
			
			if( strClassNameString.contains("class byte[]") &&
					!strClassNameString.contains("class byte[][]") ){
				int[] objectIds = classfint.getObjectIds();
			    for (int id : objectIds){
			    	
			    	if (listener.isCanceled())
						break ClassLoop;
			    	
			        IObject myObject = mOpenSnapshot.getObject(id);
			        String name = myObject.getDisplayName();
			        long addr = myObject.getObjectAddress();
			        
		            IPrimitiveArray arr = (IPrimitiveArray) myObject;
		            byte[] value = (byte[]) arr.getValueArray(0, arr.getLength());
		            
		            if ( value == null || UtilClass.isByteArrayEmpty(value) ){
		            	mLog.info("null value");
		            	continue;
		            }

		            //mMinArraySize默认，全量比对
		            if ((mMinArraySize == 0 || value.length > mMinArraySize)) {
			            String strBytesMd5 = UtilClass.getBytesMd5(value);
			            if( bytesValueMap.containsKey( strBytesMd5 ) ){
			            	NameValuePair pair = bytesValueMap.get(strBytesMd5);
			            	SameByteTree sbt = mRetTreeMap.remove(pair.mID);
			            	sbt.putAChildID(id);
			            	mLog.info("root id:"+pair.mID+"Children id:"+id+"child count"+sbt.mChildrenID.size());
			            	mRetTreeMap.put(pair.mID, sbt);

			            }else{
			            	bytesValueMap.put(strBytesMd5, new NameValuePair(name,addr,id));
			            	if (!mRetTreeMap.containsKey(id)) {
			            		mRetTreeMap.put(id, new SameByteTree(id));
							}
			            }
		            }
			    }
			}
		}
		
		//去掉只有一份的
		Iterator<Map.Entry<Integer, SameByteTree>> it = mRetTreeMap.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<Integer, SameByteTree> entry=it.next();
            SameByteTree sbt = entry.getValue();
			if (sbt.mChildrenID.size() == 0) {
				it.remove();
			}
        }
        
		ReportManager.ReportMsg msg = new ReportManager.ReportMsg(ReportManager.FINDER_RPMSG_Samebytes); 
		ReportManager.getSrvInst().sendRPMsg(msg);
		
		return this;
	}

	public Column[] getColumns() {
		// TODO Auto-generated method stub
		return new Column[] { new Column("Bytes",String.class),
				new Column("Count",Integer.class)};
	}

	public Object getColumnValue(Object row, int columnIndex) {
		// TODO Auto-generated method stub
		
		try {
			int iID = (Integer)row;
			IObject obj = mOpenSnapshot.getObject(iID);
			switch (columnIndex) {
			case 0:
				return obj.getDisplayName();
			case 1:
				if (mRetTreeMap.containsKey(iID)) {
					return mRetTreeMap.get(iID).mChildrenID.size();
				}
				else {
					return 1;//是子
				}
				
			default:
				return "";
			}
			
		} catch (SnapshotException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
		
	}

	public IContextObject getContext(final Object row) {
		// TODO Auto-generated method stub
		return new IContextObject()
        {
            public int getObjectId()
            {
                return (Integer)row;
            }
        };
	}

	public ResultMetaData getResultMetaData() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<?> getElements() {
		// TODO Auto-generated method stub
		List<Integer> rows = new ArrayList<Integer>();
		Set<Integer> retIDKeySet = mRetTreeMap.keySet();
		Iterator<Integer> it = retIDKeySet.iterator();
		while ( it.hasNext()) {
			int iID = (Integer)it.next();
			rows.add(iID);
		}
		return rows;
	}

	public boolean hasChildren(Object element) {
		// TODO Auto-generated method stub
		Integer iID = (Integer)element;
		if( mRetTreeMap.containsKey(iID) ){
			return true;
		}else{
			return false;
		}
		
	}

	public List<?> getChildren(Object parent) {
		// TODO Auto-generated method stub
		Integer iID = (Integer)parent;
		SameByteTree sbt = mRetTreeMap.get(iID);
		return sbt.mChildrenID;
	}

}
