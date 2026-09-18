import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Comparator;
import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Image;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/*
 * ============================================================
 * MOVIE CLASS
 * ============================================================
 */
class Movie {

    String title;
    String genres;
    String keywords;
    String overview;
    double rating;

    Movie(String title, String genres, String keywords,
          String overview, double rating) {

        this.title = title;
        this.genres = genres;
        this.keywords = keywords;
        this.overview = overview;
        this.rating = rating;
    }
}


/*
 * ============================================================
 * OMDB MOVIE CLASS
 * ============================================================
 */
class OMDbMovie {

    String title;
    String year;
    String director;
    String actors;
    String imdbRating;
    String poster;

    OMDbMovie(String title, String year, String director,
              String actors, String imdbRating, String poster) {

        this.title = title;
        this.year = year;
        this.director = director;
        this.actors = actors;
        this.imdbRating = imdbRating;
        this.poster = poster;
    }
}


/*
 * ============================================================
 * MAIN CLASS
 * ============================================================
 */
public class Main extends JFrame {

    // ---------------------------------------------------------
    // Global movie list
    // ---------------------------------------------------------

    static ArrayList<Movie> movies = new ArrayList<>();


    // ---------------------------------------------------------
    // GUI components
    // ---------------------------------------------------------

    JComboBox<String> movieComboBox;

    JTextArea resultArea;

    JLabel statusLabel;

    JLabel posterLabel;

    JButton recommendButton;


    // ---------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------

    public Main() {

        createGUI();
    }


    // =========================================================
    // LOAD MOVIES FROM CSV
    // =========================================================

    static void loadMovies(String fileName) {

        try {

            BufferedReader br =
                    new BufferedReader(new FileReader(fileName));

            String headerLine = br.readLine();

            if (headerLine == null) {

                System.out.println("CSV file is empty.");
                br.close();
                return;
            }


            // -------------------------------------------------
            // Find column positions
            // -------------------------------------------------

            ArrayList<String> headers = parseCSV(headerLine);

            int titleIndex =
                    findColumn(headers, "title");

            int genresIndex =
                    findColumn(headers, "genres");

            int keywordsIndex =
                    findColumn(headers, "keywords");

            int overviewIndex =
                    findColumn(headers, "overview");

            int ratingIndex =
                    findColumn(headers, "vote_average");


            // -------------------------------------------------
            // Check required columns
            // -------------------------------------------------

            if (titleIndex == -1) {

                System.out.println("Title column not found.");
                br.close();
                return;
            }


            if (genresIndex == -1) {

                System.out.println("Genres column not found.");
                br.close();
                return;
            }


            if (keywordsIndex == -1) {

                System.out.println("Keywords column not found.");
                br.close();
                return;
            }


            if (overviewIndex == -1) {

                System.out.println("Overview column not found.");
                br.close();
                return;
            }


            // -------------------------------------------------
            // Read every movie
            // -------------------------------------------------

            String line;

            while ((line = br.readLine()) != null) {

                ArrayList<String> row = parseCSV(line);


                if (row.size() <= titleIndex) {
                    continue;
                }


                String title =
                        getValue(row, titleIndex);


                if (title.trim().isEmpty()) {
                    continue;
                }


                String genres =
                        getValue(row, genresIndex);

                String keywords =
                        getValue(row, keywordsIndex);

                String overview =
                        getValue(row, overviewIndex);


                double rating = 0.0;


                if (ratingIndex != -1) {

                    try {

                        rating =
                                Double.parseDouble(
                                        getValue(row, ratingIndex)
                                );

                    } catch (Exception e) {

                        rating = 0.0;
                    }
                }


                Movie movie =
                        new Movie(
                                title,
                                extractNames(genres),
                                extractNames(keywords),
                                overview,
                                rating
                        );


                movies.add(movie);
            }


            br.close();


            System.out.println(
                    "Movies loaded successfully: "
                            + movies.size()
            );

        } catch (Exception e) {

            System.out.println(
                    "Error loading CSV: "
                            + e.getMessage()
            );
        }
    }


    // =========================================================
    // FIND COLUMN
    // =========================================================

