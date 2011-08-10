package mychangedetector.specifications;

import java.util.Map;

import mychangedetector.change_management.ChangeWrapper;

import org.eclipse.jdt.core.dom.ASTNode;

public class Constraint implements Cloneable {

	public boolean isViolatedBy(String key, Map<String, ASTNode> matched_bindings, Requirement requirement, ChangeWrapper change) {
		return false;
	}

	public Constraint copy() {
		try {
			return (Constraint) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

}
