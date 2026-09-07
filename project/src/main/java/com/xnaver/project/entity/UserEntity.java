package com.xnaver.project.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "user_t")
@NoArgsConstructor
@Getter @Setter
@Entity
public class UserEntity extends BaseEntity {

    public enum Sex {
        MALE, FEMALE,
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(nullable = false, length = 50)
    private String email;
    @Column(nullable = false, length = 50)
    private String password;
    @Column(nullable = false, length = 30)
    private String nickName;
    @Column(nullable = false)
    private int age;

    @Enumerated(value = EnumType.STRING) //enum 타입을 문자열로 저장
    private Sex sex;

    @Builder
    public UserEntity(String email, String password, String nickName, int age, Sex sex ){
        this.email = email;
        this.password = password;
        this.nickName = nickName;
        this.age = age;
        this.sex = sex;
    }



}
