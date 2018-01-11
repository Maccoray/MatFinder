package finder.cloud;


import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.ViewPart;

import finder.util.CloudConfigCenter;

public class CloudRuleEditorView extends ViewPart
{
    //mXMLShowGroup,
    private Group mRuleTreeGroup,mRuleInfoGroup;
    private Tree mRuleTree;
    private ToolBar fileToolBar;
    private String mOpenedConfigFilePathString;
    private Shell mShell;
    private Table mRuleInfotable;
    
    static String[] RuleTreeCol = {"Type","Name"};
    static String[] RuleInfoTableCol = {"Name","Value"};
    
    void initRuleTreeGroup(Composite parent)
    {
        mRuleTreeGroup = new Group(parent,SWT.LEFT_TO_RIGHT);
        mRuleTreeGroup.setLayoutData(new GridData(SWT.FILL,
                SWT.FILL, true, true));
        mRuleTreeGroup.setText("Rule Struct");
        mRuleTreeGroup.setLayout(new FillLayout());
        
        mRuleTree = new Tree(mRuleTreeGroup, SWT.MULTI | SWT.FULL_SELECTION | SWT.VIRTUAL);
  
        for(String colString:RuleTreeCol)
        {
            TreeColumn col = new TreeColumn(mRuleTree, SWT.LEFT);
            col.setText(colString);
            col.pack();
        }
        mRuleTree.setHeaderVisible(true);
        mRuleTree.setLinesVisible(true);
        
        
        mRuleTree.addListener(SWT.Selection,new Listener()
        {
            public void handleEvent(Event event)
            {
                // TODO Auto-generated method stub
                TreeItem selectedItem = null;
                selectedItem = (TreeItem)event.item;
                Object itemData = selectedItem.getData();
                
                onShowRuleInfo(itemData);
                onCreatePopupMenu();
            }
        });
    }
    
