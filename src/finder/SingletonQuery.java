package finder;

import java.util.List;

import org.eclipse.mat.collect.ArrayInt;
import org.eclipse.mat.query.IQuery;
import org.eclipse.mat.query.IResult;
import org.eclipse.mat.query.annotations.Argument;
import org.eclipse.mat.query.annotations.Category;
import org.eclipse.mat.query.annotations.CommandName;
import org.eclipse.mat.query.annotations.Help;
import org.eclipse.mat.query.annotations.Name;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.Field;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.ObjectReference;
import org.eclipse.mat.snapshot.query.ObjectListResult;
import org.eclipse.mat.util.IProgressListener;

import finder.util.ReportManager;
import finder.util.UtilClass;


@CommandName("FinderSingleton")
@Category("Finder")
@Name("8|Singleton")
@Help("Find Singleton")

public class SingletonQuery implements IQuery
{
    @Argument
    public ISnapshot mOpenSnapshot;

    public SingletonQuery()
    {
        // TODO Auto-generated constructor stub
    }

    private boolean isClassEnum(IClass classfint)
    {
        IClass classSuperClass = classfint;

        while(classSuperClass.hasSuperClass()){

            classSuperClass = classSuperClass.getSuperClass();
            String stringSuperClassName = classSuperClass.getName();
            if( stringSuperClassName.contains("Enum")){
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public IResult execute(IProgressListener listener) throws Exception
    {
        // TODO Auto-generated method stub
        ArrayInt result = new ArrayInt();
        List<UtilClass.SnapshotClassInfo> clses = UtilClass.getSnapshotClasses(mOpenSnapshot);
        for(UtilClass.SnapshotClassInfo clsInfo:clses)
        {
            IClass cls = (IClass)mOpenSnapshot.getObject(clsInfo.iID);
            if(cls != null)
            {
                List<Field> staticFields = cls.getStaticFields();
                for(Field f:staticFields)
                {
                    if (f.getValue() instanceof ObjectReference)
                    {
                        ObjectReference ref = (ObjectReference) f.getValue();
                        if (ref != null)
                        {
                            String strClsName = ref.getObject().getClazz().getName();
                            if(strClsName.endsWith(clsInfo.strName))
                            {
                                if(!isClassEnum(cls))
                                {
                                    result.add(clsInfo.iID);
                                }
                            }
                        }
                    }
                }
            }
        }
        
        ReportManager.ReportMsg msg = new ReportManager.ReportMsg(ReportManager.FINDER_RPMSG_Singleton); 
        ReportManager.getSrvInst().sendRPMsg(msg);
        
        return new ObjectListResult.Outbound(mOpenSnapshot, result.toArray());
    }

}
