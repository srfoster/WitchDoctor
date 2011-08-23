package mychangedetector.builder;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

public class CompilerMessage {
	IMarker problem;
	
	public CompilerMessage(IMarker problem)
	{
		this.problem = problem;
	}
	
	public int getOffset()
	{
		try {
			return (Integer) problem.getAttribute(IMarker.CHAR_START);
		} catch (CoreException e) {
			return -1;
		}
	}
	
	public int getLength()
	{
		try {
			return (Integer) problem.getAttribute(IMarker.CHAR_END) - getOffset();
		} catch (CoreException e) {
			return -1;
		}
	}
}
