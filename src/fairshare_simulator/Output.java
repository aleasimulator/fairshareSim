package fairshare_simulator;

import java.io.*;

/**
 * Class Output is used to store results into a text file.
 *
 * @author Dalibor Klusacek
 */
public class Output {

    /**
     * This methods writes out string "value" into file "s".
     *
     * @param s file to which will be written.
     * @param value a string to be written.
     * @throws IOException
     */
    public void writeString(String s, String value)
            throws IOException {

        PrintWriter pw = new PrintWriter(new FileWriter(s, true));
        pw.println(value);
        pw.close();
    }

    /**
     * This method deletes the contents of the file.
     *
     * @param s the file to be deleted.
     * @throws IOException
     */
    public void deleteResults(String s) throws IOException {

        PrintWriter pw = new PrintWriter(new FileWriter(s));
        //PrintWriter pw = new PrintWriter(s);
        pw.close();
    }
}
