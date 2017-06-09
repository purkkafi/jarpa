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

public class TestJarpaParser {
	
	private static JarpaArgs spaced(String args) {
		return JarpaParser.parsing(args.split(" ")).parse();
	}
	
	private static JarpaArgs equals(String args) {
		return JarpaParser.parsing(args.split(" "))
				.equalsSeparated().parse();
	}
	
	@Test(expected=NullPointerException.class)
	public void testNPEOnNullInput() {
		spaced(null);
		equals(null);
	}
	
	@Test
	public void testFlags() {
		try(JarpaArgs args = spaced("-a")) {
			assertTrue(args.get(flag("-a")));
			assertFalse(args.get(flag("-d")));
		}
		
		try(JarpaArgs args = equals("-a")) {
			assertTrue(args.get(flag("-a")));
			assertFalse(args.get(flag("-d")));
		}
	}
	
	@Test
	public void testErrorOnFlagWithArguments() {
		try(JarpaArgs args = spaced("-a 5")) {
			args.get(flag("-a"));
		} catch(JarpaException e) {
			assertThat(e.type, is(Type.FLAG_GIVEN_VALUES));
		}
		
		try(JarpaArgs args = equals("-a=5")) {
			args.get(flag("-a"));
		} catch(JarpaException e) {
			assertThat(e.type, is(Type.FLAG_GIVEN_VALUES));
		}
	}
	
	@Test
	public void testAliases() {
		try(JarpaArgs args = spaced("--foo bar")) {
			assertThat(args.get(JarpaArg.string("-f").alias("--foo")), is("bar"));
		}
		
		try(JarpaArgs args = equals("--foo=bar")) {
			assertThat(args.get(JarpaArg.string("-f").alias("--foo")), is("bar"));
		}
	}
	
	@Test
	public void testErrorOnDoubleAliases() {
		try(JarpaArgs args = spaced("--foo bar -f baz")) {
			assertEquals(args.get(JarpaArg.string("-f").alias("--foo")), "bar");
		} catch(JarpaException e) {
			assertThat(e.type, is(Type.MULTIPLE_ALIASES_PRESENT));
		}
		
		try(JarpaArgs args = equals("--foo=bar -f=baz")) {
			assertEquals(args.get(JarpaArg.string("-f").alias("--foo")), "bar");
		} catch(JarpaException e) {
			assertThat(e.type, is(Type.MULTIPLE_ALIASES_PRESENT));
		}
	}
	
	@Test
	public void testErrorOnMultipleArgumentsWhenNotAllowed() {
		try(JarpaArgs args = spaced("--arg value1 value2")) {
			args.get(string("--arg"));
		} catch(JarpaException e) {
			assertThat(e.type, is(Type.SINGLE_VALUE_EXPECTED));
		}
		
		try(JarpaArgs args = equals("--arg=value1,value2")) {
			args.get(string("--arg"));
		} catch(JarpaException e) {
			assertThat(e.type, is(Type.SINGLE_VALUE_EXPECTED));
		}
	}
	
	@Test
	public void testErrorOnMandatoryArgNotSpecified() {
		try(JarpaArgs args = spaced("-f 1 --goo 2")) {
			args.get(integer("-f"));
			args.get(integer("-g").alias("--goo"));
			args.get(integer("-h"));
		} catch(JarpaException e) {
			assertThat(e.type, is(Type.MANDATORY_ARG_NOT_SPECIFIED));
		}
		
		try(JarpaArgs args = equals("-f=1 --goo=2")) {
			args.get(integer("-f"));
			args.get(integer("-g").alias("--goo"));
			args.get(integer("-h"));
		} catch(JarpaException e) {
			assertThat(e.type, is(Type.MANDATORY_ARG_NOT_SPECIFIED));
		}
	}
	