    static int findColumn(
            ArrayList<String> headers,
            String columnName) {

        for (int i = 0; i < headers.size(); i++) {

            String header =
                    headers.get(i)
                            .trim()
                            .replace("\"", "")
                            .toLowerCase();

            if (header.equals(columnName.toLowerCase())) {

                return i;
            }
        }

        return -1;
    }


    // =========================================================
    // GET CSV VALUE
    // =========================================================

    static String getValue(
            ArrayList<String> row,
            int index) {

        if (index >= 0 && index < row.size()) {

            return row.get(index)
                    .replace("\uFEFF", "")
                    .trim();
        }

        return "";
    }


    // =========================================================
    // CSV PARSER
    // =========================================================

    static ArrayList<String> parseCSV(String line) {

        ArrayList<String> values =
                new ArrayList<>();

        StringBuilder current =
                new StringBuilder();

        boolean insideQuotes = false;


        for (int i = 0; i < line.length(); i++) {

            char c = line.charAt(i);


            if (c == '"') {

                if (insideQuotes
                        && i + 1 < line.length()
                        && line.charAt(i + 1) == '"') {

                    current.append('"');
                    i++;

                } else {

                    insideQuotes = !insideQuotes;
                }

            }

            else if (c == ',' && !insideQuotes) {

                values.add(current.toString());
                current.setLength(0);

            }

            else {

                current.append(c);
            }
        }


        values.add(current.toString());


        return values;
    }


    // =========================================================
    // EXTRACT NAMES FROM JSON-LIKE GENRE/KEYWORD DATA
    // =========================================================

    static String extractNames(String text) {

        if (text == null || text.trim().isEmpty()) {

            return "";
        }


        StringBuilder result =
                new StringBuilder();


        Pattern pattern =
                Pattern.compile(
                        "\"name\"\\s*:\\s*\"([^\"]+)\""
                );


        Matcher matcher =
                pattern.matcher(text);


        while (matcher.find()) {

            result.append(
                    matcher.group(1)
                            .toLowerCase()
                            .replace(" ", "_")
            );

            result.append(" ");
        }


        // If no JSON-style names were found,
        // use the original text.

        if (result.length() == 0) {

            return text
                    .toLowerCase()
                    .replaceAll("[^a-zA-Z0-9 ]", " ");
        }


        return result.toString();
    }


    // =========================================================
    // CREATE WORD-FREQUENCY VECTOR
    // =========================================================

    static Map<String, Integer> createVector(
            String text) {

        Map<String, Integer> vector =
                new HashMap<>();


        if (text == null) {
            return vector;
        }


        String cleaned =
                text.toLowerCase()
                        .replaceAll("[^a-zA-Z0-9_ ]", " ");


        String[] words =
                cleaned.split("\\s+");


        for (String word : words) {

            if (word.trim().isEmpty()) {
                continue;
            }


            vector.put(
                    word,
                    vector.getOrDefault(word, 0) + 1
            );
        }


        return vector;
    }


    // =========================================================
    // COSINE SIMILARITY
    // =========================================================

    static double cosineSimilarity(
            Map<String, Integer> vector1,
            Map<String, Integer> vector2) {

        Set<String> allWords =
                new HashSet<>();


        allWords.addAll(vector1.keySet());
        allWords.addAll(vector2.keySet());


        double dotProduct = 0.0;

        double magnitude1 = 0.0;

        double magnitude2 = 0.0;


        for (String word : allWords) {

            int value1 =
                    vector1.getOrDefault(word, 0);

            int value2 =
                    vector2.getOrDefault(word, 0);


            dotProduct +=
                    value1 * value2;


            magnitude1 +=
                    value1 * value1;


            magnitude2 +=
                    value2 * value2;
        }


        if (magnitude1 == 0
                || magnitude2 == 0) {

            return 0.0;
        }


        return dotProduct /
                (Math.sqrt(magnitude1)
                        * Math.sqrt(magnitude2));
    }


    // =========================================================
    // FIND MOVIE
    // =========================================================

    static Movie findMovie(String title) {

        for (Movie movie : movies) {

            if (movie.title.equals(title)) {

                return movie;
            }
        }

        return null;
    }


