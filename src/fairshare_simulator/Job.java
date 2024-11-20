/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fairshare_simulator;

/**
 * This class represents a job of a user.
 * @author Dalibor Klusacek
 */
public class Job {

    private int id;
    private long arrival;
    private long wait;
    private long start;
    private long runtime;
    private long end;
    private int cpus;
    private int GPUs;
    private long ram;
    private String nodes;
    private String queue;
    private int queueID;
    private long walltime;
    private String user;
    private String username;
    private String cluster;
    private double SPEC;
    
    /**
     * This constructor creates an instance of Job representing one job.
     * @param id id of the job.
     * @param arrival arrival time
     * @param wait job's wait time
     * @param duration job's execution time
     * @param cpus number of allocated CPUs
     * @param ram allocated RAM per core in KB
     * @param nodes number of nodes used
     * @param queue name of the job queue
     * @param user ID of the user (job owner)
     * @param GPUs number of used GPUs
     * @param username name of the user (job owner)
     */

    public Job(int id, long arrival, long wait, long duration, int cpus, long ram, String nodes, String queue, String user, int GPUs, String username) {
        this.id = id;
        this.arrival = arrival;
        this.wait = wait;
        this.start = arrival + wait;
        this.runtime = duration;
        this.end = start + duration;
        this.cpus = cpus;
        this.GPUs = GPUs;
        this.ram = ram * cpus;
        this.nodes = nodes;
        this.queue = queue;
        this.user = user;
        this.username = username;
        if (queue.equals("short")) {
            walltime = 7200;
        } else if (queue.equals("long")) {
            walltime = 2592000;
        } else if (queue.equals("normal")) {
            walltime = 86400;
        } else {
            walltime = 86400;
        }
        this.cluster = nodes;
        this.SPEC = 4.44;
        //System.out.println("ram: "+ram);

        if (Fairshare_Simulator.useSQL) {
            setNameForCluster(nodes);
        }
        setSPECforCluster(nodes);
        //System.out.println(id + ": " + nodes + " " + this.SPEC);

    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the arrival
     */
    public long getArrival() {
        return arrival;
    }

    /**
     * @param arrival the arrival to set
     */
    public void setArrival(long arrival) {
        this.arrival = arrival;
    }

    /**
     * @return the wait
     */
    public long getWait() {
        return wait;
    }

    /**
     * @param wait the wait to set
     */
    public void setWait(long wait) {
        this.wait = wait;
    }

    /**
     * @return the start
     */
    public long getStart() {
        return start;
    }

    /**
     * @param start the start to set
     */
    public void setStart(long start) {
        this.start = start;
    }

    /**
     * @return the runtime
     */
    public long getRuntime() {
        return runtime;
    }

    /**
     * @param runtime the runtime to set
     */
    public void setRuntime(long runtime) {
        this.runtime = runtime;
    }

    /**
     * @return the end
     */
    public long getEnd() {
        return end;
    }

    /**
     * @param end the end to set
     */
    public void setEnd(long end) {
        this.end = end;
    }

    /**
     * @return the cpus
     */
    public int getCpus() {
        return cpus;
    }

    /**
     * @param cpus the cpus to set
     */
    public void setCpus(int cpus) {
        this.cpus = cpus;
    }

    /**
     * @return the ram
     */
    public long getRam() {
        return ram;
    }

    /**
     * @param ram the ram to set
     */
    public void setRam(long ram) {
        this.ram = ram;
    }

    /**
     * @return the nodes
     */
    public String getNodes() {
        return nodes;
    }

    /**
     * @param nodes the nodes to set
     */
    public void setNodes(String nodes) {
        this.nodes = nodes;
    }

    /**
     * @return the queue
     */
    public String getQueue() {
        return queue;
    }

    /**
     * @param queue the queue to set
     */
    public void setQueue(String queue) {
        this.queue = queue;
    }

    /**
     * @return the walltime
     */
    public long getWalltime() {
        return walltime;
    }

    /**
     * @param walltime the walltime to set
     */
    public void setWalltime(long walltime) {
        this.walltime = walltime;
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @return the GPUs
     */
    public int getGPUs() {
        return GPUs;
    }

    /**
     * @param GPUs the GPUs to set
     */
    public void setGPUs(int GPUs) {
        this.GPUs = GPUs;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the cluster
     */
    public String getCluster() {
        return cluster;
    }

    /**
     * @param cluster the cluster to set
     */
    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    /**
     * @return the SPEC
     */
    public double getSPEC() {
        return SPEC;
    }

    /**
     * @param SPEC the SPEC to set
     */
    public void setSPEC(double SPEC) {
        this.SPEC = SPEC;
    }

    public void setSPECforCluster(String nodes) {
        if (nodes.equals("1")) {
            this.SPEC = 8.0;
        }
        if (nodes.equals("2")) {
            this.SPEC = 6.9;
        }
        if (nodes.equals("3")) {
            this.SPEC = 3.7;
        }
        if (nodes.equals("4")) {
            this.SPEC = 4.1;
        }
        if (nodes.equals("5")) {
            this.SPEC = 2.9;
        }
        if (nodes.equals("6")) {
            this.SPEC = 5.3;
        }
        if (nodes.equals("7")) {
            this.SPEC = 7.0;
        }
        if (nodes.equals("8")) {
            this.SPEC = 3.8;
        }
        if (nodes.equals("9")) {
            this.SPEC = 1.8;
        }
        if (nodes.equals("10")) {
            this.SPEC = 3.2;
        }
        if (nodes.equals("11")) {
            this.SPEC = 4.2;
        }
        if (nodes.equals("12")) {
            this.SPEC = 8.0;
        }
        if (nodes.equals("13")) {
            this.SPEC = 5.1;
        }
        if (nodes.equals("14")) {
            this.SPEC = 5.2;
        }
        if (nodes.equals("15")) {
            this.SPEC = 5.9;
        }
        if (nodes.equals("16")) {
            this.SPEC = 3.4;
        }
        if (nodes.equals("17")) {
            this.SPEC = 3.9;
        }
        if (nodes.equals("18")) {
            this.SPEC = 4.8;
        }
        if (nodes.equals("19")) {
            this.SPEC = 4.1;
        }
        if (nodes.equals("20")) {
            this.SPEC = 4.1;
        }
        if (nodes.equals("21")) {
            this.SPEC = 3.8;
        }
        if (nodes.equals("22")) {
            this.SPEC = 4.3;
        }
        if (nodes.equals("23")) {
            this.SPEC = 5.2;
        }
        if (nodes.equals("24")) {
            this.SPEC = 5.1;
        }
        if (nodes.equals("25")) {
            this.SPEC = 2.6;
        }
        if (nodes.equals("26")) {
            this.SPEC = 3.4;
        }
        if (nodes.equals("27")) {
            this.SPEC = 9.1;
        }
        if (nodes.equals("28")) {
            this.SPEC = 6.7;
        }
        if (nodes.equals("29")) {
            this.SPEC = 4.1;
        }
        if (nodes.equals("30")) {
            this.SPEC = 10.4;
        }
        if (nodes.equals("31")) {
            this.SPEC = 3.4;
        }
        if (nodes.equals("32")) {
            this.SPEC = 3.3;
        }
        if (nodes.equals("33")) {
            this.SPEC = 3.4;
        }
        if (nodes.equals("34")) {
            this.SPEC = 3.3;
        }
        if (nodes.equals("35")) {
            this.SPEC = 3.3;
        }
        if (nodes.equals("36")) {
            this.SPEC = 6.6;
        }
        if (nodes.equals("37")) {
            this.SPEC = 5.8;
        }
        if (nodes.equals("38")) {
            this.SPEC = 5.5;
        }
        if (nodes.equals("39")) {
            this.SPEC = 4.1;
        }
        if (nodes.equals("40")) {
            this.SPEC = 4.9;
        }
        if (nodes.equals("41")) {
            this.SPEC = 5.2;
        }
        if (nodes.equals("42")) {
            this.SPEC = 4.1;
        }
        if (nodes.equals("43")) {
            this.SPEC = 5.0;
        }
        if (nodes.equals("44")) {
            this.SPEC = 1.6;
        }
        if (nodes.equals("45")) {
            this.SPEC = 8.1;
        }
        if (nodes.equals("46")) {
            this.SPEC = 8.1;
        }
        if (nodes.equals("47")) {
            this.SPEC = 4.0;
        }
    }

    public void setNameForCluster(String nodes) {
        int index = 6;
        if (nodes.length() == 5) {
            index = 4;
        }
        if (nodes.length() == 6) {
            index = 5;
        }
        this.cluster = nodes.substring(1, index);
        //fix short cluster names
        if (nodes.startsWith("{luna10")) {
            this.cluster = "luna2021";
            this.SPEC = 7.4;
        } else if (nodes.startsWith("{luna20")) {
            this.cluster = "luna2022";
            this.SPEC = 7.2;
        } else if (nodes.startsWith("{luna")) {
            this.cluster = "luna2019";
            this.SPEC = 3.9;
        } else if (nodes.startsWith("{adan")) {
            this.cluster = "adan";
            this.SPEC = 5.9;
        } else if (nodes.startsWith("{krux")) {
            this.cluster = "krux";
            this.SPEC = 1;
        } else if (nodes.startsWith("{aman")) {
            this.cluster = "aman";
            this.SPEC = 2.6;
        } else if (nodes.startsWith("{phi")) {
            this.cluster = "phi";
            this.SPEC = 1.1;
        } else if (nodes.startsWith("{zia")) {
            this.cluster = "zia";
            this.SPEC = 4.1;
        } else if (nodes.startsWith("{gita")) {
            this.cluster = "gita";
            this.SPEC = 10.4;
        } else if (nodes.startsWith("{aman")) {
            this.cluster = "aman";
            this.SPEC = 2.6;
        } else if (nodes.startsWith("{ida")) {
            this.cluster = "ida";
            this.SPEC = 3.3;
        } else if (nodes.startsWith("{elan")) {
            this.cluster = "elan";
            this.SPEC = 5.2;
        } else if (nodes.startsWith("{cha")) {
            this.cluster = "cha";
            this.SPEC = 5.5;
        } else if (nodes.startsWith("{mor")) {
            this.cluster = "mor";
            this.SPEC = 5.8;
        } else if (nodes.startsWith("{pcr")) {
            this.cluster = "pcr";
            this.SPEC = 5.1;
        } else if (nodes.startsWith("{fau")) {
            this.cluster = "fau";
            this.SPEC = 5.1;
        } else if (nodes.startsWith("{fer")) {
            this.cluster = "fer";
            this.SPEC = 6.6;
        } else if (nodes.startsWith("{lex")) {
            this.cluster = "lex";
            this.SPEC = 3.5;
        } else if (nodes.startsWith("{eltu")) {
            this.cluster = "eltu";
            this.SPEC = 5.3;
        } else if (nodes.startsWith("{elwe")) {
            this.cluster = "elwe";
            this.SPEC = 7;
        }
    }
}
