package mychangedetector.test;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.custom.StyledText;

public class ScriptBuilder {
	private StyledText text;
	private List<SimulationOperation> script;
	
	public ScriptBuilder(StyledText text)
	{
		this.text = text;
		this.script = new ArrayList<SimulationOperation>();
	}
	
	private StyledText getText()
	{
		return text;
	}

	protected void typeString(String string) {
		for(int i = 0; i < string.length(); i++)
		{
			CharacterOperation op = new CharacterOperation(getText());
			op.setCharacter(string.charAt(i));
			script.add(op);
		}
	}
	
	protected void jumpToMethod(int method) {
		jumpTo(new MethodQuery(getText()));
	}
	
	protected void jumpToMethodBeginning(int method_number) {
		MethodQuery method = new MethodQuery(getText());
		BlockQuery block = new BlockQuery(method);
		BeginningQuery beginning = new BeginningQuery(block);
		
		jumpTo(beginning);
	}
	
	protected void jumpToMethodEnd(int method_number) {
		MethodQuery method = new MethodQuery(getText());
		BlockQuery block = new BlockQuery(method);
		EndQuery end = new EndQuery(block);
		
		jumpTo(end);
	}
	
	protected void jumpToOffset(int offset)
	{
		OffsetQuery query = new OffsetQuery(getText());
		query.setOffset(offset);
		jumpTo(query);
	}
	
	protected RecordSelectionOperation startSelect()
	{
		RecordSelectionOperation op = new RecordSelectionOperation(getText());
		script.add(op);
		return op;
	}
	
	protected void endSelect(RecordSelectionOperation op)
	{
		script.add(op);
	}
	
	protected void pressUpArrow()
	{
		KeyPressOperation op = new KeyPressOperation(getText());
		op.setCode(KeyPressOperation.ARROW_UP);
		
		script.add(op);
	}
	
	protected void pressDownArrow()
	{
		KeyPressOperation op = new KeyPressOperation(getText());
		op.setCode(KeyPressOperation.ARROW_DOWN);
		
		script.add(op);
	}
	
	
	protected void pressDelete()
	{
		CharacterOperation op = new CharacterOperation(getText());
		op.setCharacter('\b');
		script.add(op);
	}
	
	
	protected void pressTab()
	{
		KeyPressOperation op = new KeyPressOperation(getText());
		op.setCode(KeyPressOperation.TAB);
		
		script.add(op);
	}
	
	
	protected void jumpTo(JumpQuery query)
	{
		MoveCaretOperation op = new MoveCaretOperation(getText());
		op.setQuery(query);
		script.add(op);
	}

	public List<SimulationOperation> toScript() {
		return script;
	}

	public void addOp(SimulationOperation simulationOperation) {
		script.add(simulationOperation);
	}
}