    // =========================================================
    // CALCULATE RECOMMENDATIONS
    // =========================================================

    static ArrayList<MovieScore> calculateRecommendations(
            Movie selectedMovie) {


        ArrayList<MovieScore> scores =
                new ArrayList<>();


        String selectedText =
                selectedMovie.genres + " "
                        + selectedMovie.keywords + " "
                        + selectedMovie.overview;


        Map<String, Integer> selectedVector =
                createVector(selectedText);


        for (Movie movie : movies) {

            if (movie == selectedMovie) {
                continue;
            }


            String movieText =
                    movie.genres + " "
                            + movie.keywords + " "
                            + movie.overview;


            Map<String, Integer> movieVector =
                    createVector(movieText);


            double similarity =
                    cosineSimilarity(
                            selectedVector,
                            movieVector
                    );


            scores.add(
                    new MovieScore(
                            movie,
                            similarity
                    )
            );
        }


        // Sort by similarity

        scores.sort(
                Comparator.comparingDouble(
                        MovieScore::getScore
                ).reversed()
        );


        // Return top 5

        ArrayList<MovieScore> topFive =
                new ArrayList<>();


        for (int i = 0;
             i < Math.min(5, scores.size());
             i++) {

            topFive.add(scores.get(i));
        }


        return topFive;
    }


    // =========================================================
    // MOVIE SCORE CLASS
    // =========================================================

    static class MovieScore {

        Movie movie;

        double score;


        MovieScore(
                Movie movie,
                double score) {

            this.movie = movie;
            this.score = score;
        }


        double getScore() {

            return score;
        }
    }


    // =========================================================
    // OMDB API
    // =========================================================

