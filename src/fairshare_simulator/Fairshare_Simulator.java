/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fairshare_simulator;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import javax.swing.JFrame;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

/**
 * Main class of the Fairshare Simulator.
 * Several basic scenarios are preprepared in SimulatinSetup.java class.
 * These scenarios are invoked by calling the new SimulationSetup(..) constructor in the main method.
 *
 * @author Dalibor Klusacek
 */
public class Fairshare_Simulator {

    
    //the number of simulated ticks
    static int total_ticks;
    // how long is one tick of simulation (default is 1 hour, i.e., 3600)
    public static int tick;
    public static long first_day_epoch = 0;
    // default duration is 15 days = 15 * (3600 * 24) / tick
    public static int max_days;
    // how we measure utilization (default is CPU/GPU hours)
    public static double usage_divider;
    // simple line selector
    public static String requested_string_in_job_line = "";
    public static List<String> selected_logins;
    public static List<String> logins_with_boosted_shares = new ArrayList<String>();

    static boolean use_decay;
    public static int decay_period;
    public static double decay_factor;
    public static boolean use_SPEC;
    // allows to specify specific logins to be used only (see SimulationSetup.java)
    public static boolean use_selected_user_logins_only;
    public static boolean duplicate_users_for_shares;
    // anonymize real usernames (already done in existing workload logs)
    static boolean anonymize = false;
    // by default, SQL is disabled, but can be incorporated by modifying the sql_query and WorkloadReaderFromDB.java
    static boolean useSQL = false;

    // internal variables used to measure fairshare usage and fairshare factor for users
    public static ArrayList<ArrayList<Long>> usage_per_tick = new ArrayList<ArrayList<Long>>();
    public static ArrayList<ArrayList<Long>> cumul_usage_per_tick = new ArrayList<ArrayList<Long>>();
    static ArrayList<ArrayList<Double>> fairshare_factor = new ArrayList<ArrayList<Double>>();
    static HashMap<String, Integer> user_shares = new HashMap<String, Integer>();
    public static ArrayList<String> users = new ArrayList<String>();
    public static String Yaxis = "";

    

