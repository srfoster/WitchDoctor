package mychangedetector.specifications;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mychangedetector.ast_helpers.ASTNodeDescriptor;
import mychangedetector.ast_helpers.ZippingASTVisitor;
import mychangedetector.change_management.ChangeWrapper;
import mychangedetector.differencer.Diff;
import mychangedetector.differencer.simple_differencer.SimpleTextDiffEntity;
import mychangedetector.matching.MatchListener;
import mychangedetector.matching.MatchingASTVisitor;
import mychangedetector.matching.MyASTMatcher;
import mychangedetector.matching.constraints.MatchConstraint;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;

public class ChangeMatcher implements Cloneable {

	private ASTNodeDescriptor before_node_matcher;
	private ASTNodeDescriptor after_node_matcher;
	private String change_type;
		
	private List<MatchConstraint> constraints = new ArrayList<MatchConstraint>();
	
	MyASTMatcher matcher = new MyASTMatcher();
	
	private Map<String,ASTNode> before_matches = new HashMap<String,ASTNode>();
	private Map<String,ASTNode> after_matches  = new HashMap<String,ASTNode>();
	
	private boolean matching_before = true;

	public ChangeMatcher(){
		 matcher.addListener("before_listener", new MatchListener(){
			 public boolean propertySet(String key, ASTNode value){
				 if(matching_before)
				 {
					 before_matches.put(key,value);
				 } else {
					 after_matches.put(key,value);
				 }
				 return true;
			 }
		 });
	}
	
	public Map<String, ASTNode> getBeforeBindings(){
		return before_matches;
	}
	
	public Map<String, ASTNode> getAfterBindings(){
		return after_matches;
	}
	
	public Map<String, ASTNode> getProperties()
	{
		return matcher.getProperties();
	}
	
	public ASTNode getProperty(String key)
	{
		return matcher.getProperty(key);
	}
	
	public void setProperty(String key, ASTNode prop)
	{
		matcher.setProperty(key,prop);
	}
	
	public void setMatcher(MyASTMatcher matcher)
	{
		this.matcher = matcher;
	}
	
	public void setBeforeNodeMatcher(ASTNodeDescriptor node)
	{
		before_node_matcher = node;
	}
	
	public void setAfterNodeMatcher(ASTNodeDescriptor node)
	{
		after_node_matcher = node;
	}
	
	public void setChangeType(String type)
	{
		change_type = type;
	}
	
	public ASTNodeDescriptor getBeforeNodeMatcher()
	{
		return before_node_matcher;
	}
	
	public ASTNodeDescriptor getAfterNodeMatcher()
	{
		return after_node_matcher;
	}
	
	public String getChangeType()
	{
		return change_type;
	}
	
	public void setConstraint(MatchConstraint c)
	{
		constraints.add(c);
	}
	
	/**
	 * 
	 * @param operation : The type operation to look for : "INSERT", "DELETE", or "UPDATE"
	 * @param change    : The wrapper for the change operation.
	 */
	public void match(String operation, ChangeWrapper change)
	{
		if(change.getChangeType().equals("UPDATE"))
		{
			ASTNode before = change.getNode();
			ASTNode after  = change.getUpdatedNode();
			
			if(before != null && after != null && before.toString().equals(after.toString()))
				return;
		}
		
		String the_type = change.getChangeType();
//		if(!the_type.equals(change_type))
//			return new HashMap<String,ASTNode>();
		
		if(operation == null || operation.equals("UPDATE"))
		{
			matchUpdate(change);
		}
		
		
		matchInsertOrRemove(operation, change);
	}

	
	private void matchInsertOrRemove(String operation, ChangeWrapper change) {
		
		matching_before = true;
		
		if(operation == null || operation.equals("DELETE"))
		{
			ASTNode to_match_against = change.getNode();
			
			if(change.getChangeType().equals("INSERT"))
				to_match_against = null;

			match(to_match_against, "BEFORE");
		}
		
		matching_before = false;
		
		if(operation == null || operation.equals("INSERT"))
		{
			
			ASTNode to_match_against = null;
			
			if(change.getChangeType().equals("UPDATE"))
				to_match_against = change.getUpdatedNode();
			if(change.getChangeType().equals("INSERT"))
				to_match_against = null;
			

			match(to_match_against, "AFTER");
		}
		
	}
	
	public void match(ASTNode node, String which_matcher)
	{
		if(node != null)
		{
			if(which_matcher.equals("BEFORE")) {
				matching_before = true;
				node.accept(new MatchingASTVisitor(matcher, before_node_matcher));
			} else if (which_matcher.equals("AFTER")) {
				matching_before = false;
				node.accept(new MatchingASTVisitor(matcher, after_node_matcher));
			} else {
				throw new RuntimeException("which_matcher should be 'BEFORE' or 'AFTER'");
			}
		} else {
			System.out.println("ASTNode was null...");
		}
	}
	
	private Map<String, ASTNode> matchUpdate(ChangeWrapper change) {
		
		
		ASTNode before = change.getNode();
		ASTNode after  = change.getUpdatedNode();
		
		//When nodes are unparsable, they'll be null here.
		if(after == null && before == null)
		{
			return new HashMap<String,ASTNode>();
		}
		
		if(after != null && before != null)
		{
			
			ZippingASTVisitor zipper = new ZippingASTVisitor(before);
			after.accept(zipper);
			
			zipper.reverse();
			for(Object pair_obj : zipper)
			{
				 List<ASTNode> pair = (List<ASTNode>) pair_obj;
				 
				 ASTNode first = pair.get(0);
				 ASTNode second = pair.get(1);
				 
				 if(first.subtreeMatch(new ASTMatcher(), second))
					 continue;
				 
				 matching_before = true;
					
				 if(first != null)
					 first.accept(new MatchingASTVisitor(matcher, before_node_matcher));
				 
				 matching_before = false;
				 
				 if(second != null)
					 second.accept(new MatchingASTVisitor(matcher, after_node_matcher));
			}
			
			return matcher.getProperties();
		}
		
		
		if(before != null)
		{
			matching_before = true;
			
			before.accept(new MatchingASTVisitor(matcher,before_node_matcher));
			return matcher.getProperties();

		}
		
		if(after != null){
			matching_before = false;
			
			after.accept(new MatchingASTVisitor(matcher,after_node_matcher));;
			return matcher.getProperties();
			

		}
		
		return null;

	}
	

	
	
	

}
