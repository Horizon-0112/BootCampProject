package com.xnaver.project.repository;
import com.xnaver.project.dto.MyPageDto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
@Repository
@RequiredArgsConstructor
public class MyPageRepository {
    private final JdbcTemplate jdbc;
    public long count(String kind, long userId) {
        return jdbc.queryForObject("select count(*) from " + source(kind) + " where " + owner(kind) + " = ?", Long.class, userId);
    }
    private String source(String kind) {
        return switch (kind) {
            case "recipes" -> "recipes r";
            case "comments" -> "comments c join recipes r on r.id = c.recipe_id";
            case "bookmarks" -> "saved_recipes s join recipes r on r.id = s.recipe_id";
            default -> throw new IllegalArgumentException("Unknown list");
        };
    }
    private String owner(String kind) {
        return switch (kind) { case "recipes" -> "r.author_id"; case "comments" -> "c.user_id"; default -> "s.user_id"; };
    }
    public Page list(String kind, long userId, int page, int size) {
        String alias = switch (kind) { case "recipes" -> "r"; case "comments" -> "c"; default -> "s"; };
        String content = kind.equals("comments") ? "c.content" : "r.description";
        String sql = "select " + alias + ".id, r.id recipe_id, r.title, " + content
            + " content, r.title_image_url, " + alias + ".created_at from " + source(kind)
            + " where " + owner(kind) + " = ? order by " + alias + ".created_at desc, " + alias + ".id desc limit ? offset ?";
        var items = jdbc.query(sql, (rs, row) -> new Item(rs.getLong("id"), rs.getLong("recipe_id"),
                rs.getString("title"), rs.getString("content"), rs.getString("title_image_url"),
                rs.getTimestamp("created_at").toLocalDateTime()), userId, size, (long) page * size);
        return new Page(items, page, size, count(kind, userId));
    }
    public void removeAccountData(long userId) {
        jdbc.update("delete from profile_images where user_id = ?", userId);
        // Delete dependants before parents; the service wraps the entire operation in one transaction.
        jdbc.update("delete from saved_recipes where user_id = ? or recipe_id in (select id from recipes where author_id = ?)", userId, userId);
        jdbc.update("delete from comments where user_id = ? or recipe_id in (select id from recipes where author_id = ?)", userId, userId);
        jdbc.update("delete from recipe_ingredients where recipe_id in (select id from recipes where author_id = ?)", userId);
        jdbc.update("delete from recipe_steps where recipe_id in (select id from recipes where author_id = ?)", userId);
        jdbc.update("delete from recipes where author_id = ?", userId);
        jdbc.update("delete from user_fridge where user_id = ?", userId);
        jdbc.update("delete from password_reset_tokens where user_id = ?", userId);
    }
    public void removeResetTokens(long userId) {
        jdbc.update("delete from password_reset_tokens where user_id = ?", userId);
    }
}
