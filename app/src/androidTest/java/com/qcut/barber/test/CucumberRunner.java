package com.qcut.barber.test;

import com.qcut.barber.util.LogUtils;

import org.junit.Test;
import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@CucumberOptions(features = "features", glue = "com.qcut.barber.steps")
@RunWith(Cucumber.class)
public class CucumberRunner {

    @Test
    public void testUseAppContext() {
        LogUtils.info("CucumberRunner test executed...");
    }
}