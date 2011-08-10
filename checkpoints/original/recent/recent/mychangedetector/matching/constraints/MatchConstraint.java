package mychangedetector.matching.constraints;

import mychangedetector.matching.MyASTMatcher;
import mychangedetector.specifications.Specification;

import org.eclipse.jdt.core.dom.ASTNode;

public interface MatchConstraint {

	boolean isViolatedBy(String s, ASTNode node, MyASTMatcher matcher, Specification parent_spec);

}
