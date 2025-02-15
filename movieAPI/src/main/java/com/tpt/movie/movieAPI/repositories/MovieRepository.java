package com.tpt.movie.movieAPI.repositories;

import com.tpt.movie.movieAPI.entities.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Integer> {
}
