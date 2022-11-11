package accountService.repository;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class BreachedPasswordRepository {

    private final Set<String> breachedPasswords = Set.of("PasswordForJanuary", "PasswordForFebruary", "PasswordForMarch", "PasswordForApril",
            "PasswordForMay", "PasswordForJune", "PasswordForJuly", "PasswordForAugust",
            "PasswordForSeptember", "PasswordForOctober", "PasswordForNovember", "PasswordForDecember");

    public boolean containsPassword(String password) {
        return breachedPasswords.contains(password);
    }
}
