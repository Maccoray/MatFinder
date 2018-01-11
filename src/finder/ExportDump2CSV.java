package finder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

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
import org.eclipse.mat.util.IProgressListener;

import finder.util.CloudOutput;
import finder.util.UtilClass;


@CommandName("DumpCSV")
@Category("Finder")
@Name("12|Export to CSV")
@Help("Export snapshot file content to CSV")
public class ExportDump2CSV implements IQuery,IResultTree {
	@Argument
	public ISnapshot mOpenSnapshot;
	
	@Argument(isMandatory = false, flag = "o")
	@Help("Output csv filePath.")
	public String strOutCsvPath = "";
	
	public ExportDump2CSV() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public IResult execute(IProgressListener arg0) throws Exception {
		// TODO Auto-generated method stub
		Collection<IClass> miniDumpClasses = null;
		miniDumpClasses = mOpenSnapshot.getClassesByName("com.tencent.mobileqq.grayversion.MiniDumpConfig", false);
		if (miniDumpClasses == null)
		{
			//这是个正常的dump不是minidump，所以使用一般方式导出csv
			List<UtilClass.SnapshotClassInfo> classInfos = UtilClass.getSnapshotClasses(mOpenSnapshot);
			CloudOutput.objInfosToCsv(strOutCsvPath, classInfos);
		}
		else
		{
			System.out.println("MiniDump output file arg is [:"+strOutCsvPath+"]");
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			System.out.println("MiniDump begin at:"+df.format(new Date()));
			//这是一个minidump，采用minidump方式导出csv
			List<UtilClass.MiniDumpClassObjInfo> miniClassInfos = UtilClass.getminiDumpSnapshotClasses(mOpenSnapshot);
			System.out.println("MiniDump output file:"+strOutCsvPath);
			CloudOutput.miniObjInfosToCsv(strOutCsvPath,miniClassInfos);
		}
		
		
		return this;
	}

	@Override
	public Object getColumnValue(Object row, int columnIndex) {
        switch (columnIndex)
        {
            case 0:
                return "ok";
            case 1:
                return strOutCsvPath;
            default:
                return "Error";
        }
	}

	@Override
	public Column[] getColumns() {
		// TODO Auto-generated method stub
        // TODO Auto-generated method stub
        return new Column[] {
                new Column("Result", String.class).noTotals(),
                new Column("FilePath", String.class).noTotals()};
	}

	@Override
	public IContextObject getContext(Object row) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResultMetaData getResultMetaData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<?> getChildren(Object parent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<?> getElements() {
		// TODO Auto-generated method stub
		List<Integer>retArray = new ArrayList<Integer>();
		retArray.add(1);
		return retArray;
	}

	@Override
	public boolean hasChildren(Object element) {
		// TODO Auto-generated method stub
		return false;
	}

}
