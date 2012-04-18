package mychangedetector.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.swt.custom.StyledText;

public class MethodQuery extends JumpQuery {
	
	public MethodQuery(TextAdapter text) {
		super(text);
	}

	@Override
	public List<ASTNode> resolveToASTNodes() {
		String text = getAdapter().getText().getText();
		
		MethodDeclaration[] methods = getMethodsFor(text);
		
		return new ArrayList<ASTNode>(Arrays.asList(methods));
	}


	public static MethodDeclaration[] getMethodsFor(String s)
	{
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(s.toCharArray());
		
		ASTNode tree = parser.createAST(null);
		
		return getMethodsFor(tree);
	}
	
	public static MethodDeclaration[] getMethodsFor(ASTNode tree)
	{
		TypeDeclaration type = (TypeDeclaration) ((CompilationUnit)tree).types().get(0);
		
		MethodDeclaration[] methods = type.getMethods();
		return methods;
	}
}
