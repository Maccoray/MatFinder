package finder.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CloudConfigCenter
{
	//云命令
	public static class AnalyzerConfig
	{
		public static class ShowWhat
		{
			public String strShowWhat;
		}
		
		public static class ClassRule
		{//类规则
		    public String strRuleName = "";//规则名称
			public String strDevName = ""; //设备名称
			public String strClassName = ""; //类名称
			public int iMaxCount = 0; //最大数量
			public long lMaxSize = 0; //最大heapsize
			public boolean bShowGc = false; //是否展示gc路径
			public boolean bShowAttributes = false;//是否需要展示Attribtues
		}
		
		public static class ActivityRule
		{
		    public String strRuleName = "";//规则名称
			public String strDevName = ""; //设备名称
			public String activityName = "";//activity名称
			public int iMaxCount = 0;
			public boolean bShowGc = false; //是否需要展示GC路径
			public boolean bShowAttributes = false; //是否需要展示成员
		}
		
		public static class CompareRule
		{
		    public String strRuleName = "";//规则名称
			public String strDevName = ""; //设备名称
			public String strClassName = ""; //类名称
			public int iMaxIncCount = 0; //最大增加数量
			public long lMaxIncSize = 0; //最大增加
			public boolean bShowGc = false; //是否展示gc路径
		}
		
		public static class BitmapRule
		{
		    public String strRuleName = "";//规则名称
			public String strDevName = ""; //设备名称
			public int iMaxHeight = 0;//最大高
			public int iMaxWidth = 0;//最大宽
			public boolean bShowGc = false; //是否展示gc路径
		}
		
		public static class GCWhiteNode
		{
		    public String strRuleName = "";//规则名称
		    public String strDevName = ""; //设备名称
		    public String strNodeClassName = ""; //白名单节点
		    public String strNodeValue = ""; //专用于某些特定白名单，比如线程，就要使用线程名称来做过滤
		}
		
		public static class SameBytesRule
		{
			public String strRuleName = "";//规则名称
			public String strDevName = ""; //设备名称
			public long lMinSize = 0; //最小长度
			public long lMaxSize = 0; //最大长度
			public int iRepTimes = 0; //允许重复次数
			public boolean bShowGC = false; //是否展示GC
		}
		
		//展示项
		public List<ShowWhat> swLst = new ArrayList<ShowWhat>();
		//类规则
		public List<ClassRule> classRuleLst = new ArrayList<ClassRule>();
		//activity规则
		public List<ActivityRule> activityRuleLst = new ArrayList<ActivityRule>();
		//对比规则
		public List<CompareRule> cmpRuleLst = new ArrayList<CompareRule>();
		//位图规则
		public List<BitmapRule> btRuleLst = new ArrayList<BitmapRule>();
		//gc路径白名单
		public List<GCWhiteNode> gcWhiteNodeLst = new ArrayList<CloudConfigCenter.AnalyzerConfig.GCWhiteNode>();
		//SameBytes规则
		public List<SameBytesRule> sameBytesRuleLst = new ArrayList<CloudConfigCenter.AnalyzerConfig.SameBytesRule>();
	}
	
	static final String XMLRULENAME_MAINNode = "MemoryAnlyze";
	static final String XMLRULENAME_SUBNode = "BreakerRules";
	static final String XMLRULENAME_Activity ="ActivityRules";
	static final String XMLRULENAME_Cmp ="CmpRules";
	static final String XMLRULENAME_Class ="ClassRules";
	static final String XMLRULENAME_Bitmap ="BitmapRules";
	static final String XMLRULENAME_GC ="GCWhiteNode";
	static final String XMLRULENAME_SameBytes = "SameByteRules";
	
	static CloudConfigCenter mInstance = null;
	
	private AnalyzerConfig mac = null;
	
	private String mConfigPathString;
	
	public static CloudConfigCenter getInstance()
	{
		if(mInstance == null)
		{
			mInstance = new CloudConfigCenter();
		}
		
		return mInstance;
	}
	
	static Logger mLogCloudAnlyze = Logger.getLogger("[CloudConfigCenter]");
	
	private void initActivityRule(Node nBreakRuleType)
	{
		NodeList nlRules = nBreakRuleType.getChildNodes();
		for(int iii=0;iii<nlRules.getLength();iii++)//<Rule>
		{
			Node nRule = nlRules.item(iii);
			if(nRule.getNodeType() == Node.ELEMENT_NODE)
			{
				Element elRule = (Element)nRule;
				AnalyzerConfig.ActivityRule ar = new AnalyzerConfig.ActivityRule();
				ar.strRuleName = elRule.getAttribute("name");
				
				if(elRule.getElementsByTagName("DevName").getLength()>0)
			    {
				    if(elRule.getElementsByTagName("DevName").item(0).getFirstChild() != null)
				    {
				        ar.strDevName = elRule.getElementsByTagName("DevName").item(0).getFirstChild().getNodeValue();
				    }
			    }
				if(elRule.getElementsByTagName("Activity").getLength()>0)
				    ar.activityName = elRule.getElementsByTagName("Activity").item(0).getFirstChild().getNodeValue();
				if(elRule.getElementsByTagName("MaxCount").getLength()>0)
				    ar.iMaxCount = Integer.valueOf(elRule.getElementsByTagName("MaxCount").item(0).getFirstChild().getNodeValue());
				if(elRule.getElementsByTagName("ShowGCPath").getLength()>0)
				    ar.bShowGc = Boolean.valueOf(elRule.getElementsByTagName("ShowGCPath").item(0).getFirstChild().getNodeValue());
				if(elRule.getElementsByTagName("ShowAttributes").getLength()>0)
				{
					ar.bShowAttributes = Boolean.valueOf(elRule.getElementsByTagName("ShowAttributes").item(0).getFirstChild().getNodeValue());
				}
				mac.activityRuleLst.add(ar);
			}
		}
	}
	
	private void initClsRule(Node nBreakRuleType)
	{
		NodeList nlRules = nBreakRuleType.getChildNodes();
		for(int iii=0;iii<nlRules.getLength();iii++)//<Rule>
		{
			Node nRule = nlRules.item(iii);
			if(nRule.getNodeType() == Node.ELEMENT_NODE)
			{
			    Element elRule = (Element)nRule;
				AnalyzerConfig.ClassRule cr = new AnalyzerConfig.ClassRule();
				cr.strRuleName = elRule.getAttribute("name");
				
				if(elRule.getElementsByTagName("DevName").getLength()>0)
				{
                    if(elRule.getElementsByTagName("DevName").item(0).getFirstChild() != null)
                    {
                        cr.strDevName = elRule.getElementsByTagName("DevName").item(0).getFirstChild().getNodeValue();
                    }
                }
				if(elRule.getElementsByTagName("Name").getLength()>0)
				    cr.strClassName = elRule.getElementsByTagName("Name").item(0).getFirstChild().getNodeValue();
				if(elRule.getElementsByTagName("MaxCount").getLength()>0)
				    cr.iMaxCount = Integer.valueOf(elRule.getElementsByTagName("MaxCount").item(0).getFirstChild().getNodeValue());
				if(elRule.getElementsByTagName("MaxSize").getLength()>0)
				    cr.lMaxSize = Long.valueOf(elRule.getElementsByTagName("MaxSize").item(0).getFirstChild().getNodeValue());
				if(elRule.getElementsByTagName("ShowGCPath").getLength()>0)
				    cr.bShowGc = Boolean.valueOf(elRule.getElementsByTagName("ShowGCPath").item(0).getFirstChild().getNodeValue());
				if(elRule.getElementsByTagName("ShowAttributes").getLength()>0)
				    cr.bShowAttributes = Boolean.valueOf(elRule.getElementsByTagName("ShowAttributes").item(0).getFirstChild().getNodeValue());
				
				mac.classRuleLst.add(cr);
			}
		}
	}
	
	private void initBitmapRule(Node nBreakRuleType)
	{
		NodeList nlRules = nBreakRuleType.getChildNodes();
		for(int iii=0;iii<nlRules.getLength();iii++)//<Rule>
		{
			Node nRule = nlRules.item(iii);
			if(nRule.getNodeType() == Node.ELEMENT_NODE)
			{
				AnalyzerConfig.BitmapRule bRule = new AnalyzerConfig.BitmapRule();
				Element elRule = (Element)nRule;
				bRule.strRuleName = elRule.getAttribute("name");
				
				if(elRule.getElementsByTagName("DevName").getLength()>0)
				{
				    if(elRule.getElementsByTagName("DevName").item(0).getFirstChild() != null)
                    {
				        bRule.strDevName = elRule.getElementsByTagName("DevName").item(0).getFirstChild().getNodeValue();
                    }
				}
				if(elRule.getElementsByTagName("MaxHeight").getLength()>0)
				    bRule.iMaxHeight = Integer.valueOf(elRule.getElementsByTagName("MaxHeight").item(0).getFirstChild().getNodeValue());
				if(elRule.getElementsByTagName("MaxWidth").getLength()>0)
				    bRule.iMaxWidth = Integer.valueOf(elRule.getElementsByTagName("MaxWidth").item(0).getFirstChild().getNodeValue());
				if(elRule.getElementsByTagName("ShowGCPath").getLength()>0)
				    bRule.bShowGc = Boolean.valueOf(elRule.getElementsByTagName("ShowGCPath").item(0).getFirstChild().getNodeValue());
				mac.btRuleLst.add(bRule);
			}
		}
	}
	
	private void initCmpRule(Node nBreakRuleType)
	{
		NodeList nlRules = nBreakRuleType.getChildNodes();
		for(int iii=0;iii<nlRules.getLength();iii++)//<Rule>
		{
			Node nRule = nlRules.item(iii);
			if(nRule.getNodeType() == Node.ELEMENT_NODE)
			{
				AnalyzerConfig.CompareRule cr = new AnalyzerConfig.CompareRule();
				Element elRule = (Element)nRule;
				cr.strRuleName = elRule.getAttribute("name");
				
				if(elRule.getElementsByTagName("DevName").getLength()>0)
				{
				    if(elRule.getElementsByTagName("DevName").item(0).getFirstChild() != null)
				    {
				        cr.strDevName = elRule.getElementsByTagName("DevName").item(0).getFirstChild().getNodeValue();
				    }
				}
				    
				if(elRule.getElementsByTagName("Name").getLength()>0)
				    cr.strClassName = elRule.getElementsByTagName("Name").item(0).getFirstChild().getNodeValue();
				if(elRule.getElementsByTagName("MaxIncCount").getLength()>0)
				    cr.iMaxIncCount = Integer.valueOf(elRule.getElementsByTagName("MaxIncCount").item(0).getFirstChild().getNodeValue());
				if(elRule.getElementsByTagName("MaxIncSize").getLength()>0)
				    cr.lMaxIncSize = Long.valueOf(elRule.getElementsByTagName("MaxIncSize").item(0).getFirstChild().getNodeValue());
				if(elRule.getElementsByTagName("ShowGCPath").getLength()>0)
				    cr.bShowGc = Boolean.valueOf(elRule.getElementsByTagName("ShowGCPath").item(0).getFirstChild().getNodeValue());
				
				mac.cmpRuleLst.add(cr);
			}
		}
	}
	
	private void initGCRule(Node nBreakRuleType)
	{
        NodeList nlRules = nBreakRuleType.getChildNodes();
        for(int iii=0;iii<nlRules.getLength();iii++)//<Rule>
        {
            Node nRule = nlRules.item(iii);
            
            if(nRule.getNodeType() == Node.ELEMENT_NODE)
            {
                AnalyzerConfig.GCWhiteNode gcNode = new AnalyzerConfig.GCWhiteNode();
                Element elRule = (Element)nRule;
                gcNode.strRuleName = elRule.getAttribute("name");
                
                if(elRule.getElementsByTagName("DevName").getLength()>0)
                {
                    if(elRule.getElementsByTagName("DevName").item(0).getFirstChild() != null)
                    {
                        gcNode.strDevName = elRule.getElementsByTagName("DevName").item(0).getFirstChild().getNodeValue();
                    }
                }
                
                if(elRule.getElementsByTagName("Name").getLength()>0)
                    gcNode.strNodeClassName = elRule.getElementsByTagName("Name").item(0).getFirstChild().getNodeValue();
                
                if(elRule.getElementsByTagName("Value").getLength()>0)
                {
                    if(elRule.getElementsByTagName("Value").item(0).getFirstChild() != null)
                    {
                    	gcNode.strNodeValue = elRule.getElementsByTagName("Value").item(0).getFirstChild().getNodeValue();
                    }
                }
                
                mac.gcWhiteNodeLst.add(gcNode);
            }
        }	
	}
	
	private void initSameBytesRule(Node nBreakRuleType)
	{
		NodeList nlRules = nBreakRuleType.getChildNodes();
		for(int iii=0;iii<nlRules.getLength();iii++)//<Rule>
		{
			Node nRule = nlRules.item(iii);
			if(nRule.getNodeType() == Node.ELEMENT_NODE)
			{
				AnalyzerConfig.SameBytesRule sbr = new AnalyzerConfig.SameBytesRule();
				Element elRule = (Element)nRule;
				sbr.strRuleName = elRule.getAttribute("name");
				
				if(elRule.getElementsByTagName("DevName").getLength()>0)
				{
				    if(elRule.getElementsByTagName("DevName").item(0).getFirstChild() != null)
                    {
				    	sbr.strDevName = elRule.getElementsByTagName("DevName").item(0).getFirstChild().getNodeValue();
                    }
				}
				if(elRule.getElementsByTagName("MaxSize").getLength()>0)
					sbr.lMaxSize = Long.valueOf(elRule.getElementsByTagName("MaxSize").item(0).getFirstChild().getNodeValue());
				if(elRule.getElementsByTagName("MinSize").getLength()>0)
					sbr.lMinSize = Long.valueOf(elRule.getElementsByTagName("MinSize").item(0).getFirstChild().getNodeValue());
				if(elRule.getElementsByTagName("RepTimes").getLength()>0)
				    sbr.iRepTimes = Integer.valueOf(elRule.getElementsByTagName("RepTimes").item(0).getFirstChild().getNodeValue());
				if(elRule.getElementsByTagName("ShowGC").getLength()>0)
					sbr.bShowGC = Boolean.valueOf(elRule.getElementsByTagName("ShowGC").item(0).getFirstChild().getNodeValue());
				mac.sameBytesRuleLst.add(sbr);
			}
		}
	}
	
	//初始化配置中心
    public void init(String strParamXmlPath)
    {
        mConfigPathString = strParamXmlPath;
        
		File fParamXml = new File(strParamXmlPath);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		
		try
		{
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fParamXml);
			Element elroot = doc.getDocumentElement();
			String strMemRulesString = elroot.getNodeName();

			if(!strMemRulesString.equals(XMLRULENAME_MAINNode))
			{
				mLogCloudAnlyze.info("No [MemoryAnlyze] Element in params.xml");
				return;
			}
		
			mac = new AnalyzerConfig();
			
			//在MemoryAnlyze根节点下，本期生效的只有show节点下的内容
			if(elroot.hasChildNodes())
			{
				NodeList nl = elroot.getChildNodes();
				for(int i=0;i<nl.getLength();i++)
				{
					//获取参数
					Node nd = nl.item(i);
					//第一期功能，先找到show功能
					if(nd.getNodeType() == Node.ELEMENT_NODE)
					{
						//如果存在show段，解读show中命令
						if(nd.getNodeName().equals("Show"))//<Show>
						{
							NodeList nlShow = nd.getChildNodes();
							for(int ii = 0; ii < nlShow.getLength(); ii++)
							{
								Node ndShow = nlShow.item(ii);
								//必须是元素，而且还要有多于一条的rule
								if(ndShow.getNodeType() == Node.ELEMENT_NODE)
								{
									String strShowWhat = ndShow.getNodeName();
									AnalyzerConfig.ShowWhat sw = new AnalyzerConfig.ShowWhat();
									sw.strShowWhat = strShowWhat;
									mac.swLst.add(sw);
									continue;
								}
							}
							continue;
						}

						//如果存在break rule段，解读rules
						if(nd.getNodeName().equals(XMLRULENAME_SUBNode))//<BreakerRules>
						{
							NodeList nlBreakRule = nd.getChildNodes();
							for(int ii=0;ii<nlBreakRule.getLength();ii++)
							{
								Node nBreakRuleType = nlBreakRule.item(ii);
								
								if(nBreakRuleType.getNodeType() == Node.ELEMENT_NODE &&
										nBreakRuleType.getNodeName().equals(XMLRULENAME_Class))//classrule
								{
									initClsRule(nBreakRuleType);
									continue;
								}
								
								if(nBreakRuleType.getNodeType() == Node.ELEMENT_NODE &&
										nBreakRuleType.getNodeName().equals(XMLRULENAME_Bitmap))//BitmapRules
								{

									initBitmapRule(nBreakRuleType);
									continue;
								}
								
								if(nBreakRuleType.getNodeType() == Node.ELEMENT_NODE &&
										nBreakRuleType.getNodeName().equals(XMLRULENAME_Activity))//activityrule
								{
									initActivityRule(nBreakRuleType);
									continue;
								}
								
								
								if(nBreakRuleType.getNodeType() == Node.ELEMENT_NODE &&
										nBreakRuleType.getNodeName().equals(XMLRULENAME_Cmp))//comparerule
								{
									initCmpRule(nBreakRuleType);
									continue;
								}
								
                                if(nBreakRuleType.getNodeType() == Node.ELEMENT_NODE &&
                                        nBreakRuleType.getNodeName().equals(XMLRULENAME_GC))//gc
                                {
                                	initGCRule(nBreakRuleType);
                                }
                                
                                if (nBreakRuleType.getNodeType() == Node.ELEMENT_NODE &&
                                        nBreakRuleType.getNodeName().equals(XMLRULENAME_SameBytes))//samebytes
								{
                                	initSameBytesRule(nBreakRuleType);
								}
							}
						}
	
					}
					else
					{
						mLogCloudAnlyze.info("No [Show] Element or [Show] Element has no node");
						continue;
					}
				}
				
				return;
			}
			else
			{
				mLogCloudAnlyze.info("[MemoryAnlyze] Element has no node");
				return;
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
    }

//    public boolean isHaveActivityExecution()
//	{
//    	if(mac == null)
//    	{
//    		return false;
//    	}
//    	
//    	if(mac.activityRuleLst.size()>0)
//    	{//activity规则比较特殊，如果没有配规则，就表示所有的都是违规
//    		return true;
//    	}
//    	
//    	if(mac.swLst.size()>0)
//    	{
//    		for(AnalyzerConfig.ShowWhat sw:mac.swLst)
//    		{
//    			if(sw.strShowWhat.equals("ActivityList"))
//    			{
//    				return true;
//    			}
//    		}
//    	}
//    	return false;
//	}
    
//    public boolean isHaveBitmapExecution()
//    {
//    	if(mac == null)
//    	{
//    		return false;
//    	}
//    	
//    	if(mac.btRuleLst.size()> 0)
//    	{
//    		return true;
//    	}
//    	
//    	return false;
//    }
//    
//    public boolean isHaveCompareExectution()
//    {
//    	if(mac == null)
//    	{
//    		return false;
//    	}
//    	
//    	if(mac.cmpRuleLst.size()>0)
//    	{
//    		return true;
//    	}
//    	
//    	return false;
//    }
    
    
    /*public boolean isNeedShowClassList()
	{
    	if(mac == null)
    	{
    		return false;
    	}
    	
    	for(AnalyzerConfig.ShowWhat sw:mac.swLst)
    	{
    		if(sw.strShowWhat.equals("ClassesList"))
    		{
    			return true;
    		}
    	}
    	
    	return false;
	}
    
    
    public boolean isNeedShowObjList()
	{
    	if(mac == null)
    	{
    		return false;
    	}
	
    	for(AnalyzerConfig.ShowWhat sw:mac.swLst)
    	{
    		if(sw.strShowWhat.equals("ObjectList"))
    		{
    			return true;
    		}
    	}
    	
    	return false;
	}
    
    
    public boolean isNeedShowCmpList()
	{
    	if(mac == null)
    	{
    		return false;
    	}
    	
    	for(AnalyzerConfig.ShowWhat sw:mac.swLst)
    	{
    		if(sw.strShowWhat.equals("CmpList"))
    		{
    			return true;
    		}
    	}
    	
    	return false;
	}
    
    
    public boolean isNeedShowActivityList()
	{
    	if(mac == null)
    	{
    		return false;
    	}
    	
    	for(AnalyzerConfig.ShowWhat sw:mac.swLst)
    	{
    		if(sw.strShowWhat.equals("ActivityList"))
    		{
    			return true;
    		}
    	}
    	
    	return false;
	}
    
    public boolean isNeedShowBitmaps()
    {
    	if(mac == null)
    	{
    		return false;
    	}
    	
    	for(AnalyzerConfig.ShowWhat sw:mac.swLst)
    	{
    		if(sw.strShowWhat.equals("BitmapList"))
    		{
    			return true;
    		}
    	}
    	
    	return false;
    }*/
    
    //返回类规则列表
    public List<AnalyzerConfig.ClassRule> getClassRules()
    {
		if(mac == null)
		{
			return null;
		}
		
		return mac.classRuleLst;
    }
    
    //获取show内容
    public List<AnalyzerConfig.ShowWhat> getShowWhat()
	{
		if(mac == null)
		{
			return null;
		}
		
		return mac.swLst;
	}
    
    public List<AnalyzerConfig.ActivityRule> getActivityRules()
	{
		if(mac == null)
		{
			return null;
		}
		
		return mac.activityRuleLst;
	}
    
    public List<AnalyzerConfig.CompareRule> getCompareRules()
	{
		if(mac == null)
		{
			return null;
		}
		
		return mac.cmpRuleLst;
	}
    
    public List<AnalyzerConfig.BitmapRule> getBitmapRule()
    {
		if(mac == null)
		{
			return null;
		}
		
		return mac.btRuleLst;
    }
    
    public List<AnalyzerConfig.SameBytesRule> getSameBytesRules()
    {
    	if(mac == null)
    	{
    		return null;
    	}
    	return mac.sameBytesRuleLst;
    }
    
    public List<AnalyzerConfig.GCWhiteNode> getGCWhiteNodes()
    {
        if(mac == null)
        {
            return null;
        }
        return mac.gcWhiteNodeLst;
    }
    
    public void addActivityRule(AnalyzerConfig.ActivityRule rule)
    {
        if(rule != null && mac != null)
        {
            mac.activityRuleLst.add(rule);
        }
    }
    
    public void addBitmapRule(AnalyzerConfig.BitmapRule rule)
    {
        if(rule != null && mac != null)
        {
            mac.btRuleLst.add(rule);
        }
    }
    
    public void addClassRule(AnalyzerConfig.ClassRule rule)
    {
        if(rule != null && mac != null)
        {
            mac.classRuleLst.add(rule);
        }
    }
    
    public void addCompareRule(AnalyzerConfig.CompareRule rule)
    {
        if(rule != null && mac != null)
        {
            mac.cmpRuleLst.add(rule);
        }
    }
    
    public void addGCWhiteNode(AnalyzerConfig.GCWhiteNode rule)
    {
        if(rule != null && mac != null)
        {
            mac.gcWhiteNodeLst.add(rule);
        }
    }
    
    public void addSameByteRule(AnalyzerConfig.SameBytesRule rule)
    {
    	if (rule != null && mac != null)
		{
			mac.sameBytesRuleLst.add(rule);
		}
    }
    
    public void deleteRule(Object rule)
    {
        if(mac == null)
        {
            return;
        }
        
        if(rule instanceof CloudConfigCenter.AnalyzerConfig.ActivityRule)
        {
            for(Iterator<CloudConfigCenter.AnalyzerConfig.ActivityRule> itor=mac.activityRuleLst.iterator();
                    itor.hasNext();)
            {
                CloudConfigCenter.AnalyzerConfig.ActivityRule ar = itor.next();
                if(ar.activityName.equals(((CloudConfigCenter.AnalyzerConfig.ActivityRule)rule).activityName))
                {
                    itor.remove();
                }
            }
        }
        
        if(rule instanceof CloudConfigCenter.AnalyzerConfig.CompareRule)
        {
            for(Iterator<CloudConfigCenter.AnalyzerConfig.CompareRule> itor=mac.cmpRuleLst.iterator();
                    itor.hasNext();)
            {
                CloudConfigCenter.AnalyzerConfig.CompareRule ccr = itor.next();
                if(ccr.strClassName.equals(((CloudConfigCenter.AnalyzerConfig.CompareRule)rule).strClassName))
                {
                    itor.remove();
                }
            }
        }
        
        if(rule instanceof CloudConfigCenter.AnalyzerConfig.ClassRule)
        {
            for(Iterator<CloudConfigCenter.AnalyzerConfig.ClassRule> itor=mac.classRuleLst.iterator();
                    itor.hasNext();)
            {
                CloudConfigCenter.AnalyzerConfig.ClassRule cr = itor.next();
                if(cr.strClassName.equals(((CloudConfigCenter.AnalyzerConfig.ClassRule)rule).strClassName))
                {
                    itor.remove();
                }
            }
        }
        
        if(rule instanceof CloudConfigCenter.AnalyzerConfig.BitmapRule)
        {
            for(Iterator<CloudConfigCenter.AnalyzerConfig.BitmapRule> itor=mac.btRuleLst.iterator();
                    itor.hasNext();)
            {
                CloudConfigCenter.AnalyzerConfig.BitmapRule br = itor.next();
                if(br.strDevName.equals(((CloudConfigCenter.AnalyzerConfig.BitmapRule)rule).strDevName))
                {
                    itor.remove();
                }
            }
        }
        
        if(rule instanceof CloudConfigCenter.AnalyzerConfig.GCWhiteNode)
        {
            for(Iterator<CloudConfigCenter.AnalyzerConfig.GCWhiteNode> itor=mac.gcWhiteNodeLst.iterator();
                    itor.hasNext();)
            {
            	CloudConfigCenter.AnalyzerConfig.GCWhiteNode gcr = itor.next();
                if(gcr.strNodeClassName.equals(((CloudConfigCenter.AnalyzerConfig.GCWhiteNode)rule).strNodeClassName))
                {
                    itor.remove();
                }
            }
        }
        
        if(rule instanceof CloudConfigCenter.AnalyzerConfig.SameBytesRule)
        {
            for(Iterator<CloudConfigCenter.AnalyzerConfig.SameBytesRule> itor=mac.sameBytesRuleLst.iterator();
                    itor.hasNext();)
            {
            	CloudConfigCenter.AnalyzerConfig.SameBytesRule sbr = itor.next();
                if(sbr.strRuleName.equals(((CloudConfigCenter.AnalyzerConfig.SameBytesRule)rule).strRuleName))
                {
                    itor.remove();
                }
            }
        }
    }
    
    public void flashClassRule(Document xmlDocument,Element ruleRootElement)
    {
        Element classRuleElement = xmlDocument.createElement(XMLRULENAME_Class);
        ruleRootElement.appendChild(classRuleElement);
        for(CloudConfigCenter.AnalyzerConfig.ClassRule cr:mac.classRuleLst)
        {
            Element ruleElement = xmlDocument.createElement("Rule");
            ruleElement.setAttribute("name", cr.strRuleName);
            classRuleElement.appendChild(ruleElement);
            
            Element devElement = xmlDocument.createElement("DevName");
            devElement.setTextContent(cr.strDevName);
            Element nameElement = xmlDocument.createElement("Name");
            nameElement.setTextContent(cr.strClassName);
            Element countElement = xmlDocument.createElement("MaxCount");
            countElement.setTextContent(String.valueOf(cr.iMaxCount));
            Element sizeElement = xmlDocument.createElement("MaxSize");
            sizeElement.setTextContent(String.valueOf(cr.lMaxSize));
            Element gcElement = xmlDocument.createElement("ShowGCPath");
            gcElement.setTextContent(String.valueOf(cr.bShowGc));
            Element attrElement = xmlDocument.createElement("ShowAttributes");
            attrElement.setTextContent(String.valueOf(cr.bShowAttributes));
            
            ruleElement.appendChild(devElement);
            ruleElement.appendChild(nameElement);
            ruleElement.appendChild(countElement);
            ruleElement.appendChild(sizeElement);
            ruleElement.appendChild(gcElement); 
            ruleElement.appendChild(attrElement);
            
        }
    }
    
    public void flashActivityRule(Document xmlDocument,Element ruleRootElement)
    {
        Element activityRuleElement = xmlDocument.createElement(XMLRULENAME_Activity);
        ruleRootElement.appendChild(activityRuleElement);
        
        for(CloudConfigCenter.AnalyzerConfig.ActivityRule ar:mac.activityRuleLst)
        {
            Element ruleElement = xmlDocument.createElement("Rule");
            ruleElement.setAttribute("name", ar.strRuleName);
            activityRuleElement.appendChild(ruleElement);
            
            Element devElement = xmlDocument.createElement("DevName");
            devElement.setTextContent(ar.strDevName);
            Element activityElement = xmlDocument.createElement("Activity");
            activityElement.setTextContent(ar.activityName);
            //开发需要增加白名单activity个数限制，不能有多余2个的重复activity出现
            Element countElement = xmlDocument.createElement("MaxCount");
            countElement.setTextContent(String.valueOf(ar.iMaxCount));
            
            Element gcElement = xmlDocument.createElement("ShowGCPath");
            gcElement.setTextContent(String.valueOf(ar.bShowGc));
            
            Element attributeElement = xmlDocument.createElement("ShowAttributes");
            attributeElement.setTextContent(String.valueOf(ar.bShowAttributes));
            
            ruleElement.appendChild(devElement);
            ruleElement.appendChild(activityElement);
            ruleElement.appendChild(countElement);
            ruleElement.appendChild(gcElement);  
            ruleElement.appendChild(attributeElement);
        }
    }
    
    public void flashCmpRule(Document xmlDocument,Element ruleRootElement)
    {
        Element cmpRuleElement = xmlDocument.createElement(XMLRULENAME_Cmp);
        ruleRootElement.appendChild(cmpRuleElement);
        for(CloudConfigCenter.AnalyzerConfig.CompareRule ccr:mac.cmpRuleLst)
        {
            Element ruleElement = xmlDocument.createElement("Rule");
            ruleElement.setAttribute("name", ccr.strRuleName);
            cmpRuleElement.appendChild(ruleElement);
            
            Element devElement = xmlDocument.createElement("DevName");
            devElement.setTextContent(ccr.strDevName);
            Element nameElement = xmlDocument.createElement("Name");
            nameElement.setTextContent(ccr.strClassName);
            Element countElement = xmlDocument.createElement("MaxIncCount");
            countElement.setTextContent(String.valueOf(ccr.iMaxIncCount));
            Element sizeElement = xmlDocument.createElement("MaxIncSize");
            sizeElement.setTextContent(String.valueOf(ccr.lMaxIncSize));
            Element gcElement = xmlDocument.createElement("ShowGCPath");
            gcElement.setTextContent(String.valueOf(ccr.bShowGc));
            
            ruleElement.appendChild(devElement);
            ruleElement.appendChild(nameElement);
            ruleElement.appendChild(countElement);
            ruleElement.appendChild(sizeElement);
            ruleElement.appendChild(gcElement);
        }
    }
    
    public void flashBitmapRule(Document xmlDocument,Element ruleRootElement)
    {
        Element bitmapRuleElement = xmlDocument.createElement(XMLRULENAME_Bitmap);
        ruleRootElement.appendChild(bitmapRuleElement);
        
        for(CloudConfigCenter.AnalyzerConfig.BitmapRule br:mac.btRuleLst)
        {
            Element ruleElement = xmlDocument.createElement("Rule");
            ruleElement.setAttribute("name", br.strRuleName);
            bitmapRuleElement.appendChild(ruleElement);
            
            Element devElement = xmlDocument.createElement("DevName");
            devElement.setTextContent(br.strDevName);
            Element heightElement = xmlDocument.createElement("MaxHeight");
            heightElement.setTextContent(String.valueOf(br.iMaxHeight));
            Element widthElement = xmlDocument.createElement("MaxWidth");
            widthElement.setTextContent(String.valueOf(br.iMaxWidth));
            Element gcElement = xmlDocument.createElement("ShowGCPath");
            gcElement.setTextContent(String.valueOf(br.bShowGc));
            
            ruleElement.appendChild(devElement);
            ruleElement.appendChild(heightElement);
            ruleElement.appendChild(widthElement);
            ruleElement.appendChild(gcElement);
            
            
        }
    }
    
    public void flashGCWhiteNodes(Document xmlDocument,Element ruleRootElement)
    {
        Element gcWhiteNodeElement = xmlDocument.createElement(XMLRULENAME_GC);
        ruleRootElement.appendChild(gcWhiteNodeElement);
        for(CloudConfigCenter.AnalyzerConfig.GCWhiteNode gcNode:mac.gcWhiteNodeLst)
        {
            Element ruleElement = xmlDocument.createElement("Rule");
            ruleElement.setAttribute("name", gcNode.strRuleName);
            gcWhiteNodeElement.appendChild(ruleElement);
            
            Element devElement = xmlDocument.createElement("DevName");
            devElement.setTextContent(gcNode.strDevName);
            
            Element nameElement = xmlDocument.createElement("Name");
            nameElement.setTextContent(gcNode.strNodeClassName);
            
            Element valueElement = xmlDocument.createElement("Value");
            valueElement.setTextContent(gcNode.strNodeValue);
            
            ruleElement.appendChild(devElement);
            ruleElement.appendChild(nameElement);
            ruleElement.appendChild(valueElement);
        }
    }
    
    public void flashSameBytesRule(Document xmlDocument,Element ruleRootElement)
    {
        Element sameBytesRuleElement = xmlDocument.createElement(XMLRULENAME_SameBytes);
        ruleRootElement.appendChild(sameBytesRuleElement);
        
        for(CloudConfigCenter.AnalyzerConfig.SameBytesRule sbr:mac.sameBytesRuleLst)
        {
            Element ruleElement = xmlDocument.createElement("Rule");
            ruleElement.setAttribute("name", sbr.strRuleName);
            sameBytesRuleElement.appendChild(ruleElement);
            
            Element devElement = xmlDocument.createElement("DevName");
            devElement.setTextContent(sbr.strDevName);
            Element maxSizeElement = xmlDocument.createElement("MaxSize");
            maxSizeElement.setTextContent(String.valueOf(sbr.lMaxSize));
            Element minSizeElement = xmlDocument.createElement("MinSize");
            minSizeElement.setTextContent(String.valueOf(sbr.lMinSize));
            Element repTimeElement = xmlDocument.createElement("RepTimes");
            repTimeElement.setTextContent(String.valueOf(sbr.iRepTimes));
            Element showGCElement = xmlDocument.createElement("ShowGC");
            showGCElement.setTextContent(String.valueOf(sbr.bShowGC));
            
            ruleElement.appendChild(devElement);
            ruleElement.appendChild(maxSizeElement);
            ruleElement.appendChild(minSizeElement);
            ruleElement.appendChild(repTimeElement);
            ruleElement.appendChild(showGCElement);
        }
    }
    
    public void flashConfigFile()
    {
        if(mConfigPathString == null || mConfigPathString.length() <= 0 || mac == null)
        {
            return;
        }
        try
        {
            DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder xmlBuilder = xmlFactory.newDocumentBuilder();
            
            Document xmlDocument = xmlBuilder.newDocument();
            Element rootElement = xmlDocument.createElement(XMLRULENAME_MAINNode);
            xmlDocument.appendChild(rootElement);
            
            Element ruleRootElement = xmlDocument.createElement(XMLRULENAME_SUBNode);
            rootElement.appendChild(ruleRootElement);
            
            flashActivityRule(xmlDocument, ruleRootElement);
            flashBitmapRule(xmlDocument, ruleRootElement);
            flashClassRule(xmlDocument, ruleRootElement);
            flashCmpRule(xmlDocument, ruleRootElement);
            flashGCWhiteNodes(xmlDocument, ruleRootElement);
            flashSameBytesRule(xmlDocument, ruleRootElement);
            
            TransformerFactory tfFactory = TransformerFactory.newInstance();
            Transformer tf = tfFactory.newTransformer();
            
            DOMSource dSource = new DOMSource(xmlDocument);
            StreamResult outputFileRet = new StreamResult(new File(mConfigPathString));
            
            tf.transform(dSource, outputFileRet);
            
        } catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }   
    }
    
    public void newConfigFile(String strConfigPath)
    {
        if(strConfigPath == null || strConfigPath.length() <= 0)
        {
            return;
        }
        try
        {
            DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder xmlBuilder = xmlFactory.newDocumentBuilder();
            
            Document xmlDocument = xmlBuilder.newDocument();
            Element rootElement = xmlDocument.createElement(XMLRULENAME_MAINNode);
            xmlDocument.appendChild(rootElement);
            
            Element ruleRootElement = xmlDocument.createElement(XMLRULENAME_SUBNode);
            rootElement.appendChild(ruleRootElement);
            
            Element classRuleElement = xmlDocument.createElement(XMLRULENAME_Class);
            ruleRootElement.appendChild(classRuleElement);
            
            Element bitmapRuleElement = xmlDocument.createElement(XMLRULENAME_Bitmap);
            ruleRootElement.appendChild(bitmapRuleElement);
            
            Element cmpRuleElement = xmlDocument.createElement(XMLRULENAME_Cmp);
            ruleRootElement.appendChild(cmpRuleElement);
            
            Element activityRuleElement = xmlDocument.createElement(XMLRULENAME_Activity);
            ruleRootElement.appendChild(activityRuleElement);
            
            Element gcWhiteNodeElement = xmlDocument.createElement(XMLRULENAME_GC);
            ruleRootElement.appendChild(gcWhiteNodeElement);
            
            Element samebytesRuleElement = xmlDocument.createElement(XMLRULENAME_SameBytes);
            ruleRootElement.appendChild(samebytesRuleElement);
            
            TransformerFactory tfFactory = TransformerFactory.newInstance();
            Transformer tf = tfFactory.newTransformer();
            
            DOMSource dSource = new DOMSource(xmlDocument);
            StreamResult outputFileRet = new StreamResult(new File(strConfigPath));
            
            tf.transform(dSource, outputFileRet);
            
        } catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }   
    
        init(strConfigPath);
    }
}
