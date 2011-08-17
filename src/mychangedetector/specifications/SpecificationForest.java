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
		
		
		boolean executed = false; 
		for(Specification spec : specifications)
		{
			Specification clone = spec.copy();

			for(ChangeWrapper change : changes)
			{
				clone.match(change);
			}
			
			
			if(!clone.equals(spec))
			{
				executed = clone.tryExecute();
			}
			
			toAdd.add(clone);

		}
		
		if(executed)
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
