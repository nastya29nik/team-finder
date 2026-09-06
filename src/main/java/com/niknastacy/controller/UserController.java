package com.niknastacy.controller;

import com.niknastacy.dto.ChangePasswordDto;
import com.niknastacy.dto.ProfileEditDto;
import com.niknastacy.model.User;
import com.niknastacy.service.CustomUserDetails;
import com.niknastacy.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public String myProfile(@AuthenticationPrincipal CustomUserDetails currentUserDetails) {
        return "redirect:/users/" + currentUserDetails.getUser().getId();
    }

    @GetMapping("/{id}")
    public String showUserProfile(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails currentUserDetails,
            Model model
    ) {
        User user = userService.getUserById(id);
        boolean isOwner = (currentUserDetails != null) &&
                currentUserDetails.getUser().getId().equals(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("is_owner", isOwner);
        model.addAttribute("projects", user.getOwnedProjects());

        return "users/user-details";
    }

    @GetMapping("/edit-profile")
    public String showEditProfileForm(
            @AuthenticationPrincipal CustomUserDetails currentUserDetails,
            Model model
    ) {
        User user = userService.getUserById(currentUserDetails.getUser().getId());

        ProfileEditDto form = new ProfileEditDto();
        form.setName(user.getName());
        form.setSurname(user.getSurname());
        form.setAbout(user.getAbout());
        form.setPhone(user.getPhone());
        form.setGithubUrl(user.getGithubUrl());

        model.addAttribute("form", form);
        model.addAttribute("user", user);
        return "users/edit_profile";
    }

    @PostMapping("/edit-profile")
    public String processEditProfile(
            @Valid @ModelAttribute("form") ProfileEditDto form,
            BindingResult bindingResult,
            @RequestParam(value = "avatar", required = false) MultipartFile avatarFile,
            @AuthenticationPrincipal CustomUserDetails currentUserDetails,
            Model model
    ) {
        Long currentUserId = currentUserDetails.getUser().getId();

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", userService.getUserById(currentUserId));
            return "users/edit_profile";
        }

        try {
            userService.updateProfile(currentUserId, form, avatarFile);
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("phone", "error.phone", e.getMessage());
            model.addAttribute("user", userService.getUserById(currentUserId));
            return "users/edit_profile";
        }

        return "redirect:/users/" + currentUserId;
    }

    @GetMapping("/change-password")
    public String showChangePasswordForm(Model model) {
        model.addAttribute("form", new ChangePasswordDto());
        return "users/change_password";
    }

    @PostMapping("/change-password")
    public String processChangePassword(
            @Valid @ModelAttribute("form") ChangePasswordDto form,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails currentUserDetails
    ) {
        if (bindingResult.hasErrors()) {
            return "users/change_password";
        }

        try {
            userService.changePassword(currentUserDetails.getUser().getId(), form);
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("newPassword2", "error.password", e.getMessage());
            return "users/change_password";
        }

        return "redirect:/users/" + currentUserDetails.getUser().getId();
    }

    @GetMapping("/list")
    public String listUsers(
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model
    ) {
        Page<User> usersPage = userService.getUsersList(page);
        model.addAttribute("participants", usersPage);
        return "users/participants";
    }
}