    void onCreatePopupMenu()
    {
        MenuItem item;
        Menu ruleTreeMenu = mRuleTree.getMenu();
        
        if(ruleTreeMenu != null)
        {
            return;
        }
        
        Menu popUpMenu = new Menu(mRuleInfoGroup);
        mRuleTree.setMenu(popUpMenu);
        
        item = new MenuItem(popUpMenu, SWT.PUSH);
        item.setText("Add");
        item.addSelectionListener(new SelectionListener()
        {

            public void widgetDefaultSelected(SelectionEvent arg0)
            {
                // TODO Auto-generated method stub
            }

            public void widgetSelected(SelectionEvent arg0)
            {
                // TODO Auto-generated method stub
                TreeItem[] itemSel = mRuleTree.getSelection();
                if(itemSel.length >0)
                {
                    Object ruleData = itemSel[0].getData();
                
                    if(ruleData instanceof CloudConfigCenter.AnalyzerConfig.ActivityRule
                            || (ruleData instanceof String && ((String)ruleData).equals("ActivityRule")))
                    {
                        //增加Activity
                        AddActivityRuleDlg aarDlg = new AddActivityRuleDlg(mShell);
                        aarDlg.open();
                        
                        CloudConfigCenter.AnalyzerConfig.ActivityRule aNewRule = aarDlg.getActivityRule();
                        CloudConfigCenter.getInstance().addActivityRule(aNewRule);
                        if(aNewRule != null && mToolbarItemSave != null)
                        {
                            mToolbarItemSave.setEnabled(true);
                            initDataToShow(null);
                        }
                    }
                    
                    if(ruleData instanceof CloudConfigCenter.AnalyzerConfig.BitmapRule
                            ||(ruleData instanceof String && ((String)ruleData).equals("BitmapRule")))
                    {
                        //增加Bitmap
                        AddBitmapRuleDlg abrDlg = new AddBitmapRuleDlg(mShell);
                        abrDlg.open();
                        
                        CloudConfigCenter.AnalyzerConfig.BitmapRule bNewRule = abrDlg.getBitmapRule();
                        CloudConfigCenter.getInstance().addBitmapRule(bNewRule);
                        if(bNewRule != null && mToolbarItemSave != null)
                        {
                            mToolbarItemSave.setEnabled(true);
                            initDataToShow(null);
                        }
                    }
                    
                    if(ruleData instanceof CloudConfigCenter.AnalyzerConfig.ClassRule
                            ||(ruleData instanceof String && ((String)ruleData).equals("ClassRule")))
                    {
                        AddClassRuleDlg acrDlg = new AddClassRuleDlg(mShell);
                        acrDlg.open();
                        
                        CloudConfigCenter.AnalyzerConfig.ClassRule cNewRule = acrDlg.getClassRule();
                        CloudConfigCenter.getInstance().addClassRule(cNewRule);
                        if(cNewRule != null && mToolbarItemSave != null)
                        {
                            mToolbarItemSave.setEnabled(true);
                            initDataToShow(null);
                        }
                    }
                    
                    if(ruleData instanceof CloudConfigCenter.AnalyzerConfig.ClassRule
                            ||(ruleData instanceof String && ((String)ruleData).equals("CompareRule")))
                    {
                        AddCompareRuleDlg accrDlg = new AddCompareRuleDlg(mShell);
                        accrDlg.open();
                        
                        CloudConfigCenter.AnalyzerConfig.CompareRule ccNewRule = accrDlg.getCompareRule();
                        CloudConfigCenter.getInstance().addCompareRule(ccNewRule);
                        if(ccNewRule != null && mToolbarItemSave != null)
                        {
                            mToolbarItemSave.setEnabled(true);
                            initDataToShow(null);
                        }
                    }
                    
                    if(ruleData instanceof CloudConfigCenter.AnalyzerConfig.GCWhiteNode
                            || (ruleData instanceof String && ((String)ruleData).equals("GCWhiteNode")))
                    {
                        AddGCWhiteNodeDlg agcwnDlg = new AddGCWhiteNodeDlg(mShell);
                        agcwnDlg.open();
                        CloudConfigCenter.AnalyzerConfig.GCWhiteNode gcwn = agcwnDlg.getGCWhiteNode();
                        CloudConfigCenter.getInstance().addGCWhiteNode(gcwn);
                        if(gcwn != null && mToolbarItemSave != null)
                        {
                            mToolbarItemSave.setEnabled(true);
                            initDataToShow(null);
                        }
                    }
                    
                    if(ruleData instanceof CloudConfigCenter.AnalyzerConfig.SameBytesRule
                            || (ruleData instanceof String && ((String)ruleData).equals("SameByteRules")))
                    {
                    	AddSameBytesRuleDlg asbrDlg = new AddSameBytesRuleDlg(mShell);
                    	asbrDlg.open();
                        CloudConfigCenter.AnalyzerConfig.SameBytesRule sbr = asbrDlg.getSameBytesRule();
                        CloudConfigCenter.getInstance().addSameByteRule(sbr);
                        if(sbr != null && mToolbarItemSave != null)
                        {
                            mToolbarItemSave.setEnabled(true);
                            initDataToShow(null);
                        }
                    }
                }
            }
        });
        
        item = new MenuItem(popUpMenu, SWT.PUSH);
        item.setText("Delete");
        item.addSelectionListener(new SelectionListener()
        {

            public void widgetDefaultSelected(SelectionEvent arg0)
            {
                // TODO Auto-generated method stub
                
            }

            public void widgetSelected(SelectionEvent arg0)
            {
             // TODO Auto-generated method stub
                TreeItem[] itemSel = mRuleTree.getSelection();
                if(itemSel.length >0)
                {
                    Object ruleData = itemSel[0].getData();
                    if(ruleData instanceof CloudConfigCenter.AnalyzerConfig.ActivityRule
                            ||ruleData instanceof CloudConfigCenter.AnalyzerConfig.CompareRule
                            ||ruleData instanceof CloudConfigCenter.AnalyzerConfig.ClassRule
                            ||ruleData instanceof CloudConfigCenter.AnalyzerConfig.BitmapRule
                            ||ruleData instanceof CloudConfigCenter.AnalyzerConfig.GCWhiteNode
                            ||ruleData instanceof CloudConfigCenter.AnalyzerConfig.SameBytesRule)
                    {
                        //删除规则
                        CloudConfigCenter.getInstance().deleteRule(ruleData);
                        mToolbarItemSave.setEnabled(true);
                        initDataToShow(null);
                    }
                    else
                    {
                        //删除类别，不允许
                        CannotDeleteRuleDlg cdrDlg = new CannotDeleteRuleDlg(mShell);
                        cdrDlg.open();
                    }
                }
            }
        });

    }
    
