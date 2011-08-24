package mychangedetector.builder;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mychangedetector.change_management.ChangeStream;
import mychangedetector.differencer.Diff;
import mychangedetector.differencer.Differencer;
import mychangedetector.differencer.simple_differencer.SimpleDifferencer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;


/**
 * 
 * @author stephenfoster
 *
 *
 */

public class SampleBuilder {
	
	
	SuperResource right, left;
	
	ChangeStream stream = new ChangeStream();
	
	public static IProject project;
	
	public static boolean no_build = false;
		
	public static SampleBuilder builder = null;
	
	
	private Map<String, SuperResource> checkpoints = new HashMap<String,SuperResource>();
	private Map<String, SuperResource> originals = new HashMap<String,SuperResource>();

	
	
	public SampleBuilder(){
		if(builder == null)
			builder = this;
		
	}

	public static void pause(){
		no_build = true;
	}
	
	public static void unpause(){
		
		no_build = false;

	}
	
	public boolean isPaused()
	{
		return no_build;
	}
	


	public void resetCheckpoints(String text){
		originals = new HashMap<String,SuperResource>();
		checkpoints = new HashMap<String, SuperResource>();
		
		originals.put(right.getName(),new SuperResource(text,right.getName()));
		
	   	stream.clear();
	   	unpause();
	}
	


	public static final String BUILDER_ID = "MyChangeDetector.sampleBuilder";




	public void checkChanges(SuperResource super_resource) {
		
		
		if(no_build)
			return;
		
        right = super_resource;
        String name = right.getName();
        left = checkpoints.get(name);

        if(left == null)
        {
        	left = originals.get(right.getName());
        	if(left != null)
        		checkpoints.put(right.getName(),left);
        }
        
  
        if(left == null)
        {
        	SuperResource new_resource =  new SuperResource(right.getContents(),right.getName());
        	
        	checkpoints.put(right.getName(), new_resource);
        	left = new_resource;
        }
        

		Differencer diff = new SimpleDifferencer();
		List<Diff> list = diff.perform(left,right);

        if(list != null)
        {
        	String left_contents = left.getContents();
        	String right_contents = right.getContents();
		
			
	        stream.addSet(list, new FileVersion(left.getName(), left_contents), new FileVersion(right.getName(), right_contents));
	        stream.print();
			
        } else {
        	System.out.println("List was null.");
        }
        
	
	}
	
	
	public void setUp(SuperResource superResource) {
		originals.put(superResource.getName(),superResource);
	}

	public SuperResource getOriginal(String string) {
		return originals.get(string);
	}

	
	
	
	
	
	
	
	//These are mostly for reference now....  Should probably just email them to myself.
	
	private void addMarker(IFile file, String message, int start, int end, String type) {
		try {
			
			String color = "";
			
			if(type == "REMOVING_CLASS_DERIVABILITY")
				color = "green";
			
			if(type == "ADDITIONAL_OBJECT_STATE")
				color = "blue";
			
			if(type == "ADDITIONAL_FUNCTIONALITY")
				color = "red";
			
			if(type == "METHOD_RENAMING")
				color = "purple";
			
			if(type == "ATTRIBUTE_RENAMING")
				color = "yellow";
			
			if(type == "STATEMENT_INSERT")
				color = "green";
			
			if(type == "STATEMENT_UPDATE")
				color = "green";
			
			//System.out.println("Adding " + color + " marker");
			
			InputStream contents = file.getContents();
			String contents_string = "";
			try {
				contents_string = convertStreamToString(contents);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String string_being_marked = contents_string.substring(start,end);
			
			
			IMarker marker = file.createMarker("MyChangeDetector.highlight_"+color);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
	        marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);

			marker.setAttribute(IMarker.CHAR_START, start);
			marker.setAttribute(IMarker.CHAR_END, end);

		} catch (CoreException e) {
		}
	}

	private void deleteMarkers(IFile file)
	{
		try {
			file.deleteMarkers("MyChangeDetector.highlight_green", false, IResource.DEPTH_ZERO);
			file.deleteMarkers("MyChangeDetector.highlight_red", false, IResource.DEPTH_ZERO);
			file.deleteMarkers("MyChangeDetector.highlight_blue", false, IResource.DEPTH_ZERO);
			file.deleteMarkers("MyChangeDetector.highlight_purple", false, IResource.DEPTH_ZERO);
			file.deleteMarkers("MyChangeDetector.highlight_yellow", false, IResource.DEPTH_ZERO);

		} catch (CoreException ce) {
		}
	}
	
	public String convertStreamToString(InputStream is) throws IOException {
			/*
			* To convert the InputStream to String we use the
			* Reader.read(char[] buffer) method. We iterate until the
			* Reader return -1 which means there's no more data to
			* read. We use the StringWriter class to produce the string.
			*/
			if (is != null) {
				Writer writer = new StringWriter();
				 
				char[] buffer = new char[1024];
				try {
					Reader reader = new BufferedReader(
					new InputStreamReader(is, "UTF-8"));
					int n;
					while ((n = reader.read(buffer)) != -1) {
						writer.write(buffer, 0, n);
					}
				} finally {
					is.close();
				}
				return writer.toString();
			} else {        
				return "";
			}
	}



	
}
