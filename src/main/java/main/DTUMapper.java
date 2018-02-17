package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class DTUMapper {
    public ArrayList<Double> priceList = new ArrayList<Double>();

    public static void main(String args[])
    {
        //map();
    }

    public static double map(double DTU, double Storage){
//        double dtu = 40;
  //      double storage = 10;

        //array that stores a table for the dbaas pricing
        //each row includes {DTU, Storage, Price/hour}
        //only basic and standard
        double azureTable[][] = {
                {50,5,0.0851},{100,10,0.1701},{200,20,0.3402},{300,29,0.5102},{400,39,0.6803},
                {800, 78, 1.3606},{1200, 117, 2.0408},{1600,156,2.7211},{50,50, 0.1274},
                {100,100,0.2548}, {200,200,0.5095}, {300,300, 0.7643}, {400,400,1.019},
                {800,800,2.038}, {1200, 1170, 3.057}, {1600, 1560, 4.076}, {2000, 1950, 5.095},
                {2500,2440, 6.3687}, {3000,2930, 7.6425}, {125, 250, 0.7906}, {250, 500, 1.5812},
                {500,750, 3.1624}, {1000,1000, 6.3248}, {1500, 1500, 9.4872}, {2000,2000,12.65},
                {2500,2500, 15.82}, {3000,3000, 18.98}, {3500, 3500, 22.14}, {4000, 4000, 25.30}
        };

        ArrayList<Double> priceList = new ArrayList<Double>();

        int temp =0;
        for (int i =0; i<azureTable.length; i++)
        {
            if (azureTable[i][0]>= DTU)
            {
                if (azureTable[i][1]>= Storage)
                {
                    priceList.add(azureTable[i][2]);
                    temp++;
                }
            }
        }


        System.out.println("***************************");

        //for (int m=0; m< priceList.size();m++)
        //{
         //   System.out.println(priceList.get(m));
        //}

        return Collections.min(priceList);

    }

}