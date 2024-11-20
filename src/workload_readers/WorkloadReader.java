/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package workload_readers;

import fairshare_simulator.Job;
import fairshare_simulator.JobExecutionSimulator;
import java.util.ArrayList;

/**
 * This class is an interface to various workload reader implementations.
 * @author Dalibor Klusacek
 */
public interface WorkloadReader {
    
    
    /**
     * Skeleton of method to read the arriving jobs in given interval.
     * @param start start time of the simulation tick.
     * @param end end time of the simulation tick.
     * @param pending_job a job that has been already obtained in previous run but did not belonged there (either a Job instance or null).
     * @param simulator instance of the JobExecutionSimulator class needed to call its inner methods.
     * @return a list of newly arrived jobs
     */
    public ArrayList<Job> readArrivedJobs(long start, long end, Job pending_job, JobExecutionSimulator simulator);
    
    /**
     * Skeleton of a method that parses one job.
     * @param line line to be parsed.
     * @param duplicate boolean indictor whether this job should be duplicated (to generate secondary workload for specific simulations of different user shares).
     * @return an instance of Job.
     */
    public Job parseLine(String line, boolean duplicate);
    
    /**
     * Skeleton of a method to close a file.
     */
    public void closeFile();
    
}
