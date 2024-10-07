package com.argyriou.di.beans;

import com.argyriou.di.beans.definitions.Bean;

@Bean
public class Bean1 {
    private Bean2 bean2;

    public String check() {
        return bean2.sayHi();
    }

    public Bean2 getBean2() {
        return bean2;
    }

    public void setBean2(Bean2 bean2) {
        this.bean2 = bean2;
    }
}
