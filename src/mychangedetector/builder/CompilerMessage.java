package mychangedetector.builder;

import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;

public class CompilerMessage {
	public static CompilerMessageType UNHANDLED_EXCEPTION = new CompilerMessageType(){
		public boolean instanceOf(CompilerMessage m)
		{
			if(m == null)
				return false;
			boolean ret = m.getText().startsWith("Unhandled exception");
			
			
			return ret;
		}
	};
	
	Annotation problem;
	int offset;
	int length;
	String message;
	
	public CompilerMessage()
	{
		
	}
	
	public CompilerMessage(Annotation annotation, int offset, int length)
	{
		this.problem = annotation;
		this.offset = offset;
		this.length = length;
		this.message = annotation.getText();
	}
	
	public int getOffset()
	{
		return offset;
	}
	
	public int getLength()
	{
		return length;
	}
	
	public String getText()
	{
		return problem.getText();
	}
	
	public String toString()
	{
		return getText();
	}
	
}
