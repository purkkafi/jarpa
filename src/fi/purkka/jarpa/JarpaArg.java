package fi.purkka.jarpa;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import fi.purkka.jarpa.ValueParser.SingleValue;

/** Represents a single argument that may optionally have some
 * <i>values</i> associated with it.
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
	
	public JarpaArg<T> alias(String alias) {
		aliases.add(alias);
		return this;
	}
	
	public JarpaArg<T> aliases(String...aliases) {
		for(String alias : aliases) {
			this.aliases.add(alias);
		}
		return this;
	}
	
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
	
	private static <T> SimpleArg<T> withSingleValue(String arg, SingleValue<T> parser) {
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
			return valParser.apply(args.getRaw(alias));
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