    static OMDbMovie getOMDbMovie(
            String movieName) {

        try {

            // -------------------------------------------------
            // Read API key from environment variable
            // -------------------------------------------------

            String apiKey = "15c9be74";;


            if (apiKey == null
                    || apiKey.trim().isEmpty()) {

                System.out.println(
                        "OMDB_API_KEY environment variable "
                                + "is not set."
                );

                return null;
            }


            // -------------------------------------------------
            // Encode movie title
            // -------------------------------------------------

            String encodedTitle =
                    URLEncoder.encode(
                            movieName,
                            StandardCharsets.UTF_8
                    );


            // -------------------------------------------------
            // OMDb URL
            // -------------------------------------------------

            String url =
                    "https://www.omdbapi.com/"
                            + "?apikey="
                            + apiKey
                            + "&t="
                            + encodedTitle
                            + "&plot=short";


            // -------------------------------------------------
            // Create HTTP client
            // -------------------------------------------------

            HttpClient client =
                    HttpClient.newHttpClient();


            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .GET()
                            .build();


            // -------------------------------------------------
            // Send request
            // -------------------------------------------------

            HttpResponse<String> response =
                    client.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );


            if (response.statusCode() != 200) {

                System.out.println(
                        "OMDb HTTP Error: "
                                + response.statusCode()
                );

                return null;
            }


            String json =
                    response.body();


            // -------------------------------------------------
            // Check API response
            // -------------------------------------------------

            String responseStatus =
                    extractJSONValue(
                            json,
                            "Response"
                    );


            if ("False".equalsIgnoreCase(
                    responseStatus)) {

                System.out.println(
                        "Movie not found in OMDb: "
                                + movieName
                );

                return null;
            }


            // -------------------------------------------------
            // Extract movie information
            // -------------------------------------------------

            String title =
                    extractJSONValue(
                            json,
                            "Title"
                    );


            String year =
                    extractJSONValue(
                            json,
                            "Year"
                    );


            String director =
                    extractJSONValue(
                            json,
                            "Director"
                    );


            String actors =
                    extractJSONValue(
                            json,
                            "Actors"
                    );


            String imdbRating =
                    extractJSONValue(
                            json,
                            "imdbRating"
                    );


            String poster =
                    extractJSONValue(
                            json,
                            "Poster"
                    );


            return new OMDbMovie(
                    title,
                    year,
                    director,
                    actors,
                    imdbRating,
                    poster
            );


        } catch (Exception e) {

            System.out.println(
                    "OMDb API Error: "
                            + e.getMessage()
            );

            return null;
        }
    }


    // =========================================================
    // SIMPLE JSON VALUE EXTRACTOR
    // =========================================================

    static String extractJSONValue(
            String json,
            String key) {

        if (json == null) {
            return "";
        }


        String pattern =
                "\""
                        + Pattern.quote(key)
                        + "\"\\s*:\\s*\"([^\"]*)\"";


        Pattern compiledPattern =
                Pattern.compile(pattern);


        Matcher matcher =
                compiledPattern.matcher(json);


        if (matcher.find()) {

            return matcher.group(1)
                    .replace("\\/", "/")
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");
        }


        return "";
    }


    // =========================================================
    // CREATE GUI
    // =========================================================

    void createGUI() {

        setTitle(
                "Movie Recommendation System"
        );


        setSize(
                1000,
                700
        );


        setMinimumSize(
                new Dimension(850, 600)
        );


        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );


        setLocationRelativeTo(null);


        // -----------------------------------------------------
        // Main panel
        // -----------------------------------------------------

        JPanel mainPanel =
                new JPanel(
                        new BorderLayout(15, 15)
                );


        mainPanel.setBackground(
                new Color(25, 25, 25)
        );


        mainPanel.setBorder(
                new EmptyBorder(
                        20,
                        20,
                        20,
                        20
                )
        );


        // -----------------------------------------------------
        // Title
        // -----------------------------------------------------

        JLabel titleLabel =
                new JLabel(
                        "MOVIE RECOMMENDATION SYSTEM",
                        SwingConstants.CENTER
                );


        titleLabel.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        28
                )
        );


        titleLabel.setForeground(
                Color.WHITE
        );


        mainPanel.add(
                titleLabel,
                BorderLayout.NORTH
        );


        // -----------------------------------------------------
        // Top control panel
        // -----------------------------------------------------

        JPanel controlPanel =
                new JPanel(
                        new BorderLayout(10, 10)
                );


        controlPanel.setBackground(
                new Color(25, 25, 25)
        );


        JLabel selectLabel =
                new JLabel(
                        "Select a Movie:"
                );


        selectLabel.setForeground(
                Color.WHITE
        );


        selectLabel.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        16
                )
        );


        controlPanel.add(
                selectLabel,
                BorderLayout.WEST
        );


        // -----------------------------------------------------
        // Movie ComboBox
        // -----------------------------------------------------

        movieComboBox =
                new JComboBox<>();


        movieComboBox.setFont(
                new Font(
                        "Arial",
                        Font.PLAIN,
                        14
                )
        );


        for (Movie movie : movies) {

            movieComboBox.addItem(
                    movie.title
            );
        }


        controlPanel.add(
                movieComboBox,
                BorderLayout.CENTER
        );


        // -----------------------------------------------------
        // Recommend button
        // -----------------------------------------------------

        recommendButton =
                new JButton(
                        "Recommend"
                );


        recommendButton.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        14
                )
        );


        recommendButton.setFocusPainted(false);


        controlPanel.add(
                recommendButton,
                BorderLayout.EAST
        );


        mainPanel.add(
                controlPanel,
                BorderLayout.PAGE_START
        );


        // -----------------------------------------------------
        // Center panel
        // -----------------------------------------------------

        JPanel centerPanel =
                new JPanel(
                        new BorderLayout(15, 15)
                );


        centerPanel.setBackground(
                new Color(25, 25, 25)
        );


        // -----------------------------------------------------
        // Result area
        // -----------------------------------------------------

        resultArea =
                new JTextArea();


        resultArea.setEditable(false);


        resultArea.setLineWrap(true);


        resultArea.setWrapStyleWord(true);


        resultArea.setFont(
                new Font(
                        "Arial",
                        Font.PLAIN,
                        16
                )
        );


        resultArea.setForeground(
                Color.WHITE
        );


        resultArea.setBackground(
                new Color(40, 40, 40)
        );


        resultArea.setBorder(
                new EmptyBorder(
                        15,
                        15,
                        15,
                        15
                )
        );


        JScrollPane scrollPane =
                new JScrollPane(
                        resultArea
                );


        centerPanel.add(
                scrollPane,
                BorderLayout.CENTER
        );


        // -----------------------------------------------------
        // Poster panel
        // -----------------------------------------------------

        JPanel posterPanel =
                new JPanel(
                        new BorderLayout()
                );


        posterPanel.setBackground(
                new Color(25, 25, 25)
        );


        posterLabel =
                new JLabel(
                        "Poster",
                        SwingConstants.CENTER
                );


        posterLabel.setForeground(
                Color.WHITE
        );


        posterLabel.setPreferredSize(
                new Dimension(
                        250,
                        350
                )
        );


        posterPanel.add(
                posterLabel,
                BorderLayout.CENTER
        );


        centerPanel.add(
                posterPanel,
                BorderLayout.EAST
        );


        mainPanel.add(
                centerPanel,
                BorderLayout.CENTER
        );


        // -----------------------------------------------------
        // Status label
        // -----------------------------------------------------

        statusLabel =
                new JLabel(
                        "Ready"
                );


        statusLabel.setForeground(
                Color.LIGHT_GRAY
        );


        statusLabel.setFont(
                new Font(
                        "Arial",
                        Font.ITALIC,
                        13
                )
        );


        mainPanel.add(
                statusLabel,
                BorderLayout.SOUTH
        );


        // -----------------------------------------------------
        // Button Action
        // -----------------------------------------------------

        recommendButton.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(
                            ActionEvent e) {

                        showRecommendations();
                    }
                }
        );


        // -----------------------------------------------------
        // Add panel to frame
        // -----------------------------------------------------

        add(mainPanel);
    }


    // =========================================================
    // SHOW RECOMMENDATIONS
    // =========================================================

    void showRecommendations() {

        if (movieComboBox.getSelectedItem()
                == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a movie."
            );

            return;
        }


        String selectedTitle =
                movieComboBox
                        .getSelectedItem()
                        .toString();


        Movie selectedMovie =
                findMovie(selectedTitle);


        if (selectedMovie == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Movie not found."
            );

            return;
        }


        // -----------------------------------------------------
        // Disable button while processing
        // -----------------------------------------------------

        recommendButton.setEnabled(false);


        statusLabel.setText(
                "Calculating recommendations..."
        );


        resultArea.setText(
                "Please wait..."
        );


        posterLabel.setText(
                "Loading poster..."
        );


        posterLabel.setIcon(null);


        // -----------------------------------------------------
        // Background worker
        // -----------------------------------------------------

        SwingWorker<RecommendationResult, Void> worker =
                new SwingWorker<RecommendationResult, Void>() {


                    @Override
                    protected RecommendationResult doInBackground()
                            throws Exception {


                        // -------------------------------------
                        // Calculate recommendations
                        // -------------------------------------

                        ArrayList<MovieScore> recommendations =
                                calculateRecommendations(
                                        selectedMovie
                                );


                        // -------------------------------------
                        // Get OMDb information
                        // -------------------------------------

                        OMDbMovie omdbMovie =
                                getOMDbMovie(
                                        selectedMovie.title
                                );


                        // -------------------------------------
                        // Get poster image
                        // -------------------------------------

                        Image posterImage = null;


                        if (omdbMovie != null
                                && omdbMovie.poster != null
                                && !omdbMovie.poster.equals("N/A")
                                && !omdbMovie.poster.isEmpty()) {

                            try {

                                URL imageURL =
                                        new URL(
                                                omdbMovie.poster
                                        );


                                posterImage =
                                        ImageIO.read(
                                                imageURL
                                        );


                            } catch (Exception ex) {

                                posterImage = null;
                            }
                        }


                        return new RecommendationResult(
                                recommendations,
                                omdbMovie,
                                posterImage
                        );
                    }


                    @Override
                    protected void done() {

                        try {

                            RecommendationResult result =
                                    get();


                            displayResults(
                                    selectedMovie,
                                    result
                            );


                        } catch (Exception ex) {

                            resultArea.setText(
                                    "Error:\n"
                                            + ex.getMessage()
                            );


                            statusLabel.setText(
                                    "Error occurred."
                            );


                        } finally {

                            recommendButton.setEnabled(
                                    true
                            );
                        }
                    }
                };


        worker.execute();
    }


    // =========================================================
    // RECOMMENDATION RESULT CLASS
    // =========================================================

    static class RecommendationResult {

        ArrayList<MovieScore> recommendations;

        OMDbMovie omdbMovie;

        Image posterImage;


        RecommendationResult(
                ArrayList<MovieScore> recommendations,
                OMDbMovie omdbMovie,
                Image posterImage) {

            this.recommendations =
                    recommendations;

            this.omdbMovie =
                    omdbMovie;

            this.posterImage =
                    posterImage;
        }
    }


    // =========================================================
    // DISPLAY RESULTS
    // =========================================================

    void displayResults(
            Movie selectedMovie,
            RecommendationResult result) {


        StringBuilder output =
                new StringBuilder();


        // -----------------------------------------------------
        // Selected Movie
        // -----------------------------------------------------

        output.append(
                "SELECTED MOVIE\n"
        );


        output.append(
                "==============================\n"
        );


        output.append(
                "Title: "
                        + selectedMovie.title
                        + "\n"
        );


        output.append(
                "Dataset Rating: "
                        + selectedMovie.rating
                        + "\n\n"
        );


        // -----------------------------------------------------
        // OMDb Details
        // -----------------------------------------------------

        if (result.omdbMovie != null) {

            output.append(
                    "OMDb DETAILS\n"
            );


            output.append(
                    "==============================\n"
            );


            output.append(
                    "Year: "
                            + result.omdbMovie.year
                            + "\n"
            );


            output.append(
                    "Director: "
                            + result.omdbMovie.director
                            + "\n"
            );


            output.append(
                    "Actors: "
                            + result.omdbMovie.actors
                            + "\n"
            );


            output.append(
                    "IMDb Rating: "
                            + result.omdbMovie.imdbRating
                            + "\n\n"
            );
        }


        // -----------------------------------------------------
        // Recommendations
        // -----------------------------------------------------

        output.append(
                "TOP 5 RECOMMENDATIONS\n"
        );


        output.append(
                "==============================\n\n"
        );


        int rank = 1;


        for (MovieScore score :
                result.recommendations) {


            output.append(
                    rank
                            + ". "
                            + score.movie.title
                            + "\n"
            );


            output.append(
                    "   Similarity Score: "
                            + String.format(
                                    "%.4f",
                                    score.score
                            )
                            + "\n"
            );


            output.append(
                    "   Dataset Rating: "
                            + score.movie.rating
                            + "\n\n"
            );


            rank++;
        }


        resultArea.setText(
                output.toString()
        );


        // -----------------------------------------------------
        // Display poster
        // -----------------------------------------------------

        if (result.posterImage != null) {


            Image scaledImage =
                    result.posterImage.getScaledInstance(
                            250,
                            350,
                            Image.SCALE_SMOOTH
                    );


            posterLabel.setIcon(
                    new ImageIcon(
                            scaledImage
                    )
            );


            posterLabel.setText(
                    ""
            );


        } else {

            posterLabel.setIcon(null);


            posterLabel.setText(
                    "Poster not available"
            );
        }


        statusLabel.setText(
                "Recommendations generated successfully."
        );
    }


    // =========================================================
    // MAIN METHOD
    // =========================================================

    public static void main(
            String[] args) {


        // -----------------------------------------------------
        // Load CSV
        // -----------------------------------------------------

        loadMovies(
                "movies.csv"
        );


        // -----------------------------------------------------
        // Check if movies loaded
        // -----------------------------------------------------

        if (movies.isEmpty()) {

            JOptionPane.showMessageDialog(
                    null,
                    "No movies were loaded.\n\n"
                            + "Make sure movies.csv is in "
                            + "the same folder as Main.java."
            );

            return;
        }


        // -----------------------------------------------------
        // Start GUI
        // -----------------------------------------------------

        SwingUtilities.invokeLater(
                new Runnable() {

                    @Override
                    public void run() {

                        Main app =
                                new Main();

                        app.setVisible(true);
                    }
                }
        );
    }
}