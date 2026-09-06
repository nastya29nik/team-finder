package com.niknastacy.controller;

import com.niknastacy.dto.ProjectDto;
import com.niknastacy.dto.SkillAddResult;
import com.niknastacy.model.Project;
import com.niknastacy.model.User;
import com.niknastacy.repository.SkillRepository;
import com.niknastacy.service.CustomUserDetails;
import com.niknastacy.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final SkillRepository skillRepository;

    @GetMapping("/")
    public String index() {
        return "redirect:/projects/list";
    }

    @GetMapping("/projects/list")
    public String listProjects(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "skill", required = false) String skillName,
            Model model
    ) {
        Page<Project> projectsPage = projectService.getProjects(page, skillName);
        model.addAttribute("projects", projectsPage);
        model.addAttribute("active_skill", skillName != null && !skillName.isBlank() ? skillName : null);
        model.addAttribute("all_skills", skillRepository.findAll());
        return "projects/project_list";
    }

    @GetMapping("/projects/{id}")
    public String showProjectDetails(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails currentUserDetails,
            Model model
    ) {
        Project project = projectService.getProjectById(id);

        boolean isOwner = false;
        boolean isParticipant = false;

        if (currentUserDetails != null) {
            Long currentUserId = currentUserDetails.getUser().getId();
            isOwner = project.getOwner().getId().equals(currentUserId);
            isParticipant = project.getParticipants().stream()
                    .anyMatch(u -> u.getId().equals(currentUserId));
        }

        model.addAttribute("project", project);
        model.addAttribute("is_owner", isOwner);
        model.addAttribute("is_participant", isParticipant);

        return "projects/project-details";
    }

    @GetMapping("/projects/create-project")
    public String showCreateProjectForm(Model model) {
        model.addAttribute("form", new ProjectDto());
        model.addAttribute("is_edit", false);
        return "projects/create-project";
    }

    @PostMapping("/projects/create-project")
    public String processCreateProject(
            @Valid @ModelAttribute("form") ProjectDto form,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails currentUserDetails,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("is_edit", false);
            return "projects/create-project";
        }

        Project saved = projectService.createProject(form, currentUserDetails.getUser());
        return "redirect:/projects/" + saved.getId();
    }

    @GetMapping("/projects/{id}/edit")
    public String showEditProjectForm(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails currentUserDetails,
            Model model
    ) {
        Project project = projectService.getProjectById(id);

        if (!project.getOwner().getId().equals(currentUserDetails.getUser().getId())) {
            return "redirect:/projects/" + id;
        }

        ProjectDto form = new ProjectDto();
        form.setName(project.getName());
        form.setDescription(project.getDescription());
        form.setGithubUrl(project.getGithubUrl());
        form.setStatus(project.getStatus());

        model.addAttribute("form", form);
        model.addAttribute("is_edit", true);
        return "projects/create-project";
    }

    @PostMapping("/projects/{id}/edit")
    public String processEditProject(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("form") ProjectDto form,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails currentUserDetails,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("is_edit", true);
            return "projects/create-project";
        }

        projectService.updateProject(id, form, currentUserDetails.getUser().getId());
        return "redirect:/projects/" + id;
    }

    @PostMapping(value = {"/projects/{id}/complete", "/projects/{id}/complete/"})
    @ResponseBody
    public ResponseEntity<?> completeProject(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails currentUserDetails
    ) {
        try {
            projectService.completeProject(id, currentUserDetails.getUser().getId());
            return ResponseEntity.ok(Map.of("status", "ok", "project_status", "closed"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping(value = {"/projects/{id}/toggle-participate", "/projects/{id}/toggle-participate/"})
    @ResponseBody
    public ResponseEntity<?> toggleParticipate(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails currentUserDetails
    ) {
        boolean participant = projectService.toggleParticipate(id, currentUserDetails.getUser());
        return ResponseEntity.ok(Map.of("status", "ok", "participant", participant));
    }

    @GetMapping(value = {"/projects/skills", "/projects/skills/"})
    @ResponseBody
    public List<Map<String, Object>> searchSkills(@RequestParam(name = "q", defaultValue = "") String query) {
        return projectService.searchSkills(query);
    }

    @PostMapping(value = {"/projects/{id}/skills/add", "/projects/{id}/skills/add/"})
    @ResponseBody
    public ResponseEntity<?> addSkillToProject(
            @PathVariable("id") Long id,
            @RequestBody(required = false) Map<String, Object> payload,
            @AuthenticationPrincipal CustomUserDetails currentUserDetails
    ) {
        try {
            SkillAddResult result = projectService.addSkill(id, payload, currentUserDetails.getUser().getId());
            return ResponseEntity.ok(result);
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping(value = {"/projects/{id}/skills/{skillId}/remove", "/projects/{id}/skills/{skillId}/remove/"})
    @ResponseBody
    public ResponseEntity<?> removeSkillFromProject(
            @PathVariable("id") Long id,
            @PathVariable("skillId") Long skillId,
            @AuthenticationPrincipal CustomUserDetails currentUserDetails
    ) {
        try {
            projectService.removeSkill(id, skillId, currentUserDetails.getUser().getId());
            return ResponseEntity.ok(Map.of("status", "ok"));
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
