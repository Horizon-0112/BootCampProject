package com.xnaver.project.repository;

import com.xnaver.project.entity.recipe.RecipeStepsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeStepsRepository extends JpaRepository<RecipeStepsEntity, Long> {
}
