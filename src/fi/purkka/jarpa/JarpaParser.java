package fi.purkka.jarpa;

/** A parses that produces a {@code JarpaArgs} instance from a given
 * string array.
 * 
 * <p>{@code JarpaParser.parsing(String[])} returns a {@link DefaultParser}
 * which can be configured through its chainable methods. To use the default
 * values, invoke this class like this:</p>
 * 
 * <p>{@code JarpaArgs result = JarpaParser.parsing(args).parse();}</p>
 * 
 * <p>See the documentation of {@code DefaultParser} for further info.</p>
 * 
 * @see DefaultParser */
public abstract class JarpaParser {
	
	/** Returns a {@link DefaultParser} parsing the specified arguments.*/
	public static DefaultParser parsing(String[] args) {
		return new DefaultParser(args);
	}
	
	abstract JarpaArgs parse();
	
	static boolean isArgument(String str) {
		if(str.length() < 2) return false;
		char first = str.charAt(0);
		char second = str.charAt(1);
		return first == '-' && (Character.isLetter(second) || second == '-');
	}
	
	/** The default parser used by Jarpa; functions like
	 * {@link SpacedParser}. The methods of this class can be
	 * used to return different kinds of parsers. */
	public static class DefaultParser extends JarpaParser {
		
		private String[] args;
		
		DefaultParser(String[] args) {
			this.args = args;
		}
		
		/** Parses using {@link SpacedParser}. Though this class
		 * already uses it internally, this method may become useful if
		 * {@code SpacedParser} adds methods for configuring itself. */
		public SpacedParser spaceSeparated() {
			return new SpacedParser(args);
		}
		
		/** Parses using {@link EqualsParser}. See its documentation for
		 * details. */
		public EqualsParser equalsSeparated() {
			return new EqualsParser(args);
		}
		
		/** Parses the initially given arguments using a {@link SpacedParser}.
		 * See its documentation for details. */
		@Override
		public JarpaArgs parse() {
			return new SpacedParser(args).parse();
		}
	}
}