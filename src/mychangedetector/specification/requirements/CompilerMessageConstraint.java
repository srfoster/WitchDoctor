package mychangedetector.specification.requirements;

import java.util.Map;

import mychangedetector.builder.CompilerMessage;
import mychangedetector.builder.CompilerMessageType;
import mychangedetector.change_management.ChangeWrapper;
import mychangedetector.specifications.Constraint;
import mychangedetector.specifications.Requirement;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.swt.widgets.Button;

/**
 * 
 *  
 * 
 **/

public class CompilerMessageConstraint extends Constraint {
	private CompilerMessageType message_type;
	
	public CompilerMessageConstraint(CompilerMessageType message_type) {
		this.message_type = message_type;
	}

	@Override
	public boolean isViolatedBy(String key, Map<String, ASTNode> matched_bindings, Requirement requirement, ChangeWrapper change) {			
		ASTNode node = matched_bindings.get(key);
		
		if(node == null)
			return true;
		
		return !message_type.instanceOf((CompilerMessage)node.getProperty("compiler_message"));
	}
}
