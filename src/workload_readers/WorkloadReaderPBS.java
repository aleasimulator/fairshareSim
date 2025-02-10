/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package workload_readers;

import fairshare_simulator.Fairshare_Simulator;
import fairshare_simulator.Input;
import fairshare_simulator.Job;
import fairshare_simulator.JobArrivalComparator;
import fairshare_simulator.JobExecutionSimulator;
import fairshare_simulator.SimulationSetup;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * This class reads data from a given PBS-formatted job trace file.
 *
 * @author Dalibor Klusacek
 */
public class WorkloadReaderPBS implements WorkloadReader {

    static Input r = new Input();
    static BufferedReader br;
    public static long start_date = -1;
    private static int last_job_id = 0;
    public static ArrayList<Job> pbs_jobs = new ArrayList();

    /**
     * This constructor opens the file with the PBS formatted workload log trace. 
     * It reads and sorts all such jobs according to their arrival time.
     *
     * @param data a path to the workload (located in the ./data-set directory).
     */
    public WorkloadReaderPBS(String data) {
        String adresar = System.getProperty("user.dir");
        System.out.println("Opening folder: " + adresar + "/data-set/" + data);
        br = r.openFile(new File(adresar + "/data-set/" + data));
        // read all jobs and sort them by their arrival time
        parseAllLines();
        /*for (Job job : pbs_jobs) {
            System.out.println(job.getId() + ": " + job.getArrival());
        }
        System.out.println("========================================");*/
        Collections.sort(pbs_jobs, new JobArrivalComparator());
        /*for (Job job : pbs_jobs) {
            System.out.println(job.getId() + ": " + job.getArrival());
        }
        System.out.println("========================================");
        */
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

            if (pbs_jobs.size() <= 0) {
                break;
            } else {

                Job job = parseLine(line, false);
                Job job_duplicate = null;
                if (Fairshare_Simulator.duplicate_users_for_shares && job != null) {
                    job_duplicate = parseLine(line, true);
                    job_duplicate.setCpus(job_duplicate.getCpus() * 2);
                }
                pbs_jobs.removeFirst();

                if (job == null) {
                    //System.out.println("Null job... for " + line);
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
     * This method parses the data obtained (PBS format is expected) and creates
     * instances of all newly arrived job.     *
     * 
     */
    public void parseAllLines() {
        String line = null;
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
                if (line.length() < 5 || !line.contains(";E;") || !line.contains(Fairshare_Simulator.requested_string_in_job_line)) {
                    if (start_date < 0 && line.contains("; UnixStartTime: ")) {
                        start_date = Long.parseLong(line.replace("; UnixStartTime: ", ""));
                        Fairshare_Simulator.first_day_epoch = start_date;
                        String dated = new java.text.SimpleDateFormat("HH:mm dd-MM-yyyy").format(new java.util.Date(start_date * 1000));
                        System.out.println("Start time of workload is UnixStartTime: " + start_date + ", [" + dated + "]");
                    }
                    //System.out.println("Skipping line: " + line);
                    // skip to next line
                    continue;
                }

                //System.out.println("Parsing line: " + line);
                int si = 0;
                int ei = 0;
                si = line.indexOf("user=");
                line = line.substring(si);
                //System.out.println("Parsing line: " + line);
                String values[] = line.split(" ");
                /*
0 user=vchlum
1 group=meta
2 project=_pbs_project_default
3 jobname=STDIN
4 queue=workq
5 ctime=1733823166
6 qtime=1733823166
7 etime=1733823166
8 start=1733823167
9 exec_host=torque2/0+torque2/1
10 exec_vnode=(torque2:ncpus=1:mem=307200kb)+(torque2:ncpus=1:mem=307200kb)
11 Resource_List.mem=600mb
12 Resource_List.ncpus=2
13 Resource_List.nodect=2
14 Resource_List.place=free
15 Resource_List.select=2:ncpus=1:mem=300mb
16 Resource_List.walltime=02:00:00
17 session=4011674
18 end=1733823770
19 Exit_status=0
20 resources_used.cpupercent=0
21 resources_used.cput=00:00:00
22 resources_used.mem=888kb
23 resources_used.ncpus=2
24 resources_used.vmem=2484kb
25 resources_used.walltime=00:10:00
                 */
                String queue = values[4].substring(values[4].indexOf("=") + 1);;
                String user = values[0].substring(values[0].indexOf("=") + 1);

                //System.out.println("user, queue: " + user + " " + queue);
                if(user.equals("vchlum")){
                    user = " A";
                }else{
                    user = " B";
                }
                String username = "user" + user;

                if (Fairshare_Simulator.use_selected_user_logins_only && !Fairshare_Simulator.selected_logins.contains(username)) {
                    continue;
                }
                int cpus = Integer.parseInt(values[12].substring(values[12].indexOf("=") + 1));
                int runtime = Integer.parseInt(values[18].substring(values[18].indexOf("=") + 1)) - Integer.parseInt(values[8].substring(values[8].indexOf("=") + 1));
                
                //int soft = Integer.parseInt(values[19]);
                int gpus = 0;

                
                String nodes = "" + values[10];

                String spec = "-";
                long wait = Long.parseLong(values[8].substring(values[8].indexOf("=") + 1)) - Long.parseLong(values[5].substring(values[5].indexOf("=") + 1));
                int id = last_job_id;
                long arrival = Long.parseLong(values[5].substring(values[5].indexOf("=") + 1));
                long ram = 1000;
                //runtime = 525;
                // int id, long arrival, long wait, long duration, int cpus, long ram, String nodes, String queue, String user, int GPUs, String username
                Job job = new Job(id, arrival, wait, runtime, cpus, ram, nodes, queue, user, gpus, username);

                if (SimulationSetup.scenario == 2) {
                    if (job.getSPEC() > 3 && job.getSPEC() < 8) {
                        continue;
                    }
                }

                last_job_id++;
                //System.out.println("Adding "+job.getId()+" ncpus= "+job.getCpus());
                pbs_jobs.add(job);

            }
        }

    }

    /**
     * This method gets the first job (from the list of all PBS jobs) and returns it.
     * @param line a line to be parsed.
     * @param duplicate boolean indictor whether this job should be duplicated
     * @return an instance of Job 
     */
    public Job parseLine(String line, boolean duplicate) {
        Job job = pbs_jobs.getFirst();

        if (duplicate) {
            String user = job.getUser();
            String username = job.getUsername();
            user = user + "(share=2)";
            username = username + "(share=2)";
            Job job_dupl = new Job(job.getId(), job.getArrival(), job.getWait(), job.getRuntime(), job.getCpus(), job.getRam(), job.getNodes(), job.getQueue(), user, job.getGPUs(), username);
            return job_dupl;
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
