package mychangedetector.builder;

public class FileVersion {
	String name;
	String contents;
	public FileVersion(String name, String contents)
	{
		this.name = name;
		this.contents = contents;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getContents()
	{
		return contents;
	}
}
