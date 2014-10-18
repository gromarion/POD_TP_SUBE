package ar.edu.itba.pod.mmxivii.sube.predicate;

import com.google.common.base.Predicate;

/**
 * Valida que el double tenga como mucho dos decimales y su valor absoluto sea
 * menor a 100
 */
public class TwoDecimalPlacesAndLessThan100 implements Predicate<Double> {

	@Override
	public boolean apply(Double input) {
		return input != null && !(Math.rint(input * 100) != (input * 100) || Math.abs(input) > 100);
	}

}
