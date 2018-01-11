package finder.util;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.mat.snapshot.SnapshotInfo;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.LMinMax;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import finder.util.UtilClass.InstAttribute;
import finder.util.UtilClass.SameByteObjInfo;
import finder.util.UtilClass.SnapshotClassInfo;
import finder.util.UtilClass.SnapshotClassInfo.DominatorInfo;

public class CloudOutput
{
	public final static String CLOUDOP_DOMFILENAME = "Dominators.xml";
	public static class ClsInfo 
	{
		String clsName;
		long laddr;
		long heapSize;
		
		public ClsInfo(String clsname,long addr,long heapsize)
		{
			// TODO Auto-generated constructor stub
			this.clsName = clsname;
			this.laddr = addr;
			this.heapSize = heapsize;
		}
		
		public String getClassName()
		{
			return this.clsName;
		}
		
		public long getAddress()
		{
			return this.laddr;
		}
		
		public long getHeapSize()
		{
			return this.heapSize;
		}
	}
	
	private static CellProcessor[] getClsInfoProcessors()
	{
		final CellProcessor[] ps = new CellProcessor[]
			{
				new NotNull(),//类名
				new LMinMax(LMinMax.MIN_LONG,LMinMax.MAX_LONG),//地址
				new LMinMax(LMinMax.MIN_LONG,LMinMax.MAX_LONG)//对象大小
			};
		return ps;
	}
	
