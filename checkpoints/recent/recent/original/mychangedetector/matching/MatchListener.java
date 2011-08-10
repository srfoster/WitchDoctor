package mychangedetector.matching;

import org.eclipse.jdt.core.dom.ASTNode;

public interface MatchListener {

	boolean propertySet(String s, ASTNode node);
	
}
