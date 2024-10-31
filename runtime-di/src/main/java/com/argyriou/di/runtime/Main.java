package com.argyriou.di.runtime;

import com.argyriou.di.runtime.server.VirtualOctopusServer;

import java.io.IOException;

public class Main {
    public static int RESTART_TIMES_PER_TEST = 50_000;

    public static void main(String[] args) throws IOException {
//        var virtualOctopusServer =
//                new VirtualOctopusServer(9090, "/");
//        virtualOctopusServer.start();

        var virtualOctopusServer =
                new VirtualOctopusServer(9090, "/");

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < RESTART_TIMES_PER_TEST; i++) {
            virtualOctopusServer.loadContext();
        }
        long endTime = System.currentTimeMillis();

        double elapsedTimeInSeconds = (endTime - startTime) * 0.001;
        System.out.println("Total time = : " + elapsedTimeInSeconds + " sec");
    }
}