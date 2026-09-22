package com.xnaver.project.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserSignUpDto {

    private String email;
    private String pw;
    private String pwConfirm;
    private String nickName;
}
