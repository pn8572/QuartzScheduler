package com.mps.reports.listener.job;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * 
 * @author prasanth.pillai
 * @version 1.0
 * 
 * ReportsDownloadJob class will do the download of all the reports from the 
 * report location to the local disk where this application is deployed.
 * The process is a recursive call to the report category folders and coping
 * all the sub folders and reports in the location
 * 
 *    @see config.properties file - 
 */
public class ReportsDownloadJob implements Job 
{
	final static Logger logger = Logger.getLogger(ReportsDownloadJob.class);
	private static int count;

	@Override
	public void execute(JobExecutionContext jobContext) throws JobExecutionException {

		Properties properties = new Properties();
		File srcFolder = null;
		File destFolder = null;

		try 
		{
			String os = System.getProperty("os.name");
			properties.load(getClass().getClassLoader().getResourceAsStream("/properties/config.properties"));
			//properties.load(getClass().getClassLoader().getResourceAsStream("../../../../properties/config.properties"));
			if (!os.contains("Windows"))
			{
				logger.info("This application is running in Linux server");
				srcFolder = new File(properties.getProperty("server_report_location"));
				destFolder = new File(properties.getProperty("local_report_location"));
			} else 
			{
				logger.info("This application is running in Windows server");
				srcFolder = new File(properties.getProperty("server_report_location_win"));
				destFolder = new File(properties.getProperty("local_report_location_win"));
			}
		} 
		catch (IOException e1) 
		{
			logger.warn("Directory does not exist. or could not load the property file >>>"+e1.getMessage());
			srcFolder = new File(properties.getProperty("/mnt/mi/mps/reports"));
			destFolder = new File(properties.getProperty("/var/www/html/downloads"));
			logger.info(" The server reports location >>"+srcFolder.getName());
			logger.info(" The local reports folder >>"+destFolder.getName());
		}
		
    	if(logger.isDebugEnabled()) {
    	    logger.debug("This is debug mode");
    	}

    	//make sure source exists
    	if (!srcFolder.exists())	{
           logger.error("Directory does not exist.");
           //just exit
          // System.exit(0);
           return;

        } else {
	           try	
	           {
	        	   logger.debug("Coping reports process is about to start");
	        	   logger.info("--------------------------------------------------------------------");
	        	   logger.info("MPS Report Download Schedular start: " + jobContext.getFireTime());
    	           JobDetail jobDetail = jobContext.getJobDetail();
    	           
    	           logger.info("The MPS Reports Source location is "+srcFolder.getAbsolutePath());
    	       	   logger.info("The MPS Reports Destination location is "+destFolder.getAbsolutePath());
    	       	
    	           //copyFolder(srcFolder,destFolder);
    	           logger.info("MPS Report Download Schedular end: " + jobContext.getJobRunTime() + ", key: " + jobDetail.getKey());
    	           logger.info("MPS Report Download Schedular will start at next scheduled time: " + jobContext.getNextFireTime());
    	           logger.info("--------------------------------------------------------------------");
	           }	
	           catch (Throwable e)
	           {
	        	   e.printStackTrace();
	               logger.info("-----------------E R R O R----------------------------------------");
	        	   if(logger.isDebugEnabled())
	        	   {
	        	   		e.printStackTrace();
	        	   }
        	   		logger.error("There was an error scheduling the job.", e);
        	   		//error, just exit
        	   		//System.exit(0);
        	   		
        	   		throw new JobExecutionException();
	           }
        }
    	
    	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    	Date date = new Date();
    	logger.info("Download operation Completed on "+dateFormat.format(date)); //2016/11/16 12:08:43
	}
	
    public void copyFolder(File src, File dest) throws IOException	{

    	logger.info("The MPS Reports Source location is "+src.getAbsolutePath());
    	logger.info("The MPS Reports Destination location is "+dest.getAbsolutePath());
    	
    	src.setReadable(true);
    	dest.setReadable(true);
    	
    	src.setWritable(true);
    	dest.setWritable(true);
    	
    	if (src.isDirectory())	{
    		logger.info("The Directory is "+src.getName());

    		//if directory not exists, create it
    		if (!dest.exists())	{
    			dest.setWritable(true);
    		   dest.mkdir();
    		   logger.info("Destination directory created "+ dest.getAbsolutePath());
    		}

    		//list all the directory contents
    		String files[] = src.list();

    		for (String file : files) {
    		   //construct the src and dest file structure
    		   File srcFile = new File(src, file);
    		   File destFile = new File(dest, file);
    		   
    		   //recursive copy
    		   copyFolder(srcFile,destFile);
    		   System.out.println("Count "+count);
    		   count++;
    		}

    	} else	{
    		count++;
    		//if file, then copy it
    		//Use bytes stream to support all file types
    		InputStream in = new FileInputStream(src);
	        OutputStream out = new FileOutputStream(dest);
	        logger.info("The File is "+src.getName());
	        byte[] buffer = new byte[1024];

	        int length;
	        //copy the file content in bytes
	        while ((length = in.read(buffer)) > 0)	{
	    	   out.write(buffer, 0, length);
	        }

	        in.close();
	        out.close();
	        logger.info("File copied from " + src + " to " + dest);
    	}
    }
}
