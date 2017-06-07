package fi.purkka.jarpa;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/** Contains parsed arguments. The {@code default arguments}
 * (before any switch) are denoted by the empty switch string {@code ""}.*/
public class JarpaArgs {
	
	final Map<String, String[]> values = new HashMap<>();
	
	JarpaArgs() {}
	
	@Override
	public String toString() {
		return "{ " + values.keySet().stream()
				.map(k -> k + ": " + Arrays.toString(values.get(k)))
				.collect(Collectors.joining(", ")) + " }";
	}
}