package account.dto;

import java.time.YearMonth;

public class AccountPaymentDetails {

    private String name;
    private String lastName;
    private YearMonth period;
    private long salary;

    public AccountPaymentDetails(String name, String lastName, YearMonth period, long salary) {
        this.name = name;
        this.lastName = lastName;
        this.period = period;
        this.salary = salary;
    }

    public AccountPaymentDetails() {

    }

    public String getName() {
        return name;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPeriod() {

        return period.getMonth() + "-" + period.getYear();
    }

    public long getSalary() {
        return salary;
    }
}
