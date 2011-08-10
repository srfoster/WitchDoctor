package mychangedetector.ast_helpers;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;


public class MyASTVisitor extends ASTVisitor {
	int start;
	int end;
	
	ASTNode top_level_node = null;

	String message = "";

	int padding = 0;

	public ASTNode getTopLevelNode(){
		return top_level_node;
	}
	
	public String getMessage(){
		return message;
	}
	
	public String makePadding(){
		String ret = "";
		
		for(int i = 0; i < padding; i++)
			ret += " ";
		
		return ret;
	}

	private void appendToMessage(String string)
	{
		String mod_string = string.replace("\n","\n"+makePadding());
		message += makePadding() + mod_string + "\n";
	}

	public MyASTVisitor(int start, int end, int padding)
	{
		this.start = start;
		this.end   = end;
		this.padding = padding;
	}
	
	@Override
	public void preVisit(ASTNode node){
		if(node.getStartPosition() < this.start || node.getStartPosition() > this.end)
			return;
		
		if(top_level_node == null)
			top_level_node = node;
		
		padding += 5;
	}
	
	@Override
	public void postVisit(ASTNode node){
		if(node.getStartPosition() < this.start || node.getStartPosition() > this.end)
			return;
		
		padding -= 5;
	}

	@Override
	public boolean visit(CompilationUnit node) {
		return true;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		if(node.getStartPosition() < this.start || node.getStartPosition() > this.end)
			return true;


		appendToMessage("---->");
		appendToMessage("Method delcaration");
		appendToMessage("Name: " + node.getName());
		appendToMessage("Params: " + node.parameters());
		appendToMessage("Body: \n" + node.getBody());


		return true;
	}

	@Override
	public boolean	visit(Assignment node) {
		if(node.getStartPosition() < this.start || node.getStartPosition() > this.end)
			return true;

		appendToMessage("---->");
		appendToMessage("Assignment:");
		appendToMessage("  Left-hand side: " +node.getLeftHandSide());
		appendToMessage("  Right-hand side: " +node.getRightHandSide());
		return true;
	}

	@Override
	public boolean	visit(Block node) {
		if(node.getStartPosition() < this.start || node.getStartPosition() > this.end)
			return true;

		appendToMessage("---->");
		appendToMessage("Block");
		return true;
	}

	@Override
	public boolean	visit(BreakStatement node) {
		if(node.getStartPosition() < this.start || node.getStartPosition() > this.end)
			return true;

		appendToMessage("---->");
		appendToMessage("BreakStatement");
		return true;
	}

	@Override
	public boolean	visit(CatchClause node) 
	{
		if(node.getStartPosition() < this.start || node.getStartPosition() > this.end)
			return true;

		appendToMessage("---->");
		appendToMessage("CatchClause");
		return true;
	} 

	@Override
	public boolean	visit(ClassInstanceCreation node) 
	{
		if(node.getStartPosition() < this.start || node.getStartPosition() > this.end)
			return true;

		appendToMessage("---->");
		appendToMessage("ClassInstanceCreation");
		return true;
	}

	@Override
	public boolean	visit(ConditionalExpression node) 
	{
		if(node.getStartPosition() < this.start || node.getStartPosition() > this.end)
			return true;

		appendToMessage("---->");
		appendToMessage("ConditionalExpression");
		return true;
	}

	@Override
	public boolean	visit(ConstructorInvocation node) 
	{
		if(node.getStartPosition() < this.start || node.getStartPosition() > this.end)
			return true;

		appendToMessage("---->");
		appendToMessage("ConstructorInvocation");
		return true;
	}

	@Override
	public boolean	visit(EnhancedForStatement node) 
	{
		if(node.getStartPosition() < this.start || node.getStartPosition() > this.end)
			return true;

		System.out.println("EnhancedForStatement ");
		return true;
	}

	@Override
	public boolean	visit(ExpressionStatement node) 
	{
		if(node.getStartPosition() < this.start || node.getStartPosition() > this.end)
			return true;

		appendToMessage("---->");
		appendToMessage("ExpressionStatement");
		return true;
	}

	@Override
	public boolean	visit(FieldAccess node) 
	{
		if(node.getStartPosition() < this.start || node.getStartPosition() > this.end)
			return true;

		appendToMessage("---->");
		appendToMessage("FieldAccess");
		return true;
	}

	@Override
	public boolean	visit(FieldDeclaration node) 
	{
		if(node.getStartPosition() < this.start || node.getStartPosition() > this.end)
			return true;

		appendToMessage("---->");
		appendToMessage("FieldDeclaration");
		appendToMessage("Fragments: " + node.fragments());
		appendToMessage("Type: " + node.getType());

		return true;
	}

	@Override
	public boolean	visit(ForStatement node) 
	{
		if(node.getStartPosition() < this.start || node.getStartPosition() > this.end)
			return true;

		appendToMessage("---->");
		appendToMessage("ForStatement");
		return true;
	}

	@Override
	public boolean	visit(IfStatement node) 
	{
		if(node.getStartPosition() < this.start || node.getStartPosition() > this.end)
			return true;

		appendToMessage("---->");
		appendToMessage("IfStatement");
		return true;
	}

	@Override
	public boolean	visit(InfixExpression node) 
	{
		if(node.getStartPosition() < this.start || node.getStartPosition() > this.end)
			return true;

		appendToMessage("---->");
		appendToMessage("InfixExpression");
		return true;
	}

