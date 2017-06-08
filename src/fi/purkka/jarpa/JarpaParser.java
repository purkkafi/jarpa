package fi.purkka.jarpa;

/** A parses that produces a {@code JarpaArgs} instance from a given
 * string array.
 * 
 * <p>Note that escaping larger sequences of arguments should be handled
 * by the shell through which the args are passed. As replicating
 * whitespace exactly would be impossible, this class does not provide
 * any such functionality.</p>
 * 
 * <p>The format is as follows:</p>
 * 
 * <p>{@code [DEFAULT ARGS] [-SWITCH1] [ARGS] [-SWITCH2] [ARGS] [-SWITCHN] [ARGS] }</p>*/
public final class JarpaParser {
	
	private int index = 0;
	private final String[] args;
	
	/** Constructs an instance with the default escape string {@code '}.*/
	private JarpaParser(String[] args) {
		this.args = args;
	}
	
	/** Parses the given arguments, returning a {@code JarpaArgs}
	 * instance.*/
	public static JarpaArgs parse(String[] args) {
		JarpaParser parser = new JarpaParser(args);
		return parser.parse();
	}
	
	private JarpaArgs parse() {
		JarpaArgs jargs = new JarpaArgs();
		if(!isArgument(args[0])) {
			jargs.values.put("", nextValue());
		}
		
		while(index < args.length) {
			String arg = args[index];
			index++;
			jargs.values.put(arg, nextValue());
		}
		return jargs;
	}
	
	private String[] nextValue() {
		if(index >= args.length) { return new String[0]; }
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
	
	private static boolean isArgument(String str) {
		if(str.length() < 2) return false;
		char first = str.charAt(0);
		char second = str.charAt(1);
		return first == '-' && (Character.isLetter(second) || second == '-');
	}
}