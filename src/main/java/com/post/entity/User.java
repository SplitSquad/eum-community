package com.post.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "user")
@NoArgsConstructor
public class User {
    @Id
    private Long userId;

    private String name;
    private String nation;
    private String language;
    private String role;
    private String address;
    private int ban;

    @Builder
    public User(Long userId, String address, String role, String language, String nation, String name, int ban) {
        this.userId = userId;
        this.address = address;
        this.role = role;
        this.language = language;
        this.nation = nation;
        this.name = name;
        this.ban = ban;
    }
}
