package com.niknastacy.service;

import com.niknastacy.dto.RegisterDto;
import com.niknastacy.model.User;
import com.niknastacy.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AvatarService avatarService;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Успешная регистрация нового пользователя")
    void register_Success() {
        RegisterDto dto = new RegisterDto();
        dto.setName("Иван");
        dto.setSurname("Иванов");
        dto.setEmail("ivan@test.com");
        dto.setPassword("rawPassword");

        when(userRepository.existsByEmail("ivan@test.com")).thenReturn(false);
        when(passwordEncoder.encode("rawPassword")).thenReturn("hashedPassword");
        when(avatarService.generateAvatar("Иван")).thenReturn("avatarData");

        userService.register(dto);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Ошибка регистрации, если email уже занят")
    void register_ThrowsException_WhenEmailExists() {
        RegisterDto dto = new RegisterDto();
        dto.setEmail("busy@test.com");

        when(userRepository.existsByEmail("busy@test.com")).thenReturn(true);
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.register(dto)
        );

        assertEquals("Пользователь с таким email уже существует", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
}
