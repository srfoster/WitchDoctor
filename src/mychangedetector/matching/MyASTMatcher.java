package mychangedetector.matching;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mychangedetector.ast_helpers.ASTNodeDescriptor;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.WildcardType;

public class MyASTMatcher extends ASTMatcher {
	
	boolean did_match = false;
	
	private Map<String, ASTNode> properties = new HashMap<String, ASTNode>();
	
	private Map<String, MatchListener> listeners = new HashMap<String,MatchListener>();
	
	public void addListener(String name, MatchListener listener)
	{
		listeners.put(name, listener);
	}
	
	public void removeListener(String name)
	{
		listeners.remove(name);
	}
	
	public ASTNode getProperty(String s)
	{
		return properties.get(s);
	}
	
	public Map<String, ASTNode> getProperties() {
		return properties;
	}

	public void setProperty(String s, ASTNode n)
	{
	   
		if(getProperty(s) != null) return; // Don't reset.  Bindings should "stick" once bound.
		
	   for(String key : listeners.keySet())
	   {
		   MatchListener listener = listeners.get(key);
		   if (listener.propertySet(s,n) == false)
		   {
			   System.out.println("Would have set " + s + " to " + n + ", but a listener prevented it.");
			   return;
		   }
	   }
	   
	  // System.out.println("Setting " + s + " to " + n);

	   properties.put(s,n);
		
	  

	}
	
	
	
	public boolean didMatch()
	{
		return did_match;
	}

	
	public boolean abstractMatch(ASTNode node, Object other){	
		if(other == null || node == null)
		{
			return false;
		}
		
		ASTNodeDescriptor match = (ASTNodeDescriptor) other;
		
		if(!match.describes(node))
			return false;
		
				
		if(match.getBindingName() != null)
		{
			setProperty((String)match.getBindingName(),node);;
			match.onMatch((String)match.getBindingName(),node);
		}
		
		
		List<ASTNodeDescriptor> children = match.getChildren();
		
		this.did_match = true;
		
		if(children != null && children.size() > 0)
		{
			boolean children_matched = false;

			for(ASTNodeDescriptor sub_match : children)
			{
				MyASTMatcher new_matcher = new MyASTMatcher();
				node.accept(new MatchingASTVisitor(new_matcher,sub_match));
				
				children_matched = children_matched || new_matcher.didMatch();
			}
			
			this.did_match = this.did_match && children_matched;
		}
		
		
		return this.did_match;
	}
	
	
	
	

	public boolean	match(AnnotationTypeDeclaration node, Object other){ return abstractMatch(node, other); } 
    
	public boolean	match(AnnotationTypeMemberDeclaration node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(AnonymousClassDeclaration node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(ArrayAccess node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(ArrayCreation node, Object other){ return abstractMatch(node, other); } 

	public boolean	match(ArrayInitializer node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(ArrayType node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(AssertStatement node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(Assignment node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(BlockComment node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(Block node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(BooleanLiteral node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(BreakStatement node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(CastExpression node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(CatchClause node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(CharacterLiteral node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(ClassInstanceCreation node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(CompilationUnit node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(ConditionalExpression node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(ConstructorInvocation node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(ContinueStatement node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(DoStatement node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(EmptyStatement node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(EnhancedForStatement node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(EnumConstantDeclaration node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(EnumDeclaration node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(ExpressionStatement node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(FieldAccess node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(FieldDeclaration node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(ForStatement node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(IfStatement node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(ImportDeclaration node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(InfixExpression node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(Initializer node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(InstanceofExpression node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(Javadoc node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(LabeledStatement node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(LineComment node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(MarkerAnnotation node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(MemberRef node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(MemberValuePair node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(MethodDeclaration node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(MethodInvocation node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(MethodRef node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(MethodRefParameter node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(Modifier node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(NormalAnnotation node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(NullLiteral node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(NumberLiteral node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(PackageDeclaration node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(ParameterizedType node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(ParenthesizedExpression node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(PostfixExpression node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(PrefixExpression node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(PrimitiveType node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(QualifiedName node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(QualifiedType node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(ReturnStatement node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(SimpleName node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(SimpleType node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(SingleMemberAnnotation node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(SingleVariableDeclaration node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(StringLiteral node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(SuperConstructorInvocation node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(SuperFieldAccess node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(SuperMethodInvocation node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(SwitchCase node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(SwitchStatement node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(SynchronizedStatement node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(TagElement node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(TextElement node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(ThisExpression node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(ThrowStatement node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(TryStatement node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(TypeDeclaration node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(TypeDeclarationStatement node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(TypeLiteral node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(TypeParameter node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(VariableDeclarationExpression node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(VariableDeclarationFragment node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(VariableDeclarationStatement node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(WhileStatement node, Object other){ return abstractMatch(node, other); } 
	     
	public boolean	match(WildcardType node, Object other){ return abstractMatch(node, other); }


}
