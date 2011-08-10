package mychangedetector.ast_helpers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

public class ZippingASTVisitor extends ASTVisitor implements Iterable {

	private ASTNode to_zip_with = null;
	
	boolean first_run = true;
	
	private List<ASTNode> first_nodes = new ArrayList<ASTNode>();
	private List<ASTNode> second_nodes = new ArrayList<ASTNode>();
	
	private boolean reversed = false;
	
	public ZippingASTVisitor(ASTNode to_zip_with)
	{
		this.to_zip_with = to_zip_with;
		
		if(to_zip_with != null)
			to_zip_with.accept(this);
		
		this.first_run = false;
	}
	
	@Override
	public void preVisit(ASTNode node)
	{
		
		if(first_run == true)
			first_nodes.add(node);
		else
			second_nodes.add(node);
	}
	
	public List<ASTNode> getPair(int index)
	{
		List l = new ArrayList<ASTNode>();
		
		if(first_nodes.size() <= index)
			l.add(null);
		else
			l.add(first_nodes.get(index));
		
		if(second_nodes.size() <= index)
			l.add(null);
		else
			l.add(second_nodes.get(index));
		
		return l;
	}
	
	public int length()
	{
		int ret = first_nodes.size();
	//	if(ret != second_nodes.size())
	//		throw new RuntimeException("This is a bogus zipped list!");
		return ret;
	}

	private boolean reversed()
	{
		return reversed;
	}
	
	public void reverse()
	{
		reversed = !reversed;
	}
	
	@Override
	public Iterator iterator() {
		return new Iterator(){
		
			int reversed_current = length() - 1;
			
			int current = 0;
			
			
			@Override
			public boolean hasNext() {
				
				if(reversed())
					return reversed_current > -1;
				else	
					return current < length();
			}

			@Override
			public Object next() {
				if(reversed())
					return getPair(reversed_current--);
				else
					return getPair(current++);
			}

			@Override
			public void remove() {
				// TODO Auto-generated method stub
				
			}
			
		};
	}
}
