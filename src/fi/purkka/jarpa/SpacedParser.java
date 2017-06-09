package fi.purkka.jarpa;

import static fi.purkka.jarpa.JarpaArgs.DEFAULT_ARGUMENT;
import static fi.purkka.jarpa.JarpaArgs.EMPTY_ARRAY;

/** A parses that assumes that arguments and their values are
 * separated by spaces. For example, the following input
 * 
 * <p>{@code -arg val1 val2 -arg2 }</p>
 * 
 * <p>would be interpreted such that {@code val1} and {@code val2}
 * are values given to {@code -arg}.</p>
 * 
 * <p>Values declared before any arguments form the <i>default
 * arguments</i>, accessible through the empty string {@code ""}. In
 * this example, {@code val1} and {@code val2} are the values of the
 * default argument.</p>
 *  
 * <p>{@code val1 val2 -arg}</p> */
public class SpacedParser extends JarpaParser {
	
	private String[] args;
	private int index = 0;
	
	SpacedParser(String[] args) {
		this.args = args;
	}
	
	/** Parses the given arguments.
	 * @see SpacedParser */
	@Override
	public JarpaArgs parse() {
		JarpaArgs jargs = new JarpaArgs();
		if(!isArgument(args[0])) {
			jargs.values.put(DEFAULT_ARGUMENT, nextValue());
		}
		
		while(index < args.length) {
			String arg = args[index];
			index++;
			jargs.values.put(arg, nextValue());
		}
		return jargs;
	}
	
	private String[] nextValue() {
		if(index >= args.length) { return EMPTY_ARRAY; }
		int start = index;
		
		while(index < args.length && !isArgument(args[index])) {
			index++;
		}
		
		int end = index;
		int len = end - start;
		String[] ret = new String[len];
		System.arraycopy(args, start, ret, 0, len);
		return ret;
	}
}