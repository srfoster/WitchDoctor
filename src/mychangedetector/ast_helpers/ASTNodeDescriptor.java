package mychangedetector.ast_helpers;

import java.util.ArrayList;
import java.util.List;

import mychangedetector.matching.MatchingASTVisitor;
import mychangedetector.matching.MyASTMatcher;

import org.eclipse.jdt.core.dom.ASTNode;

public class ASTNodeDescriptor {
	String class_name;
	String binding_name;
	List<ASTNodeDescriptor> children = new ArrayList<ASTNodeDescriptor>();
	
	ASTNode node_described; //Optional
	
	public ASTNodeDescriptor(){
		
	}
	
	public ASTNodeDescriptor(String simple_name)
	{
		this.class_name = "org.eclipse.jdt.core.dom." + simple_name;
	}
	
	public ASTNodeDescriptor withChild(String simple_name)
	{
		this.addChild(new ASTNodeDescriptor(simple_name));
		return this;
	}
	
	public void setBindingName(String binding_name) {
		this.binding_name = binding_name;
	}
	
	public void setClassName(String class_name)
	{
		this.class_name = class_name;
	}

	public String getClassName() {
		return this.class_name;
	}
	
	public String getBindingName()
	{
		return binding_name;
	}
	
	public List<ASTNodeDescriptor> getChildren()
	{
		return children;
	}

	public void addChild(ASTNodeDescriptor child) {
		children.add(child);
	}
	
	public boolean describes(ASTNode node)
	{
		MyASTMatcher matcher = new MyASTMatcher();
		node.accept(new MatchingASTVisitor(matcher, this));
		return matcher.didMatch();
	}
	
	public void onMatch(String key, ASTNode node)
	{
		
		
	}
}