	@Override
	public boolean	visit(Initializer node) 
	{
		if(node.getStartPosition() < this.start || node.getStartPosition() > this.end)
			return true;

		appendToMessage("---->");
		appendToMessage("Initializer");
		return true;
	}

	@Override
	public boolean	visit(MemberRef node) 
	{
		if(node.getStartPosition() < this.start || node.getStartPosition() > this.end)
			return true;

		appendToMessage("---->");
		appendToMessage("MemberRef");
		return true;
	}

	@Override
	public boolean	visit(MemberValuePair node) 
	{
		if(node.getStartPosition() < this.start || node.getStartPosition() > this.end)
			return true;

		appendToMessage("---->");
		appendToMessage("MemberValuePair");
		return true;
	}

	@Override
	public boolean	visit(MethodInvocation node) 
	{
		if(node.getStartPosition() < this.start || node.getStartPosition() > this.end)
			return true;

		appendToMessage("---->");
		appendToMessage("MethodInvocation");
		return true;
	}

	@Override
	public boolean	visit(MethodRef node) 
	{
		if(node.getStartPosition() < this.start || node.getStartPosition() > this.end)
			return true;

		appendToMessage("---->");
		appendToMessage("MethodRef");
		return true;
	}

	@Override
	public boolean	visit(MethodRefParameter node) 
	{
		if(node.getStartPosition() < this.start || node.getStartPosition() > this.end)
			return true;

		appendToMessage("---->");
		appendToMessage("MethodRefParameter");
		return true;
	}

	@Override
	public boolean	visit(ParenthesizedExpression node) 
	{
		if(node.getStartPosition() < this.start || node.getStartPosition() > this.end)
			return true;

		appendToMessage("---->");
		appendToMessage("ParenthesizedExpression");
		return true;
	}

	@Override
	public boolean	visit(PostfixExpression node) 
	{
		if(node.getStartPosition() < this.start || node.getStartPosition() > this.end)
			return true;

		appendToMessage("---->");
		appendToMessage("PostfixExpression");
		return true;
	}

	@Override
	public boolean	visit(PrefixExpression node) 
	{
		if(node.getStartPosition() < this.start || node.getStartPosition() > this.end)
			return true;

		appendToMessage("---->");
		appendToMessage("PrefixExpression");
		return true;
	}

	@Override
	public boolean	visit(ReturnStatement node) 
	{
		if(node.getStartPosition() < this.start || node.getStartPosition() > this.end)
			return true;

		appendToMessage("---->");
		appendToMessage("ReturnStatement");
		return true;
	}


	@Override
	public boolean visit(SingleVariableDeclaration node) 
	{
		if(node.getStartPosition() < this.start || node.getStartPosition() > this.end)
			return true;

		appendToMessage("---->");
		appendToMessage("SingleVariableDeclaration");
		return true;
	}

	@Override
	public boolean visit(ThisExpression node) 
	{
		if(node.getStartPosition() < this.start || node.getStartPosition() > this.end)
			return true;

		appendToMessage("---->");
		appendToMessage("ThisExpression");
		return true;
	}

	@Override
	public boolean	visit(ThrowStatement node) 
	{
		if(node.getStartPosition() < this.start || node.getStartPosition() > this.end)
			return true;

		appendToMessage("---->");
		appendToMessage("ThrowStatement");
		return true;
	}

	@Override
	public boolean	visit(TryStatement node) 
	{
		if(node.getStartPosition() < this.start || node.getStartPosition() > this.end)
			return true;

		appendToMessage("---->");
		appendToMessage("TryStatement");
		return true;
	}

	@Override
	public boolean	visit(TypeDeclaration node) 
	{
		if(node.getStartPosition() < this.start || node.getStartPosition() > this.end)
			return true;

		appendToMessage("---->");
		appendToMessage("TypeDeclaration");
		return true;
	}

	@Override
	public boolean	visit(TypeDeclarationStatement node) 
	{
		if(node.getStartPosition() < this.start || node.getStartPosition() > this.end)
			return true;

		appendToMessage("---->");
		appendToMessage("TypeDeclarationStatement");
		return true;
	}

	@Override
	public boolean	visit(VariableDeclarationExpression node) 
	{
		if(node.getStartPosition() < this.start || node.getStartPosition() > this.end)
			return true;

		appendToMessage("---->");
		appendToMessage("VariableDeclarationExpression");
		return true;
	}

	@Override
	public boolean	visit(VariableDeclarationFragment node) 
	{
		if(node.getStartPosition() < this.start || node.getStartPosition() > this.end)
			return true;

		appendToMessage("---->");
		appendToMessage("VariableDeclarationFragment");
		return true;
	}

	@Override
	public boolean	visit(VariableDeclarationStatement node) 
	{
		if(node.getStartPosition() < this.start || node.getStartPosition() > this.end)
			return true;

		appendToMessage("---->");
		appendToMessage("VariableDeclarationStatement");
		return true;
	}

	@Override
	public boolean	visit(WhileStatement node) 
	{
		if(node.getStartPosition() < this.start || node.getStartPosition() > this.end)
			return true;

		appendToMessage("---->");
		appendToMessage("WhileStatement");
		return true;
	}

}
