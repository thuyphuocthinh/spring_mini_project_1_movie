package com.tpt.movie.movieAPI.service;

import com.tpt.movie.movieAPI.dto.MovieDto;
import com.tpt.movie.movieAPI.dto.MoviePageResponse;
import com.tpt.movie.movieAPI.entities.Movie;
import com.tpt.movie.movieAPI.exceptions.MovieNotFoundException;
import com.tpt.movie.movieAPI.repositories.MovieRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class MovieServiceImpl implements MovieService{

    private final MovieRepository movieRepository;

    private final FileService fileService;

    @Value("${project.posters}")
    // path to directory
    private String path;

    @Value("${base.url}")
    private String baseUrl;

    public MovieServiceImpl(MovieRepository movieRepository, FileService fileService) {
        this.movieRepository = movieRepository;
        this.fileService = fileService;
    }

    @Override
    public MovieDto addMovie(MovieDto movieDto, MultipartFile file) throws IOException {
        // Upload file
        if(Files.exists(Paths.get(this.path + File.separator + file.getOriginalFilename()))) {
            throw new FileAlreadyExistsException("File already exists! Please enter another file name!");
        }

        String fileName = fileService.uploadFile(path, file);

        // set the value of field poster as filename
        movieDto.setPoster(fileName);

        // map dto to movie object
        // insert => set id to null to help JPA know that save is for insert not update
        // if id is not null => save in this case is for update
        Movie movie = new Movie(
                null,
                movieDto.getTitle(),
                movieDto.getDirector(),
                movieDto.getStudio(),
                movieDto.getMovieCast(),
                movieDto.getReleaseYear(),
                movieDto.getPoster()
        );
        // save to db
        Movie savedMovie = movieRepository.save(movie);

        // generate the posterUrl = localhost+...
        String posterUrl = this.baseUrl + "/file/" + fileName;

        // map Movie object to dto object and return it

        return new MovieDto(
                savedMovie.getMovieId(),
                savedMovie.getTitle(),
                savedMovie.getDirector(),
                savedMovie.getStudio(),
                savedMovie.getMovieCast(),
                savedMovie.getReleaseYear(),
                savedMovie.getPoster(),
                posterUrl
        );
    }

    @Override
    public MovieDto getMovie(Integer movieId) {
        // 1. check the data in DB and if exists, fetch the data of given ID
        Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new MovieNotFoundException("Movie not found"));
        // 2. generate poster url
        String posterUrl = this.baseUrl + "/file/" + movie.getPoster();
        // 3. mapping
        return new MovieDto(
                movie.getMovieId(),
                movie.getTitle(),
                movie.getDirector(),
                movie.getStudio(),
                movie.getMovieCast(),
                movie.getReleaseYear(),
                movie.getPoster(),
                posterUrl
        );
    }

    @Override
    public List<MovieDto> getAllMovies() {
        // 1. fetch all data from db
        List<Movie> movieList = movieRepository.findAll();
        List<MovieDto> movieDtoList = new ArrayList<>();
        // 2. iterate through the list => generate poster url for each movie object and mapp to movie dto
        for(Movie movie : movieList) {
            String posterUrl = this.baseUrl + "/file/" + movie.getPoster();
            movieDtoList.add(new MovieDto(
                    movie.getMovieId(),
                    movie.getTitle(),
                    movie.getDirector(),
                    movie.getStudio(),
                    movie.getMovieCast(),
                    movie.getReleaseYear(),
                    movie.getPoster(),
                    posterUrl
            ));
        }
        return movieDtoList;
    }

    @Override
    public MovieDto updateMovie(Integer movieId, MovieDto movieDto, MultipartFile file) throws IOException {
        // 1. Check if movie exists with movieId
        Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new MovieNotFoundException("Movie not found"));

        // 2. If file is null, do nothing
        // if file exists, deleting existing file associated with the record
        // then, upload file
        String fileName = movie.getPoster();
        if(file != null) {
            Files.deleteIfExists(Paths.get(this.path + File.separator + fileName));
            fileName = fileService.uploadFile(this.path, file);
        }

        // 3. set movieDto poster according to step 2
        movieDto.setPoster(fileName);

        // 4. Map to Movie object
        Movie updatedMovie = new Movie(
                movie.getMovieId(),
                movieDto.getTitle(),
                movieDto.getDirector(),
                movieDto.getStudio(),
                movieDto.getMovieCast(),
                movieDto.getReleaseYear(),
                movieDto.getPoster()
        );

        // 5. Save
        Movie savedMovie = movieRepository.save(updatedMovie);

        // 6. Generate poster url
        String posterUrl = this.baseUrl + "/file/" + movie.getPoster();

        // 7. Mapping Movie to MovieDto => then return
        return new MovieDto(
                savedMovie.getMovieId(),
                savedMovie.getTitle(),
                savedMovie.getDirector(),
                savedMovie.getStudio(),
                savedMovie.getMovieCast(),
                savedMovie.getReleaseYear(),
                savedMovie.getPoster(),
                posterUrl
        );
    }

    @Override
    public String deleteMovie(Integer movieId) throws IOException {
        // Check if movie exists
        Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new RuntimeException("Movie not found"));

        // Delete file associated with this movie object
        Files.deleteIfExists(Paths.get(this.path + File.separator + movie.getPoster()));

        // Delete in db
        movieRepository.delete(movie);

        // return
        return "success";
    }

    @Override
    public MoviePageResponse getAllMoviesWithPagination(Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Page<Movie> movies = movieRepository.findAll(pageable);
        List<Movie> moviesList = movies.getContent();

        List<MovieDto> movieDtoList = new ArrayList<>();
        // 2. iterate through the list => generate poster url for each movie object and mapp to movie dto
        for(Movie movie : moviesList) {
            String posterUrl = this.baseUrl + "/file/" + movie.getPoster();
            movieDtoList.add(new MovieDto(
                    movie.getMovieId(),
                    movie.getTitle(),
                    movie.getDirector(),
                    movie.getStudio(),
                    movie.getMovieCast(),
                    movie.getReleaseYear(),
                    movie.getPoster(),
                    posterUrl
            ));
        }

        return new MoviePageResponse(
                movieDtoList,
                pageNumber,
                pageSize,
                movies.getTotalElements(),
                movies.getTotalPages(),
                movies.isLast()
        );
    }

    @Override
    public MoviePageResponse getAllMoviesWithPaginationAndSorting(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<Movie> movies = movieRepository.findAll(pageable);
        List<Movie> moviesList = movies.getContent();

        List<MovieDto> movieDtoList = new ArrayList<>();
        // 2. iterate through the list => generate poster url for each movie object and mapp to movie dto
        for(Movie movie : moviesList) {
            String posterUrl = this.baseUrl + "/file/" + movie.getPoster();
            movieDtoList.add(new MovieDto(
                    movie.getMovieId(),
                    movie.getTitle(),
                    movie.getDirector(),
                    movie.getStudio(),
                    movie.getMovieCast(),
                    movie.getReleaseYear(),
                    movie.getPoster(),
                    posterUrl
            ));
        }

        return new MoviePageResponse(
                movieDtoList,
                pageNumber,
                pageSize,
                movies.getTotalElements(),
                movies.getTotalPages(),
                movies.isLast()
        );
    }
}
