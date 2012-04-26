package mychangedetector.change_management;

import java.util.ArrayList;
import java.util.List;

import mychangedetector.builder.FileVersion;
import mychangedetector.differencer.Diff;
import mychangedetector.specifications.Specification;
import mychangedetector.specifications.SpecificationForest;

import org.evolizer.changedistiller.model.entities.SourceCodeChange;

public class ChangeStream {
	
	List<ChangeSet> list = new ArrayList<ChangeSet>();
	int max_size = 10;
	int min_size = 5;
	
	
	Specification rename_detector = Specification.newRenameSpecification();
	Specification extract_detector = Specification.newExtractMethodSpecification();
	Specification extract_variable_detector = Specification.newExtractVariableSpecification();
	Specification try_catch = Specification.newTryCatchSpecification();

	
	SpecificationForest spec_forest = new SpecificationForest();
	
	public ChangeStream()
	{
		spec_forest.addSpecification(rename_detector);
		spec_forest.addSpecification(extract_variable_detector);
		spec_forest.addSpecification(extract_detector);
		spec_forest.addSpecification(try_catch);
	}
	
	public void clear()
	{
		list.clear();
		list = new ArrayList<ChangeSet>();
		
		spec_forest.reset();
	}
	
	public Object[] toArray()
	{
		ArrayList<SourceCodeChange> ret = new ArrayList<SourceCodeChange>();
		
		for(ChangeSet set : list)
		{
			ret.addAll(set);
		}
		
		return ret.toArray();
	}
	
	public void addSet(List<Diff> new_changes, FileVersion before, FileVersion after){
		if(new_changes.size() == 0) return;
		
		if(list.size() > max_size)
		{
			for(int i = 0; i < min_size; i++){
				list.remove(0);
			}
		}
		
		ChangeSet new_set = new ChangeSet();
		new_set.setBeforeFile(before);
		new_set.setAfterFile(after);
		new_set.addAll(new_changes);
		list.add(new_set);
	
		//rename_detector.addChanges(new_set);
		
		spec_forest.handleChanges(new_set.getChanges());
	}
	
	public void print()
	{
		System.out.println("Stream contains " + list.size() + " change sets.");
		
		int count = 0;
		for(ChangeSet set : list)
		{
			count++;
			System.out.println("Set #"+count);
			set.print();
			System.out.println();
		}
		
		System.out.println("Detections...");

		spec_forest.print();

	}

	
}