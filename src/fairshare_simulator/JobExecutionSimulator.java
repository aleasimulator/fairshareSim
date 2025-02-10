/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fairshare_simulator;

import workload_readers.WorkloadReaderSWF;
import workload_readers.WorkloadReader;
import workload_readers.WorkloadReaderFromDB;
import java.util.ArrayList;
import usage_calculators.UsageCalculator;
import workload_readers.WorkloadReaderPBS;

/**
 * This class parses the workload file and simulates the execution of jobs.
 *
 * @author Dalibor Klusacek
 */
public class JobExecutionSimulator {

    int tick = Fairshare_Simulator.tick;
    String data = SimulationSetup.data;
    public Job pending_job = null;
    public Job pending_job_duplicate = null;
    public ArrayList<Job> active_jobs = new ArrayList();
    public ArrayList<Job> running_jobs = new ArrayList();
    public int curr_tick = 0;

    /**
     * Calling this contructor will start the simulation of job executions by
     * calling the method simulateJobArrivals().
     */
    public JobExecutionSimulator() {
        simulateJobArrivals();
    }

    /**
     * This method simulate the execution of jobs, one simulation tick at a
     * time. It is the main method of the whole simulator.<p>
     * After selecting the proper Workload Reader (DB/file), it reads all jobs
     * arriving at each simulation tick. It then updates all internal data
     * structures. Especially, it identifies new jobs, active jobs, running jobs
     * and completed jobs (in each tick). All internal data structures are
     * updated by calling respective methods. Once completed, charts in the main
     * class can be drawn using the generated data structures.
     */
    public void simulateJobArrivals() {
        boolean run_tick = true;
        long tick_start_epoch = 0;
        long tick_end_epoch = 0;
        Job first_job = null;
        int tot_jobs = 0;

        WorkloadReader wr = null;
        if (Fairshare_Simulator.useSQL) {
            wr = new WorkloadReaderFromDB(data);
        } else if (SimulationSetup.use_PBS_trace) {
            wr = new WorkloadReaderPBS(data);
        } else {
            wr = new WorkloadReaderSWF(data);
        }

        if (Fairshare_Simulator.use_selected_user_logins_only) {
            for (String username : Fairshare_Simulator.selected_logins) {
                if (!Fairshare_Simulator.users.contains(username)) {
                    Fairshare_Simulator.createUser(username, 1, curr_tick);
                    if (Fairshare_Simulator.duplicate_users_for_shares) {
                        Fairshare_Simulator.createUser(username + "(share=2)", 2, curr_tick);
                        Fairshare_Simulator.logins_with_boosted_shares.add(username + "(share=2)");
                    }
                }
            }
            if (Fairshare_Simulator.duplicate_users_for_shares) {
                Fairshare_Simulator.selected_logins.addAll(Fairshare_Simulator.logins_with_boosted_shares);
            }
        }

        while (run_tick && curr_tick < Fairshare_Simulator.max_days) {

            // inicializace dat a uloh
            ArrayList<Job> init_jobs = null;
            if (first_job == null) {
                init_jobs = wr.readArrivedJobs(-1, -1, null, this);
                first_job = init_jobs.remove(0);
                tot_jobs++;
                //tick_start_epoch = first_job.getArrival() - (first_job.getArrival() % 86400);                
                tick_start_epoch = Fairshare_Simulator.first_day_epoch;
                tick_end_epoch = tick_start_epoch + tick;
                active_jobs.add(first_job);
                System.out.println("job: " + first_job.getId() + " arrived as first at: " + first_job.getArrival());
            }

            //System.out.println("Read new jobs, pending: "+pending_job);
            ArrayList<Job> new_jobs = wr.readArrivedJobs(tick_start_epoch, tick_end_epoch, pending_job, this);
            if (init_jobs != null && init_jobs.size() > 0) {
                new_jobs.addAll(init_jobs);
                init_jobs.clear();
            }
            //System.out.println("New jobs in, pending: "+pending_job);

            tot_jobs += new_jobs.size();

            // read newly arrived jobs
            active_jobs.addAll(new_jobs);

            // remove already completed jobs from previous ticks
            remove_finished_jobs(tick_start_epoch, tick_end_epoch);

            // select jobs that run in this tick
            running_jobs = selectRunningJobs(tick_start_epoch, tick_end_epoch);
                                  
            // update user-structures in the main class
            updateRunningJobsStatistics(tick_start_epoch, tick_end_epoch);

//apply decay if enabled
            Fairshare_Simulator.applyDecay(curr_tick);            

            //compute_queue_arrivals(tick_start_epoch, tick_end_epoch, new_jobs, curr_tick);
            //compute_user_arrivals(tick_start_epoch, tick_end_epoch, new_jobs, curr_tick);
            //compute_queue_wait(tick_start_epoch, tick_end_epoch, new_jobs, curr_tick);
            //compute_user_data(tick_start_epoch, tick_end_epoch, new_jobs, curr_tick);
            //aktualizace seznamu uloh - vyhod vsechny uz skoncene
            String dated = new java.text.SimpleDateFormat("HH:mm dd-MM-yyyy").format(new java.util.Date(tick_start_epoch * 1000));
            System.out.println("Tick " + curr_tick + " so far jobs " + tot_jobs + " active = " + active_jobs.size() + " day = [" + dated + "]");

            //vypocet a zapis vysledku
            //compute_results(tick_start_epoch, tick_end_epoch, curr_tick);
            tick_start_epoch += tick;
            tick_end_epoch += tick;

            curr_tick++;

            if (pending_job != null && active_jobs.size() < 1) {
                while (pending_job.getArrival() >= tick_end_epoch) {
                    // make sure to not left out some users structures from previous ticks
                    Fairshare_Simulator.closeUserRecords(curr_tick);
                    // update cummulative usage
                    Fairshare_Simulator.updateCumulativeUsage(curr_tick);
                    //apply decay if enabled
                    Fairshare_Simulator.applyDecay(curr_tick);

                    tick_start_epoch += tick;
                    tick_end_epoch += tick;
                    curr_tick++;
                }
                System.out.println(pending_job.getId() + " job: Skipping to tick " + curr_tick + " pending jobs' arrival: " + pending_job.getArrival() + " next start day: " + (new java.text.SimpleDateFormat("dd-MM-yyyy").format(new java.util.Date(tick_start_epoch * 1000))));

            }

            // no more jobs, quit simulation
            if (pending_job == null && active_jobs.size() < 1) {
                System.out.println("tick: " + curr_tick + ", max_days:" + Fairshare_Simulator.max_days + " pending=" + pending_job + " active jobs=" + active_jobs.size() + " first job=" + first_job.getId());
                run_tick = false;

                continue;
            }
            System.out.println("============================================");
        }
        Fairshare_Simulator.total_ticks = curr_tick;
        wr.closeFile();
    }