    private void showActivityRule(TableItem item,String[] showPar,Object ruleData)
    {
        CloudConfigCenter.AnalyzerConfig.ActivityRule ar = (CloudConfigCenter.AnalyzerConfig.ActivityRule)ruleData;
        
        item = new TableItem (mRuleInfotable, SWT.NONE);
        showPar[0] = "RuleName";
        showPar[1] = ar.strRuleName;
        item.setText(showPar);
        
        item = new TableItem (mRuleInfotable, SWT.NONE);
        showPar[0] = "DevName";
        showPar[1] = ar.strDevName;
        item.setText(showPar);
        
        item = new TableItem (mRuleInfotable, SWT.NONE);
        showPar[0] = "activityName";
        showPar[1] = ar.activityName;
        item.setText(showPar);

        item = new TableItem (mRuleInfotable, SWT.NONE);
        showPar[0] = "MaxCount";
        showPar[1] = String.valueOf(ar.iMaxCount);
        item.setText(showPar);
        
        item = new TableItem (mRuleInfotable, SWT.NONE);
        showPar[0] = "ShowGCPath";
        showPar[1] = String.valueOf(ar.bShowGc);
        item.setText(showPar);
        
        item = new TableItem (mRuleInfotable, SWT.NONE);
        showPar[0] = "ShowAttributes";
        showPar[1] = String.valueOf(ar.bShowAttributes);
        item.setText(showPar);
    }
    
    private void showClassRule(TableItem item,String[] showPar,Object ruleData)
    {
        CloudConfigCenter.AnalyzerConfig.ClassRule cr = (CloudConfigCenter.AnalyzerConfig.ClassRule)ruleData;
        
        item = new TableItem (mRuleInfotable, SWT.NONE);
        showPar[0] = "RuleName";
        showPar[1] = cr.strRuleName;
        item.setText(showPar);
        
        item = new TableItem (mRuleInfotable, SWT.NONE);
        showPar[0] = "DevName";
        showPar[1] = cr.strDevName;
        item.setText(showPar);
        
        item = new TableItem (mRuleInfotable, SWT.NONE);
        showPar[0] = "ClassName";
        showPar[1] = cr.strClassName;
        item.setText(showPar);

        item = new TableItem (mRuleInfotable, SWT.NONE);
        showPar[0] = "MaxCount";
        showPar[1] = String.valueOf(cr.iMaxCount);
        item.setText(showPar);
        
        item = new TableItem (mRuleInfotable, SWT.NONE);
        showPar[0] = "MaxHeapSize";
        showPar[1] = String.valueOf(cr.lMaxSize);
        item.setText(showPar);
        
        item = new TableItem (mRuleInfotable, SWT.NONE);
        showPar[0] = "ShowGCPath";
        showPar[1] = String.valueOf(cr.bShowGc);
        item.setText(showPar);
        
        item = new TableItem (mRuleInfotable, SWT.NONE);
        showPar[0] = "ShowAttributes";
        showPar[1] = String.valueOf(cr.bShowAttributes);
        item.setText(showPar);
    }
    
