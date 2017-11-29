package com.gpscsp.Initialproc;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbMessageAssembly;

public class GPS_InitialProcessing_APP_JavaCompute extends MbJavaComputeNode {
	/*Getting UDP values*/
	String udpinitProcSubmtCnt	= getUserDefinedAttribute("CH_udpinitProcSubmtCnt").toString();
    String udpinitProcSplit		= getUserDefinedAttribute("CH_udpinitProcSplit").toString();
    String udprepr				= getUserDefinedAttribute("CH_repr").toString();
 
/* Method to call the initProc_SubmtCnt.sh script  */
	public static String initProc_SubmtCnt(String udpinitProcSubmtCnt,String CH_InputFileNM, String transFileId, String timeoutInSeconds) {
		int value = 0;
		try {
		ProcessBuilder pb = new ProcessBuilder(udpinitProcSubmtCnt,CH_InputFileNM,transFileId);
		Process p = pb.start();
        value 	  = p.waitFor();
         
		if ( Integer.parseInt(timeoutInSeconds) < 0 ){
			value 	  = p.waitFor();
		}
		else{
			long now = System.currentTimeMillis();
		    long timeoutInMillis = 1000L * Integer.parseInt(timeoutInSeconds);
		    long finish = now + timeoutInMillis;
		    while ( isAlive( p ) && ( System.currentTimeMillis() < finish ) )
		    {
		        Thread.sleep( 10 );
		    }
		    if ( isAlive( p ) )
		    {
		       throw new InterruptedException( "Process timeout out after " + timeoutInSeconds + " seconds" );
		    }
		    value = p.exitValue();
		}
		}
		catch (Exception e) {
        	e.printStackTrace();
      		value = 2;
		}
		return String.valueOf(value);
	}
	

public static boolean isAlive( Process p ) {
    try
    {
        p.exitValue();
        return false;
    } catch (IllegalThreadStateException e) {
        return true;
    }
}
	/* Method to call the initProc_SubmtCnt.sh script  */
	
	public static String ons(String CH_onsScriptPath, String CH_InputFilePath, String CH_SecurityType) {
		int value = 0;
		try {
		ProcessBuilder pb = new ProcessBuilder(CH_onsScriptPath,CH_InputFilePath,CH_SecurityType);
		Process p = pb.start();
        value 	  = p.waitFor();
         } catch (Exception e) {
        	e.printStackTrace();
      		value = 2;
		}
		return String.valueOf(value);
	}
/* Method to call the initProc_Split.sh script  */
	public static String initProc_Split(String udpinitProcSplit,String CH_InputSplitFileLaction,String CH_SubIDTransPrty, 
										String CH_TransFileId,String PriorityFlag,String timeoutInSeconds) {
		int value2 = 0;
		try {
		ProcessBuilder pb = new ProcessBuilder(udpinitProcSplit,CH_InputSplitFileLaction,CH_SubIDTransPrty,CH_TransFileId,PriorityFlag);
		Process p = pb.start();
        value2 	  = p.waitFor();
        if ( Integer.parseInt(timeoutInSeconds) < 0 ){
        	value2 	  = p.waitFor();
		}
		else{
			long now = System.currentTimeMillis();
		    long timeoutInMillis = 1000L * Integer.parseInt(timeoutInSeconds);
		    long finish = now + timeoutInMillis;
		    while ( isAlive( p ) && ( System.currentTimeMillis() < finish ) )
		    {
		        Thread.sleep( 10 );
		    }
		    if ( isAlive( p ) )
		    {
		       throw new InterruptedException( "Process timeout out after " + timeoutInSeconds + " seconds" );
		    }
		    value2 = p.exitValue();
		}
        } catch (Exception e) {
        	 e.printStackTrace();
      		value2 = 2;
		}
		return String.valueOf(value2);
	}
	
/* Method to call the repr.sh script  */
public static String repr(String udprepr,String CH_InputFilePath,String timeoutInSeconds) {
		int value1 = 0;
		try {
		ProcessBuilder pb = new ProcessBuilder(udprepr,CH_InputFilePath);
		Process p = pb.start();
        value1 	  = p.waitFor();
        if ( Integer.parseInt(timeoutInSeconds) < 0 ){
        	value1 	  = p.waitFor();
		}
		else{
			long now = System.currentTimeMillis();
		    long timeoutInMillis = 1000L * Integer.parseInt(timeoutInSeconds);
		    long finish = now + timeoutInMillis;
		    while ( isAlive( p ) && ( System.currentTimeMillis() < finish ) )
		    {
		        Thread.sleep( 10 );
		    }
		    if ( isAlive( p ) )
		    {
		       throw new InterruptedException( "Process timeout out after " + timeoutInSeconds + " seconds" );
		    }
		    value1 = p.exitValue();
		}
        } catch (Exception e) {
        	 e.printStackTrace();
      		value1 = 2;
		}
		return String.valueOf(value1);
	}	
/*Methood to get the modified Timestamp of a file*/	
	public static String initProc_Timestamp(String CH_InputFileNM, String CH_InputFilePath ) {
		/* TODO Auto-generated method stub */
		File file;
		String timestamp=null;
		try {
			 file = new File(CH_InputFilePath + "\\"+ CH_InputFileNM);
		     SimpleDateFormat dateFormat = new SimpleDateFormat("YYYYMMddHHmmssSSSmssSSS");
		     timestamp=dateFormat.format(file.lastModified());
				     }
		catch (Exception e) {
		//	String exception = e.getMessage();
			e.printStackTrace();
		}
		return timestamp;
	}
public void evaluate(MbMessageAssembly inAssembly) throws MbException {
	    	
}

public static String getB2bfileid(String scriptName,String fileName,String timeoutInSeconds) {
	int value = 0;
	String s="";
	try {
	ProcessBuilder pb = new ProcessBuilder("/bin/bash",scriptName,fileName);
	Process p = pb.start();
    value 	  = p.waitFor();
    s=output(p.getInputStream());
    if ( Integer.parseInt(timeoutInSeconds) < 0 ){
    	value 	  = p.waitFor();
	}
	else{
		long now = System.currentTimeMillis();
	    long timeoutInMillis = 1000L * Integer.parseInt(timeoutInSeconds);
	    long finish = now + timeoutInMillis;
	    while ( isAlive( p ) && ( System.currentTimeMillis() < finish ) )
	    {
	        Thread.sleep( 10 );
	    }
	    if ( isAlive( p ) )
	    {
	       throw new InterruptedException( "Process timeout out after " + timeoutInSeconds + " seconds" );
	    }
	    value = p.exitValue();
	} } catch (Exception e) {
    	e.printStackTrace();
  		value = 2;
	}
	return s;
}


private static String output(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try {
				br = new BufferedReader(new InputStreamReader(inputStream));
				String line = null;
				while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } 	finally {
            br.close();
			}
        return sb.toString();
}


}

