package main;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.CentralProcessor.TickType;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HWPartition;
import oshi.util.FormatUtil;
import oshi.util.Util;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class PerformanceMonitor { //extends TimerTask

    SystemInfo systemInfo;
    CentralProcessor cpu;
    GlobalMemory memory;
    HWDiskStore[] disks;


    public PerformanceMonitor() {
        systemInfo = new SystemInfo();

        cpu = systemInfo.getHardware().getProcessor();
        memory = systemInfo.getHardware().getMemory();
        disks = systemInfo.getHardware().getDiskStores();
    }

//    @Override
//    public void run() {
//        this.printStats();
//    }

    public void printGeneralInfo() {
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

    private static void printCpu(CentralProcessor processor) {
        System.out.println("Uptime: " + FormatUtil.formatElapsedSecs(processor.getSystemUptime()));

        long[] prevTicks = processor.getSystemCpuLoadTicks();
        System.out.println("CPU, IOWait, and IRQ ticks @ 0 sec:" + Arrays.toString(prevTicks));
        // Wait a second...
        Util.sleep(1000);
        long[] ticks = processor.getSystemCpuLoadTicks();
        System.out.println("CPU, IOWait, and IRQ ticks @ 1 sec:" + Arrays.toString(ticks));
        long user = ticks[TickType.USER.getIndex()] - prevTicks[TickType.USER.getIndex()];
        long nice = ticks[TickType.NICE.getIndex()] - prevTicks[TickType.NICE.getIndex()];
        long sys = ticks[TickType.SYSTEM.getIndex()] - prevTicks[TickType.SYSTEM.getIndex()];
        long idle = ticks[TickType.IDLE.getIndex()] - prevTicks[TickType.IDLE.getIndex()];
        long iowait = ticks[TickType.IOWAIT.getIndex()] - prevTicks[TickType.IOWAIT.getIndex()];
        long irq = ticks[TickType.IRQ.getIndex()] - prevTicks[TickType.IRQ.getIndex()];
        long softirq = ticks[TickType.SOFTIRQ.getIndex()] - prevTicks[TickType.SOFTIRQ.getIndex()];
        long steal = ticks[TickType.STEAL.getIndex()] - prevTicks[TickType.STEAL.getIndex()];
        long totalCpu = user + nice + sys + idle + iowait + irq + softirq + steal;

        System.out.format(
                "User: %.1f%% Nice: %.1f%% System: %.1f%% Idle: %.1f%% IOwait: %.1f%% IRQ: %.1f%% SoftIRQ: %.1f%% Steal: %.1f%%%n",
                100d * user / totalCpu, 100d * nice / totalCpu, 100d * sys / totalCpu, 100d * idle / totalCpu,
                100d * iowait / totalCpu, 100d * irq / totalCpu, 100d * softirq / totalCpu, 100d * steal / totalCpu);
        System.out.format("CPU load: %.1f%% (counting ticks)%n", processor.getSystemCpuLoadBetweenTicks() * 100);
        System.out.format("CPU load: %.1f%% (OS MXBean)%n", processor.getSystemCpuLoad() * 100);
        double[] loadAverage = processor.getSystemLoadAverage(3);
        System.out.println("CPU load averages:" + (loadAverage[0] < 0 ? " N/A" : String.format(" %.2f", loadAverage[0]))
                + (loadAverage[1] < 0 ? " N/A" : String.format(" %.2f", loadAverage[1]))
                + (loadAverage[2] < 0 ? " N/A" : String.format(" %.2f", loadAverage[2])));
        // per core CPU
        StringBuilder procCpu = new StringBuilder("CPU load per processor:");
        double[] load = processor.getProcessorCpuLoadBetweenTicks();
        for (double avg : load) {
            procCpu.append(String.format(" %.1f%%", avg * 100));
        }
        System.out.println(procCpu.toString());
    }


    private static void printDisks(HWDiskStore[] diskStores) {
        System.out.println("Disks:");
        for (HWDiskStore disk : diskStores) {
            boolean readwrite = disk.getReads() > 0 || disk.getWrites() > 0;
            System.out.format(" %s: (model: %s - S/N: %s) size: %s, reads: %s (%s), writes: %s (%s), xfer: %s ms%n",
                    disk.getName(), disk.getModel(), disk.getSerial(),
                    disk.getSize() > 0 ? FormatUtil.formatBytesDecimal(disk.getSize()) : "?",
                    readwrite ? disk.getReads() : "?", readwrite ? FormatUtil.formatBytes(disk.getReadBytes()) : "?",
                    readwrite ? disk.getWrites() : "?", readwrite ? FormatUtil.formatBytes(disk.getWriteBytes()) : "?",
                    readwrite ? disk.getTransferTime() : "?");
            HWPartition[] partitions = disk.getPartitions();
            if (partitions == null) {
                // TODO Remove when all OS's implemented
                continue;
            }
            for (HWPartition part : partitions) {
                System.out.format(" |-- %s: %s (%s) Maj:Min=%d:%d, size: %s%n", part.getIdentification(),
                        part.getName(), part.getType(), part.getMajor(), part.getMinor(),
                        FormatUtil.formatBytesDecimal(part.getSize()),
                        (part.getMountPoint().isEmpty()) ? "" : (" @ " + part.getMountPoint()));

            }
        }
    }

    public void printStats() {
        System.out.println("Processor info:");

        printCpu(cpu);

        //System.out.println("Memory info:");

        System.out.println("Disk info:");
        printDisks(disks);
    }


    public static void main(String[] args) {
        PerformanceMonitor monitor = new PerformanceMonitor();

        //monitor.printGeneralInfo();
        //monitor.printStats();

        //Timer timer = new Timer();
       // timer.schedule(monitor, 0, 5000);

    }
}
