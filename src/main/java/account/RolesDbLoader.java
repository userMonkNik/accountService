package account;

import account.entity.Role;
import account.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RolesDbLoader {

    private final RoleRepository roleRepository;

    @Autowired
    public RolesDbLoader(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
        createRoles();
    }

    private void createRoles() {

        try {
            roleRepository.save(new Role("ROLE_ADMINISTRATOR", "Administrator"));
            roleRepository.save(new Role("ROLE_USER", "User"));
            roleRepository.save(new Role("ROLE_ACCOUNTANT", "Accountant"));
            roleRepository.save(new Role("ROLE_AUDITOR", "Auditor"));

        } catch (Exception ignore) {

        }
    }

}
