package mychangedetector.sandbox;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.evolizer.changedistiller.treedifferencing.Node;

public class ASTToNodeVisitor extends ASTVisitor {
	Node root = null;
	Node current = null;
	
	public ASTToNodeVisitor()
	{
		//Node root = new MyTree
	}
	
	public Node getNode()
	{
		return root;
	}

	@Override
	public void preVisit(ASTNode node)
	{
		if(root == null)
		{
			root = new MyTreeDifferencingNode(node,true);
			current = root;
		} else {
			current = new MyTreeDifferencingNode(node,true);
		}
		
		node.setProperty("node",current);
	}
	
	@Override
	public void postVisit(ASTNode node)
	{
		if(node.getParent() != null)
		{
			Node my_node = (Node) node.getProperty("node");
			Node intended_parent = (Node) node.getParent().getProperty("node");
			
			intended_parent.add(my_node);
		}
	}
}
