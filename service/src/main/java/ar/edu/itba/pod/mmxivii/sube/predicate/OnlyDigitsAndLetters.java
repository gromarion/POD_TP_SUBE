package ar.edu.itba.pod.mmxivii.sube.predicate;

import com.google.common.base.Predicate;

public class OnlyDigitsAndLetters implements Predicate<String> {

	@Override
	public boolean apply(String input) {
		if (input == null) {
			return false;
		}
		for (int i = 0; i < input.length(); i++) {
			final char c = input.charAt(i);
			if (!Character.isDigit(c) && !Character.isLetter(c)) {
				return false;
			}
		}
		return true;
	}

}
