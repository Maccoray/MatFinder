package finder;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.query.IContextObject;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IObject;

import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;

import org.eclipse.mat.ui.snapshot.editor.HeapEditor;
import org.eclipse.mat.ui.snapshot.editor.ISnapshotEditorInput;

import finder.util.ReportManager;
import finder.util.UtilClass;


public class BitmapView extends ViewPart implements IPartListener,ISelectionChangedListener
{

    Group groupForEdit,groupForGanvas;
    Combo rgbCombo;
    Text aText,rText,gText,bText;
    HeapEditor editor;
    ISnapshot snapshot;
    Canvas canvas;
    
    static final int ARGB_565 = 0;
    static final int ARGB_8888 = 1;
    static final int ARGB_4444 = 2;
    static final int ARGB_CUSTEM = 3;
    int mGRBConfig = ARGB_565;
    
    int custemAlpha = 0;
    int custemRed = 0;
    int custemGreen = 0;
    int custemBlue = 0;
    
    Label infoLabel;
    
    Image swtImage = null;
    
    @Override
    public void createPartControl(Composite parent)
    {
        // TODO Auto-generated method stub
        parent.setLayout(new GridLayout());
        initGroups(parent);

        getSite().getPage().addPartListener(this);
        
        addHeapEditorListener();
  
    }

    private void addHeapEditorListener()
    {
        IWorkbenchPage page = getSite().getPage();
        if (page != null)
            partActivated(page.getActiveEditor());
    }
    
  //object chg
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
                if(this.snapshot == null && this.editor != null)
                {
                    final ISnapshotEditorInput input = editor.getSnapshotInput();
                    if (input.hasSnapshot())
                    {
                        this.snapshot = input.getSnapshot();
                        if(this.snapshot == null)
                        {
                            return;
                        }
                    }
                }
                IObject object = this.snapshot.getObject(objectId);
                
                if(object == null)
                {
                    return;
                }
                        
