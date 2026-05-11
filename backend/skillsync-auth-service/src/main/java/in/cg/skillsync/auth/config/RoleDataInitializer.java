package in.cg.skillsync.auth.config;

import in.cg.skillsync.auth.entity.Role;
import in.cg.skillsync.auth.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RoleDataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public RoleDataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedRole("ROLE_ADMIN");
        seedRole("ROLE_MENTOR");
        seedRole("ROLE_LEARNER");
    }

    private void seedRole(String roleName) {
        if (roleRepository.findByName(roleName).isEmpty()) {
            roleRepository.save(new Role(roleName));
        }
    }
}
