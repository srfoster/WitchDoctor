package mychangedetector.differencer.simple_differencer;

import java.util.ArrayList;
import java.util.List;

import mychangedetector.differencer.DiffEntity;
import mychangedetector.differencer.DiffRange;
import mychangedetector.editors.RefactoringEditor;

import org.eclipse.core.resources.IMarker;

import difflib.Chunk;

public class SimpleDiffEntity implements DiffEntity {
	
	Chunk chunk;
	List<String> original;
	String delimiter;
	List<IMarker> problems = new ArrayList<IMarker>();
	SimpleDifferencer parent;
	
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

	public void setParent(SimpleDifferencer parent) {
		this.parent = parent;
	}
	
}
