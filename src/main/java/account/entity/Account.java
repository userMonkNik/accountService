package account.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.util.List;

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
    @JsonIgnore
    private String grantedAuthority;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<PaymentDetails> salaryDetailsList;

    public List<PaymentDetails> getSalaryDetailsList() {
        return salaryDetailsList;
    }

    public void setSalaryDetailsList(List<PaymentDetails> salaryDetailsList) {
        this.salaryDetailsList = salaryDetailsList;
    }

    public String getGrantedAuthority() {
        return grantedAuthority;
    }

    public void setGrantedAuthority(String grantedAuthority) {
        this.grantedAuthority = grantedAuthority;
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
