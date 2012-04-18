package mychangedetector.test.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mychangedetector.editors.RefactoringEditor;
import mychangedetector.util.EclipseUtil;

import org.eclipse.core.internal.resources.File;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractMethodRefactoring;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ui.part.FileEditorInput;


public class GetExtractMethodTargetTexts {
	Connection conn;
	
	java.sql.Statement stat = null;
	
	int data_set_id = 3;
	
	int number_of_batches = 34647;
	int batch_size = 1;
	
	//String prefix = "/Users/stephenfoster/Desktop/Research/javaProjects/org.eclipse.compare/plugins/org.eclipse.compare/";
	//String prefix = "/Users/stephenfoster/Desktop/Research/javaProjects/org.eclipse.jface/";
	String prefix = "/Users/stephenfoster/Desktop/Research/javaProjects/current/struts2/";

	//String project_name = "org.eclipse.compare";
	//String project_name = "org.eclipse.jface";
	String project_name = "struts";
	
	public void execute()
	{

		
	    try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		try{
		    conn = DriverManager.getConnection("jdbc:sqlite:/Users/stephenfoster/Documents/workspace/MyChangeDetectorNew/data/data.db");
		    stat = conn.createStatement();

			for(int batch_number = 0; batch_number < number_of_batches; batch_number++)
			{
				System.out.print("Batch #: " + batch_number + " ");

				List<PendingUpdate> updates = new ArrayList<PendingUpdate>();

				ResultSet results = stat.executeQuery("select ranges.id as range_id, * from ranges left join files on ranges.file_id = files.id where data_set_id = "+data_set_id+" limit " + batch_size +" offset " + (batch_size * batch_number));
	
				
				while(results.next())
				{
	
					String full_name = results.getString("name");
					String short_name = full_name.replace(prefix,"");
					int offset = results.getInt("offset");
					int length = results.getInt("length");
					String full_text = results.getString("contents");
					
					int file_id = results.getInt("file_id");
					int range_id = results.getInt("range_id");
					
					String to_extract = full_text.substring(offset,length + offset);
				
						
	
					System.out.println(short_name); 
					
					String target_text = getTargetText(short_name, full_text, offset, length);
	
					PendingUpdate p = new PendingUpdate();
					p.contents = target_text;
					p.range_id = range_id;
					updates.add(p);
				//	System.out.println(target_text);
				}
				int insert_count = 0;
				for(PendingUpdate p : updates)
				{
					insert_count++;
					try {
						System.out.println("Inserted " + insert_count);
						String query = "insert into target_texts (range_id,contents) values ("+p.range_id+", ?)";
						
						PreparedStatement prep = conn.prepareStatement(query);
						prep.setString(1, p.contents);
						prep.addBatch();
						prep.executeBatch();
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			
			}
		} catch(SQLException s) {
			s.printStackTrace();
		}
		

	}

	private String getTargetText(String name, String full_text, int offset, int length) {
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		IProject project = ws.getRoot().getProject(project_name);
		
		IPath location = new Path(name);
		IFile file = project.getFile(location);
		
		String other_full_text = null;
		try {
			other_full_text = EclipseUtil.convertStreamToString(file.getContents());
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		ICompilationUnit element = JavaCore.createCompilationUnitFrom(file);
		
		
    	ExtractMethodRefactoring refactoring = new ExtractMethodRefactoring(element, offset, length);
    	
    	refactoring.setMethodName("new_method");
    	
    	String target = null;
    	
    	try {
    		RefactoringStatus status = refactoring.checkInitialConditions(new NullProgressMonitor());
    		System.out.println(status.toString());
			final Change change = refactoring.createChange(new NullProgressMonitor());
			

		   try {
		     change.initializeValidationData(new NullProgressMonitor());
		 								 
		    
		     RefactoringStatus valid= change.isValid(new NullProgressMonitor());
		     
		     final Change undo= change.perform(new NullProgressMonitor(){
				 public void done()
				 {
					 
				 }
			 });
		   	 
		     target = EclipseUtil.convertStreamToString(file.getContents());
		     
		     if(target.equals(full_text))
		     {
		    	 throw new Exception("Refactoring did nothing");
		     }
		     
		     undo.initializeValidationData(new NullProgressMonitor());
			 undo.perform(new NullProgressMonitor(){
				 public void done()
				 {

				 }
			 });
		     

			 
		   } catch (Exception e) {
			   e.printStackTrace();
		   } finally {
			   change.dispose();
		   }

		   
		} catch (Exception e) {
			String to_extract = other_full_text.substring(offset, offset+ length);
			System.out.println(other_full_text);
			System.out.println("*****");
			System.out.println(to_extract);
			e.printStackTrace();
		}
		
		
		/*
		try {
			file.setContents(EclipseUtil.convertStringToStream(full_text), true, false, null);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} //Undo
		*/
		
		return target;
		
	}
	
	private class PendingUpdate
	{
		public String contents;
		public int range_id;
	}
}
