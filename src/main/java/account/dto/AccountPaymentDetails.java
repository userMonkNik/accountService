package account.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

public class AccountPaymentDetails {

    private String name;
    private String lastname;
    private YearMonth period;
    private long salary;

    public AccountPaymentDetails(String name, String lastName, YearMonth period, long salary) {
        this.name = name;
        this.lastname = lastName;
        this.period = period;
        this.salary = salary;
    }

    public AccountPaymentDetails() {

    }

    public String getName() {
        return name;
    }

    public String getLastname() {
        return lastname;
    }

    public String getPeriod() {

        return period.getMonth()
                .getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                + "-"
                + period.getYear();
    }
    @JsonIgnore
    public YearMonth getYearMonthPeriod() {
        return period;
    }

    public String getSalary() {
        return String.format("%d dollar(s) %d cent(s)", salary / 100, salary % 100);
    }
}
