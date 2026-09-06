package com.niknastacy.service;

import com.niknastacy.dto.ProjectDto;
import com.niknastacy.dto.SkillAddResult;
import com.niknastacy.model.Project;
import com.niknastacy.model.Skill;
import com.niknastacy.model.User;
import com.niknastacy.repository.ProjectRepository;
import com.niknastacy.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final SkillRepository skillRepository;

    @Transactional(readOnly = true)
    public Page<Project> getProjects(int page, String skillName) {
        Pageable pageable = PageRequest.of(page, 12);
        if (skillName != null && !skillName.isBlank()) {
            return skillRepository.findByNameIgnoreCase(skillName.trim())
                    .map(skill -> projectRepository.findBySkillsContainingOrderByCreatedAtDesc(skill, pageable))
                    .orElseGet(Page::empty);
        }
        return projectRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Transactional(readOnly = true)
    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Проект не найден с id: " + id));
    }

    @Transactional
    public Project createProject(ProjectDto form, User user) {
        Project project = new Project();
        project.setName(form.getName());
        project.setDescription(form.getDescription());
        project.setGithubUrl(form.getGithubUrl());
        project.setStatus(form.getStatus());
        project.setOwner(user);
        project.getParticipants().add(user);

        return projectRepository.save(project);
    }

    @Transactional
    public Project updateProject(Long projectId, ProjectDto form, Long currentUserId) {
        Project project = getProjectById(projectId);
        checkOwner(project, currentUserId);

        project.setName(form.getName());
        project.setDescription(form.getDescription());
        project.setGithubUrl(form.getGithubUrl());
        project.setStatus(form.getStatus());

        return projectRepository.save(project);
    }

    @Transactional
    public void completeProject(Long projectId, Long currentUserId) {
        Project project = getProjectById(projectId);
        checkOwner(project, currentUserId);

        if (!"open".equals(project.getStatus())) {
            throw new IllegalStateException("Проект уже завершен");
        }
        project.setStatus("closed");
        projectRepository.save(project);
    }

    @Transactional
    public boolean toggleParticipate(Long projectId, User user) {
        Project project = getProjectById(projectId);
        boolean wasParticipant = project.getParticipants().removeIf(u -> u.getId().equals(user.getId()));
        if (!wasParticipant) {
            project.getParticipants().add(user);
        }
        projectRepository.save(project);
        return !wasParticipant;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> searchSkills(String query) {
        return skillRepository.findTop10ByNameStartingWithIgnoreCaseOrderByNameAsc(query)
                .stream()
                .map(s -> Map.<String, Object>of("id", s.getId(), "name", s.getName()))
                .toList();
    }

    @Transactional
    public SkillAddResult addSkill(Long projectId, Map<String, Object> payload, Long currentUserId) {
        Project project = getProjectById(projectId);
        checkOwner(project, currentUserId);

        if (payload == null) {
            throw new IllegalArgumentException("Payload cannot be null");
        }

        Object skillIdObj = payload.get("skill_id");
        String skillName = (String) payload.get("name");

        Skill skill;
        boolean created = false;

        if (skillIdObj != null) {
            Long skillId = Long.valueOf(skillIdObj.toString());
            skill = skillRepository.findById(skillId)
                    .orElseThrow(() -> new NoSuchElementException("Навык не найден"));
        } else if (skillName != null && !skillName.isBlank()) {
            String trimmed = skillName.trim();
            Optional<Skill> existing = skillRepository.findByNameIgnoreCase(trimmed);
            if (existing.isPresent()) {
                skill = existing.get();
            } else {
                skill = new Skill();
                skill.setName(trimmed);
                skill = skillRepository.save(skill);
                created = true;
            }
        } else {
            throw new IllegalArgumentException("Не указан id или название навыка");
        }

        boolean added = project.getSkills().add(skill);
        projectRepository.save(project);

        return new SkillAddResult(skill.getId(), skill.getId(), skill.getName(), created, added, "ok");
    }

    @Transactional
    public void removeSkill(Long projectId, Long skillId, Long currentUserId) {
        Project project = getProjectById(projectId);
        checkOwner(project, currentUserId);

        project.getSkills().removeIf(s -> s.getId().equals(skillId));
        projectRepository.save(project);
    }

    private void checkOwner(Project project, Long currentUserId) {
        if (!project.getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Вы не являетесь владельцем этого проекта");
        }
    }
}