    private void showCmpRule(TableItem item,String[] showPar,Object ruleData)
    {
        CloudConfigCenter.AnalyzerConfig.CompareRule ccr = (CloudConfigCenter.AnalyzerConfig.CompareRule)ruleData;
        
        item = new TableItem (mRuleInfotable, SWT.NONE);
        showPar[0] = "RuleName";
        showPar[1] = ccr.strRuleName;
        item.setText(showPar);
        
        item = new TableItem (mRuleInfotable, SWT.NONE);
        showPar[0] = "DevName";
        showPar[1] = ccr.strDevName;
        item.setText(showPar);
        
        item = new TableItem (mRuleInfotable, SWT.NONE);
        showPar[0] = "ClassName";
        showPar[1] = ccr.strClassName;
        item.setText(showPar);

        item = new TableItem (mRuleInfotable, SWT.NONE);
        showPar[0] = "IncMaxCount";
        showPar[1] = String.valueOf(ccr.iMaxIncCount);
        item.setText(showPar);
        
        item = new TableItem (mRuleInfotable, SWT.NONE);
        showPar[0] = "IncMaxHeapSize";
        showPar[1] = String.valueOf(ccr.lMaxIncSize);
        item.setText(showPar);
        
        item = new TableItem (mRuleInfotable, SWT.NONE);
        showPar[0] = "ShowGCPath";
        showPar[1] = String.valueOf(ccr.bShowGc);
        item.setText(showPar);
    }
    
    private void showBitmapRule(TableItem item,String[] showPar,Object ruleData)
    {
        CloudConfigCenter.AnalyzerConfig.BitmapRule br = (CloudConfigCenter.AnalyzerConfig.BitmapRule)ruleData;
        
        item = new TableItem (mRuleInfotable, SWT.NONE);
        showPar[0] = "RuleName";
        showPar[1] = br.strRuleName;
        item.setText(showPar);
        
        item = new TableItem (mRuleInfotable, SWT.NONE);
        showPar[0] = "DevName";
        showPar[1] = br.strDevName;
        item.setText(showPar);
        
        item = new TableItem (mRuleInfotable, SWT.NONE);
        showPar[0] = "MaxHeight";
        showPar[1] = String.valueOf(br.iMaxHeight);
        item.setText(showPar);

        item = new TableItem (mRuleInfotable, SWT.NONE);
        showPar[0] = "MaxWidth";
        showPar[1] = String.valueOf(br.iMaxWidth);
        item.setText(showPar);
        
        item = new TableItem (mRuleInfotable, SWT.NONE);
        showPar[0] = "ShowGCPath";
        showPar[1] = String.valueOf(br.bShowGc);
        item.setText(showPar);
	}
    
    private void showGCWhiteRule(TableItem item,String[] showPar,Object ruleData)
    {
        CloudConfigCenter.AnalyzerConfig.GCWhiteNode gcwn = (CloudConfigCenter.AnalyzerConfig.GCWhiteNode)ruleData;
        item = new TableItem (mRuleInfotable, SWT.NONE);
        showPar[0] = "RuleName";
        showPar[1] = gcwn.strRuleName;
        item.setText(showPar);
        
        item = new TableItem (mRuleInfotable, SWT.NONE);
        showPar[0] = "DevName";
        showPar[1] = gcwn.strDevName;
        item.setText(showPar);
        
        item = new TableItem (mRuleInfotable, SWT.NONE);
        showPar[0] = "NodeClassName";
        showPar[1] = gcwn.strNodeClassName;
        item.setText(showPar);
        
        item = new TableItem (mRuleInfotable, SWT.NONE);
        showPar[0] = "NodeValue";
        showPar[1] = gcwn.strNodeValue;
        item.setText(showPar);
    }
    
