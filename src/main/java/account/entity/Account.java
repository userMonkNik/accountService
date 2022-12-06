package account.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "Accounts")
public class Account {
    @Id
    @GeneratedValue
    private long id;
    @NotEmpty(message = "Name cannot be empty!")
    private String name;
    @NotEmpty(message = "Lastname cannot be empty")
    private String lastname;
    @NotNull
    @Email(regexp = ".+(@acme.com)", message = "Email must have corporate domain 'acme.com'")
    private String email;
    @NotNull
    @Size(min = 12, message = "The password length must be at least 12 chars!")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE},fetch = FetchType.EAGER)
    @JoinTable(name = "account_roles",
            joinColumns = @JoinColumn(name = "account_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private List<Role> roles = new ArrayList<>();
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<PaymentDetails> salaryDetailsList;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private boolean accountNonLocked;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private int failedAttempt;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Date lockTime;

    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    public int getFailedAttempt() {
        return failedAttempt;
    }

    public void setFailedAttempt(int failedAttempt) {
        this.failedAttempt = failedAttempt;
    }

    public Date getLockTime() {
        return lockTime;
    }

    public void setLockTime(Date lockTime) {
        this.lockTime = lockTime;
    }

    public List<PaymentDetails> getSalaryDetailsList() {
        return salaryDetailsList;
    }

    public void setSalaryDetailsList(List<PaymentDetails> salaryDetailsList) {
        this.salaryDetailsList = salaryDetailsList;
    }

    public List<String> getRoles() {
        return roles.stream()
                .map(Role::getCode)
                .sorted()
                .collect(Collectors.toList());
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }
    public void addRole(Role role) {
        roles.add(role);
    }
    public void removeRole(Role role) {
        roles.remove(role);
    }
    public long getId() {
        return id;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
