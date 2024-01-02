package com.github.yadavanuj.confined.bulkhead;
public class BulkHeadException extends RuntimeException {
    public BulkHeadException(Exception e) {
        super(e);
    }
}
