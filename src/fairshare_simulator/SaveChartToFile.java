/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fairshare_simulator;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartUtils;

/**
 * This class stores a chart into a PNG graphic file.
 * @author Dalibor Klusacek
 */
public final class SaveChartToFile {

    /**
     * Saves generated charts into a PNG file.
     * @param chart_name name of the file
     * @param chart chart to be saved
     * @param width width of the PNG
     * @param height height of the PNG
     */
    public static synchronized void save(String chart_name, JFreeChart chart, int width, int height) {
        
        
        String suffix = "";
        if(Fairshare_Simulator.use_decay){
            suffix = "-decaying-"+Fairshare_Simulator.decay_factor+"-"+Math.round(Fairshare_Simulator.decay_period/3600.0)+"h";
        }
        try {
            File file = new File(chart_name + suffix + ".png");
            System.out.println("Saving to PNG chart: " + chart_name + " with size: " + width + " x " + height + " pixels.");
            ChartUtils.saveChartAsPNG(file, chart, width, height);
        } catch (IOException ex) {
            Logger.getLogger(SaveChartToFile.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