    /**
     * Main method that starts the Fairshare Simulator.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        // Here we chose the simulation scenario !!! params: workload_log (default is "NGI_CZ_demo.swf"), scenario, usage_measurement, PBS/SWF input
        // Check SimulationSetup.init() to see and change scenarios
        SimulationSetup setup = new SimulationSetup("OpenPBS.log", 6, 1, true);
        
        // create the Workload Parser which will read the data and fill all data structures with proper values of fairhare metrics
        JobExecutionSimulator wp = new JobExecutionSimulator();

        TimeSeriesCollection dataset_ff = new TimeSeriesCollection();
        TimeSeriesCollection dataset_usage = new TimeSeriesCollection();
        TimeSeriesCollection dataset_cumul_usage = new TimeSeriesCollection();
        for (int u = 0; u < users.size(); u++) {
            String username = "";
            if (!anonymize) {
                username = users.get(u);
            } else {
                username = "user_" + u;
            }
            TimeSeries series_u = new TimeSeries(username);
            TimeSeries series_ff = new TimeSeries(username);
            TimeSeries series_c_u = new TimeSeries(username);
            dataset_usage.addSeries(series_u);
            dataset_ff.addSeries(series_ff);
            dataset_cumul_usage.addSeries(series_c_u);
        }

        System.out.println("total_tick: " + total_ticks);
        for (int curr_tick = 0; curr_tick < total_ticks; curr_tick++) {
            for (int u = 0; u < users.size(); u++) {
                TimeSeries series_u = dataset_usage.getSeries(u);
                TimeSeries series_ff = dataset_ff.getSeries(u);
                TimeSeries series_c_u = dataset_cumul_usage.getSeries(u);
                double usage_HPC = usage_per_tick.get(u).get(curr_tick) / usage_divider;
                double cumul_usage_HPC = cumul_usage_per_tick.get(u).get(curr_tick) / usage_divider;
                double ffHPC = calculate_fairshare_factor(users.get(u), curr_tick);

                Date date = new java.util.Date((first_day_epoch + (tick * curr_tick)) * 1000);
                series_u.addOrUpdate(new Minute(date, TimeZone.getDefault(), Locale.getDefault()), usage_HPC);
                series_ff.addOrUpdate(new Minute(date, TimeZone.getDefault(), Locale.getDefault()), ffHPC);
                series_c_u.addOrUpdate(new Minute(date, TimeZone.getDefault(), Locale.getDefault()), cumul_usage_HPC);

            }
        }

        // now we draw all charts as the output of the simulator
        String prefix = "";
        if (use_SPEC) {
            prefix = "Weighted ";
        }

        int width = 350;
        int height = 300;
        
        if (use_decay) {
            TimeSeriesChart example = new TimeSeriesChart(prefix + "Fairshare Factor", "Decay factor applied", dataset_ff, true, width, height);
        } else {
            TimeSeriesChart example = new TimeSeriesChart(prefix + "Fairshare Factor", "No Decaying applied", dataset_ff, true, width, height);
        }
        TimeSeriesChart exampleu = new TimeSeriesChart(prefix + "Usage in time", "", dataset_usage, false, width, height);

        if (use_decay) {
            String unit = "hours";
            if(tick==60){
                unit = "minutes";
            }
            TimeSeriesChart examplecu = new TimeSeriesChart(prefix + "Cumulative Usage", "Decay factor=" + decay_factor + ", period=" + (decay_period / tick) + " "+unit, dataset_cumul_usage, false, width, height);
        } else {
            TimeSeriesChart examplecu = new TimeSeriesChart(prefix + "Cumulative Usage", "No Decaying applied", dataset_cumul_usage, false, width, height);
        }
        
        System.out.println();
        System.out.println("=============================");
        System.out.println("    SIMULATION COMPLETED     ");
        System.out.println("=============================");
    }

    /**
     * If decaying is enabled in the fairshare, this method will apply decay
     *
     * @param curr_tick the current simulation tick
     */
    public static void applyDecay(int curr_tick) {
        if (use_decay) {
            if ((curr_tick * tick) % decay_period == 0) {
                for (int u = 0; u < users.size(); u++) {
                    
                    long prev_usage = cumul_usage_per_tick.get(u).get(curr_tick);
                    long usage_decay = Math.round(prev_usage * decay_factor);
                    System.out.println("Doing decay at: " + curr_tick+" tick(s) for user: "+users.get(u)+" usage: "+prev_usage/usage_divider+" dec: "+usage_decay/usage_divider);
                    cumul_usage_per_tick.get(u).remove(curr_tick);
                    cumul_usage_per_tick.get(u).add(curr_tick, usage_decay);
                }
            }
        }
    }

    /**
     * This method calculates the fairshare factor for a given user and
     * simulation tick.
     *
     * @param user the user for whom the fairshare factor is calculated
     * @param tick the current simulation tick
     * @return fairshare factor (0..1)
     */
    public static double calculate_fairshare_factor(String user, int tick) {
        double ff = 0.5;
        int user_index = users.indexOf(user);
        double tree_usage = cumul_usage_per_tick.get(user_index).get(tick) / calculate_total_usage(tick);
        double target_usage = user_shares.get(user) / calculate_total_shares();
        ff = Math.pow(2, -(tree_usage / target_usage));
        return ff;
    }

    /**
     * This method updates the total fairshare usage for each user at the
     * current simulation tick.
     *
     * @param tick the current simulation tick
     * @return total usage of all users in current simulation tick
     */
    public static double calculate_total_usage(int tick) {
        double tu = 0.0;
        for (int u = 0; u < users.size(); u++) {
            //System.out.println(u + " u, tick " + tick);
            tu += cumul_usage_per_tick.get(u).get(tick);
        }
        //System.out.println("----");
        return Math.max(tu, 0.00000001);
    }

    /**
     * This method calculates the total number of user shares.
     *
     * @return the total number of user shares
     */
    public static double calculate_total_shares() {
        double ts = 0.0;
        for (int u = 0; u < users.size(); u++) {
            ts += user_shares.get(users.get(u));
        }
        return Math.max(ts, 1);
    }

