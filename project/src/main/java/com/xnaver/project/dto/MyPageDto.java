package com.xnaver.project.dto;
import java.time.LocalDateTime;
import java.util.List;
public final class MyPageDto {
    private MyPageDto() {}
    public record Profile(String email, String nickName, LocalDateTime createdAt, long recipes, long comments, long bookmarks, boolean hasProfileImage) {}
    public record Item(long id, long recipeId, String title, String content, String imageUrl, LocalDateTime createdAt) {}
    public record Page(List<Item> items, int page, int size, long total) {}
    public record Update(String nickName, String currentPassword, String pw, String pwConfirm) {}
    public record Withdrawal(String currentPassword, boolean confirmed) {}
}
