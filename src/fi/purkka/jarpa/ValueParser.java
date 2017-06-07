package fi.purkka.jarpa;

/** Represents a function capable of transforming string arrays
 * into values of some type {@code T}.*/
public interface ValueParser<T> {
	
	public T apply(String[] args);
	
	/** A special case of {@code ValueParser} that only accepts a
	 * single string as its input.*/
	public static interface SingleValue<T> extends ValueParser<T> {
		
		@Override
		public default T apply(String[] args) {
			if(args.length != 1) {
				throw JarpaException.singleValueExpected(args);
			}
			return apply(args[0]);
		}
		
		public T apply(String string);
	}
}