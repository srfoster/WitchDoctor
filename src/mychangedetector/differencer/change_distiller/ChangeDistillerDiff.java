package mychangedetector.differencer.change_distiller;

import mychangedetector.differencer.Diff;
import mychangedetector.differencer.DiffEntity;

import org.evolizer.changedistiller.model.entities.Delete;
import org.evolizer.changedistiller.model.entities.Insert;
import org.evolizer.changedistiller.model.entities.Move;
import org.evolizer.changedistiller.model.entities.SourceCodeChange;
import org.evolizer.changedistiller.model.entities.SourceCodeEntity;
import org.evolizer.changedistiller.model.entities.Update;

public class ChangeDistillerDiff implements Diff {
	
	SourceCodeChange change;
	
	public ChangeDistillerDiff(SourceCodeChange change)
	{
		this.change = change;
	}

	@Override
	public boolean isUpdate()
	{
		return (change instanceof Update);
	}
	@Override
	public boolean isInsert()
	{
		return (change instanceof Insert);
	}
	@Override
	public boolean isMove()
	{
		return (change instanceof Move);
	}
	@Override
	public boolean isDelete()
	{
		return (change instanceof Delete);
	}
	
	@Override
	public String getChangeType(){
		if(isUpdate())
			return "UPDATE";
		
		if(isInsert())
			return "INSERT";
		
		if(isDelete())
			return "DELETE";
		
		if(isMove())
			return "MOVE";
		
		return null;
	}
	
	@Override
	public DiffEntity getChangedEntity()
	{
		return new ChangeDistillerDiffEntity(change.getChangedEntity());
	}
	
	@Override
	public DiffEntity getNewEntity()
	{
		return new ChangeDistillerDiffEntity(((Update)change).getNewEntity());
	}

}
