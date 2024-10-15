package com.argyriou.di.beans;

import com.argyriou.di.beans.definitions.Bean;
import com.argyriou.di.beans.definitions.Inject;

@Bean
public class Bean1 {
    @Inject
    private Bean2 bean2;
    @Inject
    private Bean3 bean3;

    public String check() {
        return bean2.sayHi();
    }

    public Bean2 getBean2() {
        return bean2;
    }

    public void setBean2(Bean2 bean2) {
        this.bean2 = bean2;
    }

    public Bean3 getBean3() {
        return bean3;
    }

    public void setBean3(Bean3 bean3) {
        this.bean3 = bean3;
    }
}
