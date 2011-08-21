package mychangedetector.differencer.simple_differencer;

import java.util.List;

import difflib.Chunk;
import mychangedetector.differencer.DiffRange;

public class SimpleDiffRange implements DiffRange{
	Chunk chunk;
	List<String> original;
	String delimiter;
	Integer offset = null;
	Integer length = null;
	
	public SimpleDiffRange(Chunk chunk, List<String> original, String delimiter) {
		this.chunk = chunk;
		this.original = original;
		this.delimiter = delimiter;
	}

	@Override
	public int getOffset() {
		if(offset != null)
			return offset;
        
		offset = 0;
		
		//(Could make this more efficient by not starting from zero for each insertion.)
		for(int i = 0; i < chunk.getPosition(); i++)
		{
			String original_line = original.get(i);
			offset += original_line.length() + delimiter.replaceAll("\\\\","").length(); //The +1 is for the length of the whitespace delimiter used to split the string in the first place.
		}
		
		
		return offset;
	}


	@Override
	public int getLength() {
		if(length != null)
			return length;
		
		length = 0;
		
		for(int i = 0; i < chunk.getLines().size(); i++)
		{
			String line = (String) chunk.getLines().get(i);
			
			length += line.length() + 1;
		}
		
		return length;
	}

}
