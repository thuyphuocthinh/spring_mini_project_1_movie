package com.tpt.movie.movieAPI.auth.utils;

import lombok.Builder;

@Builder
public record ChangePassword(
        String password,
        String repeatPassword
) {
}
