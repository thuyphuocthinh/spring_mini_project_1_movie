package com.tpt.movie.movieAPI.dto;

import java.util.List;

public record MoviePageResponse(List<MovieDto> movies,
                                Integer pageNumber,
                                Integer pageSize,
                                long totalElements,
                                int totalPages,
                                boolean isLast)
{ }
