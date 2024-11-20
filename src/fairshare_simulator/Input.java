package fairshare_simulator;

import java.io.*;
import java.util.*;

/**
 * Class Input that reads data from specified file.
 *
 * @author Dalibor Klusacek
 */
public class Input {

    BufferedReader br;

    /**
     * Opens a specified file.
     *
     * @param f the File
     * @return BufferedReader that can be used to read the lines from the file.
     */
    public BufferedReader openFile(File f) {
        BufferedReader br = null;
        try {
            FileInputStream fr = new FileInputStream(f.getAbsoluteFile());
            InputStreamReader ifr = new InputStreamReader(fr, "Cp1250");
            br = new BufferedReader(ifr);
            return br;
        } catch (IOException ioe) {
            //ioe.printStackTrace();
            System.out.println("Fail to open file!");
        }
        return br;
    }

    /**
     * Closes file mapped to the buffered reader.
     *
     * @param br BufferedReader to be closed.
     */
    public void closeFile(BufferedReader br) {
        try {
            br.close();
        } catch (IOException ioe) {
            //ioe.printStackTrace();}
            System.out.println("Fail to close file!");
        }
    }

}
