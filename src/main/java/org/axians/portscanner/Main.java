package org.axians.portscanner;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    private static String ip = null;
    private static int startPort = 0;
    private static int endPort = 0;

    public static void main(String[] args) {
        if (args.length < 3) {
            throw new IllegalArgumentException("At least 3 arguments are required.");
        } else {
            ip = args[0];
            startPort = Integer.parseInt(args[1]);
            endPort = Integer.parseInt(args[2]);
        }
        List openPorts = scanPorts(ip, startPort,endPort);
        openPorts.forEach(port -> System.out.println("port " + port + " is open"));
    }

    public static List scanPorts(String ip, int startPort , int endPort )  {
        if(startPort == 0) {
            throw new IllegalArgumentException("Start port is required");
        }

        if(endPort == 0) {
            throw new IllegalArgumentException("End port is required");
        }

        if(endPort == startPort) {
            throw new IllegalArgumentException("Start port is always less than end port");
        }

        if(ip == null) {
            throw new IllegalArgumentException("IP field is required");
        }
        //Concurrently add to linked Queue
        ConcurrentLinkedQueue openPorts = new ConcurrentLinkedQueue<>();
        // 50 Threads will open
        ExecutorService executorService = Executors.newFixedThreadPool(50);

        AtomicInteger port = new AtomicInteger(startPort);

        while (port.get() < endPort) {
            final int currentPort = port.getAndIncrement();
            executorService.submit(() -> {
                try {
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(ip, currentPort), 200);
                    socket.close();
                    openPorts.add(currentPort);
                    System.out.println(ip + " ,port open: " + currentPort);
                }
                catch (IOException e) {
                }

            });
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(10, TimeUnit.MINUTES);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        List openPortList = new ArrayList();
        System.out.println("openPortsQueue: " + openPorts.size());
        while (!openPorts.isEmpty()) {
            openPortList.add(openPorts.poll());
        }
        return openPortList;
    }
}