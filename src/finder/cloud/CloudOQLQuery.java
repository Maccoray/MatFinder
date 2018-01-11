package finder.cloud;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.mat.query.Column;
import org.eclipse.mat.query.IQuery;
import org.eclipse.mat.query.IResult;
import org.eclipse.mat.query.IResultTable;
import org.eclipse.mat.query.IResultTree;
import org.eclipse.mat.query.annotations.Argument;
import org.eclipse.mat.query.annotations.Category;
import org.eclipse.mat.query.annotations.CommandName;
import org.eclipse.mat.query.annotations.Help;
import org.eclipse.mat.query.annotations.Name;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.query.SnapshotQuery;
import org.eclipse.mat.util.IProgressListener;

@CommandName("FindCloudOQL")
@Category("FinderRule")
@Name("2|Run a OQL")
@Help("Run a OQL")

public class CloudOQLQuery implements IQuery
{
	Logger mLog = Logger.getLogger(CloudOQLQuery.class.toString());
	
	@Argument
	public ISnapshot mOpenSnapshot;
	
	
	@Argument(isMandatory = true, flag = "oql")
	@Help("OQL String.")
	public String oqlString = "";
	
	
	@Argument(isMandatory = true, flag = "of")
	@Help("Output file.")
	//public String strOutPutDir = "C:\\Users\\Novels\\Desktop\\testDump";
	public String strOutputFile = "";
	
	public CloudOQLQuery()
	{
		// TODO Auto-generated constructor stub
	}

	private void writeRetLine(String lineString) throws IOException
	{
		// TODO Auto-generated method stub
		File outputFile = new File(strOutputFile);
		FileWriter fw = new FileWriter(outputFile,true);
		
		if (!outputFile.exists())
		{
			outputFile.createNewFile();
		}
		fw.write(lineString+"\n");
		fw.flush();
		fw.close();
	}
	
	@Override
	public IResult execute(IProgressListener listener) throws Exception
	{
		// TODO Auto-generated method stub
		IResult ir = SnapshotQuery.lookup("oql", mOpenSnapshot).setArgument("queryString", oqlString).execute(listener);
		
		if(ir instanceof IResultTable)
		{
			IResultTable rTable = (IResultTable)ir;
			String strHeaderString = "";
			Column[] cols = rTable.getColumns();
			for (int iCol = 0; iCol < cols.length; iCol++)
			{
				strHeaderString += cols[iCol].getLabel()+"#,#";
			}
			writeRetLine(strHeaderString);
			
			int iRowCount = rTable.getRowCount();
			
			for (int iRowIndex = 0; iRowIndex < iRowCount; iRowIndex++)
			{
				String rowString = "";
				for (int iCol2 = 0; iCol2 < cols.length; iCol2++)
				{
					String rowCValueString = rTable.getColumnValue(rTable.getRow(iRowIndex),iCol2).toString();
					rowString += rowCValueString + "#,#";
				}
				writeRetLine(rowString);
			}
		}
		else if(ir instanceof IResultTree) {
			IResultTree rTree = (IResultTree)ir;
			String strHeaderString = "";
			Column[] cols = rTree.getColumns();
			for (int iCol = 0; iCol < cols.length; iCol++)
			{
				strHeaderString += cols[iCol].getLabel()+"#,#";
			}
			writeRetLine(strHeaderString);
			
			List<?> elements = rTree.getElements();
			for (Object rowObj:elements)
			{
				String rowString = "";
				for (int iCol2 = 0; iCol2 < cols.length; iCol2++)
				{
					String rowCValueString = rTree.getColumnValue(rowObj,iCol2).toString();
					rowString += rowCValueString + "#,#";
				}
				writeRetLine(rowString);
			}
		}

		return ir;
	}

}
