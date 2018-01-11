package finder.util;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.IMultiplePathsFromGCRootsComputer;
import org.eclipse.mat.snapshot.IPathsFromGCRootsComputer;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.Field;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.IInstance;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.IPrimitiveArray;
import org.eclipse.mat.snapshot.model.NamedReference;
import org.eclipse.mat.snapshot.model.ObjectReference;
import org.eclipse.mat.util.IProgressListener;

public class UtilClass
{
	static Logger mLog = Logger.getLogger("[UtilClass]");
    
    public static class BitmapInfo
    {
        public int mObjID;
        public long mSize;
        public int mWidth;
        public int mHeight;
        public long mAddr;
        public byte[] mbuf;
        
        BitmapInfo(int iID,long lAddr,long lSize,int iWidth,int iHeight,byte[] buf)
        {
            mObjID = iID;
            mSize = lSize;
            mWidth = iWidth;
            mHeight = iHeight;
            mAddr = lAddr;
            mbuf = buf;
        }
    }
    
    public static BitmapInfo getBitmapInfo(IObject objBitmap)
    {
        if (objBitmap == null ||
                !(objBitmap instanceof IInstance)||
                !objBitmap.getClazz().getName().equals("android.graphics.Bitmap"))
        {
            return null;
        }
        
        long lBitmapSize = 0;
        int iWidth = 0;
        int iHeight = 0;
        byte[] value = null;

        List<Field> fields = ((IInstance)objBitmap).getFields();
        for (int ii = 0; ii < fields.size(); ii++)
        {
            Field field = fields.get(ii);
            if(field.getName().contains("mBuffer"))
            {
                ObjectReference ref = (ObjectReference) field.getValue();
                if(ref != null)
                {
                    IPrimitiveArray mBufIObject;
                    try
                    {
                        mBufIObject = (IPrimitiveArray)ref.getObject();
                        if(mBufIObject != null)
                        {
                            value = (byte[]) mBufIObject.getValueArray(0, mBufIObject.getLength());
                            lBitmapSize = mBufIObject.getLength();
                        }
                    } catch (SnapshotException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        return null;
                    }

                }
            }
            if(field.getName().contains("mHeight"))
            {
                iHeight = (Integer) field.getValue();
            }
            
            if(field.getName().contains("mWidth"))
            {
                iWidth = (Integer) field.getValue();
            }
        }
        
        return new BitmapInfo(objBitmap.getObjectId(),objBitmap.getObjectAddress(),lBitmapSize,iWidth,iHeight,value);
    }
    
    public static class MiniDumpClassObjInfo
    {
    	//long lAddr;
    	//String strParentAddrs = "";
    	long lSize;
    	String strClassName;
    	int iObjCount;
    }

    private static long byteArrayToLong(byte[] b)
    {
    	return b[3] & 0xFF|(b[2]&0xFF)<<8|(b[1]&0xFF)<<16|(b[0]&0xFF)<<24;
    }

    static public boolean isAMiniBytes(byte[] bytesArr)
	{
    	if (bytesArr == null || isByteArrayEmpty(bytesArr))
		{
			return false;
		}
    	else if ( bytesArr.length == 8 && (bytesArr[0] == 55 && bytesArr[1] == 66 && bytesArr[2] == 77 && bytesArr[3] == 88))
		{
    		return true;
		}
    	else {
			return false;
		}
	}
    
    static public long getAMiniBytesSize(byte[] bytesArr)
    {
    	byte[] subBytes = new byte[4];
    	subBytes[0] = bytesArr[4];
    	subBytes[1] = bytesArr[5];
    	subBytes[2] = bytesArr[6];
    	subBytes[3] = bytesArr[7];
    	return byteArrayToLong(subBytes);
    }
    
    
    
