package fi.purkka.jarpa;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import fi.purkka.jarpa.ValueParser.SingleValueParser;

/** Represents a single argument that may optionally have some
 * <i>values</i> associated with it.
 * 
 * <p>Instances can be created with the static methods of this
 * class. They all take a string representing the argument:</p>
 * 
 * <p>{@code JarpaArg<Integer> arg = JarpaArg.integer("-i");}</p>
 * 
 * <p>Arguments may be modified by calling their chainable methods.
 * {@link JarpaArg#alias(String)} may be called to add an <i>alias</i>,
 * which may be used to represent the argument instead of the originally
 * given value.</p>
 * 
 * <p>{@code JarpaArg<Boolean> arg = JarpaArg.flag("--verbose").alias("-v");}</p>
 * 
 * <p>A <i>flag</i> (returned by {@link JarpaArg#flag()}) has a
 * {@code boolean} value that represents whether it is present.</p>
 * 
 * <p>{@link JarpaArg#optional()} may be called to make an argument optional.
 * In that case, leaving it missing causes no error; also, its type changes
 * from {@code T} to {@code Optional<T>}.</p>
 * 
 * <p>{@code JarpaArg<Optional<String>> optional = JarpaArg.string("-s").optional();}</p>
 * 
 * @param <T> The type of value this argument represents*/
public abstract class JarpaArg<T> {
	
	List<String> aliases = new ArrayList<>();
	final ValueParser<T> valParser;
	
	private JarpaArg(String arg, ValueParser<T> valParser) {
		aliases.add(arg);
		this.valParser = valParser;
	}
	
	private JarpaArg(List<String> aliases, ValueParser<T> valParser) {
		this.aliases = new ArrayList<>(aliases);
		this.valParser = valParser;
	}
	
	String mainAlias() {
		return aliases.get(0);
	}
	
	/** Gives this argument an <i>alias</i> which may be used to
	 * refer to it instead of the originally given value.*/
	public JarpaArg<T> alias(String alias) {
		aliases.add(alias);
		return this;
	}
	
	/** A convenience method for giving multiple aliases at the
	 * same time.
	 * @see JarpaArg#alias(String)*/
	public JarpaArg<T> aliases(String...aliases) {
		for(String alias : aliases) {
			this.aliases.add(alias);
		}
		return this;
	}
	
	/** Makes this argument <i>optional</i>, meaning that leaving it
	 * missing causes no error.*/
	public JarpaArg<Optional<T>> optional() {
		return new OptionalArg<>(aliases, valParser);
	}
	
	public abstract T retrieve(JarpaArgs args);
	
	/** Returns a <i>flag</i> argument. A flag argument is associated
	 * with a {@code boolean} value that represents whether
	 * it is present.*/
	public static JarpaArg<Boolean> flag(String arg) {
		return new Flag(arg);
	}
	
	/** Returns an argument with a single {@code String} value.*/
	public static JarpaArg<String> string(String arg) {
		return withSingleValue(arg, s -> s);
	}
	
	/** Returns an argument with any number of {@code String} value.*/
	public static JarpaArg<String[]> stringList(String arg) {
		return new SimpleArg<>(arg, s->s);
	}
	
	/** Returns an argument with a single {@code int} value.*/
	public static JarpaArg<Integer> integer(String arg) {
		return withSingleValue(arg, Integer::parseInt);
	}
	
	/** Returns an argument with a single {@code double} value.*/
	public static JarpaArg<Double> decimal(String arg) {
		return withSingleValue(arg, Double::parseDouble);
	}
	
	/** Returns an argument with a value of some arbitrary type. The
	 * given {@code ValueParser} is used to construct the object.
	 * @see JarpaArg#object(String, SingleValueParser)*/
	public static <T> JarpaArg<T> object(String arg, ValueParser<T> parser) {
		return new SimpleArg<>(arg, parser);
	}
	
	/** Returns an argument with a single value of some arbitrary type.
	 * The given {@code SingleValueParser} is used to construct the object.
	 * @see JarpaArg#object(String, ValueParser)*/
	public static <T> JarpaArg<T> object(String arg, SingleValueParser<T> parser) {
		return withSingleValue(arg, parser);
	}
	
	private static <T> SimpleArg<T> withSingleValue(String arg, SingleValueParser<T> parser) {
		return new SimpleArg<>(arg, parser);
	}
	
	private static class SimpleArg<T> extends JarpaArg<T> {
		
		private SimpleArg(String arg, ValueParser<T> parser) {
			super(arg, parser);
		}

		@Override
		public T retrieve(JarpaArgs args) {
			args.addOptionalArgs(aliases);
			String alias = args.usedAlias(aliases);
			if(alias == null) {
				throw JarpaException.mandatoryArgNotSpecified(mainAlias());
			}
			try {
				return valParser.apply(args.getRaw(alias));
			} catch(JarpaException e) {
				throw e;
			} catch(Exception e) {
				throw JarpaException.parseException(e);
			}
		}
	}
	
	private static class Flag extends JarpaArg<Boolean> {
		
		private Flag(String arg) {
			super(arg, v -> { throw new AssertionError("Flag cannot have values"); });
		}

		@Override
		public Boolean retrieve(JarpaArgs args) {
			args.addOptionalArgs(aliases);
			String alias = args.usedAlias(aliases);
			if(alias == null) return false;
			if(args.getRaw(alias).length != 0) {
				throw JarpaException.flagGivenValues(alias);
			}
			return true;
		}
	}
	
	private static class OptionalArg<T> extends JarpaArg<Optional<T>> {
		
		private OptionalArg(List<String> aliases, ValueParser<T> parser) {
			super(aliases, val -> Optional.of(parser.apply(val)));
		}

		@Override
		public Optional<T> retrieve(JarpaArgs args) {
			args.addOptionalArgs(aliases);
			String alias = args.usedAlias(aliases);
			if(alias != null) {
				return valParser.apply(args.getRaw(alias));
			}
			return Optional.empty();
		}
	}
}