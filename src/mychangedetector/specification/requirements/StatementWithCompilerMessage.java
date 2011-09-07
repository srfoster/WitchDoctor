package mychangedetector.specification.requirements;

import java.util.ArrayList;
import java.util.List;

import mychangedetector.builder.CompilerMessageType;
import mychangedetector.specifications.Constraint;

public class StatementWithCompilerMessage extends Statement{
	private CompilerMessageType message_type;
	
	public StatementWithCompilerMessage(String operation, String free_var_name, CompilerMessageType message_type) {
		super(operation, free_var_name);
		
		this.message_type = message_type;
	}

	@Override
	protected List<Constraint> localConstraints(){
		List<Constraint> ret = new ArrayList<Constraint>();
		
		ret.add(new CompilerMessageConstraint(message_type));
		
		return ret;
	}
}
