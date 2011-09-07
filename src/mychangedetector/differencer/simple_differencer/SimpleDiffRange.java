package mychangedetector.differencer.simple_differencer;

import java.util.List;

import difflib.Chunk;
import mychangedetector.differencer.DiffRange;

public class SimpleDiffRange implements DiffRange{
	Integer offset = null;
	Integer length = null;
	
	public SimpleDiffRange(int offset, int length) {
		this.offset = offset;
		this.length = length;
	}

	@Override
	public int getOffset() {
		return offset;
	}


	@Override
	public int getLength() {
		return length;
	}

}
