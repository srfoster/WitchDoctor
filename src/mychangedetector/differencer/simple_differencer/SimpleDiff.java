package mychangedetector.differencer.simple_differencer;

import java.util.List;

import difflib.Delta;
import mychangedetector.differencer.Diff;
import mychangedetector.differencer.DiffEntity;

public class SimpleDiff implements Diff {
	
	Delta delta;
	List<String> original;
	List<String> revised;
	String delimiter;

	public SimpleDiff(Delta delta, List<String> original, List<String> revised, String delimiter) {
		this.delta = delta;
		this.original = original;
		this.revised = revised;
		this.delimiter = delimiter;
	}

	@Override
	public boolean isUpdate() {
		return delta.getType() == Delta.TYPE.CHANGE;
	}

	@Override
	public boolean isInsert() {
		return delta.getType() == Delta.TYPE.INSERT;
	}

	@Override
	public boolean isMove() {
		return false; //There's no "move" type...
	}

	@Override
	public boolean isDelete() {
		return delta.getType() == Delta.TYPE.DELETE;

	}

	@Override
	public String getChangeType() {
		if(delta.getType() == Delta.TYPE.DELETE)
		{
			return "DELETE";
		}
		
		if(delta.getType() == Delta.TYPE.INSERT)
		{
			return "INSERT";
		}
		
		if(delta.getType() == Delta.TYPE.CHANGE)
		{
			return "UPDATE";
		}
		
		return "";
	}

	@Override
	public DiffEntity getChangedEntity() {
		return new SimpleDiffEntity(delta.getOriginal(),original, delimiter);
	}

	@Override
	public DiffEntity getNewEntity() {
		return new SimpleDiffEntity(delta.getRevised(),revised, delimiter);
	}


}
