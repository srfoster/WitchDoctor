package mychangedetector.differencer.change_distiller;

import java.util.ArrayList;
import java.util.List;

import mychangedetector.change_management.ExtendedDistiller;
import mychangedetector.differencer.Diff;
import mychangedetector.differencer.Differencer;

import org.eclipse.core.resources.IFile;
import org.evolizer.changedistiller.model.entities.SourceCodeChange;

public class ChangeDistillerDifferencer implements Differencer {

	public ChangeDistillerDifferencer()
	{
		
	}
	
	@Override
	public List<Diff> perform(IFile left, IFile right)
	{
    	
        ExtendedDistiller fDJob = new ExtendedDistiller();
        fDJob.performDistilling(left, right);
        
        List<Diff> ret = new ArrayList<Diff>();
         
        for(SourceCodeChange change : fDJob.getSourceCodeChanges())
        {
        	ret.add(new ChangeDistillerDiff(change));
        }
        
        return ret;
	} 
}
