package fi.purkka.jarpa;

import static fi.purkka.jarpa.JarpaArg.*;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fi.purkka.jarpa.JarpaException.Type;

@SuppressWarnings("resource")
public class TestJarpaParser {
	
	private static JarpaArgs jargs(String args) {
		return JarpaParser.parse(args.split(" "));
	}
	
	@Test(expected=NullPointerException.class)
	public void testNPEOnNullInput() {
		jargs(null);
	}
	
	@Test
	public void testFlags() {
		JarpaArgs args = jargs("-a");
		assertTrue(args.get(flag("-a")));
		assertFalse(args.get(flag("-d")));
		args.finish();
	}
	
	@Test
	public void testErrorOnFlagWithArguments() {
		try {
			JarpaArgs args = jargs("-a 5");
			args.get(flag("-a"));
			args.finish();
		} catch(JarpaException e) {
			assertThat(e.type, is(Type.FLAG_GIVEN_VALUES));
		}
	}
	
	@Test
	public void testAliases() {
		JarpaArgs args = jargs("--foo bar");
		assertThat(args.get(JarpaArg.string("-f").alias("--foo")), is("bar"));
		args.finish();
	}
	
	@Test
	public void testErrorOnDoubleAliases() {
		try {
			JarpaArgs args = jargs("--foo bar -f baz");
			assertEquals(args.get(JarpaArg.string("-f").alias("--foo")), "bar");
			args.finish();
		} catch(JarpaException e) {
			assertThat(e.type, is(Type.MULTIPLE_ALIASES_PRESENT));
		}
	}
	
	@Test
	public void testErrorOnMultipleArgumentsWhenNotAllowed() {
		try {
			JarpaArgs args = jargs("--arg value1 value2");
			args.get(string("--arg"));
			args.finish();
		} catch(JarpaException e) {
			assertThat(e.type, is(Type.SINGLE_VALUE_EXPECTED));
		}
	}
	
	@Test
	public void testErrorOnMandatoryArgNotSpecified() {
		try {
			JarpaArgs args = jargs("-f 1 --goo 2");
			args.get(integer("-f"));
			args.get(integer("-g").alias("--goo"));
			args.get(integer("-h"));
			args.finish();
		} catch(JarpaException e) {
			assertThat(e.type, is(Type.MANDATORY_ARG_NOT_SPECIFIED));
		}
	}
	
	@Test
	public void testErrorOnUnknownArguments() {
		try {
			JarpaArgs args = jargs("-a 1 -b 2 -c 3 -d 4");
			args.get(integer("-a"));
			args.get(integer("-b"));
			args.finish();
		} catch(JarpaException e) {
			assertThat(e.type, is(Type.UNKNOWN_ARGUMENTS));
		}
	}
	
	@Test
	public void testParsingDifferentValues() {
		JarpaArgs args = jargs("--int 356 --dec 2.65 --strings a b c");
		assertThat(args.get(integer("--int")), is(356));
		assertThat(args.get(decimal("--dec")), is(2.65));
		assertThat(args.get(stringList("--strings")), is(new String[] {"a", "b", "c"}));
		args.finish();
	}
	
	@Test
	public void testNegativeDecimals() {
		JarpaArgs args = jargs("-f -5.46");
		assertThat(args.get(decimal("-f")), is(-5.46));
		args.finish();
	}
	
	@Test
	public void testOwnExceptionOnParseError() {
		try {
			JarpaArgs args = jargs("--int 646d");
			args.get(integer("--int"));
			args.finish();
		} catch(JarpaException e) {
			assertThat(e.type, is(Type.PARSE_EXCEPTION));
		}
	}
	
	@Test
	public void testAutoCloseableOnJarpaArgs() {
		try(JarpaArgs args = jargs("-a -b")) {
			args.get(flag("-a"));
		} catch(JarpaException e) {
			assertThat(e.type, is(Type.UNKNOWN_ARGUMENTS));
		}
	}
	
	@Test
	public void testObjectList() {
		try(JarpaArgs args = jargs("--list 1 2 3")) {
			assertThat(args.get(objectList("--list", Dummy::parse)),
					hasItems(new Dummy(1), new Dummy(2), new Dummy(3)));
		}
	}
	
	private static class Dummy {
		
		Dummy(int val) {
			value = val;
		}
		
		private int value;
		
		static Dummy parse(String value) {
			return new Dummy(Integer.parseInt(value));
		}
		
		@Override
		public boolean equals(Object o) {
			return ((Dummy) o).value == value;
		}
		
		@Override
		public int hashCode() { return 1; }
	}
}