package com.argyriou.di.compiletime.beans;


import com.argyriou.di.beans.definitions.Bean;

@Bean
public class Bean2 {
    public String sayHi() {
       return "Compile time injection works";
    }
}
