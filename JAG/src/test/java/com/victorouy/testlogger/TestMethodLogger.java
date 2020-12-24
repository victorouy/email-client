package com.victorouy.testlogger;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class extends TestWatcher used to log information when test methods are performed
 *
 * @author Victor Ouy   13729282
 */
public class TestMethodLogger extends TestWatcher {
    
    private final Logger LOG = LoggerFactory.getLogger(getClass().getName());

    /**
     * Constructor
     */
    public TestMethodLogger() {
        super();
    }

    /**
     * Logging information of the test method when it begins
     *
     * @param description name of Test class
     */
    @Override
    protected void starting(Description description) {
        LOG.info("STARTING TEST: " + description.getMethodName());
    }

//    /**
//     * Logging information of the test method if it fails
//     *
//     * @param throwable
//     * @param description name of Test class
//     */
//    @Override
//    protected void failed(Throwable throwable, Description description) {
//        LOG.error("FAILED TEST: " + description.getMethodName() + "\n" + throwable.getMessage());
//    }
}