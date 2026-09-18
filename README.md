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
Display Recommendation



## 🛠️ Tech Stack

* **Language:** Java
* **Libraries:**
* **API:** OMDb API
* **Dataset:** movies.csv

---

## ⚙️ How It Works

1. The dataset is loaded and cleaned
2. Important features (genres, keywords, overview) are combined
3. Text data is converted into numerical form using CountVectorizer
4. Cosine similarity is calculated between movies
5. User selects a movie from the GUI
6. System recommends top 5 similar movies
7. Movie details and poster are fetched using API

---

## 📂 Project Structure

```
├── Movie Reccomandation.py
├── movies.csv
├── PROJECT REPORT JAVA.docx
└── README.md
```

---

## 🚀 Installation & Setup

### 1️⃣ Clone the repository

```
git clone https://github.com/your-username/movie-recommendation-system.git
cd movie-recommendation-system
```

### 2️⃣ Install dependencies


### 3️⃣ Add Dataset

* Place `movies.csv` in the project folder
* Update file path in code if required

### 4️⃣ Add API Key

* Get API key from: http://www.omdbapi.com/
* Replace in code:

```python
API_KEY = "YOUR_API_KEY"
```

### 5️⃣ Run the project

```
Java "Main.java"
```

---

## 🖥️ Output

* Select a movie from dropdown
* Click **Recommend**
* Get:

  * ✅ Top 5 recommended movies
  * 🎬 Movie poster
  * 📅 Year
  * 🎭 Actors
  * 🎬 Director
  * ⭐ IMDb rating

---

## 📊 Algorithm Used

* Content-Based Filtering
* Count Vectorization
* Cosine Similarity

---

## ✅ Advantages

* Simple and easy to use
* No user data required
* Fast recommendations
* Interactive GUI

---

## ⚠️ Limitations

* No personalized recommendations
* Depends on dataset quality
* Requires internet for API
* Limited diversity in results

---

## 🔮 Future Enhancements

* Add collaborative filtering
* Improve UI design
* Deploy as web app (Streamlit/Flask)
* Add user authentication
* Use deep learning models

---

## 🌍 Real-World Applications

* OTT platforms (Netflix, Prime Video)
* E-commerce recommendations
* Music streaming apps
* Content discovery systems

---

## 👨‍💻 Author

**Abhigyan Tiwari**
CSE (AI-ML)

---

## 📚 References

1. Oracle Java Documentation  
   Java SE Documentation – Classes, Collections, Swing, File Handling and HTTP Client  
   https://docs.oracle.com/en/java/

2. Java Swing Documentation  
   Oracle Java Swing Documentation  
   https://docs.oracle.com/javase/tutorial/uiswing/

3. Java HTTP Client Documentation  
   Oracle Java Documentation – `java.net.http.HttpClient`  
   https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpClient.html

4. OMDb API  
   Open Movie Database API – Used for retrieving movie details, IMDb ratings and posters  
   https://www.omdbapi.com/

5. OMDb API Documentation  
   OMDb API Documentation and API Key Information  
   https://www.omdbapi.com/apikey.aspx

6. Scikit-learn Documentation  
   Cosine Similarity and Machine Learning Concepts  
   https://scikit-learn.org/

7. Kaggle  
   Dataset and machine learning resources  
   https://www.kaggle.com/

8. GitHub  
   Source-code hosting and project version control  
   https://github.com/

9. Apache Commons CSV Documentation  
   Reference for CSV data processing concepts  
   https://commons.apache.org/proper/commons-csv/

10. Recommender Systems – Content-Based Filtering  
    General reference for content-based recommendation systems and similarity-based recommendations.

---

## ⭐ If you like this project

Give it a ⭐ on GitHub!
