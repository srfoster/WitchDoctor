package mychangedetector.matching.constraints;

import java.util.List;
import java.util.Map;

import mychangedetector.change_management.ChangeWrapper;
import mychangedetector.specifications.Constraint;
import mychangedetector.specifications.FreeVar;
import mychangedetector.specifications.Requirement;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

public class ConsecutiveConstraint extends Constraint {
	protected String prefix;

	public ConsecutiveConstraint(String prefix)
	{
		this.prefix = prefix;
	}
	
	@Override
	public boolean isViolatedBy(String key, Map<String, ASTNode> matched_bindings, Requirement requirement, ChangeWrapper change) {			
		if(!key.startsWith(prefix))
			return false;
		
		String[] split_key = key.split(prefix); 
		int counter = Integer.parseInt(split_key[1]);
		
		if(counter == 1)
			return false;  // The first statement in a sequence is always in order.
		
		FreeVar var = requirement.getProperty(prefix+(counter-1));  //Get the binding that was previous in the sequence.
		ChangeWrapper change_wrapper = var.getChangeContext();
		ASTNode current = change_wrapper.getNode();
		
		
		
		ASTNode next = matched_bindings.get(key); //The AST node that was just matched, but hasn't been saved yet.
		
		return !follows(current,next);  //Adjacency is violated if the next node doesn't follow the current node
		
	}
			
	private boolean follows(ASTNode first, ASTNode second)
	{
		if(first == null || second == null)
			return true;
		
		System.out.println("Checking adjacency for " + first + " vs " + second);
							
		StructuralPropertyDescriptor first_location = first.getLocationInParent();
		StructuralPropertyDescriptor second_location = second.getLocationInParent();
		
		if(!first_location.isChildListProperty() || !second_location.isChildListProperty())
			return false;

		List<ASTNode> first_siblings = (List<ASTNode>) first.getParent().getStructuralProperty(first_location);
		List<ASTNode> second_siblings = (List<ASTNode>) second.getParent().getStructuralProperty(second_location);

		int first_index = first_siblings.indexOf(first);
		int second_index = second_siblings.indexOf(second);		
		
		return first_index == second_index - 1;
	}
}
