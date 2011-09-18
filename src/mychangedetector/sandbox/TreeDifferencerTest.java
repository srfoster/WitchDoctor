package mychangedetector.sandbox;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.evolizer.changedistiller.treedifferencing.ITreeEditOperation;
import org.evolizer.changedistiller.treedifferencing.Node;

public class TreeDifferencerTest {
	public static void main(String[] args)
	{
		//Get an initial ASTNode
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(first_program.toCharArray());
		parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
		ASTNode first_tree = parser.createAST(null);
		
		

		System.out.println(first_tree);
	}
	
	
	
	
	
	
	static String first_program =
			"void blah(){if(true) return; Expression e = new Expression();}";

}
