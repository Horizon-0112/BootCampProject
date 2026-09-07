package com.xnaver.project.entity.recipe;

import com.xnaver.project.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Table(name = "recipe_ingredient_t")
@NoArgsConstructor
@Getter
@Setter
@Entity
public class RecipeIngredientEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(nullable = false, length = 20)
    private String ingredientName;
    @Column(nullable = false, length = 50)
    private String amount;
    @Column(nullable = false)
    private boolean essential;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "recipe_t_idx")
    private RecipeEntity recipeEntity;

    @Builder
    public RecipeIngredientEntity(String ingredientName, String amount, boolean essential) {
        this.ingredientName = ingredientName;
        this.amount = amount;
        this.essential = essential;
    }

}
