package com.example.beans;

import com.example.annotations.AutoInjectable;
import com.example.interfaces.SomeInterface;
import com.example.interfaces.SomeOtherInterface;

public class SomeBean {
    @AutoInjectable
    private SomeInterface field1;
    @AutoInjectable
    private SomeOtherInterface field2;

    public void foo() {
        field1.doSomething();
        field2.doSomeOther();
    }
}