package com.xnaver.project.entity.recipe;

import com.xnaver.project.entity.BaseEntity;
import com.xnaver.project.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Table(name = "recipe_t")
@NoArgsConstructor
@Getter
@Setter
@Entity
public class RecipeEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(nullable = false, length = 254)
    private String description;

    @Column(nullable = false, length = 20)
    private String difficulty;

    @Column(nullable = false)
    private int createdTime;

    @Column(nullable = false)
    private int viewCount = 0;

    @Column(nullable = false)
    private int good = 0;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "user_t_idx")
    private UserEntity userEntity;

    @Builder
    public RecipeEntity(String title, String description, String difficulty, int createdTime){
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.createdTime = createdTime;
    }
}
