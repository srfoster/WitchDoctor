package mychangedetector.sandbox;

import java.util.Enumeration;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.Statement;
import org.evolizer.changedistiller.treedifferencing.Node;

public class MyTreeDifferencingNode extends Node {

	private static final long serialVersionUID = 1L;
	private String value = null;
	
	ASTNode node;
	
	public MyTreeDifferencingNode(ASTNode node, boolean allowsChildren)
	{
		super(node,allowsChildren);
		this.node = node;
		enableInOrder();
	}
	
	@Override
	public String getValue()
	{
		return node.toString();
	}

	@Override
	public boolean isLeaf()
	{
		if(node instanceof Statement)
			return true;
		return super.isLeaf();
	}
	
	@Override
    public Enumeration children() {
		if (children == null || node instanceof Statement) {
		    return EMPTY_ENUMERATION;
		} else {
		    return children.elements();
		}
	}
	
	@Override
	public void setValue(String s){
		value = s;
	}
	
	public int getASTType(){
		return node.getNodeType();
	}
	
	public boolean isComment(){
		return node instanceof Comment;
	}
}
