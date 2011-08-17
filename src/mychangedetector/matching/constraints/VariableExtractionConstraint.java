package mychangedetector.matching.constraints;

import java.util.List;
import java.util.Map;

import mychangedetector.ast_helpers.ZippingASTVisitor;
import mychangedetector.change_management.ChangeWrapper;
import mychangedetector.specifications.Constraint;
import mychangedetector.specifications.FreeVar;
import mychangedetector.specifications.Requirement;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

public class VariableExtractionConstraint extends Constraint {
	
	String first_key,second_key,extracted_expression_binding_name,var_name_binding_name;
	
	public VariableExtractionConstraint(String first_key, String second_key, String var_name_binding_name, String extracted_expression_binding_name) {
		this.first_key = first_key;
		this.second_key = second_key;
		this.var_name_binding_name = var_name_binding_name;
		this.extracted_expression_binding_name = extracted_expression_binding_name;
	}

	
	@Override
	public boolean isViolatedBy(String key, Map<String, ASTNode> matched_bindings, Requirement requirement, ChangeWrapper change) {			
		if(!key.equals(first_key) && !key.equals(second_key))
			return false;
		
		//Adjust directionality of violation check based on which key we are checking.
		String first_key_temp = first_key;
		String second_key_temp = second_key;
		
		/*
		if(key.equals(second_key))
		{
			first_key_temp = second_key;
			second_key_temp = first_key;
		}
		*/
		
		ASTNode first_node, second_node;
		
		//See if we've already bound it.
		FreeVar first_free_var = requirement.getSpecification().getProperty(first_key_temp);
		first_node = first_free_var.binding();
			
		//See if we just bound it.
		if(first_node == null)
			first_node  = matched_bindings.get(first_key_temp);
			
		//See if we've already bound it.
		FreeVar second_free_var = requirement.getSpecification().getProperty(second_key_temp);
		second_node = second_free_var.binding();
		
		//See if we just bound it.
		if(second_node == null)
			second_node = matched_bindings.get(second_key_temp);
		
		
		if(first_node == null || second_node == null)
			return false;;
		
		return !isExtractedVar(first_node,second_node, requirement, change);
	}
	
	private boolean isExtractedVar(ASTNode first_node, ASTNode second_node, Requirement requirement, ChangeWrapper change)
	{
		ZippingASTVisitor zipper = new ZippingASTVisitor(first_node);
		second_node.accept(zipper);
		
		zipper.reverse();
		for(Object pair_obj : zipper)
		{
			 List<ASTNode> pair = (List<ASTNode>) pair_obj;
			 
			 ASTNode first = pair.get(0);
			 ASTNode second = pair.get(1);
			 
			 //If the first node is null, we can assume that the second tree
			 // is not an "extraction" of the first.
			 //Extraction implies that, for every node in the second tree, there
			 // should be a corresponding node in the first tree.
			 if(first == null)
				 break;
			 
			 //We're looking for the lowest non-null node pair that differs.
			 //  If there has been an extraction, this is the extraction point.
			 //That is, in the trees "a + (b + c)" an "a + i", we want to find
			 //  the pair "(b + c)" and "i".
			 //We also want to ensure that the second node is a single variable.
			 //And we want to be sure the second node is not a single variable --
			 //  to make this refactoring distinct from rename.
			 
			 if(second != null && !second.subtreeMatch(new ASTMatcher(),first) && (second instanceof SimpleName) && !(first instanceof SimpleName))
			 {
				 AST temp_ast = AST.newAST(AST.JLS3);
				 
				 ASTNode first_root = first.getRoot();
				 ASTNode first_root_copy = ASTNode.copySubtree(temp_ast,first_root);
				 
				 ASTNode first_parent = first.getParent();
				 
				 NodeFinder finder = new NodeFinder(first_root_copy, first_parent.getStartPosition(), first_parent.getLength());
				 ASTNode first_parent_copy = finder.getCoveredNode();
				 
				 StructuralPropertyDescriptor first_location = first.getLocationInParent();
				 
				 ASTNode second_copy = ASTNode.copySubtree(temp_ast,second);
				 
			
				 try{
					 if(first_location instanceof ChildListPropertyDescriptor)
					 {
						 List siblings = (List) first_parent.getStructuralProperty(first_location);
						 List siblings_copy = (List) first_parent_copy.getStructuralProperty(first_location);

						 int index_of_first = siblings.indexOf(first);

						 siblings_copy.set(index_of_first,second_copy);;
					 }else{
						 first_parent_copy.setStructuralProperty(first_location,second_copy);
						// second_parent_copy.setStructuralProperty(second_location,first_copy);
					 }
				 }catch(RuntimeException re){
					 re.printStackTrace();
					 return false;
				 }
				 
				 ASTNode second_root = second.getRoot();
				 
				 
				 boolean ret = first_parent_copy.subtreeMatch(new ASTMatcher(), second_copy.getParent());
				 
				 if(ret)
				 {
					 //This is a constraint, but since we have the information we need right now, let's 
					 //  add the bindings.
					 
					 requirement.setProperty(var_name_binding_name, second, change);
					 requirement.setProperty(extracted_expression_binding_name, first, change);
					 
					 return true;
				 } else {
					 return false;
				 }
			 }
		}
		
 
		return false;
	}
}
