package mychangedetector.differencer.simple_differencer;

import java.util.ArrayList;
import java.util.List;

import mychangedetector.differencer.DiffEntity;
import mychangedetector.differencer.DiffRange;
import mychangedetector.differencer.Differencer;

import org.eclipse.core.resources.IMarker;

import difflib.Chunk;

public class SimpleTextDiffEntity implements DiffEntity {
	
	Chunk chunk;
	List<String> original;
	String delimiter;
	List<IMarker> problems = new ArrayList<IMarker>();
	SimpleDifferencer parent;
	
	public SimpleTextDiffEntity(Chunk chunk, List<String> original, String delimiter) {
		this.chunk = chunk;
		this.original = original;
		this.delimiter = delimiter;
	}
	
	@Override
	public String toString(){
		return chunk.getLines().get(0).toString();
	}

	@Override
	public DiffRange getSourceRange() {
		return new SimpleDiffRange(getOffset(),getLength());
	}

	@Override
	public String getLabel() {
		return "SimpleDiffEntity";
	}

	@Override
	public void setParent(Differencer parent) {
		this.parent = (SimpleDifferencer) parent;
	}
	
	private int getOffset() {
		int offset = 0;
		
		//(Could make this more efficient by not starting from zero for each insertion.)
		for(int i = 0; i < chunk.getPosition(); i++)
		{
			String original_line = original.get(i);
			offset += original_line.length() + delimiter.replaceAll("\\\\","").length(); //The +1 is for the length of the whitespace delimiter used to split the string in the first place.
		}
		
		
		return offset;
	}


	private int getLength() {
		int length = 0;
		
		for(int i = 0; i < chunk.getLines().size(); i++)
		{
			String line = (String) chunk.getLines().get(i);
			
			length += line.length() + 1;
		}
		
		return length;
	}
	
}