    private void showSameBytesRule(TableItem item,String[] showPar,Object ruleData)
    {
        CloudConfigCenter.AnalyzerConfig.SameBytesRule sbr = (CloudConfigCenter.AnalyzerConfig.SameBytesRule)ruleData;
        item = new TableItem (mRuleInfotable, SWT.NONE);
        showPar[0] = "RuleName";
        showPar[1] = sbr.strRuleName;
        item.setText(showPar);
        
        item = new TableItem (mRuleInfotable, SWT.NONE);
        showPar[0] = "DevName";
        showPar[1] = sbr.strDevName;
        item.setText(showPar);
        
        item = new TableItem (mRuleInfotable, SWT.NONE);
        showPar[0] = "MaxSize";
        showPar[1] = String.valueOf(sbr.lMaxSize);
        item.setText(showPar);
        
        item = new TableItem (mRuleInfotable, SWT.NONE);
        showPar[0] = "MinSize";
        showPar[1] = String.valueOf(sbr.lMinSize);
        item.setText(showPar);
        
        item = new TableItem (mRuleInfotable, SWT.NONE);
        showPar[0] = "RepTime";
        showPar[1] = String.valueOf(sbr.iRepTimes);
        item.setText(showPar);
        
        item = new TableItem (mRuleInfotable, SWT.NONE);
        showPar[0] = "ShowGC";
        showPar[1] = String.valueOf(sbr.bShowGC);
        item.setText(showPar);
    }
    
    private void onShowRuleInfo(Object ruleData)
    {
        mRuleInfotable.removeAll();
        TableItem item = null;
        String[] showPar = new String[2];
        if(ruleData instanceof CloudConfigCenter.AnalyzerConfig.ActivityRule)
        {//更新展示数据
        	showActivityRule(item,showPar,ruleData);
        }
        
        if(ruleData instanceof CloudConfigCenter.AnalyzerConfig.BitmapRule)
        {//更新展示数据
        	showBitmapRule(item,showPar,ruleData);
        }
        
        
        if(ruleData instanceof CloudConfigCenter.AnalyzerConfig.ClassRule)
        {//更新展示数据
        	showClassRule(item,showPar,ruleData);
        }
        
        if(ruleData instanceof CloudConfigCenter.AnalyzerConfig.CompareRule)
        {//更新展示数据
        	showCmpRule(item,showPar,ruleData);
        }
        
        if(ruleData instanceof CloudConfigCenter.AnalyzerConfig.GCWhiteNode)
        {
        	showGCWhiteRule(item,showPar,ruleData);
        }
        
        if(ruleData instanceof CloudConfigCenter.AnalyzerConfig.SameBytesRule)
        {
        	showSameBytesRule(item,showPar,ruleData);
        }
        
        reflashRuleTable();
    }
    
//    void initXMLShowGroup(Composite parent)
//    {
//        // TODO Auto-generated method stub
//        mXMLShowGroup = new Group(parent,SWT.LEFT_TO_RIGHT);
//        mXMLShowGroup.setLayoutData(new GridData(SWT.FILL,
//                SWT.FILL, true, true, 1, 2));
//        mXMLShowGroup.setText("XML Contents");
//    }
    
    
    void initRuleInfoGroup(Composite parent)
    {
        mRuleInfoGroup = new Group(parent,SWT.LEFT_TO_RIGHT);
        mRuleInfoGroup.setLayoutData(new GridData(SWT.FILL,
                SWT.FILL, true, true));
        mRuleInfoGroup.setText("Rule Info");
        mRuleInfoGroup.setLayout(new FillLayout());
        
        mRuleInfotable = new Table (mRuleInfoGroup, SWT.FULL_SELECTION|SWT.MULTI|SWT.VIRTUAL);
        
        TableColumn tColumn;
        for(String strCloName:RuleInfoTableCol)
        {
            tColumn = new TableColumn(mRuleInfotable, SWT.NONE);
            tColumn.setText(strCloName);
            tColumn.pack();
        }

        mRuleInfotable.setLinesVisible(true);
        mRuleInfotable.setHeaderVisible(true);
    }
    
