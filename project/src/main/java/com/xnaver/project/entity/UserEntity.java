package com.xnaver.project.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "users")
@NoArgsConstructor
@Getter @Setter
@Entity
public class UserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "int")
    private Long idx;

    @Column(nullable = false, unique = true, length = 50)
    private String email;
    @Column(name = "password_hash", nullable = false, length = 255)
    private String password;
    @Column(name = "nickname", nullable = false, unique = true, length = 255)
    private String nickName;

    @Builder
    public UserEntity(String email, String password, String nickName){
        this.email = email;
        this.password = password;
        this.nickName = nickName;
    }


}
