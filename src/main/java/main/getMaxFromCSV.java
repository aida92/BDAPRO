package main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.DoubleBinaryOperator;

public class getMaxFromCSV {
    public static void main(){
        getMax();
    }

    public static double getMax() {
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        String fileName = "output.csv";
        FileReader fr = null;

        ArrayList<Double> CPUList = null;
        try {
            fr = new FileReader(fileName);
            br = new BufferedReader(fr);

            //skipping first row
            int iteration = 0;
            CPUList = new ArrayList<Double>();

            while ((line = br.readLine()) != null) {

                if (iteration == 0) {
                    iteration++;
                    continue;
                }

                String row = line.toString();
                //System.out.println(row);
                String[] rowSplit = row.split(cvsSplitBy);
                //System.out.println(rowSplit[1]);
                CPUList.add(Double.parseDouble(rowSplit[1]));


            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        double maxCPU = calculateMax(CPUList);
        System.out.println("max cpu = " +maxCPU);
        return maxCPU;
    }

    public static double calculateMax(ArrayList<Double> CPUList)
    {
        return Collections.max(CPUList);
    }
}