package com.example.demo;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class OnlineQuizApplication extends Application {
    private Stage primaryStage;
    private Scene loginScene, userScene, adminScene;
    private Map<String, String> users = new HashMap<>();
    private Map<String, String> admins = new HashMap<>();
    private List<Quiz> quizzes = new ArrayList<>();
    private Map<String, List<QuizAttempt>> userQuizAttempts = new HashMap<>();
    private String currentUser;
    private TextField usernameField;
    private PasswordField passwordField;

    public static void main(String[] args) {
        launch(args);
    }

    class Quiz {
        private String title;
        private List<Question> questions;

        public Quiz(String title) {
            this.title = title;
            this.questions = new ArrayList<>();
        }

        public void addQuestion(Question question) {
            questions.add(question);
        }

        public String getTitle() {
            return title;
        }

        public List<Question> getQuestions() {
            return questions;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setQuestions(List<Question> questions) {
            this.questions = questions;
        }
    }

    public class QuizAttempt {
        private String username;
        private String quizTitle;
        private int score;
        private int totalQuestions;
        private LocalDateTime timestamp;

        public QuizAttempt(String username, String quizTitle, int score, int totalQuestions) {
            this.username = username;
            this.quizTitle = quizTitle;
            this.score = score;
            this.totalQuestions = totalQuestions;
            this.timestamp = LocalDateTime.now();
        }

        public String getUsername() {
            return username;
        }

        public String getQuizTitle() {
            return quizTitle;
        }

        public int getScore() {
            return score;
        }

        public int getTotalQuestions() {
            return totalQuestions;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return String.format("Quiz: %s, Score: %d/%d, Date: %s",
                    quizTitle, score, totalQuestions, timestamp.toString());
        }
    }

    class Question {
        private String title;
        private List<String> options;
        private List<Integer> correctAnswers;

        public Question(String title, List<String> options, List<Integer> correctAnswers) {
            this.title = title;
            this.options = options;
            this.correctAnswers = correctAnswers;
        }

        public String getTitle() {
            return title;
        }

        public List<String> getOptions() {
            return options;
        }

        public List<Integer> getCorrectAnswers() {
            return correctAnswers;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setOptions(List<String> options) {
            this.options = options;
        }

        public void setCorrectAnswers(List<Integer> correctAnswers) {
            this.correctAnswers = correctAnswers;
        }
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Quiz Application");

        // Add a default admin
        admins.put("admin", "admin123");

        createLoginScene();
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    private void createLoginScene() {
        VBox loginLayout = new VBox(10);
        loginLayout.setPadding(new Insets(20));
        loginLayout.setAlignment(Pos.CENTER);

        usernameField = new TextField();
        usernameField.setPromptText("Username");
        passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register");

        loginButton.setOnAction(e -> login(usernameField.getText(), passwordField.getText()));
        registerButton.setOnAction(e -> register(usernameField.getText(), passwordField.getText()));

        loginLayout.getChildren().addAll(
                new Label("Quiz Application"),
                usernameField,
                passwordField,
                loginButton,
                registerButton
        );

        loginScene = new Scene(loginLayout, 300, 250);
    }


    private void login(String username, String password) {
        if (admins.containsKey(username) && admins.get(username).equals(password)) {
            currentUser = username;  // Set currentUser here
            showAdminScene();
        } else if (users.containsKey(username) && users.get(username).equals(password)) {
            currentUser = username;  // Set currentUser here
            showUserScene();
        } else {
            showAlert("Login Failed", "Invalid username or password.");
        }
    }

    // Method to register a new user or admin
    private void register(String username, String password) {
        if (users.containsKey(username) || admins.containsKey(username)) {
            showAlert("Registration Failed", "Username already exists.");
        } else {
            users.put(username, password);
            showAlert("Registration Successful", "You can now log in with your credentials.");
        }
    }

    private void showAdminScene() {
        VBox adminLayout = new VBox(10);
        adminLayout.setPadding(new Insets(20));

        Button createQuizButton = new Button("Create Quiz");
        Button editQuizButton = new Button("Edit Quiz");
        Button deleteQuizButton = new Button("Delete Quiz");
        Button logoutButton = new Button("Logout");

        createQuizButton.setOnAction(e -> showCreateQuizDialog());
        editQuizButton.setOnAction(e -> showEditQuizDialog());
        deleteQuizButton.setOnAction(e -> showDeleteQuizDialog());
        logoutButton.setOnAction(e -> {
            currentUser = null;
            usernameField.clear();
            passwordField.clear();
            primaryStage.setScene(loginScene);
        });

        adminLayout.getChildren().addAll(
                new Label("Admin Panel"),
                createQuizButton,
                editQuizButton,
                deleteQuizButton,
                logoutButton
        );

        adminScene = new Scene(adminLayout, 300, 250);
        primaryStage.setScene(adminScene);
    }

    private void showCreateQuizDialog() {
        Dialog<Quiz> dialog = new Dialog<>();
        dialog.setTitle("Create Quiz");

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField quizTitle = new TextField();
        quizTitle.setPromptText("Quiz Title");

        grid.add(new Label("Quiz Title:"), 0, 0);
        grid.add(quizTitle, 1, 0);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return new Quiz(quizTitle.getText());
            }
            return null;
        });

        Optional<Quiz> result = dialog.showAndWait();

        result.ifPresent(quiz -> {
            quizzes.add(quiz);
            showQuestionDialog(quiz);
        });
    }

    private void showQuestionDialog(Quiz quiz) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Post Questions");

        ButtonType postButtonType = new ButtonType("Post", ButtonBar.ButtonData.OK_DONE);
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OTHER);
        dialog.getDialogPane().getButtonTypes().addAll(postButtonType, addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField questionTitle = new TextField();
        questionTitle.setPromptText("Question Title");

        TextField option1 = new TextField();
        option1.setPromptText("Option 1");
        TextField option2 = new TextField();
        option2.setPromptText("Option 2");
        TextField option3 = new TextField();
        option3.setPromptText("Option 3");
        TextField option4 = new TextField();
        option4.setPromptText("Option 4");

        CheckBox correct1 = new CheckBox("Correct");
        CheckBox correct2 = new CheckBox("Correct");
        CheckBox correct3 = new CheckBox("Correct");
        CheckBox correct4 = new CheckBox("Correct");

        grid.add(new Label("Question:"), 0, 0);
        grid.add(questionTitle, 1, 0);
        grid.add(new Label("Option 1:"), 0, 1);
        grid.add(option1, 1, 1);
        grid.add(correct1, 2, 1);
        grid.add(new Label("Option 2:"), 0, 2);
        grid.add(option2, 1, 2);
        grid.add(correct2, 2, 2);
        grid.add(new Label("Option 3:"), 0, 3);
        grid.add(option3, 1, 3);
        grid.add(correct3, 2, 3);
        grid.add(new Label("Option 4:"), 0, 4);
        grid.add(option4, 1, 4);
        grid.add(correct4, 2, 4);

        dialog.getDialogPane().setContent(grid);

        // A list to hold all questions added so far
        List<Question> questions = new ArrayList<>();

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                // Add the current question to the quiz
                addQuestionToQuiz(quiz, questionTitle, option1, option2, option3, option4, correct1, correct2, correct3, correct4);

                // Clear input fields for the next question
                questionTitle.clear();
                option1.clear();
                option2.clear();
                option3.clear();
                option4.clear();
                correct1.setSelected(false);
                correct2.setSelected(false);
                correct3.setSelected(false);
                correct4.setSelected(false);

                // Reopen the dialog to allow more questions
                dialog.showAndWait();
            } else if (dialogButton == postButtonType) {
                // Add the last question before posting
                addQuestionToQuiz(quiz, questionTitle, option1, option2, option3, option4, correct1, correct2, correct3, correct4);
                return dialogButton;
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void addQuestionToQuiz(Quiz quiz, TextField questionTitle,
                                   TextField option1, TextField option2, TextField option3, TextField option4,
                                   CheckBox correct1, CheckBox correct2, CheckBox correct3, CheckBox correct4) {
        List<String> options = Arrays.asList(option1.getText(), option2.getText(), option3.getText(), option4.getText());
        List<Integer> correctAnswers = new ArrayList<>();
        if (correct1.isSelected()) correctAnswers.add(0);
        if (correct2.isSelected()) correctAnswers.add(1);
        if (correct3.isSelected()) correctAnswers.add(2);
        if (correct4.isSelected()) correctAnswers.add(3);

        if (!questionTitle.getText().isEmpty()) {
            Question question = new Question(questionTitle.getText(), options, correctAnswers);
            quiz.addQuestion(question);
        }
    }
    private void showEditQuizDialog() {
        ChoiceDialog<Quiz> dialog = new ChoiceDialog<>(null, quizzes);
        dialog.setTitle("Edit Quiz");
        dialog.setHeaderText("Select a quiz to edit");
        dialog.setContentText("Quiz:");

        Optional<Quiz> result = dialog.showAndWait();
        result.ifPresent(this::editQuiz);
    }

    private void editQuiz(Quiz quiz) {
        // Dialog to edit each question in the quiz
        for (Question question : quiz.getQuestions()) {
            editQuestionDialog(quiz, question);
        }
    }

    private void editQuestionDialog(Quiz quiz, Question question) {
        Dialog<Question> dialog = new Dialog<>();
        dialog.setTitle("Edit Question");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Edit the question title
        TextField questionTitleField = new TextField(question.getTitle());
        grid.add(new Label("Question:"), 0, 0);
        grid.add(questionTitleField, 1, 0);

        // Edit the options
        TextField option1 = new TextField(question.getOptions().get(0));
        TextField option2 = new TextField(question.getOptions().get(1));
        TextField option3 = new TextField(question.getOptions().get(2));
        TextField option4 = new TextField(question.getOptions().get(3));

        CheckBox correct1 = new CheckBox("Correct");
        CheckBox correct2 = new CheckBox("Correct");
        CheckBox correct3 = new CheckBox("Correct");
        CheckBox correct4 = new CheckBox("Correct");

        // Set the correct answers based on the current question's correct answers
        if (question.getCorrectAnswers().contains(0)) correct1.setSelected(true);
        if (question.getCorrectAnswers().contains(1)) correct2.setSelected(true);
        if (question.getCorrectAnswers().contains(2)) correct3.setSelected(true);
        if (question.getCorrectAnswers().contains(3)) correct4.setSelected(true);

        // Add the options and correct answer checkboxes to the grid
        grid.add(new Label("Option 1:"), 0, 1);
        grid.add(option1, 1, 1);
        grid.add(correct1, 2, 1);

        grid.add(new Label("Option 2:"), 0, 2);
        grid.add(option2, 1, 2);
        grid.add(correct2, 2, 2);

        grid.add(new Label("Option 3:"), 0, 3);
        grid.add(option3, 1, 3);
        grid.add(correct3, 2, 3);

        grid.add(new Label("Option 4:"), 0, 4);
        grid.add(option4, 1, 4);
        grid.add(correct4, 2, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // Update the question title
                question.setTitle(questionTitleField.getText());

                // Update the options
                List<String> updatedOptions = Arrays.asList(option1.getText(), option2.getText(), option3.getText(), option4.getText());
                question.setOptions(updatedOptions);

                // Update the correct answers
                List<Integer> correctAnswers = new ArrayList<>();
                if (correct1.isSelected()) correctAnswers.add(0);
                if (correct2.isSelected()) correctAnswers.add(1);
                if (correct3.isSelected()) correctAnswers.add(2);
                if (correct4.isSelected()) correctAnswers.add(3);
                question.setCorrectAnswers(correctAnswers);

                return question;
            }
            return null;
        });

        Optional<Question> result = dialog.showAndWait();

        result.ifPresent(updatedQuestion -> {
            showAlert("Question Updated", "The question has been updated successfully.");
        });
    }

    private void showDeleteQuizDialog() {
        ChoiceDialog<Quiz> dialog = new ChoiceDialog<>(null, quizzes);
        dialog.setTitle("Delete Quiz");
        dialog.setHeaderText("Select a quiz to delete");
        dialog.setContentText("Quiz:");

        Optional<Quiz> result = dialog.showAndWait();
        result.ifPresent(quiz -> {
            quizzes.remove(quiz);
            showAlert("Quiz Deleted", "The quiz has been deleted.");
        });
    }

    private void showUserScene() {
        VBox userLayout = new VBox(10);
        userLayout.setPadding(new Insets(20));

        Button takeQuizButton = new Button("Take Quiz");
        Button viewHistoryButton = new Button("View Quiz History");
        Button logoutButton = new Button("Logout");

        takeQuizButton.setOnAction(e -> showTakeQuizDialog());
        viewHistoryButton.setOnAction(e -> showQuizHistory());
        logoutButton.setOnAction(e -> {
            currentUser = null;
            usernameField.clear();
            passwordField.clear();
            primaryStage.setScene(loginScene);
        });

        userLayout.getChildren().addAll(
                new Label("User Panel"),
                takeQuizButton,
                viewHistoryButton,
                logoutButton
        );

        userScene = new Scene(userLayout, 300, 250);
        primaryStage.setScene(userScene);
    }
    private void showTakeQuizDialog() {
        if (quizzes.isEmpty()) {
            showAlert("No Quizzes", "There are no quizzes available to take.");
            return;
        }

        ChoiceDialog<Quiz> dialog = new ChoiceDialog<>(null, quizzes);
        dialog.setTitle("Take Quiz");
        dialog.setHeaderText("Select a quiz to take");
        dialog.setContentText("Quiz:");

        Optional<Quiz> result = dialog.showAndWait();
        result.ifPresent(this::takeQuiz);
    }

    private void takeQuiz(Quiz quiz) {
        VBox quizLayout = new VBox(10);
        quizLayout.setPadding(new Insets(20));

        Label quizTitle = new Label(quiz.getTitle());
        quizLayout.getChildren().add(quizTitle);

        List<Question> questions = quiz.getQuestions();
        int[] currentQuestionIndex = {0};
        int[] score = {0};

        Label questionLabel = new Label();
        VBox optionsBox = new VBox(5);
        Button submitButton = new Button("Submit Answer");

        quizLayout.getChildren().addAll(questionLabel, optionsBox, submitButton);

        Scene quizScene = new Scene(new ScrollPane(quizLayout), 400, 500);

        List<CheckBox> checkBoxes = new ArrayList<>();

        Runnable displayQuestion = () -> {
            if (currentQuestionIndex[0] < questions.size()) {
                Question question = questions.get(currentQuestionIndex[0]);
                questionLabel.setText(String.format("Question %d of %d: %s",
                        currentQuestionIndex[0] + 1, questions.size(), question.getTitle()));

                optionsBox.getChildren().clear();
                checkBoxes.clear();

                for (String option : question.getOptions()) {
                    CheckBox checkBox = new CheckBox(option);
                    checkBoxes.add(checkBox);
                    optionsBox.getChildren().add(checkBox);
                }

                submitButton.setDisable(false);
            } else {
                showQuizResults(quiz, score[0]);
            }
        };

        submitButton.setOnAction(e -> {
            Question currentQuestion = questions.get(currentQuestionIndex[0]);
            List<Integer> selectedAnswers = new ArrayList<>();

            for (int i = 0; i < checkBoxes.size(); i++) {
                if (checkBoxes.get(i).isSelected()) {
                    selectedAnswers.add(i);
                }
            }

            boolean isCorrect = selectedAnswers.equals(currentQuestion.getCorrectAnswers());
            if (isCorrect) {
                score[0]++;
            }

            showFeedback(isCorrect, currentQuestion.getCorrectAnswers());

            currentQuestionIndex[0]++;
            displayQuestion.run();
        });

        displayQuestion.run();

        primaryStage.setScene(quizScene);
    }

    private void showFeedback(boolean isCorrect, List<Integer> correctAnswers) {
        String feedbackMessage = isCorrect ? "Correct!" : "Incorrect. The correct answer(s) were: " + correctAnswers.toString();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Question Feedback");
        alert.setHeaderText(null);
        alert.setContentText(feedbackMessage);
        alert.showAndWait();
    }

    private void showQuizResults(Quiz quiz, int score) {
        String resultMessage = String.format("Quiz completed!\n\nYour score: %d out of %d\nPercentage: %.2f%%",
                score, quiz.getQuestions().size(), (double)score / quiz.getQuestions().size() * 100);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Quiz Results");
        alert.setHeaderText("Quiz: " + quiz.getTitle());
        alert.setContentText(resultMessage);
        alert.showAndWait();

        // Record the quiz attempt with the current username
        QuizAttempt attempt = new QuizAttempt(currentUser, quiz.getTitle(), score, quiz.getQuestions().size());
        userQuizAttempts.computeIfAbsent(currentUser, k -> new ArrayList<>()).add(attempt);

        primaryStage.setScene(userScene);
    }

    private void showQuizHistory() {
        List<QuizAttempt> attempts = userQuizAttempts.getOrDefault(currentUser, new ArrayList<>());

        if (attempts.isEmpty()) {
            showAlert("No Quiz History", "You haven't taken any quizzes yet.");
            return;
        }

        VBox historyLayout = new VBox(10);
        historyLayout.setPadding(new Insets(20));

        Label usernameLabel = new Label("Quiz History for: " + currentUser);
        usernameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        historyLayout.getChildren().add(usernameLabel);

        Label historyTitle = new Label("Your Quiz Attempts");
        historyTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        historyLayout.getChildren().add(historyTitle);

        TableView<QuizAttempt> historyTable = new TableView<>();

        TableColumn<QuizAttempt, String> usernameColumn = new TableColumn<>("Username");
        usernameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));

        TableColumn<QuizAttempt, String> quizColumn = new TableColumn<>("Quiz");
        quizColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getQuizTitle()));

        TableColumn<QuizAttempt, String> scoreColumn = new TableColumn<>("Score");
        scoreColumn.setCellValueFactory(cellData -> {
            QuizAttempt attempt = cellData.getValue();
            return new SimpleStringProperty(String.format("%d/%d (%.2f%%)",
                    attempt.getScore(), attempt.getTotalQuestions(),
                    (double)attempt.getScore() / attempt.getTotalQuestions() * 100));
        });

        TableColumn<QuizAttempt, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        ));

        historyTable.getColumns().addAll(usernameColumn, quizColumn, scoreColumn, dateColumn);
        historyTable.getItems().addAll(attempts);

        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button backButton = new Button("Back to User Panel");
        backButton.setOnAction(e -> primaryStage.setScene(userScene));

        historyLayout.getChildren().addAll(historyTable, backButton);

        Scene historyScene = new Scene(new ScrollPane(historyLayout), 600, 400);
        primaryStage.setScene(historyScene);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}