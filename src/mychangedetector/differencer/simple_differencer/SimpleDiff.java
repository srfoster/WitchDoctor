package mychangedetector.differencer.simple_differencer;

import java.util.ArrayList;
import java.util.List;

import mychangedetector.differencer.Diff;
import mychangedetector.differencer.DiffEntity;

import org.eclipse.core.resources.IMarker;

import difflib.Delta;

public class SimpleDiff implements Diff {
	
	Delta delta;
	List<String> original;
	List<String> revised;
	String delimiter;
	List<IMarker> problems = new ArrayList<IMarker>();
	SimpleDifferencer parent;
	
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
		SimpleDiffEntity entity = new SimpleDiffEntity(delta.getOriginal(),original, delimiter);
		entity.setParent(parent);
		
		return entity;
	}

	@Override
	public DiffEntity getNewEntity() {
		SimpleDiffEntity entity = new SimpleDiffEntity(delta.getRevised(),revised, delimiter);
		entity.setParent(parent);
		
		return entity;
	}

	public void setParent(SimpleDifferencer parent) {
		this.parent = parent;
	}


}
