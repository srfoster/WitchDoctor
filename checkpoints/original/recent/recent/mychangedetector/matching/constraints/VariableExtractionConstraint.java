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
		
		
		ASTNode first_node, second_node;
		
		if(key.equals(first_key))
		{
			first_node  = matched_bindings.get(first_key);
			FreeVar second_free_var = requirement.getSpecification().getProperty(second_key);
			if(second_free_var == null || second_free_var.isNotBound())
				return false;
			
			second_node = second_free_var.binding();
		} else {
			second_node  = matched_bindings.get(second_key);
			FreeVar first_free_var = requirement.getSpecification().getProperty(first_key);
			if(first_free_var == null || first_free_var.isNotBound())
				return false;
			
			first_node = first_free_var.binding();
		}
		
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
					 first_parent_copy.setStructuralProperty(first_location,second_copy);
				 }catch(RuntimeException re){
					 return false;
				 }
				 
				 ASTNode second_root = second.getRoot();
				 
				 
				 boolean ret = second_root.subtreeMatch(new ASTMatcher(), first_root_copy);
				 
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
