/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fairshare_simulator;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class initializes internal variables based on (1) simulated scenario
 * (e.g., decay, SPEC, etc.) and (2) used usage policy (what we measure as
 * "usage").
 *
 * @author Dalibor Klusacek
 */
public class SimulationSetup {

    // name of the SWF-formatted data set
    public static String data;
    public static int scenario;
    public static int usage_policy;
    public static String sql_query
            = "SELECT a.acct_id_string, a.create_time, (a.start_time-a.create_time) as wait_time, (a.end_time-a.start_time) as runtime, a.req_ncpus, a.req_mem, a.acct_user_id, u.user_name, a.queue, a.req_walltime, a.soft_walltime, s.gpu, array_agg(h.hostname) as node\n"
            + "      from acct_pbs_record a, acct_hosts_used s, acct_user u, acct_host h\n"
            + "      WHERE a.create_time >= extract(epoch from timestamp '2023-01-01') and a.create_time < extract(epoch from timestamp '2023-01-15')     \n"
            + "      and s.acct_id_string=a.acct_id_string and s.acct_host_id=h.acct_host_id \n"
            + "      and a.acct_user_id=u.acct_user_id\n"
            + "      and (ci_acct_pbs_server_id = 25 or ci_acct_pbs_server_id = 29) \n"
            + "      and (h.hostname ILIKE 'gita%' or h.hostname ILIKE 'aman%')"
            + "GROUP BY\n"
            + "a.acct_id_string, a.create_time, wait_time, runtime, a.req_ncpus, a.req_mem, a.acct_user_id, u.user_name, a.queue, a.req_walltime, a.soft_walltime, s.gpu\n"
            + "ORDER BY create_time asc;";

    /**
     * This constructor initializes internal variables based on (1) simulated
     * scenario (e.g., decay, SPEC, etc.) and (2) used usage policy (what we
     * measure as "usage").
     *
     * @author Dalibor Klusacek
     * @param scenario simulation scenario
     * @param usage_policy usage measurement policy (CPU time, CPU+RAM,
     * CPU+RAM+GPU)
     */
    public SimulationSetup(String data_set, int scenario, int usage_policy) {
        SimulationSetup.scenario = scenario;
        System.out.println("=================================================================");
        System.out.println("!!! OPEN SimulationSetup.java TO CHANGE SIMULATION PARAMETERS !!!");
        System.out.println(" SimulationSetup.java is the place to put your modifications of");
        System.out.println("   simulated scenarios (fairshare usage measurement policy or ");
        System.out.println("        decay/SPEC weighting enabling/disabling etc. ");
        System.out.println("=================================================================");

        init(data_set, scenario, usage_policy);
    }

     /** This method chooses proper parameters for several different scenarios (and allow to add new ones).
      * 
      * @param data_set the workload log to read
      * @param scenario scenario to simulate (e.g., metrics, SPEC weighting, decay)
      * @param usage_policy which metric to use for measuring resource usage (e.g., CPU, CPU+RAM, CPU+RAM+GPU (via Proc. Equivalent)) 
      */
    public static void init(String data_set, int scenario, int usage_policy) {
        SimulationSetup.data = data_set;

        // how long is one tick of simulation (default is 1 hour, i.e., 3600)
        Fairshare_Simulator.tick = 3600;

        // default duration is 15 days = 15 * (3600 * 24) / tick
        Fairshare_Simulator.max_days = 15 * (3600 * 24) / Fairshare_Simulator.tick;

        // how we measure utilization (default is CPU/GPU hours)
        Fairshare_Simulator.usage_divider = Fairshare_Simulator.tick;
        // usage_policy (how we measure resource usage): 1 = cpu, 2 = cpu+ram, 3 = cpu+ram+gpu
        SimulationSetup.usage_policy = usage_policy;

        switch (scenario) {
            case 1:
                //CPU, GPU and PE (Processor Equivalent) metrics to measure usage of resources 
                Fairshare_Simulator.use_selected_user_logins_only = true;
                Fairshare_Simulator.selected_logins = Arrays.asList("user_41", "user_77", "user_87", "user_14");
                Fairshare_Simulator.duplicate_users_for_shares = false;
                Fairshare_Simulator.use_SPEC = false;
                Fairshare_Simulator.use_decay = false;
                break;
            case 2:
                //SPEC weighted jobs
                Fairshare_Simulator.use_selected_user_logins_only = true;
                Fairshare_Simulator.selected_logins = Arrays.asList("user_12", "user_27", "user_30", "user_11");
                Fairshare_Simulator.duplicate_users_for_shares = false;
                Fairshare_Simulator.use_SPEC = true;
                Fairshare_Simulator.use_decay = false;
                break;
            case 3:
                //Decay jobs 
                Fairshare_Simulator.use_selected_user_logins_only = true;
                Fairshare_Simulator.selected_logins = Arrays.asList("user_1", "user_5", "user_6", "user_8");
                Fairshare_Simulator.duplicate_users_for_shares = false;
                Fairshare_Simulator.use_SPEC = false;
                Fairshare_Simulator.use_decay = true;
                Fairshare_Simulator.decay_period = 24 * 3600;
                Fairshare_Simulator.decay_factor = 0.5;
                break;
            case 4:
                // 2 users with different shares
                Fairshare_Simulator.use_selected_user_logins_only = true;
                Fairshare_Simulator.selected_logins = new ArrayList<>(Arrays.asList("user_6"));
                Fairshare_Simulator.duplicate_users_for_shares = true;
                Fairshare_Simulator.use_SPEC = false;
                Fairshare_Simulator.use_decay = false;
                break;
            case 5:
                // All jobs of All users
                Fairshare_Simulator.use_selected_user_logins_only = false;
                Fairshare_Simulator.duplicate_users_for_shares = false;
                Fairshare_Simulator.use_SPEC = false;
                Fairshare_Simulator.use_decay = false;
                break;

            default:

        }

    }

}
