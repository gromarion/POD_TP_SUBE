package ar.edu.itba.pod.mmxivii.sube.predicate;

import com.google.common.base.Predicate;

public class PositiveDouble implements Predicate<Double> {

	@Override
	public boolean apply(Double input) {
		return input != null && input > 0;
	}

}
