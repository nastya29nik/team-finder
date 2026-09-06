package com.niknastacy.service;

import com.niknastacy.dto.ChangePasswordDto;
import com.niknastacy.dto.ProfileEditDto;
import com.niknastacy.dto.RegisterDto;
import com.niknastacy.model.User;
import com.niknastacy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AvatarService avatarService;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + email));
        return new CustomUserDetails(user);
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден с id: " + id));
    }

    @Transactional
    public void register(RegisterDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setSurname(dto.getSurname());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setAvatar(avatarService.generateAvatar(dto.getName()));
        user.setPhone("+79990000000");
        user.setActive(true);
        user.setStaff(false);

        userRepository.save(user);
    }

    @Transactional
    public void updateProfile(Long userId, ProfileEditDto form, MultipartFile avatarFile) {
        User user = getUserById(userId);

        String normalizedPhone = form.getPhone();
        if (normalizedPhone != null && normalizedPhone.startsWith("8")) {
            normalizedPhone = "+7" + normalizedPhone.substring(1);
        }

        if (normalizedPhone != null && !normalizedPhone.equals(user.getPhone())) {
            if (userRepository.existsByPhone(normalizedPhone)) {
                throw new IllegalArgumentException("Этот номер телефона уже используется другим пользователем");
            }
        }

        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                String base64 = Base64.getEncoder().encodeToString(avatarFile.getBytes());
                user.setAvatar("data:" + avatarFile.getContentType() + ";base64," + base64);
            } catch (IOException e) {
                log.error("Ошибка при обработке загруженного аватара", e);
                throw new RuntimeException("Не удалось загрузить аватар", e);
            }
        }

        user.setName(form.getName());
        user.setSurname(form.getSurname());
        user.setAbout(form.getAbout());
        user.setPhone(normalizedPhone);
        user.setGithubUrl(form.getGithubUrl());

        userRepository.save(user);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordDto form) {
        User user = getUserById(userId);

        if (!passwordEncoder.matches(form.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Неверный текущий пароль");
        }

        if (!form.getNewPassword1().equals(form.getNewPassword2())) {
            throw new IllegalArgumentException("Новые пароли не совпадают");
        }

        user.setPassword(passwordEncoder.encode(form.getNewPassword1()));
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Page<User> getUsersList(int page) {
        return userRepository.findAll(
                PageRequest.of(page, 12, Sort.by(Sort.Direction.DESC, "id"))
        );
    }
}