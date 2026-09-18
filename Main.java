import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.*;

class Movie {
    String title, genres, keywords, overview;
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

class OMDbMovie {
    String title, year, director, actors, imdbRating, poster;

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

public class Main extends JFrame {

    static ArrayList<Movie> movies = new ArrayList<>();

    JComboBox<String> movieBox;
    JTextArea resultArea;
    JLabel posterLabel, statusLabel;
    JButton recommendButton;

    public Main() {
        createGUI();
    }

    // ---------------- LOAD CSV ----------------

    static void loadMovies(String fileName) {

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {

            String header = br.readLine();

            if (header == null) {
                System.out.println("CSV file is empty.");
                return;
            }

            ArrayList<String> headers = parseCSV(header);

            int titleIndex = findColumn(headers, "title");
            int genresIndex = findColumn(headers, "genres");
            int keywordsIndex = findColumn(headers, "keywords");
            int overviewIndex = findColumn(headers, "overview");
            int ratingIndex = findColumn(headers, "vote_average");

            if (titleIndex == -1 || genresIndex == -1 ||
                keywordsIndex == -1 || overviewIndex == -1) {

                System.out.println("Required columns are missing.");
                return;
            }

            String line;

            while ((line = br.readLine()) != null) {

                ArrayList<String> row = parseCSV(line);

                if (row.size() <= titleIndex)
                    continue;

                String title = getValue(row, titleIndex);

                if (title.isEmpty())
                    continue;

                String genres = getValue(row, genresIndex);
                String keywords = getValue(row, keywordsIndex);
                String overview = getValue(row, overviewIndex);

                double rating = 0;

                if (ratingIndex != -1) {
                    try {
                        rating = Double.parseDouble(
                                getValue(row, ratingIndex));
                    } catch (Exception e) {
                        rating = 0;
                    }
                }

                movies.add(new Movie(
                        title,
                        extractNames(genres),
                        extractNames(keywords),
                        overview,
                        rating
                ));
            }

            System.out.println("Movies loaded: " + movies.size());

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ---------------- CSV FUNCTIONS ----------------

    static int findColumn(ArrayList<String> headers, String name) {

        for (int i = 0; i < headers.size(); i++) {
            String h = headers.get(i)
                    .replace("\"", "")
                    .trim()
                    .toLowerCase();

            if (h.equals(name.toLowerCase()))
                return i;
        }

        return -1;
    }

    static String getValue(ArrayList<String> row, int index) {

        if (index >= 0 && index < row.size())
            return row.get(index)
                    .replace("\uFEFF", "")
                    .trim();

        return "";
    }

    static ArrayList<String> parseCSV(String line) {

        ArrayList<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quotes = false;

        for (int i = 0; i < line.length(); i++) {

            char c = line.charAt(i);

            if (c == '"') {

                if (quotes && i + 1 < line.length()
                        && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    quotes = !quotes;
                }

            } else if (c == ',' && !quotes) {

                values.add(current.toString());
                current.setLength(0);

            } else {
                current.append(c);
            }
        }

        values.add(current.toString());
        return values;
    }

    // ---------------- FEATURE EXTRACTION ----------------

    static String extractNames(String text) {

        if (text == null || text.isEmpty())
            return "";

        Pattern pattern =
                Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"");

        Matcher matcher = pattern.matcher(text);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            result.append(
                    matcher.group(1)
                            .toLowerCase()
                            .replace(" ", "_")
            ).append(" ");
        }

        if (result.length() == 0)
            return text.toLowerCase()
                    .replaceAll("[^a-zA-Z0-9 ]", " ");

        return result.toString();
    }

    // ---------------- TEXT VECTOR ----------------

    static Map<String, Integer> createVector(String text) {

        Map<String, Integer> vector = new HashMap<>();

        if (text == null)
            return vector;

        String[] words = text.toLowerCase()
                .replaceAll("[^a-zA-Z0-9_ ]", " ")
                .split("\\s+");

        for (String word : words) {

            if (!word.isEmpty())
                vector.put(
                        word,
                        vector.getOrDefault(word, 0) + 1
                );
        }

        return vector;
    }

    // ---------------- COSINE SIMILARITY ----------------

    static double cosineSimilarity(
            Map<String, Integer> a,
            Map<String, Integer> b) {

        Set<String> words = new HashSet<>();
        words.addAll(a.keySet());
        words.addAll(b.keySet());

        double dot = 0;
        double magA = 0;
        double magB = 0;

        for (String word : words) {

            int x = a.getOrDefault(word, 0);
            int y = b.getOrDefault(word, 0);

            dot += x * y;
            magA += x * x;
            magB += y * y;
        }

        if (magA == 0 || magB == 0)
            return 0;

        return dot / (Math.sqrt(magA) * Math.sqrt(magB));
    }

