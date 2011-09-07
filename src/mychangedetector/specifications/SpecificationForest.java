package mychangedetector.specifications;

import java.util.ArrayList;
import java.util.List;

import mychangedetector.change_management.ChangeWrapper;

import org.eclipse.jdt.core.dom.ASTNode;

public class SpecificationForest {
	
	private List<Specification> specifications = new ArrayList<Specification>();
	private List<Specification> empty_specs = new ArrayList<Specification>();

	
	public void addSpecification(Specification s)
	{
		specifications.add(s);
		empty_specs.add(s);
	}
	
	public void reset()
	{
		specifications = empty_specs;
	}
	
	public void handleChanges(List<ChangeWrapper> changes)
	{
		ArrayList<Specification> toAdd = new ArrayList<Specification>();
		
		boolean no_progress = true;
		
		boolean executed = false; 
		for(Specification spec : specifications)
		{
			Specification clone = spec.copy();

			for(ChangeWrapper change : changes)
			{
				clone.match(change);
			}
			
			
			if(!specifications.contains(clone))
			{
				no_progress = false;
				executed = clone.tryExecute();
				toAdd.add(clone);
			}
			
			toAdd.add(spec);
			
		}
		
		if(executed) // || no_progress)
			reset();
		else
			specifications = toAdd;
	}
	

	
	public void print()
	{
		System.out.println("Current matches");
		for(Specification spec : specifications)
		{
			System.out.println(spec);
		}
	}

}