	@Test
	public void testErrorOnUnknownArguments() {
		try(JarpaArgs args = spaced("-a 1 -b 2 -c 3 -d 4")) {
			args.get(integer("-a"));
			args.get(integer("-b"));
		} catch(JarpaException e) {
			assertThat(e.type, is(Type.UNKNOWN_ARGUMENTS));
		}
		
		try(JarpaArgs args = equals("-a=1 -b=2 -c=3 -d=4")) {
			args.get(integer("-a"));
			args.get(integer("-b"));
		} catch(JarpaException e) {
			assertThat(e.type, is(Type.UNKNOWN_ARGUMENTS));
		}
	}
	
	@Test
	public void testUnknownDefaultArgumentMessage() {
		try(JarpaArgs args = spaced("arg")) {	
		} catch(JarpaException e) {
			assertThat(e.type, is(Type.UNKNOWN_ARGUMENTS));
			assertThat(e.getMessage(), is("Unknown argument [default argument]"));
		}
		
		try(JarpaArgs args = equals("arg")) {	
		} catch(JarpaException e) {
			assertThat(e.type, is(Type.UNKNOWN_ARGUMENTS));
			assertThat(e.getMessage(), is("Unknown argument [default argument]"));
		}
	}
	
	@Test
	public void testParsingDifferentValues() {
		try(JarpaArgs args = spaced("--int 356 --dec 2.65 --strings a b c")) {
			assertThat(args.get(integer("--int")), is(356));
			assertThat(args.get(decimal("--dec")), is(2.65));
			assertThat(args.get(stringArray("--strings")), is(new String[] {"a", "b", "c"}));
		}
		
		try(JarpaArgs args = equals("--int=356 --dec=2.65 --strings=a,b,c")) {
			assertThat(args.get(integer("--int")), is(356));
			assertThat(args.get(decimal("--dec")), is(2.65));
			assertThat(args.get(stringArray("--strings")), is(new String[] {"a", "b", "c"}));
		}
	}
	
	@Test
	public void testNegativeDecimals() {
		try(JarpaArgs args = spaced("-f -5.46")) {
			assertThat(args.get(decimal("-f")), is(-5.46));
		}
		
		try(JarpaArgs args = equals("-f=-5.46")) {
			assertThat(args.get(decimal("-f")), is(-5.46));
		}
	}
	
	@Test
	public void testOwnExceptionOnParseError() {
		try(JarpaArgs args = spaced("--int 646d")) {
			args.get(integer("--int"));
		} catch(JarpaException e) {
			assertThat(e.type, is(Type.PARSE_EXCEPTION));
		}
		
		try(JarpaArgs args = equals("--int=646d")) {
			args.get(integer("--int"));
		} catch(JarpaException e) {
			assertThat(e.type, is(Type.PARSE_EXCEPTION));
		}
	}
	
	@Test
	public void testAutoCloseableOnJarpaArgs() {
		try(JarpaArgs args = spaced("-a -b")) {
			args.get(flag("-a"));
		} catch(JarpaException e) {
			assertThat(e.type, is(Type.UNKNOWN_ARGUMENTS));
		}
	}
	
	@Test
	public void testObjectList() {
		try(JarpaArgs args = spaced("--list 1 2 3")) {
			assertThat(args.get(objectList("--list", Dummy::parse)),
					hasItems(new Dummy(1), new Dummy(2), new Dummy(3)));
		}
		
		try(JarpaArgs args = equals("--list=1,2,3")) {
			assertThat(args.get(objectList("--list", Dummy::parse)),
					hasItems(new Dummy(1), new Dummy(2), new Dummy(3)));
		}
	}
	
	@Test
	public void testDefaultArgsOnEqualsParser() {
		try(JarpaArgs args = equals("def1 -a def2")) {
			args.get(flag("-a"));
			assertThat(args.get(stringList("")), hasItems("def1", "def2"));
		}
	}
	
	@Test
	public void testNegators() {
		try(JarpaArgs args = spaced("-n")) {
			assertThat(args.get(flag("-y").negator("-n")), is(false));
		}
	}
	
	@Test
	public void testExceptionOnFlagAndNegator() {
		try(JarpaArgs args = spaced("-y -n")) {
			args.get(flag("--yes").alias("-y").negators("--no", "-n"));
		} catch(JarpaException e) {
			assertThat(e.type, is(Type.FLAG_AND_NEGATOR_PRESENT));
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