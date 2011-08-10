package mychangedetector.ast_helpers;

import org.eclipse.jdt.core.dom.ASTNode;

public abstract class JoinPoint {
	protected ASTNode node;
	
	protected JoinAction before_action;

	public void add(){
		if(before_action != null)
			before_action.doAction(this);
		doAdd();
	}
	public void add(int position){
		if(before_action != null)
			before_action.doAction(this);
		doAdd(position);
	}
	public void replace(ASTNode replacement){
		if(before_action != null)
			before_action.doAction(this);
		doReplace(replacement);
	}
	
	protected abstract void doAdd();
	protected abstract void doAdd(int pos);
	protected abstract void doReplace(ASTNode r);
	
	public void setNode(ASTNode node)
	{
		this.node = node;
	}
	
	public void setBeforeAction(JoinAction action){
		this.before_action = action;
	}
}
