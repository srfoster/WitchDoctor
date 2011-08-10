package mychangedetector.ast_helpers;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

public class ListJoinPoint extends JoinPoint {
	private List<ASTNode> list;

	public ListJoinPoint(List<ASTNode> list, ASTNode child) {
		this.list = list;
		this.node = child;
	}

	@Override
	public void doAdd() {
		list.add(node);
	}

	@Override
	public void doAdd(int position) {
		list.add(position,node);
	}

	@Override
	public void doReplace(ASTNode replacement) {
		int location_to_add = list.indexOf(node);
		list.set(location_to_add,replacement);
	}

}