    // ---------------- RECOMMENDATIONS ----------------

    static ArrayList<MovieScore> recommend(Movie selected) {

        ArrayList<MovieScore> result = new ArrayList<>();

        String selectedText =
                selected.genres + " " +
                selected.keywords + " " +
                selected.overview;

        Map<String, Integer> selectedVector =
                createVector(selectedText);

        for (Movie movie : movies) {

            if (movie == selected)
                continue;

            String text =
                    movie.genres + " " +
                    movie.keywords + " " +
                    movie.overview;

            double similarity = cosineSimilarity(
                    selectedVector,
                    createVector(text)
            );

            result.add(new MovieScore(movie, similarity));
        }

        result.sort(
                Comparator.comparingDouble(
                        MovieScore::getScore
                ).reversed()
        );

        return new ArrayList<>(
                result.subList(
                        0,
                        Math.min(5, result.size())
                )
        );
    }

    static class MovieScore {

        Movie movie;
        double score;

        MovieScore(Movie movie, double score) {
            this.movie = movie;
            this.score = score;
        }

        double getScore() {
            return score;
        }
    }

    // ---------------- OMDB API ----------------

    static OMDbMovie getOMDbMovie(String movieName) {

        try {

            String apiKey = "15c9be74";

            String title = URLEncoder.encode(
                    movieName,
                    StandardCharsets.UTF_8
            );

            String url =
                    "https://www.omdbapi.com/?" +
                    "apikey=" + apiKey +
                    "&t=" + title +
                    "&plot=short";

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .GET()
                            .build();

            HttpResponse<String> response =
                    client.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() != 200)
                return null;

            String json = response.body();

            if ("False".equalsIgnoreCase(
                    jsonValue(json, "Response")))
                return null;

            return new OMDbMovie(
                    jsonValue(json, "Title"),
                    jsonValue(json, "Year"),
                    jsonValue(json, "Director"),
                    jsonValue(json, "Actors"),
                    jsonValue(json, "imdbRating"),
                    jsonValue(json, "Poster")
            );

        } catch (Exception e) {

            System.out.println(
                    "OMDb Error: " + e.getMessage()
            );

            return null;
        }
    }

    static String jsonValue(String json, String key) {

        String pattern =
                "\"" + Pattern.quote(key) +
                "\"\\s*:\\s*\"([^\"]*)\"";

        Matcher matcher =
                Pattern.compile(pattern)
                        .matcher(json);

        if (matcher.find()) {

            return matcher.group(1)
                    .replace("\\/", "/")
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");
        }

        return "";
    }

    // ---------------- GUI ----------------

