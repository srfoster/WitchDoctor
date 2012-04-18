package mychangedetector.ast_helpers;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;

public class NodeComparisonStrategy {

	public boolean comp(Statement first, Statement second)
	{
		return first.toString().equals(second.toString());
	}
	
	public boolean comp(MethodDeclaration first, MethodDeclaration second)
	{
		String first_name = first.getName().toString();
		String second_name = second.getName().toString();
		
		boolean ret = first_name.equals(second_name);
		
		return ret;
	}
	
	public boolean comp(ASTNode first, ASTNode second)
	{
		
		if(first instanceof MethodDeclaration && second instanceof MethodDeclaration)
			return comp((MethodDeclaration)first, (MethodDeclaration)second);
		
		if(first instanceof Statement && second instanceof Statement)
			return comp((Statement)first, (Statement)second);
		
		return false;
	}
}
