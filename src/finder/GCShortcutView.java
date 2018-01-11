package finder;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.bindings.keys.SWTKeySupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.query.IContextObject;
import org.eclipse.mat.snapshot.IPathsFromGCRootsComputer;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.IPrimitiveArray;
import org.eclipse.mat.snapshot.model.NamedReference;
import org.eclipse.mat.ui.snapshot.editor.HeapEditor;
import org.eclipse.mat.ui.snapshot.editor.ISnapshotEditorInput;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import finder.util.UtilClass;

public class GCShortcutView extends ViewPart implements ITreeContentProvider,
IPartListener,ISelectionChangedListener{

	private TreeViewer tr = null;
	static public ISnapshot snapshot;
	private HeapEditor editor;
	public List<String> excludes = Arrays.asList( //
            new String[] { "java.lang.ref.Reference:referent" }); 
    Map<IClass, Set<String>> excludeMap = null;
    
	private static class GCNodeInfoModel
	{
		public GCNodeInfoModel parent = null;
		public List<GCNodeInfoModel> children = new ArrayList<GCShortcutView.GCNodeInfoModel>();
		public int objID;
        
		public GCNodeInfoModel(int objID,GCNodeInfoModel parent) {
			// TODO Auto-generated constructor stub
			this.parent = parent;
			this.objID = objID;
		}
		
		@Override
		public String toString()
		{
			String rv = "";
			try {
				IObject objC = snapshot.getObject(objID);
				String clsNameString = objC.getClazz().getName();
				rv = objC.getDisplayName();
				
				//表明有上级引用关系
				if (children.size() > 0)
				{
					GCNodeInfoModel realParent = children.get(0);
					long lParentAddr = snapshot.mapIdToAddress(realParent.objID);
					//System.out.println("Parent's addr is "+String.valueOf(lParentAddr));
					List<NamedReference> refs = objC.getOutboundReferences();
	                for (NamedReference reference : refs)
	                {
	                	long lOutRefAddr = reference.getObjectAddress();
	                	//System.out.println("Outref's addr is "+lOutRefAddr);
	                    if (lOutRefAddr == lParentAddr)
	                    {
	                    	rv = reference.getName()+", "+rv; //$NON-NLS-1$
	                    }
	                }
				}
				
				if( clsNameString.contains("byte[]") &&
						!clsNameString.contains("byte[][]") )
				{
					//miniDump中的byte是被置换成空的
		            IPrimitiveArray arr = (IPrimitiveArray) objC;
		            byte[] value = (byte[]) arr.getValueArray(0, arr.getLength());
		            if (UtilClass.isAMiniBytes(value))
		            {
		            	rv += "[MiniSize]:"+String.valueOf(UtilClass.getAMiniBytesSize(value));
		            }
				}
			} catch (SnapshotException e) {
				// TODO Auto-generated catch block
				rv = e.toString();
			}
			return rv;
		}
	}
	
	private GCNodeInfoModel selecedGCNode = null;
	private ISelectionChangedListener treeSelectListener = new ISelectionChangedListener()
	{
		@Override
		public void selectionChanged(SelectionChangedEvent event)
		{
			// TODO Auto-generated method stub
			if (event.getSelection() instanceof IStructuredSelection)
			{
	           
	           ITreeSelection selection = (ITreeSelection) tr.getSelection();
	           Object element = selection.getFirstElement();
	           selecedGCNode = (GCNodeInfoModel)element;
	           if (selecedGCNode != null)
	           {
	        	   System.out.println(selecedGCNode.toString());
	           }
	           
			}
		}
	};
	
	private Listener treeKeyDownListener = new Listener(){

		@Override
		public void handleEvent(Event event)
		{
			// TODO Auto-generated method stub
			if(event.type==SWT.KeyDown&&SWTKeySupport.convertEventToModifiedAccelerator(event)==(SWT.CTRL+'C'))
			{
				System.out.println("Ctrl+C GC Path.");
				if (selecedGCNode != null)
				{
					System.out.println(selecedGCNode.toString());
					Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
					Transferable tText = new StringSelection(selecedGCNode.toString());  
					cb.setContents(tText, null);
				}
			}	
		}
		
	};
	
	@Override
	public void createPartControl(Composite parent) {
		// TODO Auto-generated method stub
		parent.setLayout(new FillLayout());
		
		if(tr == null)
		{
			tr = new TreeViewer(parent);
			tr.setLabelProvider(new LabelProvider());
			tr.setContentProvider(this);
			
			//这里，没有removeListener有可能会泄露
			tr.addSelectionChangedListener(treeSelectListener);
			tr.getTree().addListener(SWT.KeyDown, treeKeyDownListener);
		}
		
//		tr.a(SWT.KeyDown, new Listener()
//		{
//
//			@Override
//			public void handleEvent(Event event)
//			{
//				// TODO Auto-generated method stub

//			}
//			
//		});
		
		getSite().getPage().addPartListener(this);
		addHeapEditorListener();
	}
	
    private void addHeapEditorListener()
    {
        IWorkbenchPage page = getSite().getPage();
        if (page != null)
            partActivated(page.getActiveEditor());
    }
    
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object[] getElements(Object inputElement) {
		// TODO Auto-generated method stub
		return ((GCNodeInfoModel)inputElement).children.toArray();
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		// TODO Auto-generated method stub
		return getElements(parentElement);
	}

	@Override
	public Object getParent(Object element) {
		// TODO Auto-generated method stub
		if( element == null) {
			return null;
		}

		return ((GCNodeInfoModel)element).parent;
	}

	@Override
	public boolean hasChildren(Object element) {
		// TODO Auto-generated method stub
		return ((GCNodeInfoModel)element).children.size() > 0;
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		// TODO Auto-generated method stub
        ISelection selection = event.getSelection();
        updateOnSelection(selection);
	}

	@Override
	public void partActivated(IWorkbenchPart part) {
		// TODO Auto-generated method stub
        if (!(part instanceof HeapEditor)) { return; }
        HeapEditor heapEditor = (HeapEditor) part;

        if (this.editor != heapEditor)
        {
            if (this.editor != null)
            {
                this.editor.removeSelectionChangedListener(this);
            }
            
            this.editor = heapEditor;

            final ISnapshotEditorInput input = heapEditor.getSnapshotInput();
            if (input.hasSnapshot())
            {
                snapshot = input.getSnapshot();
            }
            else
            {
                snapshot = null;
                
                input.addChangeListener(new ISnapshotEditorInput.IChangeListener()
                {	
                    public void onBaselineLoaded(ISnapshot snapshot)
                    {}

                    public void onSnapshotLoaded(ISnapshot s)
                    {
                        if (snapshot != null)
                            snapshot = s;
                        //一定要remove掉前面的那个，否则会内存泄漏
                        input.removeChangeListener(this);
                    }
                }
                );
            }

            this.editor.addSelectionChangedListener(this);
            //object chg
            updateOnSelection(this.editor.getSelection());
        }
	}


	private void updateOnSelection(ISelection selection)
    {
        IContextObject objectSet = null;

        if (selection instanceof IStructuredSelection)
        {
            Object object = ((IStructuredSelection) selection).getFirstElement();
            if (object instanceof IContextObject)
                objectSet = (IContextObject) object;
        }

        if (objectSet == null || objectSet.getObjectId() < 0)
        {
            return;
        }
        
        else
        {
            final int objectId = objectSet.getObjectId();
            try
            {
                if(snapshot == null && this.editor != null)
                {
                    final ISnapshotEditorInput input = editor.getSnapshotInput();
                    if (input.hasSnapshot())
                    {
                        snapshot = input.getSnapshot();
                        if(snapshot == null)
                        {
                            return;
                        }
                    }
                }
                
                
                excludeMap = UtilClass.convert(snapshot, excludes);
                IPathsFromGCRootsComputer cp = snapshot.getPathsFromGCRoots(objectId,excludeMap);
                
                if(cp == null)
                {
                    return;
                }
                
                int [] gcArray = cp.getNextShortestPath();
                GCNodeInfoModel gcRootNode = new GCNodeInfoModel(0,null);
                GCNodeInfoModel parent = gcRootNode;
                
                if (gcArray == null || gcArray.length == 0) {
                	tr.setInput(null);
                	tr.refresh();
                	return;
				}
                
                for( int i = gcArray.length -1; i >= 0; i-- ) {
        			GCNodeInfoModel child = new GCNodeInfoModel(gcArray[i], parent);
        			parent.children.add(child);
        			parent = child;
        		}
                tr.setInput(gcRootNode);
                tr.expandAll();
            }
            catch (SnapshotException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
	
	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
		// TODO Auto-generated method stub
		partActivated(part);
	}

	@Override
	public void partClosed(IWorkbenchPart part) {
		// TODO Auto-generated method stub
        if(part instanceof HeapEditor)
        {
            HeapEditor heapEditor = (HeapEditor) part;
            if (this.editor == heapEditor)
            {
                this.editor.removeSelectionChangedListener(this);
                snapshot = null;
                this.editor = null;
            }
        }
	}

	@Override
	public void partDeactivated(IWorkbenchPart part) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void partOpened(IWorkbenchPart part) {
		// TODO Auto-generated method stub
		
	}
    @Override
    public void dispose()
    {
    	tr.removeSelectionChangedListener(treeSelectListener);
    	tr.getTree().removeListener(SWT.KeyDown, treeKeyDownListener);
        if (this.editor != null)
        {
            this.editor.removeSelectionChangedListener(this);
            this.editor = null;
        }

        getSite().getPage().removePartListener(this);
        super.dispose();
    }
}