    void createGUI() {

        setTitle("Movie Recommendation System");
        setSize(1000, 700);
        setMinimumSize(new Dimension(850, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel main = new JPanel(
                new BorderLayout(15, 15)
        );

        main.setBackground(new Color(25, 25, 25));
        main.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel(
                "MOVIE RECOMMENDATION SYSTEM",
                SwingConstants.CENTER
        );

        title.setFont(
                new Font("Arial", Font.BOLD, 28)
        );

        title.setForeground(Color.WHITE);

        main.add(title, BorderLayout.NORTH);

        JPanel controls = new JPanel(
                new BorderLayout(10, 10)
        );

        controls.setBackground(
                new Color(25, 25, 25)
        );

        JLabel label = new JLabel("Select a Movie:");
        label.setForeground(Color.WHITE);
        label.setFont(
                new Font("Arial", Font.BOLD, 16)
        );

        controls.add(label, BorderLayout.WEST);

        movieBox = new JComboBox<>();

        for (Movie movie : movies)
            movieBox.addItem(movie.title);

        controls.add(movieBox, BorderLayout.CENTER);

        recommendButton = new JButton("Recommend");

        controls.add(
                recommendButton,
                BorderLayout.EAST
        );

        main.add(
                controls,
                BorderLayout.PAGE_START
        );

        JPanel center = new JPanel(
                new BorderLayout(15, 15)
        );

        center.setBackground(
                new Color(25, 25, 25)
        );

        resultArea = new JTextArea();

        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);

        resultArea.setFont(
                new Font("Arial", Font.PLAIN, 16)
        );

        resultArea.setForeground(Color.WHITE);
        resultArea.setBackground(
                new Color(40, 40, 40)
        );

        resultArea.setBorder(
                new EmptyBorder(15, 15, 15, 15)
        );

        center.add(
                new JScrollPane(resultArea),
                BorderLayout.CENTER
        );

        posterLabel = new JLabel(
                "Poster",
                SwingConstants.CENTER
        );

        posterLabel.setForeground(Color.WHITE);

        posterLabel.setPreferredSize(
                new Dimension(250, 350)
        );

        center.add(
                posterLabel,
                BorderLayout.EAST
        );

        main.add(center, BorderLayout.CENTER);

        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(Color.LIGHT_GRAY);

        main.add(
                statusLabel,
                BorderLayout.SOUTH
        );

        recommendButton.addActionListener(
                e -> showRecommendations()
        );

        add(main);
    }

    // ---------------- SHOW RESULTS ----------------

    void showRecommendations() {

        if (movieBox.getSelectedItem() == null)
            return;

        String title =
                movieBox.getSelectedItem().toString();

        Movie selected = null;

        for (Movie movie : movies) {

            if (movie.title.equals(title)) {
                selected = movie;
                break;
            }
        }

        if (selected == null)
            return;

        Movie finalSelected = selected;

        recommendButton.setEnabled(false);
        statusLabel.setText(
                "Generating recommendations..."
        );

        resultArea.setText("Please wait...");
        posterLabel.setIcon(null);
        posterLabel.setText("Loading poster...");

        SwingWorker<Result, Void> worker =
                new SwingWorker<Result, Void>() {

            protected Result doInBackground()
                    throws Exception {

                ArrayList<MovieScore> recommendations =
                        recommend(finalSelected);

                OMDbMovie info =
                        getOMDbMovie(finalSelected.title);

                Image poster = null;

                if (info != null &&
                    info.poster != null &&
                    !info.poster.equals("N/A") &&
                    !info.poster.isEmpty()) {

                    try {
                        poster = ImageIO.read(
                                new URL(info.poster)
                        );
                    } catch (Exception ignored) {}
                }

                return new Result(
                        recommendations,
                        info,
                        poster
                );
            }

            protected void done() {

                try {
                    displayResults(
                            finalSelected,
                            get()
                    );

                } catch (Exception e) {

                    resultArea.setText(
                            "Error: " + e.getMessage()
                    );

                } finally {

                    recommendButton.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    static class Result {

        ArrayList<MovieScore> recommendations;
        OMDbMovie info;
        Image poster;

        Result(
                ArrayList<MovieScore> recommendations,
                OMDbMovie info,
                Image poster) {

            this.recommendations = recommendations;
            this.info = info;
            this.poster = poster;
        }
    }

    // ---------------- DISPLAY ----------------

    void displayResults(
            Movie selected,
            Result result) {

        StringBuilder output =
                new StringBuilder();

        output.append("SELECTED MOVIE\n");
        output.append("==============================\n");
        output.append("Title: ")
                .append(selected.title)
                .append("\n");
        output.append("Dataset Rating: ")
                .append(selected.rating)
                .append("\n\n");

        if (result.info != null) {

            output.append("OMDb DETAILS\n");
            output.append("==============================\n");
            output.append("Year: ")
                    .append(result.info.year)
                    .append("\n");
            output.append("Director: ")
                    .append(result.info.director)
                    .append("\n");
            output.append("Actors: ")
                    .append(result.info.actors)
                    .append("\n");
            output.append("IMDb Rating: ")
                    .append(result.info.imdbRating)
                    .append("\n\n");
        }

        output.append("TOP 5 RECOMMENDATIONS\n");
        output.append("==============================\n\n");

        int rank = 1;

        for (MovieScore score :
                result.recommendations) {

            output.append(rank++)
                    .append(". ")
                    .append(score.movie.title)
                    .append("\n");

            output.append("   Similarity Score: ")
                    .append(
                            String.format(
                                    "%.4f",
                                    score.score
                            )
                    )
                    .append("\n");

            output.append("   Dataset Rating: ")
                    .append(score.movie.rating)
                    .append("\n\n");
        }

        resultArea.setText(
                output.toString()
        );

        if (result.poster != null) {

            Image scaled =
                    result.poster.getScaledInstance(
                            250,
                            350,
                            Image.SCALE_SMOOTH
                    );

            posterLabel.setIcon(
                    new ImageIcon(scaled)
            );

            posterLabel.setText("");

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

    // ---------------- MAIN ----------------

    public static void main(String[] args) {

        loadMovies("movies.csv");

        if (movies.isEmpty()) {

            JOptionPane.showMessageDialog(
                    null,
                    "No movies were loaded.\n" +
                    "Make sure movies.csv is in the same folder."
            );

            return;
        }

        SwingUtilities.invokeLater(() -> {

            Main app = new Main();
            app.setVisible(true);
        });
    }
}