    private ToolItem mToolbarItemSave = null;
    void initToolbar(Composite parent)
    {
        fileToolBar = new ToolBar(parent, SWT.HORIZONTAL|SWT.SHADOW_IN);
        fileToolBar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        ToolItem itemOpen = new ToolItem (fileToolBar, SWT.PUSH);
        itemOpen.setText("OpenConfig...");

        itemOpen.addSelectionListener(new SelectionListener()
        {

            public void widgetDefaultSelected(SelectionEvent arg0)
            {
                // TODO Auto-generated method stub
                
            }

            public void widgetSelected(SelectionEvent arg0)
            {
                // TODO Auto-generated method stub
                if(mShell != null)
                {
                    FileDialog dialog = new FileDialog (mShell, SWT.OPEN);
                    String [] filterNameStrings = {"XML Files (*.xml)"};
                    String [] fileExtFilter = {"*.xml"};
                    dialog.setFilterNames(filterNameStrings);
                    dialog.setFilterExtensions(fileExtFilter);
                    
                    dialog.setText("Open CloudConfig");
                    mOpenedConfigFilePathString = dialog.open();  
                    initDataToShow(mOpenedConfigFilePathString);
                }
            }
            
        });

        ToolItem itemNew = new ToolItem (fileToolBar, SWT.PUSH);
        itemNew.setText("NewConfig...");
        
        itemNew.addSelectionListener(new SelectionListener()
        {

            public void widgetDefaultSelected(SelectionEvent arg0)
            {
                // TODO Auto-generated method stub
                
            }

            public void widgetSelected(SelectionEvent arg0)
            {
                // TODO Auto-generated method stub
                if(mShell != null)
                {
                    FileDialog dialog = new FileDialog (mShell, SWT.SAVE);
                    String [] filterNameStrings = {"XML Files (*.xml)"};
                    String [] fileExtFilter = {"*.xml"};
                    dialog.setFilterNames(filterNameStrings);
                    dialog.setFilterExtensions(fileExtFilter);
                    
                    dialog.setText("New CloudConfig");
                    mOpenedConfigFilePathString = dialog.open();
                    CloudConfigCenter.getInstance().newConfigFile(mOpenedConfigFilePathString);
                    initDataToShow(mOpenedConfigFilePathString);
                }
            }
            
        });
        
        mToolbarItemSave = new ToolItem (fileToolBar, SWT.PUSH);
        mToolbarItemSave.setText("Save...");
        
        mToolbarItemSave.addSelectionListener(new SelectionListener()
        {

            public void widgetDefaultSelected(SelectionEvent arg0)
            {
                // TODO Auto-generated method stub
                
            }

            public void widgetSelected(SelectionEvent arg0)
            {
                // TODO Auto-generated method stub
                CloudConfigCenter.getInstance().flashConfigFile();
                mToolbarItemSave.setEnabled(false);
            }
            
        });
        mToolbarItemSave.setEnabled(false);
        
    }
    
