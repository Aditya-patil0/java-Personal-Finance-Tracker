import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class PersonalFinanceTracker extends Application {
    private Connection connection;

    @Override
    public void start(Stage primaryStage) {
        connectToDatabase();

        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 20;");

        Label titleLabel = new Label("Personal Finance Tracker");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextField categoryField = new TextField();
        categoryField.setPromptText("Enter Category");

        TextField amountField = new TextField();
        amountField.setPromptText("Enter Amount");

        Button addButton = new Button("Add Expense");
        addButton.setOnAction(e -> addExpense(categoryField.getText(), Double.parseDouble(amountField.getText())));

        PieChart expenseChart = new PieChart();
        Button refreshButton = new Button("Refresh Summary");
        refreshButton.setOnAction(e -> updateChart(expenseChart));

        root.getChildren().addAll(titleLabel, categoryField, amountField, addButton, refreshButton, expenseChart);

        primaryStage.setScene(new Scene(root, 400, 500));
        primaryStage.setTitle("Finance Tracker");
        primaryStage.show();
    }

    private void connectToDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/FinanceTracker", "root", "password");
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Database Connection Failed!");
            alert.showAndWait();
            System.exit(1);
        }
    }

    private void addExpense(String category, double amount) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO Expenses (category, amount) VALUES (?, ?)")) {
            stmt.setString(1, category);
            stmt.setDouble(2, amount);
            stmt.executeUpdate();
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Expense Added Successfully!");
            alert.showAndWait();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateChart(PieChart chart) {
        Map<String, Double> expenses = new HashMap<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT category, SUM(amount) AS total FROM Expenses GROUP BY category")) {
            while (rs.next()) {
                expenses.put(rs.getString("category"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        chart.getData().clear();
        for (Map.Entry<String, Double> entry : expenses.entrySet()) {
            chart.getData().add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
