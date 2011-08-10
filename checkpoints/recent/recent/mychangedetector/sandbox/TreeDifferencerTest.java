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
		parser.setSource(third_program.toCharArray());
		ASTNode first_tree = parser.createAST(null);
		
		//Get a second ASTNode
		ASTParser parser2 = ASTParser.newParser(AST.JLS3);
		parser2.setSource(fourth_program.toCharArray());
		ASTNode second_tree = parser2.createAST(null);
		
		//Make a Node Tree out of the first ASTNode
		
		ASTToNodeVisitor node_builder = new ASTToNodeVisitor();
		first_tree.accept(node_builder);
		Node first_node = node_builder.getNode();
				
		//Make a Node Tree out of the second ASTNode
		
		ASTToNodeVisitor node_builder2 = new ASTToNodeVisitor();
		second_tree.accept(node_builder2);
		Node second_node = node_builder2.getNode();
		
		//Run the tree differencer
		
		MyTreeDifferencer diff = new MyTreeDifferencer();
		diff.calculateEditScript(first_node,second_node);
		
		List<ITreeEditOperation> script = diff.getEditScript();
		
		System.out.println(script);
		
		//Examine the edit script
	}
	
	
	
	
	
	
	static String first_program = "public class Other {" +
			"int a = 1;" +
			"int b = 2;" +
			"int c = 3;" +
			"String fool = \"\";" +
			"" +
			"public String fool(String s)" +
			"{" +
			"	a = b + c;" +
			"	b = a + b;" +
			"	c = b + c;" +
			"	d = a + b + c;" +
			"	return foo;" +
			"}" +
			"public Other(String s)" +
			"{" +
			"	a = 1;" +
			"	fool = s;" +
			"}" +
			"public String bar()" +
			"{" +
			"	a = 5;" +
			"	return a;" +
			"}" +
			"}";
	
	static String second_program = "public class Other {" +
			"int a = 1;" +
			"int b = 2;" +
			"int c = 3;" +
			"String fool = \"\";" +
			"" +
			"public String fool(String s)" +
			"{" +
			"	a = b + c;" +
			"	b = a + b;" +
			"	c = b + c;" +
//			"	d = a + b + c;" +
			"	return foo;" +
			"}" +
			"public Other(String s)" +
			"{" +
			"	a = 1;" +
			"	fool = s;" +
			"}" +
			"public String bar()" +
			"{" +
			"	a = 5;" +
			"	return a;" +
			"}" +
			"}";
	
	static String third_program = "public class Other {" +
			"public Other(String s)" +
			"{" +
			"	a = 1;" +
			"	fool = s;" +
			"}" +
			"}";
	
	static String fourth_program = "public class Other {" +
			"public Other(String s)" +
			"{" +
			"	a = 1;" +
			"	fool = s;" +
			"}" +
			"}";
}
