package mychangedetector.builder;

import java.util.ArrayList;
import java.util.List;

import mychangedetector.util.EclipseUtil;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

public class SuperResource {
	IFile file;
	List<CompilerMessage> compiler_messages = new ArrayList<CompilerMessage>();
	
	String contents;
	String name;
	
	public SuperResource(IFile file)
	{
		this.file = file;
		try {
			this.contents = EclipseUtil.convertStreamToString(file.getContents());
			this.name = file.getName();
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public SuperResource(String contents, String name) {
		this.contents = contents;
		this.name = name;
	}

	public String getName()
	{
		return name;
	}
	
	@Deprecated
	public IFile getFile()
	{
		return file;
	}
	
	public String getContents()
	{
		return contents;
	}
	
	public void setCompilerMessages(List<CompilerMessage> compiler_messages)
	{
		this.compiler_messages = compiler_messages;
	}
	
	public List<CompilerMessage> getCompilerMessages()
	{
		return this.compiler_messages;
	}

}
