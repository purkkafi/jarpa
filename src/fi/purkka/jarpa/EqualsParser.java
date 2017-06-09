package fi.purkka.jarpa;

import static fi.purkka.jarpa.JarpaArgs.DEFAULT_ARGUMENT;
import static fi.purkka.jarpa.JarpaArgs.EMPTY_ARRAY;

import java.util.ArrayList;
import java.util.List;

/** A parser that assumes that values are given to arguments
 * according to the following format:
 * 
 * <p>{@code -arg=val1,val2,val3,...,valn}</p>
 * 
 * <p>The main practical difference to {@code SpacedParser} is
 * that the <i>default argument</i> gathers all values not given
 * to some other argument. In the following example, {@code d1},
 * {@code d2} and {@code d3} form the values of the default argument.</p>
 * 
 * <p>{@code d1 -arg d2 d3 -arg2=val }</p>
 * 
 * <p>A {@code SpacedParser} would instead interpret that {@code d2}
 * and {@code d3} are values given to {@code -arg}.</p> */
public final class EqualsParser extends JarpaParser {
	
	private String[] args;
	
	EqualsParser(String[] args) {
		this.args = args;
	}
	
	/** Parses the initially given arguments as described in
	 * the documentation of this class.
	 * @see EqualsParser */
	@Override
	public JarpaArgs parse() {
		JarpaArgs jargs = new JarpaArgs();
		List<String> defaults = new ArrayList<>();
		
		for(String str : args) {
			if(isArgument(str)) {
				parseArg(str, jargs);
			} else {
				defaults.add(str);
			}
		}
		
		if(!defaults.isEmpty()) {
			jargs.values.put(DEFAULT_ARGUMENT,
					defaults.toArray(new String[defaults.size()]));
		}
		
		return jargs;
	}
	
	private static void parseArg(String str, JarpaArgs jargs) {
		if(str.indexOf('=') != -1) {
			String[] parts = str.split("=", 2);
			String arg = parts[0];
			String[] values = parts[1].split(",");
			
			jargs.values.put(arg, values);
		} else {
			jargs.values.put(str, EMPTY_ARRAY);
		}
	}
}