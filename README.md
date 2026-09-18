# Movie_recommendation_
# 🎬 Movie Recommendation System

A Java-based **Movie Recommendation System** that recommends movies based on their similarity to a selected movie.

The system uses a **content-based recommendation approach**, where movie genres, keywords, and overview are processed to calculate similarity using **Cosine Similarity**.

The application also integrates the **OMDb API** to display additional movie information such as posters, release year, director, actors, and IMDb rating.

---

## 📌 Project Overview

The Movie Recommendation System allows users to:

- Select a movie from a large movie dataset.
- Analyze the movie's genres, keywords, and overview.
- Calculate similarity between movies.
- Recommend the **Top 5 most similar movies**.
- Fetch additional movie information using the OMDb API.
- Display movie posters.
- Display IMDb ratings, directors, actors, and release year.

The project is implemented completely in **Java** using a graphical user interface built with **Java Swing**.

---

## 🎯 Objectives

The main objectives of this project are:

1. To develop a movie recommendation system using Java.
2. To implement a content-based recommendation algorithm.
3. To calculate movie similarity using Cosine Similarity.
4. To process movie information from a CSV dataset.
5. To integrate an external movie API.
6. To create a user-friendly graphical interface.
7. To display movie information and recommendations dynamically.

---

## 🧠 Recommendation Technique

This project uses **Content-Based Filtering**.

The recommendation is based on the characteristics of movies rather than user ratings or preferences.

### Features Used

The system uses:

- Genres
- Keywords
- Movie Overview

These features are combined to create a representation of each movie.

The similarity between movies is then calculated using **Cosine Similarity**.

### Basic Workflow

```text
Movie Dataset
     ↓
Extract Movie Features
     ↓
Genres + Keywords + Overview
     ↓
Create Feature Vectors
     ↓
Calculate Cosine Similarity
     ↓
Sort Movies by Similarity
     ↓
Select Top 5 Movies
     ↓
Display Recommendations
