package com.argyriou.di.beans;


import com.argyriou.di.beans.definitions.Bean;

@Bean
public class Bean2 {
    public String sayHi() {
       return "Run time injection works";
    }
}
