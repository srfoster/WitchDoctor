package mychangedetector.ast_helpers;

import org.eclipse.jdt.core.dom.ASTNode;

public class SimpleJoinPoint extends JoinPoint {
	
	JoinAction action;
	
	public SimpleJoinPoint(JoinAction action)
	{
		this.action = action;
	}

	@Override
	public void doAdd() {
		action.doAction(this);
	}

	@Override
	public void doAdd(int position) {
		action.doAction(this);
	}

	@Override
	public void doReplace(ASTNode replacement) {
		action.doAction(this);
	}

}
