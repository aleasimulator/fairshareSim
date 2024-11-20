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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * This class reads data from a database connection.
 *
 * @author Dalibor Klusacek
 */
public class WorkloadReaderFromDB implements WorkloadReader {

    static Input r = new Input();
    static ResultSet rs;

    /**
     * The constructor creates connection to the database.
     *
     * @param data
     */
    public WorkloadReaderFromDB(String data) {

        System.out.println("Opening Database... ");
        this.rs = connectToDB(SimulationSetup.sql_query);
        int size = 0;
        try {
            rs.last();
            size = rs.getRow();
            rs.beforeFirst();
        } catch (Exception ex) {
            System.out.println("ERR: Cannot know the number of lines....: " + ex.getMessage());

        }
        System.out.println("Number of retrieved lines from DB: " + size);

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
     * @return
     */
    public ArrayList<Job> readArrivedJobs(long start, long end, Job pending_job, JobExecutionSimulator simulator) {
        ArrayList<Job> jobs = new ArrayList();

        // add pending job if it belongs here
        if (simulator.pending_job != null && simulator.pending_job.getArrival() >= start && simulator.pending_job.getArrival() < end) {
            jobs.add(simulator.pending_job);
            //System.out.println("Adding PENDING job to jobs RUNNING... pending job is:" + simulator.pending_job.getId());
            simulator.pending_job = null;
        }
        // this job will start later
        if (simulator.pending_job != null && simulator.pending_job.getArrival() > end) {
            //System.out.println("PENDING job starts in the future - SKIP the search... pending job is:" + simulator.pending_job.getId());
            return jobs;
        }
        String line = "";
        boolean run = true;
        boolean row_exists = false;
        int columnCount = 0;
        try {
            ResultSetMetaData metadata = rs.getMetaData();
            columnCount = metadata.getColumnCount();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        while (run) {
            try {
                row_exists = this.rs.next();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (!row_exists) {
                break;
            } else {
                try {
                    line = "";
                    for (int i = 1; i <= columnCount; i++) {
                        line += rs.getString(i) + " | ";
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                }
                //System.out.println("line: "+line);
                //System.out.println("-------------------------"+columnCount);
                if (line.length() < 5 || line.contains("acct_id_string") || line.contains("---+----") || line.contains(" rows)") || !line.contains(Fairshare_Simulator.requested_string_in_job_line)) {
                    // skip to next line
                    continue;
                }

                Job job = parseLine(line, false);
                if (job == null) {
                    continue;
                }

                if (start == -1 && end == -1) {
                    //System.out.println("Adding first ever job..."+line);
                    long tick_start_epoch = job.getArrival() - (job.getArrival() % 86400);
                    Fairshare_Simulator.first_day_epoch = tick_start_epoch;
                    jobs.add(job);
                    run = false;
                } else if (job.getArrival() >= start && job.getArrival() < end) {
                    //System.out.println("Adding job...");
                    jobs.add(job);

                } else {
                    simulator.pending_job = job;

                    //System.out.println("New Pending job... " + simulator.pending_job.getId());
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
     * This method parses the data obtained and creates an instance of one newly
     * arrived job.
     *
     * @param line a line to be parsed.
     * @param duplicate boolean indictor whether this job should be duplicated
     * (to generate secondary workload for specific simulations of different
     * user shares).
     * @return an instance of Job.
     */
    public Job parseLine(String line, boolean duplicate) {
        line = line.replaceAll("\\s+", "");
        String values[] = line.split("\\|");
        //a.acct_id_string, a.create_time, wait_time, runtime, a.req_ncpus, a.req_mem, a.acct_user_id, u.user_name, a.queue, a.req_walltime, a.soft_walltime, s.gpu
        String queue = values[8];
        String username = values[7];
        if (Fairshare_Simulator.use_selected_user_logins_only && !Fairshare_Simulator.selected_logins.contains(username)) {
            return null;
        }
        String user = values[6];
        int cpus = Integer.parseInt(values[4]);
        int runtime = Integer.parseInt(values[3]);
        int gpus = Integer.parseInt(values[11]);
        String nodes = values[12];
        String spec = "-";
        long wait = Long.parseLong(values[2]);

        // int id, long arrival, long wait, long duration, int cpus, long ram, String nodes, String queue, String user, int GPUs, String username
        Job job = new Job(Integer.parseInt((values[0].split("\\.")[0]).split("\\[")[0]), Long.parseLong(values[1]), wait, runtime, cpus, Long.parseLong(values[5]), nodes, queue, user, gpus, username);
        return job;

    }

    /**
     * This method does nothing since we use DB and not a file.
     */
    public void closeFile() {

    }

    /**
     * This method connects to a specified database.
     *
     * @param sql_query the SQL command that should be executed.
     * @return returns a ResultSet that can be parsed.
     */
    private ResultSet connectToDB(String sql_query) {

        Scanner keyboard = new Scanner(System.in);
        System.out.println("Enter server hostname:");
        String hostname = keyboard.nextLine();

        System.out.println("Enter database name:");
        String dbname = keyboard.nextLine();
        
        System.out.println("Enter your DB username:");
        String username = keyboard.nextLine();

        System.out.println("Enter your DB password:");
        String passwd = keyboard.nextLine();

        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://"+hostname+"/"+dbname, username, passwd)) {
            System.out.println("Connecting to: jdbc:postgresql://"+hostname+"/"+dbname); 
            // When this class first attempts to establish a connection, it automatically loads any JDBC 4.0 drivers found within 
            // the class path. Note that your application must manually load any JDBC drivers prior to version 4.0.
//          Class.forName("org.postgresql.Driver"); 
            System.out.println("Connected to PostgreSQL database!\n");
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            System.out.println("Executing SQL: \n----------------------------------\n" + sql_query);
            System.out.println("----------------------------------\n");
            System.out.println("\n-----------------------------------");
            System.out.println("  THIS MAY LAST SEVERAL MINUTES...  ");
            System.out.println("-----------------------------------\n");
            ResultSet resultSet = statement.executeQuery(sql_query);
            //while (resultSet.next()) {
            //    System.out.println(resultSet.getString("ci_acct_pbs_server_id") + " " + resultSet.getString("hostname"));
            //}
            return resultSet;

        } /*catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC driver not found.");
            e.printStackTrace();
        }*/ catch (SQLException e) {
            System.out.println("Connection failure.");
            e.printStackTrace();
        }
        return null;

    }

}
