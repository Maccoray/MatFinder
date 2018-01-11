package finder.cloud;

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
import org.eclipse.mat.snapshot.DominatorsSummary;
import org.eclipse.mat.snapshot.DominatorsSummary.ClassDominatorRecord;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.SnapshotFactory;
import org.eclipse.mat.snapshot.SnapshotInfo;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.util.IProgressListener;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import finder.util.*;
import finder.util.CloudConfigCenter.AnalyzerConfig;
import finder.util.CloudOutput.OutputObjAttributesInfo;
import finder.util.UtilClass.SameByteObjInfo;
import finder.util.UtilClass.SnapshotClassInfo.DominatorInfo;
import finder.util.UtilClass.SnapshotClassInfo.SnapshotObjInfo;

@CommandName("FindCloudDebug")
@Category("FinderRule")
@Name("1|Run Memory rules")
@Help("Debug Cloud Configration")
public class CloudLeafQuery implements IQuery,IResultTree
{
	Logger mLog = Logger.getLogger(CloudLeafQuery.class.toString());
			
	@Argument
	public ISnapshot mOpenSnapshot;
	
	@Argument(isMandatory = false, flag = "c")
	@Help("Rule file path.")
	public File mFileMemoryRule = null;

	@Argument(isMandatory = false, flag = "o")
	@Help("Output dir.")
	//public String strOutPutDir = "C:\\Users\\Novels\\Desktop\\testDump";
	public String strOutPutDir = "";
	
	@Argument(isMandatory = false, flag = "s")
	@Help("Older Hprof file.")
	public File mOlderSnapshotFile = null;

	
	@Argument(isMandatory = false, flag = "p")
    @Help("Cloud srv used,no need manual type.")
	public String strParamXmlPath = "";
	
	
    @Argument(isMandatory = false, flag = "f")
    @Help("Cloud srv used,no need manual type.")
    //public String strSecondSnapshotPath = "C:\\Users\\Novels\\Desktop\\testDump\\com.qzone��̬feeds1conv.hprof";
    public String strSecondSnapshotPath = "";
    
    //����ֵ
	List<CloudLeafQueryExecuteRet> mExecuteRets = new ArrayList<CloudLeafQuery.CloudLeafQueryExecuteRet>();

	//����Υ��
	List<UtilClass.CmpClassInfo> breakCmpRuleClassinfoes = new ArrayList<UtilClass.CmpClassInfo>();
	List<UtilClass.BitmapInfo> breakRuleBitmaps = new ArrayList<UtilClass.BitmapInfo>();
	List<UtilClass.SnapshotClassInfo> breakClsRuleClassinfoes = new ArrayList<UtilClass.SnapshotClassInfo>();
	List<UtilClass.ActivityInfo> breakRuleActivitysLst = new ArrayList<UtilClass.ActivityInfo>();
	Map<String, List<SameByteObjInfo>> breakRuleSameBytesMap = null;
	
	
	//��Ҫ��ʾgcpath��objs
    List<Integer> needShowGCObjs = new ArrayList<Integer>();
    
    //��Ҫ��ʾattributes��objs
    List<Integer> needShowAttributesObjsIntegers = new ArrayList<Integer>();
    
    //���յı�ʾ
    static final String SNAPSHOT_IndexOlder = "SnapshotOlder";
    static final String SNAPSHOT_IndexNewer = "SnapshotNewer";
    //�ڴ���� info�ſ�
    Map<String,SnapshotInfo> snapshotInfos = new HashMap<String,SnapshotInfo>();

	public CloudLeafQuery()
	{
		// TODO Auto-generated constructor stub
	}

	public static class CloudLeafQueryExecuteRet
	{
		String strCmd;
		int iResult;//0ʧ�ܣ�1�ɹ�
		
		public CloudLeafQueryExecuteRet(String cmd,int iret)
		{
			strCmd = cmd;
			iResult = iret;
		}
	}
	
