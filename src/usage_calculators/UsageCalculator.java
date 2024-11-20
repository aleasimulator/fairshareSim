/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package usage_calculators;

import fairshare_simulator.Job;

/**
 * This class contains various implementations of the usage metrics (both single- and multi-resource oriented).
 * @author Dalibor Klusacek
 */
public final class UsageCalculator {

    /**
     * This method calculates job's resource usage using Processor Equivalent (PE) formula (CPU+RAM).
     * @param job a job that needs the calculation
     * @return calculated usage
     */
    public static int calculateUsagePE_CPU_RAM(Job job) {
        long total_ram = 223364771316L;
        int total_cpus = 23968;
        long ram_used = job.getRam();
        Long ur = Math.round((ram_used / (total_ram*1.0)) * total_cpus);
        int usage_RAM = Integer.valueOf(ur.intValue());
                
        return usage_RAM + job.getCpus();
        /*
        set server resources_default.infrastructure_mem = 223364771316kb
        set server resources_default.infrastructure_ncpus = 23968
        set server resources_default.infrastructure_ngpus = 300
        set server resources_default.infrastructure_scratch_local = 2358257817496kb
        set server resources_default.infrastructure_scratch_shared = 145424034734080kb
        set server resources_default.infrastructure_scratch_ssd = 1555866402672kb
         */
    }
    
    /**
     * This method calculates job's resource usage using Processor Equivalent formula (CPU only).
     * @param job a job that needs the calculation
     * @return calculated usage
     */
    public static int calculateUsagePE_CPU(Job job) {        
        return job.getCpus();
     }
    
    /**
     * This method calculates job's resource usage using Processor Equivalent formula (CPU+RAM+GPU).
     * @param job a job that needs the calculation
     * @return calculated usage
     */
    public static int calculateUsagePE_CPU_RAM_GPU(Job job) {
        long total_ram = 223364771316L;
        int total_cpus = 23968;
        int total_gpus = 300;
        int gpu_used = job.getGPUs();
        long ram_used = job.getRam();
        Long ur = Math.round((ram_used / (total_ram*1.0)) * total_cpus);
        int usage_RAM = Integer.valueOf(ur.intValue());
        Long ug = Math.round((gpu_used / (total_gpus*1.0)) * total_cpus);
        int usage_GPU = Integer.valueOf(ug.intValue());
        if(gpu_used>0)
        System.out.println(job.getUsername()+": GPU usage: " + usage_GPU + " " + gpu_used+" eqv:"+((gpu_used / (total_gpus*1.0)) * total_cpus));
        return usage_RAM + usage_GPU + job.getCpus();     
    }
}