    /**
     * This method selects only those jobs that were running during this
     * simulation tick.
     *
     * @param start tick start time
     * @param end tick end time
     * @return jobs that are running during this tick
     */
    private ArrayList<Job> selectRunningJobs(long start, long end) {
        running_jobs.clear();
        ArrayList<Job> running = new ArrayList<Job>();
        running.clear();
        for (int i = 0; i < active_jobs.size(); i++) {
            Job job = active_jobs.get(i);
            // this is a job which runs in this period
            if (job.getStart() < end && job.getEnd() > start) {
                running.add(job);
            }
        }
        return running;
    }

    /**
     * This method calculates resource usage of running jobs during this tick
     * and then updates corresponding users records. It uses one of the
     * supported usage-measuring metrics based on Processor Equivalent (PE).
     *
     * @param start tick start time
     * @param end tick end time
     */
    private void updateRunningJobsStatistics(long start, long end) {

        for (int i = 0; i < running_jobs.size(); i++) {
            Job job = running_jobs.get(i);
            // this is a job which runs in this period
            String user = job.getUsername();
            if (!Fairshare_Simulator.users.contains(user)) {
                Fairshare_Simulator.createUser(user, 1, curr_tick);
                if (Fairshare_Simulator.duplicate_users_for_shares) {
                    Fairshare_Simulator.createUser(user + "(share=2)", 2, curr_tick);
                    Fairshare_Simulator.selected_logins.add(user + "(share=2)");
                }
            }
            int user_index = Fairshare_Simulator.users.indexOf(user);
            // fill in all structures up till this current tick
            Fairshare_Simulator.adjustUserRecords(user_index, curr_tick);
            
            // update this user's tick's record with the running job
            long jend = Math.min(end, job.getEnd());
            long jstart = Math.max(start, job.getStart());
            long dur_per_tick = (jend - jstart);

            // Change here the metric used to account resource usage
            int usage = 1;

            switch (SimulationSetup.usage_policy) {
                case 1:
                    usage = UsageCalculator.calculateUsagePE_CPU(job);
                    Fairshare_Simulator.Yaxis = "CPU";
                    break;
                case 2:
                    usage = UsageCalculator.calculateUsagePE_CPU_RAM(job);
                    Fairshare_Simulator.Yaxis = "PE(CPU+RAM)";
                    break;
                case 3:
                    usage = UsageCalculator.calculateUsagePE_CPU_RAM_GPU(job);
                    Fairshare_Simulator.Yaxis = "PE(CPU+RAM+GPU)";
                    break;

                default:
                    usage = 1;
            }

            // 0.946 factor to reflect nonprecision accounting of OpenPBS
            long cpu_seconds_per_tick = Math.round(dur_per_tick * usage * 0.946);
            if (Fairshare_Simulator.use_SPEC) {
                cpu_seconds_per_tick = Math.round(cpu_seconds_per_tick * job.getSPEC());
            }
            //System.out.println(usage+" CPU, "+dur_per_tick+" duration");
            Fairshare_Simulator.updateUserRecords(user_index, curr_tick, cpu_seconds_per_tick);
        }
        // make sure to not left out some users structures from previous ticks
        Fairshare_Simulator.closeUserRecords(curr_tick);

        // update cummulative usage
        Fairshare_Simulator.updateCumulativeUsage(curr_tick);

    }

    /**
     * This method removes jobs that have finished in this simulation tick.
     *
     * @param start tick start time
     * @param end tick end time
     */
    private void remove_finished_jobs(double start, double end) {
        String output = "";
        for (int i = 0; i < active_jobs.size(); i++) {
            Job job = active_jobs.get(i);
            if (job.getEnd() <= start) {
                active_jobs.remove(job);

                i--;
            }
        }

    }
}
