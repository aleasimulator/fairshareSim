/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package workload_readers;

import fairshare_simulator.Fairshare_Simulator;
import fairshare_simulator.Input;
import fairshare_simulator.Job;
import fairshare_simulator.JobExecutionSimulator;
import fairshare_simulator.SimulationSetup;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class reads data from a given SWF-formatted workload file.
 *
 * @author Dalibor Klusacek
 */
public class WorkloadReaderSWF implements WorkloadReader {

    static Input r = new Input();
    static BufferedReader br;
    public static long start_date = -1;

    /**
     * This constructor opens the file with the SWF formatted workload.
     *
     * @param data a path to the workload (located in the ./data-set directory).
     */
    public WorkloadReaderSWF(String data) {
        String adresar = System.getProperty("user.dir");
        System.out.println("Opening folder: " + adresar + "/data-set/" + data);
        br = r.openFile(new File(adresar + "/data-set/" + data));

    }

    /**
     * This method reads all jobs belonging to this simulation time interval.
     *
     * @param start start time of the simulation tick.
     * @param end end time of the simulation tick.
     * @param pending_job a job that has been already obtained in previous run
     * but did not belonged there (either a Job instance or null).
     * @param simulator instance of the JobExecutionSimulator class needed to
     * call its inner methods.
     * @return a list of newly arrived jobs
     */
    public ArrayList<Job> readArrivedJobs(long start, long end, Job pending_job, JobExecutionSimulator simulator) {
        ArrayList<Job> jobs = new ArrayList();

        // add pending job if it belongs here
        if (simulator.pending_job != null && simulator.pending_job.getArrival() >= start && simulator.pending_job.getArrival() < end) {
            jobs.add(simulator.pending_job);
            //System.out.println("Adding PENDING job to jobs RUNNING... pending job is:" + simulator.pending_job.getId());
            simulator.pending_job = null;
            if (simulator.pending_job_duplicate != null) {
                jobs.add(simulator.pending_job_duplicate);
                simulator.pending_job_duplicate = null;
            }
        }
        // this job will start later
        if (simulator.pending_job != null && simulator.pending_job.getArrival() > end) {
            //System.out.println("PENDING job starts in the future - SKIP the search... pending job is:" + simulator.pending_job.getId());
            return jobs;
        }
        String line = "";
        boolean run = true;
        while (run) {

            try {
                line = br.readLine();

            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (line == null) {
                break;
            } else {
                if (line.length() < 5 || line.contains(";") || line.contains("acct_id_string") || line.contains("---+----") || line.contains(" rows)") || !line.contains(Fairshare_Simulator.requested_string_in_job_line)) {
                    if (start_date < 0 && line.contains("; UnixStartTime: ")) {
                        start_date = Long.parseLong(line.replace("; UnixStartTime: ", ""));
                        Fairshare_Simulator.first_day_epoch = start_date;
                        String dated = new java.text.SimpleDateFormat("HH:mm dd-MM-yyyy").format(new java.util.Date(start_date * 1000));
                        System.out.println("Start time of workload is UnixStartTime: " + start_date+", ["+dated+"]");
                    }
                    //System.out.println("Skipping line: " + line);
                    // skip to next line
                    continue;
                }

                Job job = parseLine(line, false);
                Job job_duplicate = null;
                if (Fairshare_Simulator.duplicate_users_for_shares && job != null) {
                    job_duplicate = parseLine(line, true);
                    job_duplicate.setCpus(job_duplicate.getCpus() * 2);
                }
                if (job == null) {
                    continue;
                }

                if (start == -1 && end == -1) {
                    //System.out.println("Adding first ever job...");
                    jobs.add(job);
                    if (job_duplicate != null) {
                        jobs.add(job_duplicate);
                    }
                    run = false;
                } else if (job.getArrival() >= start && job.getArrival() < end) {
                    //System.out.println("Adding job...");
                    jobs.add(job);
                    if (job_duplicate != null) {
                        jobs.add(job_duplicate);
                    }

                } else {
                    simulator.pending_job = job;
                    simulator.pending_job_duplicate = job_duplicate;

                    System.out.println("New Pending job... " + simulator.pending_job.getId());
                    run = false;
                }
            }
        }
        if (simulator.pending_job != null) {
            //System.out.println("IS New Pending job... " + simulator.pending_job.getId());
        }
        return jobs;

    }

    /**
     * This method parses the data obtained (SWF format is expected) and creates
     * an instance of one newly arrived job.
     *
     * @param line a line to be parsed.
     * @param duplicate boolean indictor whether this job should be duplicated
     * (to generate secondary workload for specific simulations of different
     * user shares).
     * @return an instance of Job.
     */
    public Job parseLine(String line, boolean duplicate) {
        //System.out.println("Parsing line: "+line);
        line = line.replaceFirst("^\\s*", "");
        String values[] = line.split("\\s+");

        String queue = values[14];

        String user = values[11];
        String username = "user_" + values[11];
        if (duplicate) {
            user = user + "(share=2)";
            username = username + "(share=2)";
        }
        if (Fairshare_Simulator.use_selected_user_logins_only && !Fairshare_Simulator.selected_logins.contains(username)) {
            return null;
        }

        int cpus = Integer.parseInt(values[4]);
        int runtime = Integer.parseInt(values[3]);
        //int soft = Integer.parseInt(values[19]);
        int gpus = 0;
        if (values.length > 18) {
            gpus = Integer.parseInt(values[18]);
        }

        // pro CPU vs PE_RAM: (Integer.parseInt(user)>5)
        // pro GPU vs CPU
        boolean has_gpu = false;
        if (!Fairshare_Simulator.use_selected_user_logins_only) {
            if (gpus > 0 && Integer.parseInt(user) < 100) {
                has_gpu = true;
            }
            if (!has_gpu) {
                return null;
            }
        }

        String nodes = "" + values[15];

        String spec = "-";
        long wait = Long.parseLong(values[2]);
        int id = Integer.parseInt(values[0]);
        long arrival = start_date + Long.parseLong(values[1]);
        // int id, long arrival, long wait, long duration, int cpus, long ram, String nodes, String queue, String user, int GPUs, String username
        Job job = new Job(id, arrival, wait, runtime, cpus, Long.parseLong(values[9]), nodes, queue, user, gpus, username);

        
        if (SimulationSetup.scenario == 2) {
            if (job.getSPEC() > 3 && job.getSPEC() < 8) {
                job = null;
            }
        }
        
        return job;

    }

    /**
     * This method closes the file.
     */
    public void closeFile() {
        r.closeFile(br);
    }

}
