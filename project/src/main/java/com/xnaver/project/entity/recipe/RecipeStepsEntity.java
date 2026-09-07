package com.xnaver.project.entity.recipe;

import com.xnaver.project.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Table(name = "recipe_steps_t")
@NoArgsConstructor
@Getter
@Setter
@Entity
public class RecipeStepsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(nullable = false)
    private int stepNum;
    @Column(nullable = false, length = 50)
    private String description;
    @Column(nullable = false)
    private String imageURL;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "recipe_t_idx")
    private RecipeEntity recipeEntity;

    @Builder
    public RecipeStepsEntity(int stepNum, String description, String imageURL){
        this.stepNum = stepNum;
        this.description = description;
        this.imageURL = imageURL;
    }
}
