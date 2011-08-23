package mychangedetector.builder;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;

public class SuperResource {
	IFile file;
	List<CompilerMessage> compiler_messages = new ArrayList<CompilerMessage>();
	
	public SuperResource(IFile file)
	{
		this.file = file;
	}
	
	public IFile getFile()
	{
		return file;
	}
	
	public void setCompilerMessages(List<CompilerMessage> compiler_messages)
	{
		this.compiler_messages = compiler_messages;
	}

}
