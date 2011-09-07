package mychangedetector.differencer.simple_differencer;

import mychangedetector.builder.CompilerMessage;
import mychangedetector.differencer.DiffEntity;
import mychangedetector.differencer.DiffRange;
import mychangedetector.differencer.Differencer;
import difflib.Chunk;

public class SimpleMessageDiffEntity implements DiffEntity {
	Chunk chunk;
	Differencer parent;
	
	public SimpleMessageDiffEntity(Chunk chunk) {
		this.chunk = chunk;
	}
	
	@Override
	public DiffRange getSourceRange() {
		return new SimpleDiffRange(getOffset(),getLength());
	}

	@Override
	public String getLabel() {
		if(getMessage() != null)
		{
			return getMessage().getText();
		}
		
		return "";
	}

	@Override
	public void setParent(Differencer parent) {
		this.parent = parent;
	}

	public CompilerMessage getMessage()
	{
		if(chunk.getLines().size() < 1)
			return null;
		
		CompilerMessage m = (CompilerMessage) chunk.getLines().get(0);
		
		return m;
	}
	
	private int getOffset()
	{
		if(getMessage() != null)
			return getMessage().getOffset();
		
		return 0;
	}
	
	private int getLength()
	{
		if(getMessage() != null)
			return getMessage().getLength();
		
		return 0;
	}
}
