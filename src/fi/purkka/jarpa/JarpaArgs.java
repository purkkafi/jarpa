package fi.purkka.jarpa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Contains parsed arguments. The {@code default arguments}
 * (before any switch) are denoted by the empty switch string {@code ""}.*/
public class JarpaArgs {
	
	private final static String[] EMPTY_ARRAY = new String[0];
	
	final Map<String, String[]> values = new HashMap<>();
	private final Set<String> optionalArgs = new HashSet<>();
	
	{
		optionalArgs.add("");
	}
	
	JarpaArgs() {}
	
	/** Returns the value given for an argument according
	 * to its parameters.*/
	public <T> T get(JarpaArg<T> arg) {
		return arg.retrieve(this);
	}
	
	/** Verifies that no arguments were specified that weren't
	 * retrieved.*/
	public void finish() {
		List<String> extras = new ArrayList<>();
		for(String arg : values.keySet()) {
			if(!optionalArgs.contains(arg)) {
				extras.add(arg);
			}
		}
		
		if(!extras.isEmpty()) {
			throw JarpaException.unknownArguments(
					extras.toArray(new String[extras.size()]));
		}
	}
	
	String[] getRaw(String arg) {
		return values.getOrDefault(arg, EMPTY_ARRAY);
	}
	
	String usedAlias(List<String> aliases) {
		String found = null;
		for(String alias : aliases) {
			if(values.containsKey(alias)) {
				if(found == null) {
					found = alias;
				} else {
					throw JarpaException.multipleAliasesPresent(found, alias);
				}
			}
		}
		return found;
	}
	
	void addOptionalArgs(List<String> args) {
		optionalArgs.addAll(args);
	}
	
	@Override
	public String toString() {
		return "{ " + values.keySet().stream()
				.map(k -> k + ": " + Arrays.toString(values.get(k)))
				.collect(Collectors.joining(", ")) + " }";
	}
}