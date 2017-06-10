package fi.purkka.jarpa;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	TestJarpaParser.class,
	TestConditions.class
})
public class TestJarpa {}