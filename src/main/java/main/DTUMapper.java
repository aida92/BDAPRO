package main;

import java.util.ArrayList;
import java.util.Arrays;

public class DTUMapper {
    public ArrayList<Double> priceList = new ArrayList<Double>();

    public static void main(String args[])
    {
        map();
    }

    public static void map(){
        double dtu = 40;
        double storage = 10;

        //array that stores a table for the dbaas pricing
        //each row includes {DTU, Storage, Price/hour}
        double azureTable[][] = {
                {50,5,0.0851},{100,10,0.1701},{200,20,0.3402},{300,29,0.5102},{400,39,0.6803},
                {800, 78, 1.3606},{1200, 117, 2.0408},{1600,156,2.7211},{50,50, 0.1274},
                {100,100,0.2548}, {200,200,0.5095}, {300,300, 0.7643}, {400,400,1.019},
                {800,800,2.038}
        };

        ArrayList<Double> priceList = new ArrayList<Double>();

        int temp =0;
        for (int i =0; i<9; i++)
        {
            if (azureTable[i][0]>= dtu)
            {
                if (azureTable[i][1]>= storage)
                {
                    priceList.add(azureTable[i][2]);
                    temp++;
                }
            }
        }


        System.out.println("***************************");

        for (int m=0; m< priceList.size();m++)
        {
            System.out.println(priceList.get(m));
        }

    }

}

