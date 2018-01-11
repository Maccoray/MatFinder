package finder.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

//本类用于上报
public class ReportManager
{
    public static ReportManager mInst = null;
    
    public static ReportManager getSrvInst()
    {
        if (mInst == null)
        {
            mInst = new ReportManager();
            
            if(mInst != null)
            {
                InetAddress ia;
                try {
                    //获取主机名称
                    ia = InetAddress.getLocalHost();
                    String strHN = ia.getHostName();
                    mInst.mLog.info("HostName:"+strHN);
                    mInst.setHostName(strHN);
                    //启动任务线程
                    new Thread(mInst.mReportRunable).start();
                    
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            else
            {
                mInst.mLog.info("New rpSrv inst Failed");
            }
        }
        return mInst;
    }
    
    Logger mLog = Logger.getLogger(ReportManager.class.toString());
    
    public static class ReportMsg
    {
        int miProjectID = 1;
        String mstrActionString;
        String mstrHostNameString;
        public ReportMsg(String strActionName)
        {
            mstrActionString = strActionName;
        }
    }
    
    //上报类型
    public final static String FINDER_RPMSG_Enum = "EnumOpt";
    public final static String FINDER_RPMSG_Top = "TopOpt";
    public final static String FINDER_RPMSG_Cmp = "CmpOpt";
    public final static String FINDER_RPMSG_Activity = "ActivityOpt";
    public final static String FINDER_RPMSG_Bitmap = "BitmapOpt";
    public final static String FINDER_RPMSG_Samebytes = "SamebytesOpt";
    public final static String FINDER_RPMSG_ShowBitmapInView = "ShowBitmapInViewOpt";
    public final static String FINDER_RPMSG_AboutFinder = "AboutFinderOpt";
    public final static String FINDER_RPMSG_3DumpsCmp = "3DumpsCmpOpt";
    public final static String FINDER_RPMSG_Singleton = "SingletonOpt";
    public final static String FINDER_RPMSG_AllBInOC = "AllBtInOneCacheOpt";
    public final static String FINDER_RPMSG_Fragments = "FragmentsOpt";
    
    private List<ReportMsg> mRPMsgs = new ArrayList<ReportManager.ReportMsg>();
    
    private String mStrHostName;
    
    public void setHostName(String strHostName)
    {
        mStrHostName = strHostName;
    }
    
    
    public void sendRPMsg(ReportMsg msg) {
        msg.mstrHostNameString = mStrHostName;
        
        synchronized (mRPMsgs)
        {
            mRPMsgs.add(msg);
        }
        
    }
    
    private Runnable mReportRunable = new Runnable() {
        
        public void run() {
            // TODO Auto-generated method stub
            while(true)
            {
                synchronized (mRPMsgs)
                {
                    if (mRPMsgs.size()>0)
                    {
                         for(Iterator<ReportMsg> itMsg = mRPMsgs.iterator();itMsg.hasNext();)
                         {
                             HttpReport(itMsg.next());
                             itMsg.remove();
                         }
                    }
                    else
                    {
                        try {
                            Thread.sleep(30);
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
                
            }
        }
    }; 
    
//TX上报
    final static String FIND_RPADDR_Format = "http://10.6.198.114:8080/magnifier/memory/client_report" +
            "?projectid=%d&hostname=%s&action=%s&checksum=%d";

    final static int FIND_MSG_CheckSalt = 0x565;
    
    private int getMsgChecksum(ReportMsg msg) {
        int iChecksum = 0;
        String msgStrStream = msg.miProjectID + msg.mstrActionString + msg.mstrHostNameString;
        char[] msgStrArray = msgStrStream.toCharArray();
        
        for(char c:msgStrArray)
        {
            iChecksum += c;
        }
        
        iChecksum = iChecksum%FIND_MSG_CheckSalt;
        
        return iChecksum;
    }
    
    private void HttpReport(ReportMsg msg)
    {
        URL rpURL;
        try {
            rpURL = new URL(String.format(FIND_RPADDR_Format, 
                    msg.miProjectID,msg.mstrHostNameString,msg.mstrActionString,getMsgChecksum(msg)));
            
            HttpURLConnection conn =  (HttpURLConnection) rpURL.openConnection();
            conn.setConnectTimeout(1000);
            conn.setReadTimeout(1000);

            conn.connect();
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(),"utf-8"));
            String lines;
            while ((lines = reader.readLine()) != null)
            {
                mLog.info(lines);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        
    }
}