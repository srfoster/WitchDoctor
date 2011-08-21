package mychangedetector.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

public class EclipseUtil {
	public static String fileToString(IFile file)
	{
		try{
			return convertStreamToString(file.getContents());
		} catch (CoreException ce){
			ce.printStackTrace();
			return "";
		}
	}
	
	public static String convertStreamToString(InputStream is) {
		try{
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
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return "";
		} 
}
}
