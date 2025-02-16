package com.tpt.movie.movieAPI.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tpt.movie.movieAPI.dto.MovieDto;
import com.tpt.movie.movieAPI.dto.MoviePageResponse;
import com.tpt.movie.movieAPI.exceptions.EmptyFileException;
import com.tpt.movie.movieAPI.service.MovieService;
import com.tpt.movie.movieAPI.utils.AppConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/movie")
public class MovieController {
    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @PostMapping("/add-movie")
    public ResponseEntity<MovieDto> addMovieHandler(@RequestPart MultipartFile file, @RequestPart String movieDtoStr) throws IOException, EmptyFileException {
        if(file.isEmpty()) {
            throw new EmptyFileException("File is empty!");
        }
        MovieDto movieDto = convertToMovieDto(movieDtoStr);
        return new ResponseEntity<MovieDto>(movieService.addMovie(movieDto, file), HttpStatus.CREATED);
    }

    private MovieDto convertToMovieDto(String movieDtoStr) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(movieDtoStr, MovieDto.class);
    }

    @GetMapping("/{movieId}")
    public ResponseEntity<MovieDto> getMovieHandler(@PathVariable Integer movieId) {
        return new ResponseEntity<MovieDto>(movieService.getMovie(movieId), HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<MovieDto>> getAllMovieHandler() {
        return new ResponseEntity<List<MovieDto>>(movieService.getAllMovies(), HttpStatus.OK);
    }

    @PutMapping("/update/${movieId}")
    public ResponseEntity<MovieDto> updateMovieHandler(@PathVariable Integer movieId, @RequestPart String movieDtoStr, @RequestPart MultipartFile file) throws IOException {
        if(file.isEmpty()) file = null;
        MovieDto movieDto = convertToMovieDto(movieDtoStr);
        return ResponseEntity.ok(movieService.updateMovie(movieId, movieDto, file));
    }

    @DeleteMapping("/delete/${movieId}")
    public ResponseEntity<String> deleteMovieHandler(@PathVariable Integer movieId) throws IOException {
        return ResponseEntity.ok(movieService.deleteMovie(movieId));
    }

    @GetMapping("/with-pagination")
    public ResponseEntity<MoviePageResponse> getMoviesWithPagination(
            @RequestParam(defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize
    ) {
        return ResponseEntity.ok(movieService.getAllMoviesWithPagination(pageNumber, pageSize));
    }

    @GetMapping("/with-pagination-sorting")
    public ResponseEntity<MoviePageResponse> getMoviesWithPaginationAndSort(
            @RequestParam(defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(defaultValue = AppConstants.SORT_BY, required = false) String sortBy,
            @RequestParam(defaultValue = AppConstants.SORT_DIR, required = false) String sortDir
    ) {
        return ResponseEntity.ok(movieService.getAllMoviesWithPaginationAndSorting(pageNumber, pageSize, sortBy, sortDir));
    }
}
