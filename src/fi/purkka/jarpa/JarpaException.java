package fi.purkka.jarpa;

import java.util.Arrays;
import java.util.stream.Collectors;

import fi.purkka.jarpa.conditions.Condition;

/** Specifies that something went wrong in Jarpa code.*/
public class JarpaException extends RuntimeException {

	private static final long serialVersionUID = 8322602205455482820L;
	
	final Type type;
	
	private JarpaException(Type type, String msg) {
		super(msg);
		this.type = type;
	}
	
	/** Indicates that multiple aliases of a single argument were
	 * present, for example that both {@code -v} and its alias
	 * {@code --verbose} were found.*/
	public static JarpaException multipleAliasesPresent(String...aliases) {
		return new JarpaException(Type.MULTIPLE_ALIASES_PRESENT,
				"Multiple aliases of same switch present: " + Arrays.stream(aliases)
				.collect(Collectors.joining(", ")));
	}
	
	/** Indicates that some values were given to a flag which should
	 * not have any.*/
	public static JarpaException flagGivenValues(String flag) {
		return new JarpaException(Type.FLAG_GIVEN_VALUES,
				"A flag was given values: " + flag);
	}
	
	/** Indicates that multiple values were given to an argument
	 * expecting a single one.*/
	public static JarpaException singleValueExpected(String[] given) {
		return new JarpaException(Type.SINGLE_VALUE_EXPECTED, given.length == 0 ?
				"Expected single value, given nothing" :
				"Expected single value, given " + Arrays.stream(given)
				.collect(Collectors.joining(", ")));
	}
	
	/** Indicates that a mandatory argument is missing.*/
	public static JarpaException mandatoryArgNotSpecified(String arg) {
		return new JarpaException(Type.MANDATORY_ARG_NOT_SPECIFIED,
				"Mandatory argument " + arg + " not specified");
	}
	
	/** Indicates that unknown arguments were found.*/
	public static JarpaException unknownArguments(String...args) {
		return new JarpaException(Type.UNKNOWN_ARGUMENTS, args.length == 1 ?
				"Unknown argument " + args[0] : "Unknown arguments " +
				Arrays.stream(args).collect(Collectors.joining(", ")));
	}
	
	/** Indicates that an exception occurred while parsing values.*/
	public static JarpaException parseException(Throwable t) {
		return new JarpaException(Type.PARSE_EXCEPTION, "Parse exception: "
				+ t.getClass().getSimpleName()
				+ ": " + t.getMessage());
	}
	
	/** Indicates that both a flag and its negator were present. */
	public static JarpaException flagAndNegatorPresent(String flag, String negator) {
		return new JarpaException(Type.FLAG_AND_NEGATOR_PRESENT, "Both flag "
				+ flag + " and its negator " + negator + " are present");
	}
	
	/** Indicates that a condition is logically invalid. */
	public static JarpaException invalidCondition(String condition, String reason) {
		return new JarpaException(Type.INVALID_CONDITION, "Invalid condition '" + condition
				+ "': " + reason);
	}
	
	/** Indicates that a {@link Condition} was failed. */
	public static JarpaException failedConditon(String argument, String failure) {
		return new JarpaException(Type.CONDITION_FAILED, "Illegal value for " + argument
				+ ": " + failure);
	}
	
	static enum Type {
		MULTIPLE_ALIASES_PRESENT,
		FLAG_GIVEN_VALUES,
		SINGLE_VALUE_EXPECTED,
		MANDATORY_ARG_NOT_SPECIFIED,
		UNKNOWN_ARGUMENTS,
		FLAG_AND_NEGATOR_PRESENT,
		CONDITION_FAILED,
		INVALID_CONDITION,
		PARSE_EXCEPTION
	}
}