    private void initDataToShow(String configFilePathString)
    {
        // TODO Auto-generated method stub
        //清空一下
        mRuleTree.removeAll();
        
        if(configFilePathString != null && configFilePathString.length() > 0)
        {
            CloudConfigCenter.getInstance().init(configFilePathString);
        }
        
        List<CloudConfigCenter.AnalyzerConfig.ActivityRule> ar = CloudConfigCenter.getInstance().getActivityRules();
        List<CloudConfigCenter.AnalyzerConfig.BitmapRule> br = CloudConfigCenter.getInstance().getBitmapRule();
        List<CloudConfigCenter.AnalyzerConfig.ClassRule> cr = CloudConfigCenter.getInstance().getClassRules();
        List<CloudConfigCenter.AnalyzerConfig.CompareRule> ccr = CloudConfigCenter.getInstance().getCompareRules();
        List<CloudConfigCenter.AnalyzerConfig.GCWhiteNode> gcwn = CloudConfigCenter.getInstance().getGCWhiteNodes();
        List<CloudConfigCenter.AnalyzerConfig.SameBytesRule> sbrLst = CloudConfigCenter.getInstance().getSameBytesRules();
        
        if(ar != null && br != null && cr != null && ccr != null && sbrLst != null)
        {
            TreeItem item = new TreeItem (mRuleTree, SWT.NONE);
            item.setText("ActivityRule");
            item.setData("ActivityRule");

            for (CloudConfigCenter.AnalyzerConfig.ActivityRule arinfo:ar) 
            {
                TreeItem subItem = new TreeItem (item,SWT.NONE);
                String[] strColValue = new String[2];
                strColValue[0] = arinfo.activityName;
                strColValue[1] = arinfo.strRuleName;
                subItem.setText(strColValue);
                subItem.setData(arinfo);
            }
            
            item = new TreeItem (mRuleTree, SWT.NONE);
            item.setText("BitmapRule");
            item.setData("BitmapRule");
            
            for(CloudConfigCenter.AnalyzerConfig.BitmapRule brinfo:br)
            {
                TreeItem subItem = new TreeItem (item,SWT.NONE);
                String[] strColValue = new String[2];
                strColValue[0] = brinfo.strDevName;
                strColValue[1] = brinfo.strRuleName;
                subItem.setText(strColValue);
                subItem.setData(brinfo);
            }

            item = new TreeItem (mRuleTree, SWT.NONE);
            item.setText("ClassRule");
            item.setData("ClassRule");
            
            for(CloudConfigCenter.AnalyzerConfig.ClassRule cinfo:cr)
            {
                TreeItem subItem = new TreeItem (item,SWT.NONE);
                String[] strColValue = new String[2];
                strColValue[0] = cinfo.strClassName;
                strColValue[1] = cinfo.strRuleName;
                subItem.setText(strColValue);
                subItem.setData(cinfo);
            }
            
            item = new TreeItem (mRuleTree, SWT.NONE);
            item.setText("CompareRule");
            item.setData("CompareRule");
            
            for(CloudConfigCenter.AnalyzerConfig.CompareRule ccinfo:ccr)
            {
                TreeItem subItem = new TreeItem (item,SWT.NONE);
                String[] strColValue = new String[2];
                strColValue[0] = ccinfo.strClassName;
                strColValue[1] = ccinfo.strRuleName;
                subItem.setText(strColValue);
                subItem.setData(ccinfo);
            }
            
            item = new TreeItem(mRuleTree, SWT.NONE);
            item.setText("GCWhiteNode");
            item.setData("GCWhiteNode");
            
            for(CloudConfigCenter.AnalyzerConfig.GCWhiteNode gcnode:gcwn)
            {
                TreeItem subItem = new TreeItem (item,SWT.NONE);
                String[] strColValue = new String[2];
                strColValue[0] = gcnode.strNodeClassName;
                strColValue[1] = gcnode.strRuleName;
                subItem.setText(strColValue);
                subItem.setData(gcnode);
            }
            
            item = new TreeItem(mRuleTree, SWT.NONE);
            item.setText("SameByteRules");
            item.setData("SameByteRules");
            
            for(CloudConfigCenter.AnalyzerConfig.SameBytesRule sbr:sbrLst)
            {
                TreeItem subItem = new TreeItem (item,SWT.NONE);
                String[] strColValue = new String[2];
                strColValue[0] = sbr.strDevName;
                strColValue[1] = sbr.strRuleName;
                subItem.setText(strColValue);
                subItem.setData(sbr);
            }
            
        }
        
        reflushRuleTree();
    }
    
    private void reflushRuleTree()
    {
        TreeColumn[] cols = mRuleTree.getColumns();
        for(TreeColumn col:cols)
        {
            col.pack();
        }
    }
    
    
    private void reflashRuleTable()
    {
        TableColumn[] cols = mRuleInfotable.getColumns();
        for(TableColumn col:cols)
        {
            col.pack();
        }
    }
    @Override
    public void createPartControl(Composite parent)
    {
        // TODO Auto-generated method stub
        mShell = parent.getShell();
        
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        parent.setLayout(gridLayout);

        initToolbar(parent);
        initRuleTreeGroup(parent);
        initRuleInfoGroup(parent);
        //initXMLShowGroup(parent);
    }

    @Override
    public void setFocus()
    {
        // TODO Auto-generated method stub

    }

}
