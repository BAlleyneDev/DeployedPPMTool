package io.balleyndev.ppmtool.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import io.balleyndev.ppmtool.domain.User;

@Component
public class UserValidator implements Validator {

	// Validates the pbject
	@Override
	public boolean supports(Class<?> aClass) {

		return User.class.equals(aClass);
	}

	@Override
	public void validate(Object object, Errors errors) {

		User user = (User) object;

		if (user.getPassword().length() < 6) {
			errors.rejectValue("password", "Length", "Password must be at least 6 characters");
		}

		if (!user.getPassword().equals(user.getConfirmPassword())) {
			errors.rejectValue("confirmPassword", "Match", "Password must match");
		}

	}

}
