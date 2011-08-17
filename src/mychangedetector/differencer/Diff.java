package mychangedetector.differencer;

public interface Diff {

	public boolean isUpdate();

	public boolean isInsert();

	public boolean isMove();

	public boolean isDelete();

	public String getChangeType();

	public DiffEntity getChangedEntity();

	public DiffEntity getNewEntity();

	public DiffEntity getParentEntity();

}