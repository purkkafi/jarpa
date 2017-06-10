package fi.purkka.jarpa.conditions;

import java.util.function.IntPredicate;

import fi.purkka.jarpa.JarpaException;
import fi.purkka.jarpa.conditions.IntCondition;

/** Contains conditions relevant to integers.
 * 
 * @see Condition */
public class IntCondition extends Condition<Integer> {
	
	private IntCondition(IntPredicate pred, String msg) {
		super(i -> pred.test(i), msg);
	}
	
	private IntPredicate intPredicate() {
		return i -> predicate.test(i);
	}
	
	/** Returns a {@code Condition} that is true only if this and the given
	 * condition are both true.
	 * @see Condition#and(Condition)*/
	public IntCondition and(IntCondition other) {
		return new IntCondition(intPredicate().and(other.intPredicate()),
				message + " and " + other.message);
	}
	
	/** Returns a {@code Condition} that is only true if the given condition
	 * is true for every element of an {@code int} array. */
	public static Condition<int[]> wholeArray(IntCondition condition) {
		return new Condition<>(arr -> {
			for(int i : arr) {
				if(!condition.predicate.test(i)) { return false; }
			}
			return true;
		}, "all " + condition.message);
	}
	
	private final static IntCondition POSITIVE = 
			new IntCondition(i -> i > 0, "must be positive");
	
	/** Returns a {@code Condition} that requires that the given {@code int}
	 * is positive. */
	public static IntCondition positive() {
		return POSITIVE;
	}
	
	private final static IntCondition NEGATIVE = 
			new IntCondition(i -> i < 0, "must be negative");
	
	/** Returns a {@code Condition} that requires that the given {@code int}
	 * is negative. */
	public static IntCondition negative() {
		return NEGATIVE;
	}
	
	private final static IntCondition NONZERO = 
			new IntCondition(i -> i != 0, "must be nonzero");
	
	/** Returns a {@code Condition} that requires that the given {@code int}
	 * is not equal to {@code 0}. */
	public static IntCondition nonZero() {
		return NONZERO;
	}
	
	/** Returns a {@code Condition} that requires that the given {@code int}
	 * is in the given range. */
	public static IntCondition inRange(int from, int to) {
		if(from >= to) throw JarpaException.invalidCondition("inRange", "from >= to");
		return new IntCondition(i -> i >= from && i <= to,
				"must be between " + from + " and " + to);
	}
}