	public static void classInfoToCsv(String strFilePath,List<UtilClass.SnapshotClassInfo> clsInfoLst)
	{
		if(strFilePath == null || clsInfoLst.size() <= 0)
		{
			return;
		}
		
		ICsvBeanWriter bnWriter = null;
		try
		{
			bnWriter = new CsvBeanWriter(new FileWriter(strFilePath), CsvPreference.STANDARD_PREFERENCE);
			String[] headers = {"ClassName","Address","HeapSize"};
			bnWriter.writeHeader(headers);
			CellProcessor[] cps = getClsInfoProcessors();
			List<UtilClass.SnapshotClassInfo> needOutputDomCls = new ArrayList<UtilClass.SnapshotClassInfo>();
			for(UtilClass.SnapshotClassInfo info : clsInfoLst)
			{
				if (info.objs == null)
				{
					ClsInfo cinforw =  new ClsInfo(info.strName,-1,-1);
			        bnWriter.write(cinforw,headers,cps);
			        if (info.doers.size()>0)
					{
						//添加一个需要输出doer的class
			        	needOutputDomCls.add(info);
					}
			        continue;
				}
			    for(UtilClass.SnapshotClassInfo.SnapshotObjInfo objInfo : info.objs)
				{
			        ClsInfo cinforw =  new ClsInfo(info.strName,objInfo.laddr,objInfo.lSize);
			        bnWriter.write(cinforw,headers,cps);
				}
			}
			
			if (needOutputDomCls.size() > 0)
			{
				String trimFilePathString = strFilePath.trim();
				String doerFilePathString = trimFilePathString.substring(0,strFilePath.lastIndexOf(File.separator));
				clsDominatorsToXML(doerFilePathString+File.separator+CLOUDOP_DOMFILENAME, needOutputDomCls);
			}
			
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(bnWriter != null)
		{
			try
			{
				bnWriter.close();
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static class OutputObjInfo
	{
		String objName;
		long address;
		long heapsize;
		
		public OutputObjInfo(String name,long addr,long lsize)
		{
			this.objName = name;
			this.address = addr;
			this.heapsize = lsize;
		}
		
		public String getObjName()
		{
			return this.objName;
		}
		
		public long getObjAddress()
		{
			return this.address;
		}
		
		public long getObjSize()
		{
			return this.heapsize;
		}
	}
	
	private static CellProcessor[] getObjInfoProcessors()
	{
		final CellProcessor[] ps = new CellProcessor[]
			{
				new NotNull(),//类名
				new LMinMax(0L,LMinMax.MAX_LONG),//对象数量
				new LMinMax(0L,LMinMax.MAX_LONG)//对象大小
			};
		return ps;
	}
	
	public static void objInfosToCsv(String strFilePath,List<UtilClass.SnapshotClassInfo> clsInfoLst)
	{
		if(strFilePath == null || clsInfoLst.size() <= 0)
		{
			return;
		}
		
		ICsvBeanWriter bnWriter = null;
		try
		{
			bnWriter = new CsvBeanWriter(new FileWriter(strFilePath), CsvPreference.STANDARD_PREFERENCE);
			String[] headers = {"ObjName","ObjAddress","ObjSize"};
			bnWriter.writeHeader(headers);
			CellProcessor[] cps = getObjInfoProcessors();
			
			for(UtilClass.SnapshotClassInfo info : clsInfoLst)
			{
				for(UtilClass.SnapshotClassInfo.SnapshotObjInfo oInfo : info.objs)
				{
					OutputObjInfo oObjInfo =  new OutputObjInfo(oInfo.strName,oInfo.laddr,oInfo.lSize);
					bnWriter.write(oObjInfo,headers,cps);
				}
				
			}
			
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(bnWriter != null)
		{
			try
			{
				bnWriter.close();
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	
	public static class OutputCmpClsInfo
	{
		String clsName;
		long lIncRetainedSize;
		int iIncCount;
		
		public OutputCmpClsInfo(String name,long lrs,int iInc)
		{
			this.clsName = name;
			this.lIncRetainedSize = lrs;
			this.iIncCount = iInc;
		}
		
		public String getClassName()
		{
			return this.clsName;
		}
		
		public long getIncRetainedSize()
		{
			return this.lIncRetainedSize;
		}
		
		public int getIncCount()
		{
			return this.iIncCount;
		}
	}
	
	private static CellProcessor[] getCmpClsInfoProcessors()
	{
		final CellProcessor[] ps = new CellProcessor[]
			{
				new NotNull(),//类名
				new LMinMax(LMinMax.MIN_LONG,LMinMax.MAX_LONG),//增量大小
				new ParseInt()//增量个数
			};
		return ps;
	}
	
	public static void cmpClsInfoToCsv(String strFilePath,List<UtilClass.CmpClassInfo> cmpClsInfos)
	{
		if(strFilePath == null || cmpClsInfos.size() <= 0)
		{
			return;
		}
		
		ICsvBeanWriter bnWriter = null;
		try
		{
			bnWriter = new CsvBeanWriter(new FileWriter(strFilePath), CsvPreference.STANDARD_PREFERENCE);
			String[] headers = {"ClassName","IncRetainedSize","IncCount"};
			bnWriter.writeHeader(headers);
			CellProcessor[] ccps = getCmpClsInfoProcessors();
			
			for(UtilClass.CmpClassInfo ccinfo:cmpClsInfos)
			{
				OutputCmpClsInfo occinfo = new OutputCmpClsInfo(ccinfo.strClsName,ccinfo.lIncRetainedSize,
						ccinfo.iIncCount);
				bnWriter.write(occinfo,headers,ccps);
			}
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(bnWriter != null)
		{
			try
			{
				bnWriter.close();
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	private static CellProcessor[] getActivityListProcessors()
	{
		final CellProcessor[] ps = new CellProcessor[]
			{
				new NotNull(),//显示名
				new LMinMax(0,LMinMax.MAX_LONG),//大小
				new LMinMax(0,LMinMax.MAX_LONG),//地址
			};
		return ps;
	}
	
	public static class OutputActivityInfo
	{

		String activityName;
		long lSize;
		long lAddr;
		
		public OutputActivityInfo(String activityName,long lAddr,long lSize)
		{
			this.activityName = activityName;
			this.lSize = lSize;
			this.lAddr = lAddr;
		}
		
		public String getActivityName()
		{
			return this.activityName;
		}
		
		public long getActivityAddr()
		{
			return this.lAddr;
		}
		
		public long getActivitySize()
		{
			return this.lSize;
		}
	}
	
	public static void activityListToCsv(String strFilePath,List<UtilClass.ActivityInfo> ainfos)
	{
		if(strFilePath == null || ainfos.size() <= 0)
		{
			return;
		}
		
		ICsvBeanWriter bnWriter = null;
		try
		{
			bnWriter = new CsvBeanWriter(new FileWriter(strFilePath), CsvPreference.STANDARD_PREFERENCE);
			String[] headers = {"ActivityName","ActivityAddr","ActivitySize"};
			bnWriter.writeHeader(headers);
			CellProcessor[] ccps = getActivityListProcessors();
			
			for(UtilClass.ActivityInfo ainfo:ainfos)
			{
				OutputActivityInfo oainfo = new OutputActivityInfo(ainfo.strActivityName,ainfo.lAddr,ainfo.lSize);
				bnWriter.write(oainfo,headers,ccps);
			}
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(bnWriter != null)
		{
			try
			{
				bnWriter.close();
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static class OutputObjAttributesInfo
	{
		public String clazzName;
		public long laddrs;
		public List<InstAttribute> attributes;
	}
	
	private static String removeSpecialWords(String str)
	{
		if (str != null) {
			return str.replace('$','_');
		}
		else {
			return null;
		}
	}
	
    public static void objAttributesToXML(String strFilePath,List<OutputObjAttributesInfo> allObjInfos)
	{
		if(strFilePath == null || allObjInfos.size() <= 0)
		{
			return;
		}
		try
		{
			DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder xmlBuilder = xmlFactory.newDocumentBuilder();
			
			Document xmlDocument = xmlBuilder.newDocument();
			Element rootElement = xmlDocument.createElement("ClassInstanceAttr");
			xmlDocument.appendChild(rootElement);
			
			for(OutputObjAttributesInfo objInfo:allObjInfos)
			{
				Element objElement = xmlDocument.createElement(removeSpecialWords(objInfo.clazzName));
				objElement.setAttribute("Address", String.valueOf(objInfo.laddrs));
				rootElement.appendChild(objElement);
				
				for(InstAttribute attr:objInfo.attributes)
				{
					Element attrElement = xmlDocument.createElement("Attribute");
					attrElement.setAttribute("Name", attr.strAttrName);
					attrElement.setAttribute("Value", attr.strAttrValue);
					if (attr.strRefAddr != null) {
						attrElement.setAttribute("RefAddr", attr.strRefAddr);
					}
					
					if (attr.strType != null)
					{
						attrElement.setAttribute("Type", attr.strType);
					}
					objElement.appendChild(attrElement);
				}
			}

			TransformerFactory tfFactory = TransformerFactory.newInstance();
			Transformer tf = tfFactory.newTransformer();
			
			DOMSource dSource = new DOMSource(xmlDocument);
			StreamResult outputFileRet = new StreamResult(new File(strFilePath));
			
			tf.transform(dSource, outputFileRet);
			
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
    
    
    
    public static void gcPathsToXML(String strFilePath,List<UtilClass.GCInfo> gcPaths)
	{
		if(strFilePath == null || gcPaths.size() <= 0)
		{
			return;
		}
		try
		{
			DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder xmlBuilder = xmlFactory.newDocumentBuilder();
			
			Document xmlDocument = xmlBuilder.newDocument();
			Element rootElement = xmlDocument.createElement("GCPaths");
			xmlDocument.appendChild(rootElement);
			
			for(UtilClass.GCInfo gcInfo:gcPaths)
			{
				Element gcElement = xmlDocument.createElement("GCPath");
				rootElement.appendChild(gcElement);
//				if(gcInfo != null)
				for(UtilClass.SnapshotClassInfo.SnapshotObjInfo pathNode:gcInfo.gcNodesLst)
				{
					Element pathElement = xmlDocument.createElement("Path");
					pathElement.setAttribute("classname", pathNode.strName);
					pathElement.setAttribute("addr", Long.toString(pathNode.laddr));
					if(pathNode.strValue != null && pathNode.strValue.length() > 0)
					{
						//如果这个节点下是有值的，那么就要加一个子属性
						pathElement.setAttribute("value", pathNode.strValue);
					}
					gcElement.appendChild(pathElement);
				}
				
			}

			
			TransformerFactory tfFactory = TransformerFactory.newInstance();
			Transformer tf = tfFactory.newTransformer();
			
			DOMSource dSource = new DOMSource(xmlDocument);
			StreamResult outputFileRet = new StreamResult(new File(strFilePath));
			
			tf.transform(dSource, outputFileRet);
			
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
    
    public static void clsDominatorsToXML(String strFilePath,List<SnapshotClassInfo> clsinfos)
	{
		if(strFilePath == null || clsinfos.size() <= 0)
		{
			return;
		}
		
		try
		{
			DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder xmlBuilder = xmlFactory.newDocumentBuilder();
			
			Document xmlDocument = xmlBuilder.newDocument();
			Element rootElement = xmlDocument.createElement("ClassDominators");
			xmlDocument.appendChild(rootElement);
			
			for (SnapshotClassInfo doCls:clsinfos)
			{
				if (doCls.doers.size() == 0)
				{
					continue;
				}
				System.out.println("Class["+doCls.strName+"]'s Doer count is "+String.valueOf(doCls.doers.size()));
				Element doerClsElement = xmlDocument.createElement("DominatorItem");
				doerClsElement.setAttribute("ClassName", doCls.strName);
				rootElement.appendChild(doerClsElement);
				
				for(DominatorInfo doInfo:doCls.doers)
				{
					Element doerElement = xmlDocument.createElement("Dominator");
					doerElement.setAttribute("DominatorName", doInfo.strDoName);
					doerElement.setAttribute("DoerCount", Integer.toString(doInfo.iDoersCount));
					doerElement.setAttribute("DoedCount", Integer.toString(doInfo.iDoedCount));
					doerClsElement.appendChild(doerElement);
				}
			}

			
			TransformerFactory tfFactory = TransformerFactory.newInstance();
			Transformer tf = tfFactory.newTransformer();
			
			DOMSource dSource = new DOMSource(xmlDocument);
			StreamResult outputFileRet = new StreamResult(new File(strFilePath));
			
			tf.transform(dSource, outputFileRet);
			
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
    
	private static CellProcessor[] getBitmapListProcessors()
	{
		final CellProcessor[] ps = new CellProcessor[]
			{
				new NotNull(),//显示名
				new LMinMax(0,LMinMax.MAX_LONG),//地址
				new LMinMax(0,LMinMax.MAX_LONG),//大小
				new ParseInt(),//高
				new ParseInt()//宽
			};
		return ps;
	}
	
	public static class OutputBitmapInfo
	{

		String name;
		long lSize;
		long lAddr;
		int iHeight;
		int iWidth;
		
		public OutputBitmapInfo(String name,long lSize,long lAddr,int iHeight,int iWidth)
		{
			this.name = name;
			this.lSize = lSize;
			this.lAddr = lAddr;
			this.iHeight = iHeight;
			this.iWidth = iWidth;
		}
		
		public String getName()
		{
			return this.name;
		}
		
		public long getAddr()
		{
			return this.lAddr;
		}
		
		public long getSize()
		{
			return this.lSize;
		}
		
		public int getHeight()
		{
			return this.iHeight;
		}
		
		public int getWidth()
		{
			return this.iWidth;
		}
	}
    
    public static void bitmapInfoToCsv(String strFilePath,List<UtilClass.BitmapInfo> bitmapLst)
	{
		if(strFilePath == null || bitmapLst.size() <= 0)
		{
			return;
		}
		
		ICsvBeanWriter bnWriter = null;
		try
		{
			bnWriter = new CsvBeanWriter(new FileWriter(strFilePath), CsvPreference.STANDARD_PREFERENCE);
			String[] headers = {"Name","Addr","Size","Height","Width"};
			bnWriter.writeHeader(headers);
			CellProcessor[] ccps = getBitmapListProcessors();
			
			for(UtilClass.BitmapInfo binfo:bitmapLst)
			{
				OutputBitmapInfo oainfo = new OutputBitmapInfo("android.graphics.Bitmap",
						binfo.mSize,binfo.mAddr,binfo.mHeight,binfo.mWidth);
				bnWriter.write(oainfo,headers,ccps);
			}
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(bnWriter != null)
		{
			try
			{
				bnWriter.close();
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
    
    
    private static CellProcessor[] getSnapshotInfoCellProcessors()
    {
        final CellProcessor[] ps = new CellProcessor[]
            {
                new NotNull(),//显示名
                new NotNull(),//大小
                new ParseInt(),//对象数量
                new ParseInt(),//类数量
                new ParseInt()//根数量
            };
        return ps;
    }
    
    public static class OutputSnapshotInfo
    {
        String strSnapshotIndex;
        String fAllocationHeapSize;//单位kb
        int iObjectCount;
        int iClassCount;
        int iGCRootCount;
        
        public OutputSnapshotInfo(String strsi,String fahs,int ioc,int icc,int igcrc)
        {
            strSnapshotIndex = strsi;
            fAllocationHeapSize = fahs;
            iObjectCount = ioc;
            iClassCount = icc;
            iGCRootCount = igcrc;
        }
        
        public String getSnapshotIndex()
        {
            return strSnapshotIndex;
        }
        
        public String getAllocationHeapSize()
        {
            return fAllocationHeapSize;
        }
        
        public int getObjectCount()
        {
            return iObjectCount;
        }
        
        public int getClassCount()
        {
            return iClassCount;
        }
        
        public int getGCRootCount()
        {
            return iGCRootCount;
        }
    }
    
    public static void snapshotInfoToCsv(String strFilePath,Map<String,SnapshotInfo> snapShotInfos)
    {
        if(strFilePath == null || snapShotInfos.size() <= 0)
        {
            return;
        }
        
        ICsvBeanWriter sInfoWriter = null;
        try
        {
            sInfoWriter = new CsvBeanWriter(new FileWriter(strFilePath), CsvPreference.STANDARD_PREFERENCE);
            String[] headers = {"SnapshotIndex","AllocationHeapSize","ObjectCount","ClassCount","GCRootCount"};
            sInfoWriter.writeHeader(headers);
            CellProcessor[] ccps = getSnapshotInfoCellProcessors();
            
            Set<Map.Entry<String,SnapshotInfo>> set = snapShotInfos.entrySet();
            
            for(Iterator<Map.Entry<String,SnapshotInfo>> it = set.iterator();it.hasNext();)
            {
                Map.Entry<String, SnapshotInfo> entry = (Map.Entry<String, SnapshotInfo>) it.next();
                
                OutputSnapshotInfo sInfo = new OutputSnapshotInfo(entry.getKey(),
                        String.valueOf((double)entry.getValue().getUsedHeapSize()/1024),
                        entry.getValue().getNumberOfObjects(),
                        entry.getValue().getNumberOfClasses(),
                        entry.getValue().getNumberOfGCRoots());
                
                sInfoWriter.write(sInfo,headers,ccps);
            }
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        if(sInfoWriter != null)
        {
            try
            {
                sInfoWriter.close();
            } catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    public static class OutputSameBytesInfo
    {
        String strMD5;//md5值
        String strDisplayName;//名称
        long lAddr; //地址
        long lSize; //大小
        
        public OutputSameBytesInfo(String sMd5,String sDis,long ad,long sz)
        {
        	strMD5 = sMd5;
        	strDisplayName = sDis;
        	lAddr = ad;
        	lSize = sz;
        }
        
        public String getMD5()
        {
            return strMD5;
        }
        
        public String getDisplayName()
        {
            return strDisplayName;
        }
        
        public long getAddress()
        {
            return lAddr;
        }
        
        public long getSize()
        {
            return lSize;
        }
    }
    
	private static CellProcessor[] getSameBytesInfoProcessors()
	{
		final CellProcessor[] ps = new CellProcessor[]
			{
				new NotNull(),//md5
				new NotNull(),//DisplayName
				new LMinMax(0L,LMinMax.MAX_LONG),//地址
				new LMinMax(0L,LMinMax.MAX_LONG)//大小
			};
		return ps;
	}
	
    public static void sameBytesToCsv (String strFilePath,Map<String, List<SameByteObjInfo>> sbmap)
    {
		if(strFilePath == null || sbmap.size() <= 0)
		{
			return;
		}
		
		ICsvBeanWriter bnWriter = null;
		try
		{
			bnWriter = new CsvBeanWriter(new FileWriter(strFilePath), CsvPreference.STANDARD_PREFERENCE);
			String[] headers = {"MD5","DisplayName","Address","Size"};
			bnWriter.writeHeader(headers);
			CellProcessor[] cps = getSameBytesInfoProcessors();
			
			Iterator<Map.Entry<String, List<SameByteObjInfo>>> iter1 = sbmap.entrySet().iterator();
			while(iter1.hasNext())
			{
				Map.Entry<String, List<SameByteObjInfo>> ent = iter1.next();
				for(SameByteObjInfo sboi:ent.getValue())
				{
					OutputSameBytesInfo opsb = new OutputSameBytesInfo(ent.getKey(),
							sboi.strDisplayNameString, sboi.lAddr, sboi.lSize);
					bnWriter.write(opsb,headers,cps);
				}
			}	
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(bnWriter != null)
		{
			try
			{
				bnWriter.close();
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }
    
    
    public static class OutputMiniObjInfo
    {
        String strClassName = "";
        long lSize = 0; //大小
        int iCount = 0; //多少实例
        
        //long lAddr; //地址
        //String strParentAddr = ""; //父gc节点地址
        
        public OutputMiniObjInfo(String sClassName,long sz,int iCot)
        {        	
        	strClassName = sClassName;
        	//lAddr = ad;
        	lSize = sz;
        	iCount = iCot;
//        	if (strPads != null)
//			{
//        		for (long lPaddress:lPads)
//    			{
//            		strParentAddr += String.valueOf(lPaddress);
//            		strParentAddr += "#@#";
//    			}
//			}
//        	else
//        	{
				//strParentAddr = strPads;
//			}
        	
        }
        
        public String getClassName()
        {
            return strClassName;
        }
        
        public long getSize()
        {
            return lSize;
        }
        
        public int getCount()
        {
            return iCount;
        }
        
//        public long getAddress()
//        {
//            return lAddr;
//        }     
//        public String getParentAddress()
//		{
//			return strParentAddr;
//		}
    }
    
	private static CellProcessor[] getMiniObjInfoProcessors()
	{
		final CellProcessor[] ps = new CellProcessor[]
			{
				new NotNull(),//名称
				new LMinMax(0L,LMinMax.MAX_LONG),//大小
				new ParseInt()//多少个实例
				//new LMinMax(0L,LMinMax.MAX_LONG),//地址
				//new NotNull()//父gc节点地址
			};
		return ps;
	}
	
    public static void miniObjInfosToCsv(String strFilePath,List<UtilClass.MiniDumpClassObjInfo> miniObjInfos)
    {
		if(strFilePath == null || miniObjInfos.size() <= 0)
		{
			return;
		}
		
		ICsvBeanWriter bnWriter = null;
		try
		{
			bnWriter = new CsvBeanWriter(new FileWriter(strFilePath), CsvPreference.STANDARD_PREFERENCE);
			String[] headers = {"ClassName","Size","Count"};
			bnWriter.writeHeader(headers);
			CellProcessor[] cps = getMiniObjInfoProcessors();
			
			for (UtilClass.MiniDumpClassObjInfo miniObj:miniObjInfos)
			{
				OutputMiniObjInfo omoi = new OutputMiniObjInfo(miniObj.strClassName,
						miniObj.lSize,miniObj.iObjCount);
					bnWriter.write(omoi,headers,cps);
			}	
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(bnWriter != null)
		{
			try
			{
				bnWriter.close();
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		System.out.println("MiniDump end at:"+df.format(new Date()));
    }
    
    
}