  //获取所有class信息
    public static List<MiniDumpClassObjInfo> getminiDumpSnapshotClasses(ISnapshot openSnapshot)
	{
		// TODO Auto-generated method stub
    	List<MiniDumpClassObjInfo> miniDumpObjsLst = new ArrayList<MiniDumpClassObjInfo>();
    	Collection<IClass> classes;
    	
		try
		{
			classes = openSnapshot.getClasses();
			
	        for(IClass cl : classes )
	        {
	        	String strClassNameString = cl.getDisplayName();
	        	System.out.println("Current Dump Class:"+strClassNameString);
	        	int[] objectIds;
				objectIds = cl.getObjectIds();
				
				if (strClassNameString.contains("java.lang.ref.FinalizerReference"))
				{
		    		//Finalref没有处理价值，全部都变成根就可以了
//		    		miniObj.lSize = 0;
//		    		miniObj.lParentAddrs = null;
//		    		miniDumpObjsLst.add(miniObj);
		    		continue;//直接跳过
				}
				
				MiniDumpClassObjInfo miniObj = new MiniDumpClassObjInfo();
				miniObj.strClassName = cl.getName();
				miniObj.iObjCount = objectIds.length;
				miniObj.lSize = cl.getHeapSizePerInstance();
				//miniObj.lSize = 0;
				
				for (int id : objectIds)
				{
					IObject objC = openSnapshot.getObject(id);
			    	if( strClassNameString.contains("class byte[]") &&
							!strClassNameString.contains("class byte[][]") )
					{
						//miniDump中的byte是被置换成空的
						IObject bytesObjOrg = openSnapshot.getObject(id);
			            IPrimitiveArray arr = (IPrimitiveArray) bytesObjOrg;
			            byte[] value = (byte[]) arr.getValueArray(0, arr.getLength());
			            
			            if ( value == null || isByteArrayEmpty(value))
			            {
			            	miniObj.lSize += 4;
			            }
			            else if(value.length == 8 && (value[0] == 55 && value[1] == 66 && value[2] == 77 && value[3] == 88))
			            {
			            	byte[] subBytes = new byte[4];
			            	subBytes[0] = value[4];
			            	subBytes[1] = value[5];
			            	subBytes[2] = value[6];
			            	subBytes[3] = value[7];
			            	miniObj.lSize += byteArrayToLong(subBytes);
			            	//System.out.println("Mini Bytes length "+String.valueOf(miniObj.lSize));
			            }
			            else
			            {
			            	miniObj.lSize += objC.getUsedHeapSize();
						}
					}
			    	else
			    	{
			    		miniObj.lSize += objC.getUsedHeapSize();
					}
				}
				
				miniDumpObjsLst.add(miniObj);
				
//			    for (int id : objectIds)
//			    {
//			    	MiniDumpClassObjInfo miniObj = new MiniDumpClassObjInfo();
//			    	IObject objC = openSnapshot.getObject(id);
//			    	miniObj.strClassName = objC.getClazz().getName();
//			    	miniObj.lAddr = objC.getObjectAddress();
//			    	//System.out.println("Mini Obj's Address:"+String.valueOf(miniObj.lAddr));
//			    	
//			    	
//			    	
//			    	if( strClassNameString.contains("class byte[]") &&
//							!strClassNameString.contains("class byte[][]") )
//					{
//						//miniDump中的byte是被置换成空的
//						IObject bytesObjOrg = openSnapshot.getObject(id);
//			            IPrimitiveArray arr = (IPrimitiveArray) bytesObjOrg;
//			            byte[] value = (byte[]) arr.getValueArray(0, arr.getLength());
//			            
//			            if ( value == null || isByteArrayEmpty(value))
//			            {
//			            	miniObj.lSize = 4;
//			            }
//			            else if(value.length == 8 && (value[0] == 55 && value[1] == 66 && value[2] == 77 && value[3] == 88))
//			            {
//			            	byte[] subBytes = new byte[4];
//			            	subBytes[0] = value[4];
//			            	subBytes[1] = value[5];
//			            	subBytes[2] = value[6];
//			            	subBytes[3] = value[7];
//			            	miniObj.lSize = byteArrayToLong(subBytes);
//			            	//System.out.println("Mini Bytes length "+String.valueOf(miniObj.lSize));
//			            }
//			            else
//			            {
//			            	miniObj.lSize = objC.getUsedHeapSize();
//						}
//					}
//			    	else
//			    	{
//			    		miniObj.lSize = objC.getUsedHeapSize();
//					}
//
//		    		List<String> excludes = Arrays.asList( //
//		    	            new String[] { "java.lang.ref.WeakReference:referent",
//		    	            		"java.lang.ref.SoftReference:referent" }); 
//		    		
//		    	    Map<IClass, Set<String>> excludeMap = null;
//		    		excludeMap = convert(openSnapshot, excludes);
//	                IPathsFromGCRootsComputer cp = openSnapshot.getPathsFromGCRoots(id,excludeMap);
//	                
//	                if(cp == null)
//	                {
//	                    continue;
//	                }
//	                
//	                int [] gcArray = cp.getNextShortestPath();
//	                if (gcArray == null || gcArray.length <= 1)
//	                {
//	                	//根节点
//	                	miniObj.strParentAddrs = "0";
//	                }
//	                else
//	                {
//	                	//miniObj.lParentAddrs = new ArrayList<Long>();
//	                	for (int i=0;i<gcArray.length;i++)
//						{
//	                		IObject parentObject = openSnapshot.getObject(gcArray[i]);
//	                		String strParentClazzName = parentObject.getClazz().getName();
//	                		if (parentObject == null || strParentClazzName.contains("FinalizerReference"))
//							{
//	                			//直接跳过这样被java.lang.ref.FinalizerReference命中的类
//								continue;
//							}
//	                		//miniObj.lParentAddrs.add(parentObject.getObjectAddress());
//	                		miniObj.strParentAddrs += strParentClazzName;
//	                		miniObj.strParentAddrs += "#@#";
//						}
//					}
//	                miniDumpObjsLst.add(miniObj);
//	                
//	                //一个类一个对象，测试使用
//	                //break;
//			    }
	        }
		}
		catch (SnapshotException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
    
        return miniDumpObjsLst;
	}
    
    public static class SnapshotClassInfo
    {
    	public static class SnapshotObjInfo
    	{
    	    public String strName;
    		public long laddr;
    		public long lSize;
    		public int iID;
    		public String strValue; //复用给gc的时候才用到的
    	}
    	
    	public static class DominatorInfo
    	{
    		public String strDoName;
    		public int iDoersCount;//统治者有多少个
    		public int iDoedCount;//被统治者有多少个
    	}
    	
    	public String strName;
    	//对象列表
    	public List<SnapshotObjInfo> objs = new ArrayList<UtilClass.SnapshotClassInfo.SnapshotObjInfo>();
    	public long lSize;
    	public int iID;
    	//统治者列表
    	public List<DominatorInfo> doers = new ArrayList<UtilClass.SnapshotClassInfo.DominatorInfo>();
    }
    
    //获取所有class信息
    public static List<SnapshotClassInfo> getSnapshotClasses(ISnapshot openSnapshot)
	{
		// TODO Auto-generated method stub
    	List<SnapshotClassInfo> snapshotClassInfos= new ArrayList<SnapshotClassInfo>();
    	
    	Collection<IClass> classes;
    	
		try
		{
			classes = openSnapshot.getClasses();
			
	        for(IClass cl : classes )
	        {
	        	int[] objectIds;
	        	
	        	SnapshotClassInfo ci = new SnapshotClassInfo();
				ci.iID = cl.getObjectId();
				ci.lSize = 0;

				ci.strName = cl.getName();
					
				objectIds = cl.getObjectIds();
				
			    for (int id : objectIds)
			    {
			    	SnapshotClassInfo.SnapshotObjInfo objInfo = new SnapshotClassInfo.SnapshotObjInfo();
			    	
			    	IObject objC = openSnapshot.getObject(id);
			    	objInfo.strName = objC.getClazz().getName();
			    	objInfo.laddr = objC.getObjectAddress();
			    	objInfo.lSize = objC.getRetainedHeapSize();
			    	objInfo.iID = objC.getObjectId();
			    	
			    	ci.objs.add(objInfo);
			    	ci.lSize += objInfo.lSize;
			    }

				snapshotClassInfos.add(ci);
	        }
		} catch (SnapshotException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    
        return snapshotClassInfos;
	}
    
    
    public static class CmpClassInfo
    {
    	public String strClsName = "";
    	public long lIncRetainedSize = 0;
    	public int iIncCount = 0;
    	public List<Integer> incObjsLst = new ArrayList<Integer>();
    	
    }
    
    public static List<CmpClassInfo> cmpDump(List<SnapshotClassInfo> ClsInfos,
    		List<SnapshotClassInfo> olderClassInfos)
    {
    	List<CmpClassInfo> retCmpLst = new ArrayList<CmpClassInfo>();
    	if(ClsInfos != null && ClsInfos.size()!=0
    			&&olderClassInfos != null && olderClassInfos.size()!=0)
    	{
    		for(SnapshotClassInfo s1Cls : ClsInfos)
    		{
    			for(SnapshotClassInfo s2Cls : olderClassInfos)
    			{
    				if(s1Cls.strName.equals(s2Cls.strName))
    				{
    					CmpClassInfo cmpCls = new CmpClassInfo();
    					cmpCls.strClsName = s1Cls.strName;
    					cmpCls.lIncRetainedSize = s1Cls.lSize - s2Cls.lSize;
    					cmpCls.iIncCount = s1Cls.objs.size() - s2Cls.objs.size();
    					
    					if(cmpCls.iIncCount > 0)
    					{
    					    boolean isNew = true;
    					    //只有正向增加的对象才有增加的obj列表
    					    for(SnapshotClassInfo.SnapshotObjInfo objNew:s1Cls.objs)
    					    {
                                for(SnapshotClassInfo.SnapshotObjInfo objOld:s2Cls.objs)
                                {
                                    if(objNew.laddr == objOld.laddr)
                                    {
                                        //如果新的对象在老的dump中已有
                                        isNew = false;
                                        break;
                                    }
                                    else 
                                    {
                                        isNew = true;
                                        continue;
                                    }
                                }
                                
                                if(isNew)
                                {
                                    //如果是新的就增加入列表
                                    cmpCls.incObjsLst.add(objNew.iID);
                                }
    					    }

    					}
    					retCmpLst.add(cmpCls);
    					continue;
    				}
    			}
    		}
    	}
    	else
    	{
    		mLog.info("Cmpare dumps contents error");
		}

    	return retCmpLst;
    }
    
    public static class ActivityInfo
    {
    	public String strActivityName;
    	long lSize;
    	long lAddr;
    	public int iID;
    	public List<InstAttribute> attrs;//所有属性，暂存
    	public String strIndentifyValue;//长存
    }
    
    public static class InstAttribute
    {
    	public String strAttrName;//filed类型名称
    	public String strAttrValue;//ref类型里面展示的是技术描述，filed类型里面是直接的值
    	public String strRefAddr;//ref类型必须包含地址
    	public String strType;//type字段，类别
    }
    
    //得到一个对象的所有属性
    public static List<InstAttribute> getObjectAllAttributes(ISnapshot st,int ObjID)
    {
    	List<InstAttribute> atts = new ArrayList<UtilClass.InstAttribute>();
    	
    	try {
			IObject object = st.getObject(ObjID);
			if(object instanceof IInstance)
			{
				IInstance inst = (IInstance)object;
				List<Field> fields = inst.getFields();
				
				for(Field f:fields)
				{
					InstAttribute attr = new InstAttribute();
					attr.strAttrName = f.getName();
					if (f.getValue() instanceof ObjectReference)
					{
						//这是一个ref，要获取它的对象地址
						ObjectReference refObj = (ObjectReference)f.getValue();
						if (refObj != null)
						{
							NamedReference nameRefObj = new NamedReference(st, refObj.getObjectAddress(), f.getName());
							IObject objRefed = st.getObject(st.mapAddressToId(nameRefObj.getObjectAddress()));
							attr.strRefAddr = String.valueOf(nameRefObj.getObjectAddress());
							attr.strAttrValue = objRefed.getClassSpecificName();
							attr.strType = objRefed.getClazz().getName();
							if (attr.strAttrValue == null) {
								attr.strAttrValue = objRefed.getTechnicalName();
							}
						}
						else
						{
							attr.strAttrValue = "null";
						}
					}
					else {
						if (f.getValue() != null)
						{
							attr.strAttrValue = f.getValue().toString();
							attr.strRefAddr = null;
							attr.strType = null;
						}
						else {
							attr.strAttrValue = "null";
						}
					}
					
					
                    atts.add(attr);
				}
			}
		} catch (SnapshotException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return atts;
    }
    
    public static List<ActivityInfo> getSnapshotActivitys(ISnapshot st)
    {
    	List<ActivityInfo> retActivitysLst = new ArrayList<UtilClass.ActivityInfo>();
    	Collection<IClass> classes;
    	
		try
		{
			classes = st.getClasses();
			for(IClass cl : classes )
	        {
				IClass classfint = cl;
				IClass classSuperClass = classfint;

				while(classSuperClass.hasSuperClass())
				{
						
					classSuperClass = classSuperClass.getSuperClass();
					String stringSuperClassName = classSuperClass.getName();
					if( stringSuperClassName.equals("android.app.Activity") )
					{
						int[] objectIds = classfint.getObjectIds();
						for(int iObjID:objectIds)
						{
							IObject activityObject = st.getObject(iObjID);
							ActivityInfo aInfo = new ActivityInfo();
							aInfo.strActivityName = activityObject.getClazz().getName();
							aInfo.lSize = activityObject.getRetainedHeapSize();
							aInfo.lAddr = activityObject.getObjectAddress();
							aInfo.iID = activityObject.getObjectId();
							//增加所有的属性表
							aInfo.attrs = getObjectAllAttributes(st, aInfo.iID);
							
							retActivitysLst.add(aInfo);
						}
						break;
					}
				}
	        }
		} catch (SnapshotException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	return retActivitysLst;
    }
    
    
    public static class GCInfo
    {
    	public List<SnapshotClassInfo.SnapshotObjInfo> gcNodesLst =
    			new ArrayList<UtilClass.SnapshotClassInfo.SnapshotObjInfo>();//GC节点info
    }
    
    
    //因为没有暴露这个函数，只能从mat中抄过来。
    public static Map<IClass, Set<String>> convert(ISnapshot snapshot, List<String> excludes)
            throws SnapshotException
	{
		Map<IClass, Set<String>> excludeMap = null;
		
		if (excludes != null && !excludes.isEmpty())
		{
		    excludeMap = new HashMap<IClass, Set<String>>();
		
		    for (String entry : excludes)
		    {
		        String pattern = entry;
		        Set<String> fields = null;
		        int colon = entry.indexOf(':');
		
		        if (colon >= 0)
		        {
		            fields = new HashSet<String>();
		
		            StringTokenizer tokens = new StringTokenizer(entry.substring(colon + 1), ","); //$NON-NLS-1$
		            while (tokens.hasMoreTokens())
		                fields.add(tokens.nextToken());
		
		            pattern = pattern.substring(0, colon);
		        }
		
		        for (IClass clazz : snapshot.getClassesByName(Pattern.compile(pattern), true))
		            excludeMap.put(clazz, fields);
		    }
		}
		
		return excludeMap;
	}

    //线程名，因为线程需要获取名称作为gc白名单
    public static final String CLASS_THREAD = "java.lang.Thread";
    
    public static String arrayAsString(IPrimitiveArray charArray, int offset, int count, int limit)
    {
        if (charArray.getType() != IObject.Type.CHAR)
            return null;

        int length = charArray.getLength();

        int contentToRead = count <= limit ? count : limit;
        if (contentToRead > length - offset)
            contentToRead = length - offset;

        char[] value;
        if (offset == 0 && length == contentToRead)
            value = (char[]) charArray.getValueArray();
        else
            value = (char[]) charArray.getValueArray(offset, contentToRead);

        if (value == null)
            return null;

        StringBuilder result = new StringBuilder(value.length);
        for (int ii = 0; ii < value.length; ii++)
        {
            char val = value[ii];
            if (val >= 32 && val < 127)
                result.append(val);
            else
                result.append("\\u").append(String.format("%04x", 0xFFFF & val)); //$NON-NLS-1$//$NON-NLS-2$
        }
        if (limit < count)
            result.append("..."); //$NON-NLS-1$
        return result.toString();
    }
    
    public static String getStringValue(IObject stringObject, int limit) throws SnapshotException
    {
        Object valueObj = stringObject.resolveValue("value"); //$NON-NLS-1$
        if (!(valueObj instanceof IPrimitiveArray))
            return null;
        IPrimitiveArray charArray = (IPrimitiveArray) valueObj;

        Object countObj = stringObject.resolveValue("count"); //$NON-NLS-1$
        // count and offset fields were removed with JDK7u6
        if (countObj == null)
        {
            return arrayAsString(charArray, 0, charArray.getLength(), limit);
        }
        else
        {
            if (!(countObj instanceof Integer))
                return null;
            Integer count = (Integer) countObj;
            if (count.intValue() == 0)
                return ""; //$NON-NLS-1$

            Object offsetObj = stringObject.resolveValue("offset"); //$NON-NLS-1$
            if (!(offsetObj instanceof Integer))
                return null;
            Integer offset = (Integer) offsetObj;

            return arrayAsString(charArray, offset, count, limit);
        }
    }

    
    public static GCInfo getAObjGCPath(int iObj,ISnapshot ost) throws SnapshotException
	{
    	List<String> excludes = Arrays.asList( //
                new String[] { "java.lang.ref.Reference:referent"  }); 
        Map<IClass, Set<String>> excludeMap = null;
        
        excludeMap = UtilClass.convert(ost, excludes);
        IPathsFromGCRootsComputer cp = ost.getPathsFromGCRoots(iObj,excludeMap);
        //如果压根没有这个gc路径，那么直接返回null
        if(cp == null)
        {
        	System.out.println(ost.getObject(iObj).getDisplayName()+" has NoneGCInfo.");
            return null;
        }
        
        int [] gcArray = cp.getNextShortestPath();
        GCInfo gcNodes = new GCInfo();
        
        if (gcArray == null || gcArray.length == 0) {
        	System.out.println(ost.getObject(iObj).getDisplayName()+" has NoneShortestGCPath.");
        	return null;
		}
        
        int iChildID = -1;
        
        //for( int i = gcArray.length -1; i >= 0; i-- )
        for( int i = 0; i < gcArray.length; i++ )
        {
//			GCNodeInfoModel child = new GCNodeInfoModel(gcArray[i], parent);
//			parent.children.add(child);
//			parent = child;
        	int iGCNodeID = gcArray[i];
        	IObject objInst = ost.getObject(iGCNodeID);
        	if(objInst != null)
    		{
        		SnapshotClassInfo.SnapshotObjInfo objInfo = new SnapshotClassInfo.SnapshotObjInfo();
        		if(objInst instanceof IClass)//如果是个被classload加载类
    			{
    			    IClass cls = (IClass)objInst;
    			    objInfo.strName = cls.getName();
    			}
        		else
    			{
    			    objInfo.strName = objInst.getClazz().getName();
    			    if(objInfo.strName.equals(CLASS_THREAD)//如果是线程的话，那么要获取线程的名称
    			    		||objInfo.strName.equals("android.os.HandlerThread"))
    			    {
    			        IObject name = (IObject) objInst.resolveValue("name");
    			        objInfo.strValue = getStringValue(name,1024);
    			    }
    			    objInfo.iID = iGCNodeID;
        			objInfo.laddr = objInst.getObjectAddress();
        			objInfo.lSize = objInst.getRetainedHeapSize();
        			
        			if(iChildID != -1)
        			{
        				//如果存在父节点，那么就可以在这里获取一个refname，方便定位
        				List<NamedReference> refs = objInst.getOutboundReferences();
        				long lPAddr = ost.mapIdToAddress(iChildID);
//        				System.out.println("Parent's addr is "+String.valueOf(lPAddr));
        				
    	                for (NamedReference reference : refs)
    	                {
    	                	long lOutRefAddr = reference.getObjectAddress();
//    	                	System.out.println("Outref's addr is "+lOutRefAddr);
    	                    if (lOutRefAddr == lPAddr)
    	                    {
    	                    	objInfo.strName = reference.getName()+", "+objInfo.strName;
    	                    }
    	                }
        			}
        			iChildID = iGCNodeID;
        			
        			gcNodes.gcNodesLst.add(objInfo);
                }
        		
    		}
		}
		return gcNodes;
	}
    
    
    public static List<GCInfo> getObjsGcPath(int[] iObjIDs,ISnapshot openedSnapshot,IProgressListener listener) throws SnapshotException
    {
    	List<GCInfo> objGCPathList = new ArrayList<UtilClass.GCInfo>();
    	
//        List<String> excludes = Arrays.asList( //
//                new String[] { "java.lang.ref.Reference:referent" });
//    	Map<IClass, Set<String>> excludeMap = convert(openedSnapshot, excludes);
//    	
//    	IMultiplePathsFromGCRootsComputer computer = openedSnapshot.getMultiplePathsFromGCRoots(iObjIDs,
//                excludeMap);
//    	
//    	Object[] paths = computer.getAllPaths(listener);
    	listener.beginTask("Beging get obj's gc pathInfl", iObjIDs.length);
        //for (int i = 0; i < paths.length; i++)
    	for(int iIndex = 0;iIndex<iObjIDs.length;iIndex++)//替换获取gcPath的接口
        {
        	GCInfo gcPathInfo = /*new GCInfo();*/ getAObjGCPath(iObjIDs[iIndex], openedSnapshot);
        	listener.worked(iIndex);
        	
        	if(listener.isCanceled())
        	{
        		return null;
        	}
//        	int[] iGCNodeIDs = (int[]) paths[i];
//        	int iChildID = -1;
//        	for(int ii=0;ii<iGCNodeIDs.length;ii++)
//        	{
//        		int iGCNodeID = -1;
//        		iGCNodeID = iGCNodeIDs[ii];
//        		IObject obj = openedSnapshot.getObject(iGCNodeID);
//        		
//        		if(obj != null)
//        		{
//
//        			SnapshotClassInfo.SnapshotObjInfo objInfo = new SnapshotClassInfo.SnapshotObjInfo();
//        			if(obj instanceof IClass)//如果是个被classload加载类
//        			{
//        			    IClass cls = (IClass)obj;
//        			    objInfo.strName = cls.getName();
//        			    
//        			}
//        			else
//        			{
//        			    objInfo.strName = obj.getClazz().getName();
//        			    if(objInfo.strName.equals(CLASS_THREAD)
//        			    		||objInfo.strName.equals("android.os.HandlerThread"))
//        			    {
//        			        IObject name = (IObject) obj.resolveValue("name");
//        			        objInfo.strValue = getStringValue(name,1024);
//        			    }
//                    }
//        			objInfo.iID = iGCNodeID;
//        			objInfo.laddr = obj.getObjectAddress();
//        			objInfo.lSize = obj.getRetainedHeapSize();
//        			
//        			if(iChildID != -1)
//        			{
//        				//如果存在父节点，那么就可以在这里获取一个refname，方便定位
//        				List<NamedReference> refs = obj.getOutboundReferences();
//        				long lPAddr = openedSnapshot.mapIdToAddress(iChildID);
//        				System.out.println("Parent's addr is "+String.valueOf(lPAddr));
//        				
//    	                for (NamedReference reference : refs)
//    	                {
//    	                	long lOutRefAddr = reference.getObjectAddress();
//    	                	System.out.println("Outref's addr is "+lOutRefAddr);
//    	                    if (lOutRefAddr == lPAddr)
//    	                    {
//    	                    	objInfo.strName = reference.getName()+", "+objInfo.strName;
//    	                    }
//    	                }
//        			}
//        			iChildID = iGCNodeID;
//        			
//        			gcPathInfo.gcNodesLst.add(objInfo);
//        		}
//        		
//        		
//        	}
        	if(gcPathInfo != null)
        	{
        		objGCPathList.add(gcPathInfo);
        	}
        }
    	listener.done();
        return objGCPathList;
    }
    
    
    private static List<String> subClassName(ISnapshot st,String strClsName) throws SnapshotException
    {
        Collection<IClass> allByNameClasses = st.getClassesByName(strClsName, true);
        if(allByNameClasses == null)
        {
            return null;
        }
        
        List<String> allClassNames = new ArrayList<String>();
        for(IClass cls:allByNameClasses)
        {
            allClassNames.add(cls.getName());
        }
        return allClassNames;
    }
    
    //判断某个类的所有对象，是否包含某些gc节点
    public static List<SnapshotClassInfo.SnapshotObjInfo> classNoHaveGCNode(IClass c,List<String> containStrs,boolean isContainSubClassString,IProgressListener listener) throws SnapshotException
    {
        if(c == null)
        {
            System.out.println("classNoHaveGCNode:Error no class input");
            return null;
        }
        
        List<SnapshotClassInfo.SnapshotObjInfo> retLst = new ArrayList<UtilClass.SnapshotClassInfo.SnapshotObjInfo>();
        int [] objectIds = c.getObjectIds();
        ISnapshot openedSnapshot = c.getSnapshot();
        if(openedSnapshot == null)
        {
            System.out.println("classNoHaveGCNode:Cann't get snapshot from a class");
            return null;
        }
        
        if(isContainSubClassString)
        {
            List<String> allFilter = new ArrayList<String>();
            for(String name:containStrs)
            {
                List<String> subFilterStrings = subClassName(openedSnapshot, name);
                if(subFilterStrings != null)
                {
                    allFilter.addAll(subFilterStrings);
                }
            }
            containStrs = allFilter;
        }
        
        List<GCInfo> gcPathInfos = getObjsGcPath(objectIds,openedSnapshot,listener);
        
        for(GCInfo gcPath:gcPathInfos)
        {
            boolean isHave = false;
            SnapshotClassInfo.SnapshotObjInfo objInfo = null;
            objInfo = gcPath.gcNodesLst.get(0);
            
            for(SnapshotClassInfo.SnapshotObjInfo gcNode:gcPath.gcNodesLst)
            {
                for(String cstr:containStrs)
                {
                    if(gcNode.strName.equals(cstr))
                    {
                        isHave = true;
                        break;
                    }
                }
                
                if(isHave)
                {
                    break;
                }
            }
            
            if( !isHave && objInfo != null )
            {
                retLst.add(objInfo);
            }
        }
        
        return retLst;
    }
    
    //得到一个bytes的md5
	public static String getBytesMd5(byte[] value){
		String strRet = "";
		StringBuilder r = new StringBuilder();
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] md5ValueKey = md5.digest(value);
            
            
            for (int i = 0; i < md5ValueKey.length; i++) {
            	int val = ((int) md5ValueKey[i]) & 0xff;
            	if (val < 16)
            		r.append("0");
            	r.append(Integer.toHexString(val));
            }

		} catch (Exception e) {
			// TODO: handle exception
		}
		
		strRet += r.toString();
		return strRet;
	}
    
	public static boolean isByteArrayEmpty(byte[] value) {
		if(value.length == 0){
			return true;
		}
		
		byte vC = value[0];
		boolean bRet = true;
		for(byte v : value){
			if(v != vC){
				bRet = false;//只要有一个和buf首部不同，我们就认为这个Array不是空的
			}
		}
		return bRet;
	}
	
	
	
    
    public static class SameByteObjInfo
    {
    	public long lSize = 0; //内存大小
    	public int objID = 0;
    	public String strDisplayNameString = "";
    	public long lAddr = 0;
    }
    
    public static HashMap<String,List<SameByteObjInfo>> getSameBytes(ISnapshot st) throws SnapshotException
    {
		Collection<IClass> classes = st.getClasses();
		if(classes.isEmpty())
		{
			return null;
		}
		
		HashMap<String, List<SameByteObjInfo>> bytesValueMap =
				new HashMap<String, List<SameByteObjInfo>>();
		
		//ArrayInt result = new ArrayInt();
		
		Iterator<IClass> iter = classes.iterator();
		while(iter.hasNext())
		{
			IClass classfint = iter.next();
			String strClassNameString = classfint.getDisplayName();
			
			if( strClassNameString.contains("class byte[]") &&
					!strClassNameString.contains("class byte[][]") )
			{
				int[] objectIds = classfint.getObjectIds();
			    for (int id : objectIds)
			    {
			    	
			        IObject bytesObjOrg = st.getObject(id);
		            IPrimitiveArray arr = (IPrimitiveArray) bytesObjOrg;
		            byte[] value = (byte[]) arr.getValueArray(0, arr.getLength());
		            
		            if ( value == null || isByteArrayEmpty(value) ){
		            	continue;
		            }

		            String strBytesMd5 = UtilClass.getBytesMd5(value);
			        if( bytesValueMap.containsKey( strBytesMd5 ) )
			        {
			        	List<SameByteObjInfo> sojLst = bytesValueMap.get(strBytesMd5);
			        	//找到相同的bytes
			        	SameByteObjInfo sbi = new SameByteObjInfo();
			        	sbi.lSize = value.length;
			        	sbi.objID = id;
			        	sbi.strDisplayNameString = bytesObjOrg.getDisplayName();
			        	sbi.lAddr = bytesObjOrg.getObjectAddress();
			        	if (sojLst!=null)
			        	{
							sojLst.add(sbi);
						}
			        }
			        else
			        {
			        	//否则增加一个新的
			        	List<SameByteObjInfo> sojAddLst = new ArrayList<UtilClass.SameByteObjInfo>();
			        	SameByteObjInfo sbiorg = new SameByteObjInfo();
			        	sbiorg.lSize = value.length;
			        	sbiorg.objID = id;
			        	sbiorg.strDisplayNameString = bytesObjOrg.getDisplayName();
			        	sbiorg.lAddr = bytesObjOrg.getObjectAddress();
			        	sojAddLst.add(sbiorg);
				        bytesValueMap.put(strBytesMd5,sojAddLst);
			        }
		        }
			}
		}
		
		//删除只有一个的
		Iterator<Entry<String, List<SameByteObjInfo>>> iter1 =
				bytesValueMap.entrySet().iterator();
		while (iter1.hasNext()) {
			Map.Entry<String, List<SameByteObjInfo>> ent =
					(Map.Entry<String, List<SameByteObjInfo>>) iter1.next();
			List<SameByteObjInfo> sameByteList = ent.getValue();
			if (sameByteList.size() == 1)
			{
				iter1.remove();
			}
		}
		return bytesValueMap;
    }
    
}
