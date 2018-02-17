package main;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HWPartition;
import oshi.util.FormatUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class PerformanceMonitor implements Runnable {

    private SystemInfo systemInfo;
    private CentralProcessor cpu;
    private GlobalMemory memory;
    private HWDiskStore[] disks;

    private String output = "output.csv";
    private PrintWriter writer;

    private boolean run = true;

    public PerformanceMonitor() {
        systemInfo = new SystemInfo();

        cpu = systemInfo.getHardware().getProcessor();
        memory = systemInfo.getHardware().getMemory();
        disks = systemInfo.getHardware().getDiskStores();

        try {
            writer = new PrintWriter(new File(output));
            writer.write("Timestamp,CPU % load(ticks), CPU % load(OS MX Bean),Reads,Reads(GiB),Writes,Writes(GiB)\n");
        } catch (FileNotFoundException e) {
            System.out.println("Cannot open the file!");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (run) {
            this.printStats();
            try {
                // Sleep for one second
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
        writer.close();
    }

    public void printSystemInfo() {
        System.out.println("Processor: " + cpu.getName());
        System.out.println(cpu.getPhysicalProcessorCount() + " physical cores");
        System.out.println(cpu.getLogicalProcessorCount() + " logical cores");
        System.out.println("Identifier: " + cpu.getIdentifier());
        System.out.println("Processor ID: " + cpu.getProcessorID());

        System.out.println();
        System.out.println("Total memory: " + memory.getTotal());
        System.out.println("Available memory: " + memory.getAvailable());
        System.out.println("Total swap: " + memory.getSwapTotal());

        System.out.println();
        System.out.println("No of disks: " + disks.length);
        int i = 1;
        for (HWDiskStore disk: disks) {
            System.out.println("Disk " + i++);
            System.out.println("Model " + disk.getModel());
            System.out.println("Size: " + disk.getSize() + ", Reads: " + disk.getReads() + ", Writes: " + disk.getWrites());
            int j = 1;
            for (HWPartition partition: disk.getPartitions()) {
                System.out.println("  partition " + (j++) + ": " + partition.getName() +
                        ", maj:min " + partition.getMajor() + ":" + partition.getMinor() +
                        ", Size: " + partition.getSize());
            }
        }
    }

    private void printCpu(CentralProcessor processor) {
        StringBuilder result = new StringBuilder();
        /* CPU load (counting ticks) */
        result.append(String.format("%.1f,", processor.getSystemCpuLoadBetweenTicks() * 100));
        /* CPU load (OS MXBean) */
        result.append(String.format("%.1f,", processor.getSystemCpuLoad() * 100));

        writer.write(result.toString());
    }


    public void printDisks(HWDiskStore[] diskStores) {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (HWDiskStore disk : diskStores) {
            if  (!first)
                result.append(",,");

            first = false;

            System.out.println(diskStores[0].getReads() + " * * *");
            System.out.println(diskStores[0].getWrites() + " * * *");

            boolean readwrite = disk.getReads() > 0 || disk.getWrites() > 0;
            result.append(readwrite ? disk.getReads() + "," : "?,");
            result.append(readwrite ? FormatUtil.formatBytes(disk.getReadBytes()) + "," : "?,");
            result.append(readwrite ? disk.getWrites() + "," : "?,");
            result.append(readwrite ? FormatUtil.formatBytes(disk.getWriteBytes()) + "\n" : "?\n");

            writer.write(result.toString());
        }
    }

    public void printStats() {
        /* Add the timestamp */
        writer.write(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS,").format(new Date()));
        printCpu(cpu);
        printDisks(disks);
    }

    public void setRun(boolean run) {
        this.run = run;
    }


    public static void main(String[] args) {
        PerformanceMonitor monitor = new PerformanceMonitor();
        Scanner s = new Scanner(System.in);
        Thread t = new Thread(monitor);

        System.out.println("Enter 's' to start, 'q' to quit:");
        if (s.next().toLowerCase().equals("s")) {
            System.out.println("=========started=========");
            t.start();
        }

        while (!s.next().toLowerCase().equals("q")) ;

        monitor.run = false;
        t.interrupt();
        System.out.println("==========ended==========");
    }
}