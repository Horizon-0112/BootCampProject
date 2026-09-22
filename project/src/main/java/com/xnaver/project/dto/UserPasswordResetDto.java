package com.xnaver.project.dto;
import lombok.Getter;
import lombok.Setter;
@Getter @Setter
public class UserPasswordResetDto {
    private String token;
    private String pw;
    private String pwConfirm;
}