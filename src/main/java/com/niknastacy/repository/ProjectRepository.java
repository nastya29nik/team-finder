package com.niknastacy.repository;

import com.niknastacy.model.Project;
import com.niknastacy.model.Skill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    @EntityGraph(attributePaths = {"owner"})
    Page<Project> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"owner"})
    Page<Project> findBySkillsContainingOrderByCreatedAtDesc(Skill skill, Pageable pageable);
}