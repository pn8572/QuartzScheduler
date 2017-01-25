package com.mps.reports.listener;

import static org.quartz.JobBuilder.newJob;

import java.io.IOException;
import java.text.ParseException;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;

import org.apache.log4j.Logger;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.ee.servlet.QuartzInitializerListener;
import org.quartz.impl.StdSchedulerFactory;

import com.mps.reports.listener.job.ReportsDownloadJob;

/**
 * 
 * @author prasanth.pillai
 * @version 1.0
 */

@WebListener
public class QuartzListener extends QuartzInitializerListener  
{
	final static Logger logger = Logger.getLogger(QuartzListener.class);
	Scheduler scheduler = null;
	Properties properties = new Properties();
    @Override
    public void contextInitialized(ServletContextEvent servletContext) 
    {
    	logger.info("**************** MPS Report Download Schedular will start at the schduled time *********************\n");
    	try 
        {
    		//properties.load(getClass().getClassLoader().getResourceAsStream("../../../../properties/config.properties"));
    		logger.info("** S T A R T **");
    		properties.load(getClass().getClassLoader().getResourceAsStream("/properties/config.properties"));
    		String frequency = properties.getProperty("frequency");
    		logger.info("The scheduled frequncy for running the report download activity is "+frequency);
    		// Setup the Job class and the Job group
            JobDetail job = newJob(ReportsDownloadJob.class).withIdentity("MPSReportsCronQuartzJob", "Group").build();
            
            // Create a Trigger for scheduled frequency as per the config.properties file.
            Trigger trigger = TriggerBuilder.newTrigger().withIdentity("TriggerName", "Group").withSchedule(CronScheduleBuilder.cronSchedule(frequency)).build();
            
            /*
             * cronSchedule("0 0 9 1/1 * ? *")) - is set for running the scheduler everyday at 9:00 am
             * @see http://www.cronmaker.com/ to see the cron formatting for different alternative
             * 
             */
            
            // Setup the Job and Trigger with Scheduler & schedule jobs
            scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.start();
            scheduler.scheduleJob(job, trigger);
            logger.info("** E N D **");

        } 
        catch (SchedulerException | ParseException | IOException  e) 
        {
            logger.error("There was an error scheduling the job.", e);
        }
    }
}