                String clsNameString = object.getClazz().getName();
                if(clsNameString.startsWith("android.graphics.Bitmap"))
                {
                    UtilClass.BitmapInfo btinfo = UtilClass.getBitmapInfo(object);
                    if (btinfo != null)
                    {
                        infoLabel.setText("Bitmap's width:"+btinfo.mWidth+" and height:"+btinfo.mHeight);
                        
                        int [] rgbs = convertBytes2IntRGB(btinfo.mbuf,btinfo.mWidth,btinfo.mHeight,mGRBConfig);
                        if(rgbs != null)
                        {
                            BufferedImage image = new BufferedImage(btinfo.mWidth,btinfo.mHeight,
                                    BufferedImage.TYPE_INT_ARGB);
                            image.setRGB(0, 0, btinfo.mWidth, btinfo.mHeight, rgbs, 0, btinfo.mWidth);
                            ImageData swtImageData = convertToSwt(image);
                            //ImageData swtImageData = createSwtImage(btinfo.mbuf,btinfo.mWidth,btinfo.mHeight,mGRBConfig);
                            if(swtImageData != null)
                            {
                                swtImage = new Image(canvas.getDisplay(), swtImageData);
                                ReportManager.ReportMsg msg = new ReportManager.ReportMsg(ReportManager.FINDER_RPMSG_ShowBitmapInView); 
                                ReportManager.getSrvInst().sendRPMsg(msg);
                                if(canvas != null)
                                {
                                    canvas.redraw();
                                }
                            }
                            else
                            {
                                infoLabel.setText("Error for decode image to swt!");
                            }

                        }
                    }
                }
                else
                {
                    infoLabel.setText("This isn't a bitmap");
                }
                
            } catch (SnapshotException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    
    private ImageData convertToSwt(BufferedImage bufferedImage)
    {
        if (bufferedImage.getColorModel() instanceof DirectColorModel) {
            DirectColorModel colorModel = (DirectColorModel) bufferedImage.getColorModel();
            PaletteData palette = new PaletteData(colorModel.getRedMask(), colorModel.getGreenMask(),
                colorModel.getBlueMask());
            ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(),
                colorModel.getPixelSize(), palette);
            WritableRaster raster = bufferedImage.getRaster();
            int[] pixelArray = new int[4];//argb8888:4
            for (int y = 0; y < data.height; y++) {
              for (int x = 0; x < data.width; x++) {
                raster.getPixel(x, y, pixelArray);
                int pixel = palette.getPixel(new RGB(pixelArray[0], pixelArray[1], pixelArray[2]));
                data.setPixel(x, y, pixel);
              }
            }
            return data;
          } else if (bufferedImage.getColorModel() instanceof IndexColorModel) {
            IndexColorModel colorModel = (IndexColorModel) bufferedImage.getColorModel();
            int size = colorModel.getMapSize();
            byte[] reds = new byte[size];
            byte[] greens = new byte[size];
            byte[] blues = new byte[size];
            colorModel.getReds(reds);
            colorModel.getGreens(greens);
            colorModel.getBlues(blues);
            RGB[] rgbs = new RGB[size];
            for (int i = 0; i < rgbs.length; i++) {
              rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF, blues[i] & 0xFF);
            }
            PaletteData palette = new PaletteData(rgbs);
            ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(),
                colorModel.getPixelSize(), palette);
            data.transparentPixel = colorModel.getTransparentPixel();
            WritableRaster raster = bufferedImage.getRaster();
            int[] pixelArray = new int[1];
            for (int y = 0; y < data.height; y++) {
              for (int x = 0; x < data.width; x++) {
                raster.getPixel(x, y, pixelArray);
                data.setPixel(x, y, pixelArray[0]);
              }
            }
            return data;
          }
          return null;
    }

    
    
    private int [] convertBytes2IntRGB(byte[] bufRgb,int width,int height,int typeRGB)
    {
    	if(bufRgb == null)
    	{
    		return null;
    	}
    	
        int [] intRGB = new int[width*height];
        switch (typeRGB)
        {
            case BitmapView.ARGB_8888:
            {
                int bufRgbStep = 0;
            
                for(int i=0;i<width*height;i++)
                {
                    if(bufRgbStep > bufRgb.length-4)
                    {
                        break;
                    }
                    int b = bufRgb[bufRgbStep];
                    int g = bufRgb[bufRgbStep+1];
                    int r = bufRgb[bufRgbStep+2];
                    int a = bufRgb[bufRgbStep+3];
                    bufRgbStep+=4;
                    
                    intRGB[i] |= (a&0xff) << 24;
                    intRGB[i] |= (r&0xff) << 16;
                    intRGB[i] |= (g&0xff) << 8;
                    intRGB[i] |= (b&0xff);

                }
            }
                break;
            case BitmapView.ARGB_565:
            {
                int bufRgbStep = 0;
                
                for(int i=0;i<width*height;i++)
                {
                    if(bufRgbStep > bufRgb.length-2)
                    {
                        break;
                    }
                    int b = bufRgb[bufRgbStep]&0x1f;
                    int gComp = bufRgb[bufRgbStep]&0xe0;
                    int g = gComp >> 5;
                    gComp = bufRgb[bufRgbStep+1]&0x07;
                    g |= gComp << 3;
                    int rComp = bufRgb[bufRgbStep+1]&0xf8;
                    int r = rComp >> 3;
                    int a = 0xff;
                    bufRgbStep+=2;
                    
                    intRGB[i] |= (a&0xff) << 24;
                    intRGB[i] |= (((r&0xff) << 16)*8);
                    intRGB[i] |= (((g&0xff) << 8)*4);
                    intRGB[i] |= (((b&0xff))*8);

                }
            }
                break;
            case BitmapView.ARGB_4444:
            {
                int bufRgbStep = 0;
                
                for(int i=0;i<width*height;i++)
                {
                    if(bufRgbStep > bufRgb.length-2)
                    {
                        break;
                    }
                    int b = bufRgb[bufRgbStep]&0xf;
                    int g = (bufRgb[bufRgbStep]&0xf0)>>4;
                    int r = bufRgb[bufRgbStep+1]&0xf;
                    int a = (bufRgb[bufRgbStep+1]&0xf0)>>4;
                    bufRgbStep+=2;
                    
                    intRGB[i] |= (a&0xff) << 24;
                    intRGB[i] |= (((r&0xff) << 16)*17);
                    intRGB[i] |= (((g&0xff) << 8)*17);
                    intRGB[i] |= ((b&0xff)*17);
                    
                }
            }
            break;
            default:
                break;
        }
        
        

        return intRGB;
    }
    
    //select RGBCfg chg
    private void updateRGBSel(Combo selColorCfg)
    {
        if(selColorCfg == null)
        {
            return;
        }
        String selTextString = selColorCfg.getText();
        if (selTextString.equals("RGB565"))
        {
            mGRBConfig = BitmapView.ARGB_565;
        }
        if (selTextString.equals("ARGB8888"))
        {
            mGRBConfig = BitmapView.ARGB_8888;
        }
        
        if (selTextString.equals("ARGB4444"))
        {
            mGRBConfig = BitmapView.ARGB_4444;
        }
        
        if (selTextString.equals("Custom"))
        {
            mGRBConfig = BitmapView.ARGB_CUSTEM;
        }
        if (editor != null)
            updateOnSelection(editor.getSelection());
    }
    
    private void initGroups(Composite parent)
    {
        groupForEdit = new Group (parent, SWT.LEFT_TO_RIGHT);
        groupForEdit.setLayout (new GridLayout ());
        groupForEdit.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        groupForEdit.setText ("Bitmap informations.");
        
        rgbCombo = new Combo(groupForEdit,SWT.READ_ONLY);
        rgbCombo.setItems(new String[] {"RGB565", "ARGB8888", "ARGB4444"});
        rgbCombo.setText(rgbCombo.getItem(0));
        
        rgbCombo.addSelectionListener (new SelectionAdapter ()
        {
            public void widgetSelected (SelectionEvent event)
            {
                updateRGBSel((Combo)event.widget);
            }
        });
        
        infoLabel = new Label(groupForEdit, SWT.WRAP);
        infoLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        infoLabel.setText("Bitmap view information");

        groupForGanvas = new Group(parent, SWT.LEFT_TO_RIGHT);
        groupForGanvas.setLayout(new GridLayout());
        groupForGanvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        groupForGanvas.setText("Show");
        
        canvas = new Canvas (groupForGanvas, SWT.LEFT_TO_RIGHT);
        
        canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        canvas.addPaintListener(new PaintListener () 
        {
            public void paintControl(PaintEvent e) 
            {
                GC gc = e.gc;
                if(gc != null && swtImage != null)
                {
                    gc.drawImage(swtImage,0,0);
                }
            }
        });
    }
    
    @Override
    public void setFocus()
    {
        // TODO Auto-generated method stub
        
    }

    public void selectionChanged(SelectionChangedEvent event)
    {
        // TODO Auto-generated method stub
        ISelection selection = event.getSelection();
        updateOnSelection(selection);
    }

    public void partActivated(IWorkbenchPart part)
    {
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
                this.snapshot = input.getSnapshot();
            }
            else
            {
                this.snapshot = null;
                
                input.addChangeListener(new ISnapshotEditorInput.IChangeListener()
                {

                    public void onBaselineLoaded(ISnapshot snapshot)
                    {}

                    public void onSnapshotLoaded(ISnapshot snapshot)
                    {
                        if (snapshot == null)
                            BitmapView.this.snapshot = snapshot;

                        input.removeChangeListener(this);
                    }
                });
            }

            this.editor.addSelectionChangedListener(this);
            //object chg
            updateOnSelection(this.editor.getSelection());
        }
        

    }
    
    public void partBroughtToTop(IWorkbenchPart part)
    {
        // TODO Auto-generated method stub
        partActivated(part);
    }

    public void partClosed(IWorkbenchPart part)
    {
        // TODO Auto-generated method stub
        if(part instanceof HeapEditor)
        {
            HeapEditor heapEditor = (HeapEditor) part;
            if (this.editor == heapEditor)
            {
                this.editor.removeSelectionChangedListener(this);

                this.snapshot = null;
                this.editor = null;
            }
        }


    }

    public void partDeactivated(IWorkbenchPart arg0)
    {
        // TODO Auto-generated method stub
    }

    public void partOpened(IWorkbenchPart arg0)
    {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void dispose()
    {
        if (this.editor != null)
        {
            this.editor.removeSelectionChangedListener(this);
            this.editor = null;
        }

        getSite().getPage().removePartListener(this);
        super.dispose();
    }
}