	void executeShowAttributes()
	{
		if(needShowAttributesObjsIntegers.size() <= 0)
		{
			return;
		}
		
		List<OutputObjAttributesInfo>  allOutputAttrs = new ArrayList<CloudOutput.OutputObjAttributesInfo>();
		for (Integer id:needShowAttributesObjsIntegers) {
			try {
				OutputObjAttributesInfo objInfo = new OutputObjAttributesInfo();
				objInfo.clazzName = mOpenSnapshot.getObject(id).getClazz().getName();
				objInfo.laddrs = mOpenSnapshot.getObject(id).getObjectAddress();
				objInfo.attributes = UtilClass.getObjectAllAttributes(mOpenSnapshot, id);
				allOutputAttrs.add(objInfo);
			} catch (SnapshotException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				mExecuteRets.add(new CloudLeafQueryExecuteRet("showAttrsInfo",0));
			}	
		}
		CloudOutput.objAttributesToXML(strOutPutDir+File.separator+"InstanceAttributes.xml",allOutputAttrs);
		mExecuteRets.add(new CloudLeafQueryExecuteRet("showAttrsInfo",1));
	}
	
	void executeShowGCPathes(IProgressListener listener)
	{
		if(needShowGCObjs.size() <= 0)
		{
			return;
		}
		
		Integer[] objs = new Integer[needShowGCObjs.size()];
		needShowGCObjs.toArray(objs);
		int[] iConvObjs = new int[objs.length];
		for(int i=0;i<objs.length;i++)
		{
			iConvObjs[i] = objs[i];
		}
		
		try
		{
			List<UtilClass.GCInfo> gcPaths = UtilClass.getObjsGcPath(iConvObjs, mOpenSnapshot, listener);
			CloudOutput.gcPathsToXML(strOutPutDir+File.separator+"GCPaths.xml",gcPaths);
			mExecuteRets.add(new CloudLeafQueryExecuteRet("GCPaths",1));
		} catch (SnapshotException e)
		{
			mExecuteRets.add(new CloudLeafQueryExecuteRet("GCPaths",0));
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private List<UtilClass.BitmapInfo> filterBitmapsFromClsInfo(List<UtilClass.SnapshotClassInfo> classInfos) throws SnapshotException
	{
		final List<UtilClass.BitmapInfo> bitmapLst = new ArrayList<UtilClass.BitmapInfo>();
		if(classInfos != null)
		{
			for(UtilClass.SnapshotClassInfo cinfo:classInfos)
	        {
				if(cinfo.strName.equals("android.graphics.Bitmap"))
				{
					for(UtilClass.SnapshotClassInfo.SnapshotObjInfo objInfo:cinfo.objs)
					{
						UtilClass.BitmapInfo bInfo = UtilClass.getBitmapInfo(mOpenSnapshot.getObject(objInfo.iID));
						
						bitmapLst.add(bInfo);
					}
				}
	        }
		}
		return bitmapLst;
	}

	private void executeBitmapRule(List<UtilClass.BitmapInfo> bitmapLst,IProgressListener listener)
	{
	    
		List<CloudConfigCenter.AnalyzerConfig.BitmapRule> bRuleLst = CloudConfigCenter.getInstance().getBitmapRule();
		for(CloudConfigCenter.AnalyzerConfig.BitmapRule bRule:bRuleLst)
		{
			for(UtilClass.BitmapInfo bi:bitmapLst)
			{
				if(bi.mHeight >= bRule.iMaxHeight || bi.mWidth >= bRule.iMaxWidth)
				{
					breakRuleBitmaps.add(bi);
					
					if(bRule.bShowGc)
					{
						needShowGCObjs.add(bi.mObjID);
					}
				}
			}
			
			CloudOutput.bitmapInfoToCsv(strOutPutDir+File.separator+"BitmapBreakList.csv", breakRuleBitmaps);
			mExecuteRets.add(new CloudLeafQueryExecuteRet("BitmapRule",1));
		}
	}
	
	private void executeCmpRule(List<UtilClass.CmpClassInfo> cmpInfos,IProgressListener listener)
	{
		List<CloudConfigCenter.AnalyzerConfig.CompareRule> cmpRules = CloudConfigCenter.getInstance().getCompareRules();
		if(cmpRules == null)
		{
			return;
		}
		
		if(cmpInfos == null)
		{
			mExecuteRets.add(new CloudLeafQueryExecuteRet("CompareBreakRule",0));
			return;
		}

		for(CloudConfigCenter.AnalyzerConfig.CompareRule crule:cmpRules)
		{
			for(UtilClass.CmpClassInfo cinfo:cmpInfos)
			{
				//�ҵ���Ӧ������
				if(crule.strClassName.equals(cinfo.strClsName))
				{
					if(cinfo.iIncCount>crule.iMaxIncCount || cinfo.lIncRetainedSize>(crule.lMaxIncSize*1024))
					{//Υ�������ˣ���¼�����б�
						breakCmpRuleClassinfoes.add(cinfo);
						
						if(crule.bShowGc)
						{
							//��Ҫչʾgc
							if(cinfo.incObjsLst != null && cinfo.incObjsLst.size() > 0)
							{
								needShowGCObjs.addAll(cinfo.incObjsLst);
							}
						}
					}
				}
			}
		}
		CloudOutput.cmpClsInfoToCsv(strOutPutDir+File.separator+"CompareBreakRule.csv",breakCmpRuleClassinfoes);
		
		mExecuteRets.add(new CloudLeafQueryExecuteRet("CompareBreakRule",1));
	}
	
	
	//����classrule����
	private void executeClassRules(List<UtilClass.SnapshotClassInfo> classInfos,IProgressListener listener) throws SnapshotException
	{
		List<CloudConfigCenter.AnalyzerConfig.ClassRule> clsRules = CloudConfigCenter.getInstance().getClassRules();
		if(clsRules == null || clsRules.size() == 0)
		{
			return;
		}
		
		if(classInfos == null)
		{
			mExecuteRets.add(new CloudLeafQueryExecuteRet("ClassBreakRule",0));
			return;
		}

		for(CloudConfigCenter.AnalyzerConfig.ClassRule crule:clsRules)
		{
			for(UtilClass.SnapshotClassInfo cinfo:classInfos)
			{
				Pattern pt = Pattern.compile(crule.strClassName);
				Matcher mc = pt.matcher(cinfo.strName);
				//�ҵ���Ӧ������
				//if(crule.strClassName.equals(cinfo.strName))
				if(mc.find())
				{
					if(cinfo.objs.size()>crule.iMaxCount || 
							(cinfo.lSize>(crule.lMaxSize*1024)))
					{//Υ�������ˣ���¼�����б�
						
						//�������100����������ʹ��gc��ʽ����Ӧ�ò���Immediate Dominators��ʽ
						if(crule.iMaxCount >= 100)
						{
							Pattern skipPattern = Pattern.compile("java.*|com\\.sun\\..*|..*android..*");
							
							int [] ids = new int [cinfo.objs.size()];
							int i = 0;
							for (SnapshotObjInfo objInfo:cinfo.objs)
							{
								ids[i] = objInfo.iID;
								i++;
							}
							
							DominatorsSummary summary = mOpenSnapshot.getDominatorsOf(ids, skipPattern, listener);
							ClassDominatorRecord [] doRecords = summary.getClassDominatorRecords();
							
							for (ClassDominatorRecord doItem:doRecords)
							{
								DominatorInfo doInfo = new DominatorInfo();
								doInfo.strDoName = doItem.getClassName();
								doInfo.iDoersCount = doItem.getDominatorCount();
								doInfo.iDoedCount = doItem.getDominatedCount();
								cinfo.doers.add(doInfo);
							}
							//����Ҫ�����б���
							cinfo.objs.clear();
							cinfo.objs = null;
							breakClsRuleClassinfoes.add(cinfo);
							continue;
						}
						
						breakClsRuleClassinfoes.add(cinfo);
						
						if(crule.bShowGc)
						{
							//��Ҫչʾgc
							for(UtilClass.SnapshotClassInfo.SnapshotObjInfo objInfo:cinfo.objs)
							{
								needShowGCObjs.add(objInfo.iID);
							}
						}
						
						if(crule.bShowAttributes)
						{
							//��Ҫչʾ����
							for(UtilClass.SnapshotClassInfo.SnapshotObjInfo objInfo:cinfo.objs)
							{
								needShowAttributesObjsIntegers.add(objInfo.iID);
							}
						}
					}
				}
			}
		}
		
		executeClassGCWhiteRule(listener);
		
		CloudOutput.classInfoToCsv(strOutPutDir+File.separator+"ClassBreakRule.csv",breakClsRuleClassinfoes);
		
		mExecuteRets.add(new CloudLeafQueryExecuteRet("ClassBreakRule",1));
	}

    private boolean isObjHasGCNode(IObject obj,List<CloudConfigCenter.AnalyzerConfig.GCWhiteNode> gcNodes,IProgressListener listener) throws SnapshotException
    {
//        for debug
        //if(!obj.getClazz().getName().equals("com.tencent.mobileqq.activity.SplashActivity"))
//        {
//            return true;
//        }
//        
        int [] parObjID = new int[1];
        parObjID[0] = obj.getObjectId();
        List<UtilClass.GCInfo> gcPathInfo= UtilClass.getObjsGcPath(parObjID, obj.getSnapshot(), listener);
        
        if(gcPathInfo.size() == 0)
        {
            //finalRef�ڵ㣬�����Ķ���ܿ���ͷŵ�
            return true;
        }
        
        //��GC�ˣ�����û��gc����������϶�����
        if(gcNodes == null || gcNodes.size() ==0)
        {
            return false;
        }

        if(gcPathInfo != null&&gcPathInfo.size() == 1)
        {
            UtilClass.GCInfo gcpath = gcPathInfo.get(0);

            if(gcpath != null && gcpath.gcNodesLst.size() > 1)
            {
                int start = 0;
                for(UtilClass.SnapshotClassInfo.SnapshotObjInfo objinfo:gcpath.gcNodesLst)
                {
                    //����ջ���Ľڵ㣬���ڵ���������ʹ��
                    if(start == 0)
                    {
                        start ++;
                        continue;
                    }
                    
                    for(CloudConfigCenter.AnalyzerConfig.GCWhiteNode gcNode:gcNodes)
                    {
                        //���û������value���Ͳ��û�ȡvalue��
                        if(gcNode.strNodeValue.length() == 0 && objinfo.strName.equals(gcNode.strNodeClassName))
                        {
                            return true;
                        }//�ҵ��˶�Ӧ�İ����������������û�������ֵ
                        else if(gcNode.strNodeValue.length() != 0 && objinfo.strName.equals(gcNode.strNodeClassName))
                        {
                            if(objinfo.strValue != null)
                            {
                                //���gc�ڵ�Ҳ��value�Ļ�����Ϊ����thread֮������Ľڵ㶼û��value��
                                if(gcNode.strNodeValue.equals(objinfo.strValue))
                                {
                                    return true;
                                }
                            }
                            else {
                                System.out.println("GC white node:"+gcNode.strNodeClassName+
                                        " has a value rule:"+gcNode.strNodeValue+
                                        ",but engine have not gotten node's value");
                            }
                        }
                    }
                    start ++;
                }
            }

        }
        
        return false;
    }
    
    private void executeClassGCWhiteRule(IProgressListener listener) throws SnapshotException
    {
        List<CloudConfigCenter.AnalyzerConfig.GCWhiteNode> whiteGCNodes = CloudConfigCenter.getInstance().getGCWhiteNodes();
        List<UtilClass.SnapshotClassInfo.SnapshotObjInfo> removeobjs = new ArrayList<UtilClass.SnapshotClassInfo.SnapshotObjInfo>();
       
        if(breakClsRuleClassinfoes.size() > 0 && whiteGCNodes.size() > 0)
        {
            for(UtilClass.SnapshotClassInfo clsInfo:breakClsRuleClassinfoes)
            {
            	if(clsInfo.objs == null)
            	{
            		//��1000�����п��ܺ�gc�������߼�������ͻ�������ͻbug
            		continue;
            	}
            	
                for(UtilClass.SnapshotClassInfo.SnapshotObjInfo objInfo:clsInfo.objs)
                {

                    IObject obj = mOpenSnapshot.getObject(objInfo.iID);
                    if(isObjHasGCNode(obj,whiteGCNodes,listener) == true)
                    {
                        removeobjs.add(objInfo);
                    }
                }
                
                //�ѱ����������еĶ��󶼷�����
                if(removeobjs.size() > 0)
                {
                    clsInfo.objs.removeAll(removeobjs);
                }
            }
        }
    }
    
    private void executeActivityGCWhiteRule(IProgressListener listener) throws SnapshotException
    {
        // TODO Auto-generated method stub
        List<CloudConfigCenter.AnalyzerConfig.GCWhiteNode> whiteGCNodes = CloudConfigCenter.getInstance().getGCWhiteNodes();
        List<UtilClass.ActivityInfo> removeActivitys = new ArrayList<UtilClass.ActivityInfo>();
        if(breakRuleActivitysLst.size() > 0 && whiteGCNodes.size() > 0)
        {
            for(UtilClass.ActivityInfo activity:breakRuleActivitysLst)
            {

                IObject obj = mOpenSnapshot.getObject(activity.iID);
                if(isObjHasGCNode(obj,whiteGCNodes,listener) == true)
                {
                    removeActivitys.add(activity);
                }
            }
        }
        
        if(removeActivitys.size() > 0)
        {
            breakRuleActivitysLst.removeAll(removeActivitys);
        }
    }
    
    private void executeSameBytesRules(Map<String, List<SameByteObjInfo>> sboiMap)
	{
		if (sboiMap == null)
		{
			mExecuteRets.add(new CloudLeafQueryExecuteRet("SameBytesRule",0));
			return;
		}
		
		List<AnalyzerConfig.SameBytesRule> sameBytesRules = CloudConfigCenter.getInstance().getSameBytesRules();
		boolean bShowGC = false;
		for (AnalyzerConfig.SameBytesRule sbr:sameBytesRules)
		{
			bShowGC = sbr.bShowGC;
			Iterator<Map.Entry<String, List<SameByteObjInfo>>> iter1 = sboiMap.entrySet().iterator();
			while(iter1.hasNext())
			{
				Map.Entry<String, List<SameByteObjInfo>> ent = iter1.next();
				if (sbr.iRepTimes < ent.getValue().size())
				{//���������������
					SameByteObjInfo sboi = ent.getValue().get(0);
					if (sbr.lMaxSize > sboi.lSize && sbr.lMinSize < sboi.lSize)
					{
						//���Ϲ�������1��������Сֵ��С�����ֵ���ظ�������������涨
						continue;
					}
					else if(sbr.lMaxSize == 0 && sbr.lMinSize < sboi.lSize)
					{
						//���Ϲ�������2��������Сֵ����û���������ֵ���ظ�������������涨
						continue;
					}
					else if(sbr.lMinSize == 0 && sbr.lMaxSize > sboi.lSize)
					{
						//���Ϲ�������3��С�����ֵ����û��������Сֵ���ظ�������������涨
						continue;
					}
					else if(sbr.lMaxSize == 0 && sbr.lMinSize == 0)
					{
						//���Ϲ�������4��û�������κδ�С��ֵ���ظ�������������涨
						continue;
					}
				}
				
				//�����Ϲ���
				iter1.remove();
			}
		}
		
		if (bShowGC)
		{
			Iterator<Map.Entry<String, List<SameByteObjInfo>>> iter1 = sboiMap.entrySet().iterator();
			while(iter1.hasNext())
			{
				Map.Entry<String, List<SameByteObjInfo>> ent = iter1.next();
				List<SameByteObjInfo> SameBytesObjLst = ent.getValue();
				for (SameByteObjInfo sboiItem:SameBytesObjLst)
				{
					//ȫ���ӵ�showGC����
					needShowGCObjs.add(sboiItem.objID);
				}
			}
		}
		
		CloudOutput.sameBytesToCsv(strOutPutDir+File.separator+"SameBytesBreakRule.csv",breakRuleSameBytesMap);
		mExecuteRets.add(new CloudLeafQueryExecuteRet("SameBytesRule",1));
	}
    
    private List<UtilClass.ActivityInfo> getActivitiesByName(List<UtilClass.ActivityInfo> aInfoLst,String activityClsName/*,String identifyName*/)
    {
        List<UtilClass.ActivityInfo> retList = new ArrayList<UtilClass.ActivityInfo>();
        for(UtilClass.ActivityInfo ai:aInfoLst)
        {
            if(activityClsName.equals(ai.strActivityName))
            {
//            	//��ȡָ����activity��������У��Ͱ�Indentify����
//            	for(InstAttribute atr:ai.attrs)
//            	{
//            		if(atr.strAttrName.equals(identifyName))
//            		{
//            			ai.strIndentifyValue = atr.strAttrValue;
//            			//�ѱ�ע�ŵ����̱�����
//            		}
//            	}
                retList.add(ai);
            }
        }
        
        
        return retList;
    }
    
	private void executeActivityRules(List<UtilClass.ActivityInfo> aInfoLst,IProgressListener listener) throws SnapshotException
	{
		List<CloudConfigCenter.AnalyzerConfig.ActivityRule> arlst = CloudConfigCenter.getInstance().getActivityRules();
		if(arlst == null)
		{
			return;
		}
		
		if(aInfoLst == null)
		{
			mExecuteRets.add(new CloudLeafQueryExecuteRet("ActivityRule",0));
			return;
		}

	    CloudConfigCenter.AnalyzerConfig.ActivityRule ar = null;
	    boolean bGCNeed = false;
	    boolean bAttributesNeed = false;
	    if(arlst.size() != 0)
	    {
		    for(Iterator<CloudConfigCenter.AnalyzerConfig.ActivityRule>itor = arlst.iterator();
		            itor.hasNext();)
		    {
		        ar = itor.next();
		        bGCNeed = ar.bShowGc;//���һ��������Ч���ô��ʵ��
		        bAttributesNeed = ar.bShowAttributes;//����֮ǰ�����߼������һ��������Ч
		        List<UtilClass.ActivityInfo> activityLst = getActivitiesByName(aInfoLst, ar.activityName);
		        if(activityLst.size() <= ar.iMaxCount)
		        {
		            //û��Υ������
		            aInfoLst.removeAll(activityLst);
		        }
		    }
	    }

	    //ʣ�µĶ���Υ����
	    breakRuleActivitysLst.addAll(aInfoLst);
	    
	    if(bGCNeed)
	    {//������gc path
	        for(UtilClass.ActivityInfo ob:breakRuleActivitysLst)
	        {
	            needShowGCObjs.add(ob.iID);
	        }
	    }

	    if(bAttributesNeed)
	    {//������gc path
	        for(UtilClass.ActivityInfo ob:breakRuleActivitysLst)
	        {
	            needShowAttributesObjsIntegers.add(ob.iID);
	        }
	    }
	    
		//�����Ľ��飬�Ʒ���ƽ̨�ܹ��Զ���activity��gc���ˣ��Ⲣ����gcչʾ��xml�����ã�ֻ��ActivityBreakRuleList.csv������
		executeActivityGCWhiteRule(listener);//��������ǳ���
		CloudOutput.activityListToCsv(strOutPutDir+File.separator+"ActivityBreakRuleList.csv", breakRuleActivitysLst);
		mExecuteRets.add(new CloudLeafQueryExecuteRet("ActivityRule",1));
	}
	
	
	private void executeSnapshotInfoSave()
    {
        if(snapshotInfos.size() > 0)
        {
            CloudOutput.snapshotInfoToCsv(strOutPutDir+File.separator+"SnapshotInfos.csv",snapshotInfos);
            mExecuteRets.add(new CloudLeafQueryExecuteRet("SaveSnapshotInfo",1));
        }
    }
	
	public IResult execute(IProgressListener listener) throws Exception
	{
		// TODO Auto-generated method stub
		if(strParamXmlPath != null && strParamXmlPath.length()>0)
		{
		    CloudConfigCenter.getInstance().init(strParamXmlPath);
		}
		else if(mFileMemoryRule != null)
		{
		    CloudConfigCenter.getInstance().init(mFileMemoryRule.getPath());
		}
		else
		{
		    return null;
        }
		
		breakRuleActivitysLst.clear();
		breakRuleBitmaps.clear();
		breakCmpRuleClassinfoes.clear();
		breakClsRuleClassinfoes.clear();
		
		if (breakRuleSameBytesMap != null)
		{
			breakRuleSameBytesMap.clear();
			breakRuleSameBytesMap  = null;
		}
		
		//����snapshotInfo
		SnapshotInfo newerSnapshotInfo = mOpenSnapshot.getSnapshotInfo();
		snapshotInfos.put(SNAPSHOT_IndexNewer, newerSnapshotInfo);
		
		List<UtilClass.SnapshotClassInfo> classInfos  = null;
		classInfos = UtilClass.getSnapshotClasses(mOpenSnapshot);

		if(CloudConfigCenter.getInstance().getClassRules().size() > 0)
		{
		    executeClassRules(classInfos,listener);
		}
		
		if (CloudConfigCenter.getInstance().getBitmapRule().size() > 0)
		{
			//����bitmap
			List<UtilClass.BitmapInfo> btInfos = filterBitmapsFromClsInfo(classInfos);
			executeBitmapRule(btInfos,listener);
		}
		
        ISnapshot olderSnaphot = null;
        if(strSecondSnapshotPath != null && strSecondSnapshotPath.length() != 0)
        {
            olderSnaphot = SnapshotFactory.openSnapshot(new File(strSecondSnapshotPath), listener);
            
        }
        else if(mOlderSnapshotFile != null)
        {
            olderSnaphot = SnapshotFactory.openSnapshot(mOlderSnapshotFile, listener);
        }
        else
        {
            mExecuteRets.add(new CloudLeafQueryExecuteRet("CmpList",0));
        }
        
        if(olderSnaphot != null)
        {
            SnapshotInfo olderSnapshotInfo = olderSnaphot.getSnapshotInfo();
            snapshotInfos.put(SNAPSHOT_IndexOlder, olderSnapshotInfo);
        }
        
        //ִ��compare����
		if(CloudConfigCenter.getInstance().getCompareRules().size() > 0)
		{
		    if(olderSnaphot != null)
	        {
                List<UtilClass.SnapshotClassInfo> oldClassInfos = UtilClass.getSnapshotClasses(olderSnaphot);
                List<UtilClass.CmpClassInfo> cmpClsInfos = UtilClass.cmpDump(classInfos,oldClassInfos);
                executeCmpRule(cmpClsInfos,listener);
	        }
		}
		
		//activity�б���Ϊactivity���ж��Ǹ��ݸ������ƶ����ģ����Զ���classinfoû�и�����ϵ��
		if(CloudConfigCenter.getInstance().getActivityRules().size() > 0)
		{
			List<UtilClass.ActivityInfo> aInfoLst = UtilClass.getSnapshotActivitys(mOpenSnapshot);
			executeActivityRules(aInfoLst,listener);
		}
		
		if (CloudConfigCenter.getInstance().getSameBytesRules().size() > 0)
		{
			breakRuleSameBytesMap = UtilClass.getSameBytes(mOpenSnapshot);
			executeSameBytesRules(breakRuleSameBytesMap);
		}
		
		//gc�б�
		executeShowGCPathes(listener);
		
		//attributes
		executeShowAttributes();
		
		//�����������
		executeSnapshotInfoSave();
		return this;
	}

	public Object getColumnValue(Object arg0, int columnIndex)
	{
		// TODO Auto-generated method stub
		CloudLeafQueryExecuteRet ret = (CloudLeafQueryExecuteRet)arg0;
        switch (columnIndex)
        {
            case 0:
                return ret.strCmd;
            case 1:
                return ret.iResult;
            default:
                return "Error";
        }
	}

	public Column[] getColumns()
	{
		// TODO Auto-generated method stub
        return new Column[] {
                new Column("Cmds", String.class).noTotals(),
                new Column("Result", String.class).noTotals()};
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

	public List<?> getChildren(Object parent)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public List<?> getElements()
	{
		// TODO Auto-generated method stub
		return mExecuteRets;
	}

	public boolean hasChildren(Object element)
	{
		// TODO Auto-generated method stub
		return false;
	}

}
