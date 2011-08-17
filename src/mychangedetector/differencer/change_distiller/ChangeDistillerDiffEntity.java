package mychangedetector.differencer.change_distiller;

import mychangedetector.differencer.DiffEntity;
import mychangedetector.differencer.DiffRange;

import org.evolizer.changedistiller.model.entities.SourceCodeEntity;

public class ChangeDistillerDiffEntity implements DiffEntity {
	
	private SourceCodeEntity entity;
	
	public ChangeDistillerDiffEntity(SourceCodeEntity entity)
	{
		this.entity = entity;
	}
	
	public DiffRange getSourceRange()
	{
		return new ChangeDistillerDiffRange(entity.getSourceRange());
	}
	
	public String getLabel()
	{
		return entity.getLabel();
	}

	  
}
