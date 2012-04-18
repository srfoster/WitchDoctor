package mychangedetector.matching.constraints;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mychangedetector.change_management.ChangeWrapper;
import mychangedetector.specifications.Constraint;
import mychangedetector.specifications.FreeVar;
import mychangedetector.specifications.Requirement;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

public class MethodExtractionConstraint extends Constraint {
	
	String first_key,second_key;
	
	public MethodExtractionConstraint(String first_key, String second_key) {
		this.first_key = first_key;
		this.second_key = second_key;
	}

	
	@Override
	public boolean isViolatedBy(String key, Map<String, ASTNode> matched_bindings, Requirement requirement, ChangeWrapper change) {			
		if(!key.equals(first_key) && !key.equals(second_key))
			return false;
		
		String first_key_temp = first_key;
		String second_key_temp = second_key;
		
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
		
		return !isExtractedMethod(first_node,second_node, requirement, change);
	}
	
	private boolean isExtractedMethod(ASTNode method_call_node, ASTNode hook_statement_node, Requirement requirement, ChangeWrapper change)
	{

		 AST temp_ast = AST.newAST(AST.JLS3);
		 
		 ASTNode first_root = hook_statement_node.getRoot();
		 ASTNode first_root_copy = ASTNode.copySubtree(temp_ast,first_root);  // This will be the entire tree before the hook statement was deleted
		 
		 NodeFinder finder = new NodeFinder(first_root_copy, hook_statement_node.getStartPosition(), hook_statement_node.getLength());
		 ASTNode hook_statement_node_copy = finder.getCoveredNode();
		 
		 ASTNode hook_statement_method_copy = hook_statement_node_copy.getParent();
		 
		 while(!(hook_statement_method_copy instanceof MethodDeclaration))
		 {
			 hook_statement_method_copy = hook_statement_method_copy.getParent();
		 }
		 
		 //Now that we have a copy of the method in question, we'll be able to modify it and compare it with the parent of the method call, checking for method extractions.
		 //We just need the method call's parent.
		 
		 ASTNode method_call_method_copy = method_call_node.getParent();
		 ASTNode method_call_statement_copy = method_call_node.getParent();
		 
		 while(!(method_call_method_copy instanceof MethodDeclaration))
		 {
			 method_call_method_copy = method_call_method_copy.getParent();
			 
			 if(!(method_call_statement_copy instanceof Statement)) //Need to get the parent statement node too.
			 {
				 method_call_statement_copy = method_call_statement_copy.getParent();
			 }
		 }
		 
		 //The simplest check we can make is that the hook statement has been replaced by the method_call.
		 //  So we find the location of the hook statement copy in the hook statement copy's parent and replace it with a copy of the method call.
		 
		 
		 StructuralPropertyDescriptor hook_location = hook_statement_node_copy.getLocationInParent();  //This is the location for the replacement.
		 
		 ASTNode method_call_node_copy = ASTNode.copySubtree(temp_ast,method_call_statement_copy);  //We'll use this to replace.
		 
	
		 try{
			 int current_statement_number = 1;
			 List extracted_block = new ArrayList<ASTNode>();

			 extracted_block.add(hook_statement_node);

			 if(hook_location instanceof ChildListPropertyDescriptor)
			 {
				 List siblings = (List) hook_statement_node.getParent().getStructuralProperty(hook_location);
				 List siblings_copy = (List) hook_statement_node_copy.getParent().getStructuralProperty(hook_location);
				 
				 int index_of_first = siblings.indexOf(hook_statement_node);
	
				 siblings_copy.set(index_of_first,method_call_node_copy);
				 
				//Now compare the methods to see if the replacement we did caused them to be identical.
				 boolean extraction_detected = hook_statement_method_copy.subtreeMatch(new ASTMatcher(), method_call_method_copy); 
				 
				 
				 if(extraction_detected)
				 {
					 setRemovedStatements(extracted_block, requirement, change);
					 return true;
				 }
				 
				 //Now we start deleting the siblings to see if multiple lines have been replaced.
				 
				 while(siblings_copy.size() > index_of_first + 1 && siblings_copy.get(index_of_first + 1) != null)
				 {
					 siblings_copy.remove(index_of_first + 1);;
					 
					 extraction_detected = hook_statement_method_copy.subtreeMatch(new ASTMatcher(), method_call_method_copy); 
					 
					 extracted_block.add(siblings.get(index_of_first + current_statement_number++));
					 
					 if(extraction_detected)
					 {
						 setRemovedStatements(extracted_block, requirement, change);
						 return true;
					 }
				 }

				 return false;
			 }else{
				 hook_statement_node.getParent().setStructuralProperty(hook_location,method_call_node_copy);
				 

				//Now compare the methods to see if the replacement we did caused them to be identical.
				 boolean extraction_detected = hook_statement_method_copy.subtreeMatch(new ASTMatcher(), method_call_method_copy); 
				 
				 if(extraction_detected)
				 {
					 setRemovedStatements(extracted_block, requirement, change);

					 return extraction_detected;
				 }
			 }
		 }catch(RuntimeException re){
			 re.printStackTrace();
			 return false;
		 }
		 
		 return false;

	}


	private void setRemovedStatements(List extracted_block, Requirement requirement, ChangeWrapper change) {
		int count = 0;
		for(Object o : extracted_block)
			requirement.setProperty("removed_statement_"+count++, (ASTNode) o, change);
		
		System.out.println();
	}
}
