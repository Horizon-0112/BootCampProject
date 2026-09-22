package com.xnaver.project.service;

import com.xnaver.project.entity.ProfileImage;
import com.xnaver.project.entity.UserEntity;
import com.xnaver.project.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import javax.imageio.ImageIO;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProfileImageService {
    private static final long MAX_BYTES = 5 * 1024 * 1024;
    private final UserRepository users;
    private final ProfileImageRepository images;

    private UserEntity user(String email) {
        return users.findForUpdateByEmail(email).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "다시 로그인해 주세요."));
    }

    @Transactional
    public void upload(String email, MultipartFile file) {
        byte[] data = normalize(file);
        var user = user(email);
        var existing = images.findById(user.getIdx());
        if (existing.isPresent()) existing.get().replace(data);
        else images.save(new ProfileImage(user, data));
        images.flush();
    }

    @Transactional(readOnly = true)
    public byte[] read(String email) {
        var user = users.findByEmail(email).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "다시 로그인해 주세요."));
        return images.findById(user.getIdx()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "등록된 프로필 사진이 없습니다.")).getData();
    }

    @Transactional
    public void delete(String email) {
        var user = user(email);
        images.findById(user.getIdx()).ifPresent(images::delete);
        images.flush();
    }

    private AuthValidationException invalid(String message) {
        return new AuthValidationException(Map.of("file", message));
    }

    private byte[] normalize(MultipartFile file) {
        if (file == null || file.isEmpty()) throw invalid("사진을 선택해 주세요.");
        if (file.getSize() > MAX_BYTES) throw invalid("사진은 5MB 이하로 선택해 주세요.");
        try (var stream = ImageIO.createImageInputStream(new ByteArrayInputStream(file.getBytes()))) {
            var readers = ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) throw invalid("JPG 또는 PNG 이미지 파일을 선택해 주세요.");
            var reader = readers.next();
            try {
                String format = reader.getFormatName();
                if (!format.equalsIgnoreCase("JPEG") && !format.equalsIgnoreCase("PNG"))
                    throw invalid("JPG 또는 PNG 이미지만 등록할 수 있습니다.");
                reader.setInput(stream, true, true);
                int width = reader.getWidth(0), height = reader.getHeight(0);
                if (width < 1 || height < 1 || width > 8192 || height > 8192 || (long) width * height > 16_000_000)
                    throw invalid("이미지는 최대 1,600만 화소, 가로·세로 8,192픽셀 이하여야 합니다.");
                BufferedImage source = reader.read(0);
                BufferedImage result = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
                var graphics = result.createGraphics();
                try {
                    graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    int side = Math.min(width, height), x = (width - side) / 2, y = (height - side) / 2;
                    graphics.drawImage(source, 0, 0, 512, 512, x, y, x + side, y + side, null);
                } finally { graphics.dispose(); }
                var output = new ByteArrayOutputStream();
                ImageIO.write(result, "png", output);
                return output.toByteArray();
            } finally { reader.dispose(); }
        } catch (IOException | IllegalArgumentException e) {
            if (e instanceof AuthValidationException validation) throw validation;
            throw invalid("이미지를 읽을 수 없습니다. 다른 JPG 또는 PNG 파일을 선택해 주세요.");
        }
    }
}
