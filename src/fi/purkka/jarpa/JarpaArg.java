package fi.purkka.jarpa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import fi.purkka.jarpa.ValueParser.SingleValueParser;
import fi.purkka.jarpa.conditions.Condition;

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
 * @param <T> The type of value this argument represents */
public abstract class JarpaArg<T> {
	
	List<String> aliases = new ArrayList<>();
	final ValueParser<T> valParser;
	private final List<Condition<T>> conditions = new ArrayList<>();
	
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
	 * refer to it instead of the originally given value. */
	public JarpaArg<T> alias(String alias) {
		aliases.add(alias);
		return this;
	}
	
	/** A convenience method for giving multiple aliases at the
	 * same time.
	 * @see JarpaArg#alias(String) */
	public JarpaArg<T> aliases(String...aliases) {
		for(String alias : aliases) {
			this.aliases.add(alias);
		}
		return this;
	}
	
	/** Adds a {@link Condition} that is verified to be true when
	 * the value is returned or else an exception will be thrown. */
	public JarpaArg<T> require(Condition<T> condition) {
		conditions.add(condition);
		return this;
	}
	
	void verifyConditions(T value) {
		for(Condition<T> condition : conditions) {
			if(!(condition.predicate.test(value))) {
				throw JarpaException.failedConditon(mainAlias(),
						condition.message);
			}
		}
	}
	
	/** Makes this argument <i>optional</i>, meaning that leaving it
	 * missing causes no error. */
	public JarpaArg<Optional<T>> optional() {
		return new OptionalArg<>(aliases, valParser);
	}
	
	abstract T retrieve(JarpaArgs args);
	
	/** Returns a <i>flag</i> argument. A flag argument is associated
	 * with a {@code boolean} value that represents whether
	 * it is present. */
	public static Flag flag(String arg) {
		return new Flag(arg);
	}
	
	/** Returns an argument with a single {@code String} value. */
	public static JarpaArg<String> string(String arg) {
		return withSingleValue(arg, s -> s);
	}
	
	/** Returns an argument with any number of {@code String} value
	 * as an array. */
	public static JarpaArg<String[]> stringArray(String arg) {
		return new SimpleArg<>(arg, s -> s);
	}
	
	/** Returns an argument with any number of {@code String} value
	 * as a {@code List}. */
	public static JarpaArg<List<String>> stringList(String arg) {
		return new SimpleArg<>(arg, Arrays::asList);
	}
	
	/** Returns an argument with a single {@code int} value. */
	public static JarpaArg<Integer> integer(String arg) {
		return withSingleValue(arg, Integer::parseInt);
	}
	
	/** Returns an argument with any number of {@code int} values. */
	public static JarpaArg<int[]> integerArray(String arg) {
		return new SimpleArg<>(arg, strings ->
				Arrays.stream(strings)
				.mapToInt(Integer::parseInt)
				.toArray());
	}
	
	/** Returns an argument with a single {@code double} value. */
	public static JarpaArg<Double> decimal(String arg) {
		return withSingleValue(arg, Double::parseDouble);
	}
	
	/** Returns an argument with any number of {@code double} values. */
	public static JarpaArg<double[]> decimalArray(String arg) {
		return new SimpleArg<>(arg, strings ->
				Arrays.stream(strings)
				.mapToDouble(Double::parseDouble)
				.toArray());
	}
	
	/** Returns an argument with a value of some arbitrary type. The
	 * given {@code ValueParser} is used to construct the object.
	 * @see JarpaArg#object(String, SingleValueParser) */
	public static <T> JarpaArg<T> object(String arg, ValueParser<T> parser) {
		return new SimpleArg<>(arg, parser);
	}
	
	/** Returns an argument with a single value of some arbitrary type.
	 * The given {@code SingleValueParser} is used to construct the object.
	 * @see JarpaArg#object(String, ValueParser) */
	public static <T> JarpaArg<T> object(String arg, SingleValueParser<T> parser) {
		return withSingleValue(arg, parser);
	}
	
	/** Returns an argument with any number of values of some arbitrary type.
	 * The given {@code SingleValueParser} is used to construct the objects. */
	public static <T> JarpaArg<List<T>> objectList(String arg, SingleValueParser<T> parser) {
		return new SimpleArg<>(arg, strings ->
				Arrays.stream(strings)
				.map(parser::apply)
				.collect(Collectors.toList()));
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
				T val = valParser.apply(args.getRaw(alias));
				verifyConditions(val);
				return val;
			} catch(JarpaException e) {
				throw e;
			} catch(Exception e) {
				throw JarpaException.parseException(e);
			}
		}
	}
	
	/** Represents a <i>flag argument</i> which has a boolean value
	 * based on whether it is present. */
	public static class Flag extends JarpaArg<Boolean> {
		
		private List<String> negators = new ArrayList<>();
		
		private Flag(String arg) {
			super(arg, v -> { throw new AssertionError("Flag cannot have values"); });
		}
		
		@Override
		public Flag alias(String alias) {
			return (Flag) super.alias(alias);
		}
		
		@Override
		public Flag aliases(String...aliases) {
			return (Flag) super.aliases(aliases);
		}
		
		/** Adds a <i>negator</i> that, if present, makes the value
		 * of this flag be {@code false}. */
		public Flag negator(String negator) {
			negators.add(negator);
			return this;
		}
		
		/** Adds multiple negators.
		 * @see Flag#negator(String) */
		public Flag negators(String...negators) {
			for(String neg : negators) this.negators.add(neg);
			return this;
		}

		@Override
		Boolean retrieve(JarpaArgs args) {
			args.addOptionalArgs(aliases);
			args.addOptionalArgs(negators);
			String alias = args.usedAlias(aliases);
			String negator = args.usedAlias(negators);
			
			if(alias == null && negator == null) return false;
			if(alias != null && negator != null) {
				throw JarpaException.flagAndNegatorPresent(alias, negator);
			}
			
			if(alias != null) {
				if(args.getRaw(alias).length != 0) {
					throw JarpaException.flagGivenValues(alias);
				}
				verifyConditions(true);
				return true;
			}
			
			if(args.getRaw(negator).length != 0) {
				throw JarpaException.flagGivenValues(negator);
			}
			
			verifyConditions(false);
			return false;
		}
	}
	
	private static class OptionalArg<T> extends JarpaArg<Optional<T>> {
		
		private OptionalArg(List<String> aliases, ValueParser<T> parser) {
			super(aliases, val -> Optional.of(parser.apply(val)));
		}

		@Override
		Optional<T> retrieve(JarpaArgs args) {
			args.addOptionalArgs(aliases);
			String alias = args.usedAlias(aliases);
			if(alias != null) {
				Optional<T> val = valParser.apply(args.getRaw(alias));
				verifyConditions(val);
				return val;
			}
			verifyConditions(Optional.empty());
			return Optional.empty();
		}
	}
}