    /**
     * This method creates new user and sets up all related data structures.
     *
     * @param curr_tick the current simulation tick
     */
    public static void createUser(String username, int shares, int curr_tick) {
        usage_per_tick.add(new ArrayList<Long>());
        cumul_usage_per_tick.add(new ArrayList<Long>());
        fairshare_factor.add(new ArrayList<Double>());
        users.add(username);
        user_shares.put(username, shares);
        int user_index = users.indexOf(username);
        // new user appeared at curr_tick, so prepare all data structures
        for (int tick = 0; tick <= curr_tick; tick++) {
            Integer def = 0;
            usage_per_tick.get(user_index).add(def.longValue());
            cumul_usage_per_tick.get(user_index).add(def.longValue());
            //fairshare_factor.get(user_index).add(0.0);
        }
    }

    /**
     * This method checks if all data structures of this user are up-to-date
     * (and add empty fields if not).
     *
     * @param curr_tick the current simulation tick
     */
    public static void adjustUserRecords(int user_index, int curr_tick) {
        // check if all data structures of this user are up-to-date (and add empty fields if not)
        int diff = (curr_tick + 1) - usage_per_tick.get(user_index).size();
        if (diff > 0) {
            Integer def = 0;
            long def_cumul;
            def_cumul = 0;
            if (cumul_usage_per_tick.get(user_index).size() > 0) {
                def_cumul = cumul_usage_per_tick.get(user_index).get((cumul_usage_per_tick.get(user_index).size() - 1));
            }
            for (int tick = 0; tick < diff; tick++) {
                usage_per_tick.get(user_index).add(def.longValue());
                cumul_usage_per_tick.get(user_index).add(def_cumul);
            }
        }
    }

    /**
     * This method makes sure to not left out some users structures from
     * previous ticks and fills them accordingly.
     *
     * @param curr_tick the current simulation tick
     */
    public static void closeUserRecords(int curr_tick) {
        // check if all data structures of this user are up-to-date (and add empty fields if not)
        for (int u = 0; u < users.size(); u++) {
            int diff = (curr_tick + 1) - usage_per_tick.get(u).size();
            if (diff > 0) {
                Integer def = 0;
                long def_cumul;
                def_cumul = 0;
                if (cumul_usage_per_tick.get(u).size() > 0) {
                    def_cumul = cumul_usage_per_tick.get(u).get((cumul_usage_per_tick.get(u).size() - 1));
                }
                for (int tick = 0; tick < diff; tick++) {
                    usage_per_tick.get(u).add(def.longValue());
                    cumul_usage_per_tick.get(u).add(def_cumul);
                    //fairshare_factor.get(user_index).add(0.0);
                }
            }
        }
    }

    /**
     * This method updates a user record for current tick based on their
     * resource consumption during this tick.
     *
     * @param user_index index of the user
     * @param curr_tick current simulation tick
     * @param used_resource_seconds_per_tick used resources per this tick (e.g.,
     * CPU seconds or Processor Equivalent including other resources)
     */
    public static void updateUserRecords(int user_index, int curr_tick, long used_resource_seconds_per_tick) {
        // update structures for this user        
        Long prev_usage = usage_per_tick.get(user_index).remove(curr_tick);
        usage_per_tick.get(user_index).add(curr_tick, (prev_usage + used_resource_seconds_per_tick));

    }

    /**
     * This method updates all cumulative usages of all users for this
     * simulation tick.
     *
     * @param curr_tick the current simulation tick
     */
    public static void updateCumulativeUsage(int curr_tick) {
        // update cummulative usage for this user        
        if (curr_tick > 0) {
            for (int u = 0; u < users.size(); u++) {
                Long new_usage = usage_per_tick.get(u).get(curr_tick);
                Long prev_cumul_usage = cumul_usage_per_tick.get(u).get(curr_tick - 1);
                cumul_usage_per_tick.get(u).remove(curr_tick);
                cumul_usage_per_tick.get(u).add(curr_tick, (prev_cumul_usage + new_usage));
            }
        }
    }

}
