package com.roomate.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StartOneApplication {
	public static void main(String[] args) {
		SpringApplication.run(StartOneApplication.class, args);
	}

	// @Bean
	// CommandLineRunner initRoles(RoleRepository rolesRepository) {
	// return args -> {
	// if (rolesRepository.findByName("ROLE_ADMIN").isEmpty()) {
	// rolesRepository.save(new RolesEntity(null, "ROLE_ADMIN"));
	// rolesRepository.save(new RolesEntity(null, "ROLE_HEAD_ROOMMATE"));
	// rolesRepository.save(new RolesEntity(null, "ROLE_ASSISTANT_ROOMMATE"));
	// rolesRepository.save(new RolesEntity(null, "ROLE_ROOMMATE"));
	// }
	// };
	// }

	//
	// @Bean
	// public CommandLineRunner demo(UserRepository repository) {
	// return (args) -> {
	// save a few customers
	// repository.save(new UserEntity("Jack", "Bauer", "Chese@gmail.com", " "));
	// repository.save(new UserEntity("Chloe", "O'Brian", "Obrian@gmail.com",
	// "1234567890"));
	// };
	// }
}
