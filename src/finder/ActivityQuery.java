package finder;

import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;



import org.eclipse.mat.collect.ArrayInt;
import org.eclipse.mat.query.IQuery;
import org.eclipse.mat.query.IResult;
import org.eclipse.mat.query.annotations.Argument;
import org.eclipse.mat.query.annotations.Category;
import org.eclipse.mat.query.annotations.CommandName;
import org.eclipse.mat.query.annotations.Help;
import org.eclipse.mat.query.annotations.Name;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.query.ObjectListResult;
import org.eclipse.mat.util.IProgressListener;

import finder.util.ReportManager;


@CommandName("FinderActivity")
@Category("Finder")
@Name("1|Activity")
@Help("Find Activity")

public class ActivityQuery implements IQuery
{
    @Argument
	public ISnapshot mOpenSnapshot;
	
	@Argument(isMandatory = false, flag = "b")
	@Help("Base Activity class Name")
	public String mStringBaseClassName = "android.app.Activity";
	
	Logger mLog = Logger.getLogger(ActivityQuery.class.toString());
	
	public ActivityQuery() {
		// TODO Auto-generated constructor stub
	}

	public IResult execute(IProgressListener listener) throws Exception
	{
		Collection<IClass> classes = mOpenSnapshot.getClasses();
		if(classes.isEmpty()){
			return null;
		}
		
		Iterator<IClass> iter = classes.iterator();
		ArrayInt result = new ArrayInt();
		
		while(iter.hasNext()) {
			
			if (listener.isCanceled())
				break;
			
			IClass classfint = iter.next();
			IClass classSuperClass = classfint;

			while(classSuperClass.hasSuperClass()){
					
				classSuperClass = classSuperClass.getSuperClass();
				String stringSuperClassName = classSuperClass.getName();
				if( stringSuperClassName.equals(mStringBaseClassName) ){
					mLog.info("Find SuperClass !"+mStringBaseClassName+
							" ActivityName:" + classfint.getName());
					
					int[] objectIds = classfint.getObjectIds();
					result.addAll(objectIds);
					break;
				}
			}
		}
		ReportManager.ReportMsg msg = new ReportManager.ReportMsg(ReportManager.FINDER_RPMSG_Activity); 
		ReportManager.getSrvInst().sendRPMsg(msg);
		
		return new ObjectListResult.Outbound(mOpenSnapshot, result.toArray());
	}

}
