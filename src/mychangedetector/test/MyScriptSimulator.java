package mychangedetector.test;

import java.util.Arrays;
import java.util.List;


public class MyScriptSimulator extends DiffSimulator {

	public MyScriptSimulator()
	{
		
	}

	@Override
	public void act()
	{
		
		if(target == null)
		{
			String t = getText().getText();
			
			List<String> split = Arrays.asList(t.split("\n"));
			
			int counter = 0;
			
			String my_target = "";
			for(String s : split)
			{
				if(counter != 21 && counter != 2 && counter != 14)
					my_target += s + "\n";
				
				
				
				if(counter == 14)
				{
					my_target += "\tnewLine();\n";
					my_target += "newLine();\n";

				}
				
				counter++;
			}
			setTargetText(my_target);
		}
		
		super.act();
	}



}
