package fi.purkka.jarpa;

import static fi.purkka.jarpa.JarpaArg.integer;
import static fi.purkka.jarpa.JarpaArg.integerArray;
import static fi.purkka.jarpa.conditions.IntCondition.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.stream.Stream;

import org.hamcrest.Matcher;
import org.junit.Test;

import fi.purkka.jarpa.JarpaException.Type;
import fi.purkka.jarpa.conditions.Condition;
import fi.purkka.jarpa.conditions.IntCondition;

public class TestConditions {
	
	private static Type intArg(int i, IntCondition cond) {
		try(JarpaArgs jargs = JarpaParser
				.parsing(new String[] {"--int", ""+i}).parse()) {
			jargs.get(integer("--int").require(cond));
			return null;
		} catch(JarpaException e) {
			return e.type;
		}
	}
	
	private static Type intArrayArg(int[] arr, Condition<int[]> cond) {
		try(JarpaArgs jargs = JarpaParser
				.parsing(
						Stream.concat(Stream.of("--int"), 
						Arrays.stream(arr)
						.mapToObj(i -> ""+i))
						.toArray(String[]::new)
						).parse()) {
			jargs.get(integerArray("--int").require(cond));
			return null;
		} catch(JarpaException e) {
			return e.type;
		}
	}
	
	private final static Matcher<Type> FAILS = is(Type.CONDITION_FAILED);
	private final static Matcher<Object> SUCCEEDS = nullValue();
	
	@Test
	public void testPositive() {
		assertThat(intArg(-5, positive()), FAILS);
		assertThat(intArg(0, positive()), FAILS);
		assertThat(intArg(5, positive()), SUCCEEDS);
	}
	
	@Test
	public void testNegative() {
		assertThat(intArg(-5, negative()), SUCCEEDS);
		assertThat(intArg(0, negative()), FAILS);
		assertThat(intArg(5, negative()), FAILS);
	}
	
	@Test
	public void testNonZero() {
		assertThat(intArg(-5, nonZero()), SUCCEEDS);
		assertThat(intArg(0, nonZero()), FAILS);
		assertThat(intArg(5, nonZero()), SUCCEEDS);
	}
	
	@Test
	public void testInRange() {
		assertThat(intArg(7, inRange(5, 10)), SUCCEEDS);
		assertThat(intArg(3, inRange(5, 10)), FAILS);
		assertThat(intArg(14, inRange(5, 10)), FAILS);
		
		try {
			inRange(10, 5);
		} catch(JarpaException e) {
			assertThat(e.type, is(Type.INVALID_CONDITION));
		}
	}
	
	@Test
	public void testWholeIntArray() {
		assertThat(intArrayArg(new int[] { 4, -2, 6 }, wholeArray(positive())), FAILS);
		assertThat(intArrayArg(new int[] { 4, -2, 6 }, wholeArray(negative())), FAILS);
		assertThat(intArrayArg(new int[] { 4, -2, 6 }, wholeArray(nonZero())), SUCCEEDS);
	}
	
	@Test
	public void testAndMessage() {
		try {
			JarpaArgs jargs = JarpaParser.parsing(new String[]{ "-a", "6" }).parse();
			jargs.get(integer("-a").require(nonZero().and(negative())));
		} catch(JarpaException e) {
			assertThat(e.getMessage(), is("Illegal value for -a: must be"
					+ " nonzero and must be negative"));
		}
	}
}