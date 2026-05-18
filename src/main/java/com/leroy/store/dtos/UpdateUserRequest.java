package com.leroy.store.dtos;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String email;
    private String name;
}
