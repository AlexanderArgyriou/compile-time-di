package com.argyriou.di;

import com.argyriou.di.Server.VirtualOctopusServer;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        var virtualOctopusServer =
                new VirtualOctopusServer(9090, "/");
        virtualOctopusServer.start();
    }
}