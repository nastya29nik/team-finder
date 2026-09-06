package com.niknastacy.service;

import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;

@Service
public class AvatarService {

    private final String[] colors = {
            "#4A90E2", "#50E3C2", "#B8E986", "#F5A623",
            "#9013FE", "#E94E77", "#D68189", "#2ECC71"
    };

    public String generateAvatar(String name) {
        String letter = (name != null && !name.isBlank()) ? name.substring(0, 1).toUpperCase() : "?";
        String color = colors[new Random().nextInt(colors.length)];

        String svg = String.format(
                "<svg xmlns='http://www.w3.org/2000/svg' width='100' height='100' viewBox='0 0 100 100'>" +
                        "<rect width='100' height='100' rx='50' fill='%s'/>" +
                        "<text x='50%%' y='50%%' alignment-baseline='central' text-anchor='middle' fill='white' font-size='42' font-family='Arial, sans-serif' font-weight='bold'>%s</text>" +
                        "</svg>",
                color, letter
        );

        return "data:image/svg+xml;base64," + Base64.getEncoder().encodeToString(svg.getBytes(StandardCharsets.UTF_8));
    }
}
