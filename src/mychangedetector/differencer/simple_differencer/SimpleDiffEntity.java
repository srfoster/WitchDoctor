package mychangedetector.differencer.simple_differencer;

import java.util.List;

import mychangedetector.differencer.DiffEntity;
import mychangedetector.differencer.DiffRange;
import difflib.Chunk;

public class SimpleDiffEntity implements DiffEntity {
	
	Chunk chunk;
	List<String> original;
	String delimiter;
	
	public SimpleDiffEntity(Chunk chunk, List<String> original, String delimiter) {
		this.chunk = chunk;
		this.original = original;
		this.delimiter = delimiter;
	}

	@Override
	public DiffRange getSourceRange() {
		return new SimpleDiffRange(chunk,original, delimiter);
	}

	@Override
	public String getLabel() {
		return "SimpleDiffEntity";
	}

}
