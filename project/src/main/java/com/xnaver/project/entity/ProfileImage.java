package com.xnaver.project.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "profile_images")
@Getter
@NoArgsConstructor
public class ProfileImage {
    @Id
    @Column(name = "user_id", columnDefinition = "int")
    private Long userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", columnDefinition = "int")
    private UserEntity user;

    @Lob
    @Column(nullable = false, columnDefinition = "mediumblob")
    private byte[] data;

    public ProfileImage(UserEntity user, byte[] data) {
        this.user = user;
        this.data = data;
    }

    public void replace(byte[] data) { this.data = data; }
}
