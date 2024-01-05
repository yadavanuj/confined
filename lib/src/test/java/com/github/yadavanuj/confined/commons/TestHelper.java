package com.github.yadavanuj.confined.commons;

public class TestHelper {
    private final String serviceKeyFormat;

    public TestHelper(String serviceKeyFormat) {
        this.serviceKeyFormat = serviceKeyFormat;
    }

    public String getServiceKey(int serviceId) {
        return String.format(serviceKeyFormat, serviceId);
    }
}
