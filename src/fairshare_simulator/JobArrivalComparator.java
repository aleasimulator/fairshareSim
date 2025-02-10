/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fairshare_simulator;

import java.util.Comparator;

/**
 *
 * @author dklus
 */
public class JobArrivalComparator implements Comparator {
    
    /**
     * Compares two gridlets according to their start time
     */
    public int compare(Object o1, Object o2) {
        Job g1 = (Job) o1;
        Job g2 = (Job) o2;
        long priority1 = (long) g1.getArrival();
        long priority2 = (long) g2.getArrival();
        if(priority1 > priority2) return 1;
        if(priority1 == priority2) return 0;
        if(priority1 < priority2) return -1;
        return 0;
    }
    
}
