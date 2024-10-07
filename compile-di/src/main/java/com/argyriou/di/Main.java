package com.argyriou.di;

import com.argyriou.di.compiletime.DIProcessor;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        DIProcessor processor = new DIProcessor();
        processor.process(null, null);
    }
}