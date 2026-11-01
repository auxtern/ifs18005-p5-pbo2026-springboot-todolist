package org.delcom.ifs18005springboottodolist.usecase;

import org.delcom.ifs18005springboottodolist.adapter.repository.UserRepository;
import org.delcom.ifs18005springboottodolist.domain.PasswordHasher;
import org.delcom.ifs18005springboottodolist.domain.entity.Role;
import org.delcom.ifs18005springboottodolist.domain.entity.User;
import org.delcom.ifs18005springboottodolist.domain.exception.EntityNotFoundException;
import org.delcom.ifs18005springboottodolist.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserUseCaseTest {

    private UserUseCase userUseCase;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = new UserRepository();
        userUseCase = new UserUseCase(userRepository);
    }

    @Test
    void listGetUpdateProfileAndPhoto() throws Exception {
        User user = userRepository.saveNew("budi", PasswordHasher.hash("pass"), "Budi", Role.USER);

        assertEquals(1, userUseCase.listUsers().size());
        assertEquals("Budi", userUseCase.getUser(user.getId()).getFullName());

        User updated = userUseCase.updateProfile(user.getId(), "Budi Santoso", "baru123");
        assertEquals("Budi Santoso", updated.getFullName());
        assertEquals(PasswordHasher.hash("baru123"), updated.getPasswordHash());

        User withPhoto = userUseCase.updatePhoto(user.getId(), "profiles/a.png");
        assertEquals("profiles/a.png", withPhoto.getPhotoPath());

        userUseCase.updateProfile(user.getId(), "Budi", "   ");
        assertEquals(PasswordHasher.hash("baru123"), userUseCase.getUser(user.getId()).getPasswordHash());

        userUseCase.updateProfile(user.getId(), "Budi", null);
        assertEquals(PasswordHasher.hash("baru123"), userUseCase.getUser(user.getId()).getPasswordHash());
    }

    @Test
    void validationAndNotFound() {
        assertThrows(EntityNotFoundException.class, () -> userUseCase.getUser(99));
        assertThrows(EntityNotFoundException.class, () -> userUseCase.updateProfile(99, "A", null));
        assertThrows(EntityNotFoundException.class, () -> userUseCase.updatePhoto(99, "x.png"));

        User user = userRepository.saveNew("cita", PasswordHasher.hash("pass"), "Cita", Role.USER);
        assertThrows(ValidationException.class, () -> userUseCase.updateProfile(user.getId(), null, null));
        assertThrows(ValidationException.class, () -> userUseCase.updateProfile(user.getId(), " ", null));
        assertThrows(ValidationException.class, () -> userUseCase.updateProfile(user.getId(), "Nama", "ab"));
        assertThrows(ValidationException.class, () -> userUseCase.updatePhoto(user.getId(), " "));
        assertThrows(ValidationException.class, () -> userUseCase.updatePhoto(user.getId(), null));
    }
}
