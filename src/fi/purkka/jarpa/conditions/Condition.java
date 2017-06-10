package fi.purkka.jarpa.conditions;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/** Describes a condition that is required to be true for some
 * argument or else a {@code JarpaException} will be thrown.
 * 
 * <p>Use {@link JarpaArg#require(Condition)} to impose conditions
 * upon arguments.</p>
 * 
 * <p>Each condition has a nice, user-readable error message that
 * will be passed to {@code JarpaException} if a condition is not met.</p>
 * 
 * <p>There are a few specialized classes for specific kinds of conditions
 * listed below.</p>
 * 
 * @see IntCondition */
public class Condition<T> {
	
	public final Predicate<T> predicate;
	public final String message;
	
	Condition(Predicate<T> pred, String msg) {
		predicate = pred;
		message = msg;
	}
	
	/** Returns a {@code Condition} that is true only if this and the given
	 * condition are both true. The usage of this method is preferable to
	 * invoking {@code require()} multiple times, as this one combines the
	 * error messages. */
	public Condition<T> and(Condition<T> other) {
		return new Condition<>(predicate.and(other.predicate),
				message + " and " + other.message);
	}
	
	/** Returns a {@code Condition} that is only true if the given condition
	 * is true for every element of an array. */
	public static <U> Condition<U[]> wholeArray(Condition<U> condition) {
		return new Condition<>(list -> {
			for(U u : list) {
				if(!condition.predicate.test(u)) { return false; }
			}
			return true;
		}, "all " + condition.message);
	}
	
	/** Returns a {@code Condition} that is only true if the given condition
	 * is true for every element of a list. */
	public static <U> Condition<List<U>> wholeList(Condition<U> condition) {
		return new Condition<>(list -> {
			for(U u : list) {
				if(!condition.predicate.test(u)) { return false; }
			}
			return true;
		}, "all " + condition.message);
	}
	
	/** Returns a {@code Condition} that is true if the value of an
	 * {@code Optional} is not present or otherwise if the given condition
	 * is true for the value. */
	public static <U> Condition<Optional<U>> ifPresent(Condition<U> condition) {
		return new Condition<>(opt -> opt.map(condition.predicate::test).orElse(true)
			, condition.message);
	}
}