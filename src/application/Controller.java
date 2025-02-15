package application;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

public class Controller implements Initializable {

    @FXML
    private VBox pnItems = null;
    @FXML
    private Button btnHome;

    @FXML
    private Button btnDatasheets;

    @FXML
    private Button btnVisualizations;

    @FXML
    private Button btnSettings;

    @FXML
    private Button btnSignout;

    @FXML
    private Pane pnlHome;

    @FXML
    private Pane pnlDatasheets;

    @FXML
    private Pane pnlVisualizations;

    @FXML
    private Pane pnlSignout;

    @FXML
    private Pane pnlSettings;

    @FXML
    private DatePicker datePicker;

    @FXML
    private TextField amountField;

    @FXML
    private ComboBox<String> detailComboBox;

    @FXML
    private ComboBox<String> typeComboBox;

    @FXML
    private PieChart incomePieChart;

    @FXML
    private PieChart expensePieChart;
    
    @FXML
    private LineChart<String, Number> lineChart;
    
    @FXML
    private LineChart<String, Number> balanceLineChart;

    private double balance = 0;

    private Map<String, Double> incomeMap = new HashMap<>();
    private Map<String, Double> expenseMap = new HashMap<>();
    private Map<LocalDate, Double> incomeData = new TreeMap<>();
    private Map<LocalDate, Double> expenseData = new TreeMap<>();
    private Map<LocalDate, Double> balanceData = new TreeMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pnlHome.setStyle("-fx-background-color : #1620A1");
        pnlHome.toFront();
        typeComboBox.setItems(FXCollections.observableArrayList("Income", "Expense"));
    }

    public void handleClicks(ActionEvent actionEvent) {
        if (actionEvent.getSource() == btnHome) {
            pnlHome.setStyle("-fx-background-color : #1620A1");
            pnlHome.toFront();
        }
        if (actionEvent.getSource() == btnDatasheets) {
            pnlDatasheets.setStyle("-fx-background-color : #02030A");
            pnlDatasheets.toFront();
        }
        if (actionEvent.getSource() == btnVisualizations) {
            pnlVisualizations.setStyle("-fx-background-color : #02030A");
            pnlVisualizations.toFront();
        }
        if (actionEvent.getSource() == btnSignout) {
            pnlSignout.setStyle("-fx-background-color : #000000");
            pnlSignout.toFront();
        }
        if (actionEvent.getSource() == btnSettings) {
            pnlSettings.setStyle("-fx-background-color : #000000");
            pnlSettings.toFront();
        }
    }

    @FXML
    private void handleAddEntry(ActionEvent event) {
        String date = datePicker.getValue().toString();
        String amount = amountField.getText();
        String detail = detailComboBox.getValue();
        String type = typeComboBox.getValue();

        if (amount == null || amount.isEmpty()) {
            // Display error message
            return;
        }

        double amountValue;
        try {
            amountValue = Double.parseDouble(amount);
        } catch (NumberFormatException e) {
            // Display error message
            return;
        }

        if ("Expense".equals(type)) {
            amountValue = -amountValue;
            expenseMap.put(detail, expenseMap.getOrDefault(detail, 0.0) + Math.abs(amountValue));
        } else {
            incomeMap.put(detail, incomeMap.getOrDefault(detail, 0.0) + amountValue);
        }
        balance += amountValue;
        String balanceStr = String.format("%.2f", balance);

        boolean exists = detailComboBox.getItems().stream().anyMatch(existingDetail -> existingDetail.equalsIgnoreCase(detail));
        if (!exists) {
            detailComboBox.getItems().add(detail.toLowerCase());
        }
        
        boolean isIncome = "Income".equals(type);
        updateIncomeExpenseData(datePicker.getValue(), amountValue, isIncome);
        updateBalanceData(datePicker.getValue(), amountValue);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Item.fxml"));
            Node node = loader.load();
            ItemController itemController = loader.getController();
            itemController.setData(date, detail, String.valueOf(amountValue), balanceStr, this);
            pnItems.getChildren().add(node);
        } catch (IOException e) {
            e.printStackTrace();
        }

        updatePieCharts();
        updateLineChart();
        updateBalanceLineChart();
    }
    
    private void updateIncomeExpenseData(LocalDate date, double amount, boolean isIncome) {
        if (isIncome) {
            incomeData.put(date, incomeData.getOrDefault(date, 0.0) + amount);
        } else {
            expenseData.put(date, expenseData.getOrDefault(date, 0.0) + amount);
        }
    }
    
    private void updateBalanceData(LocalDate date, double amount) {
        balance += amount;
        balanceData.put(date, balance);
    }
    
    @SuppressWarnings("unchecked")
	private void updateLineChart() {
        lineChart.getData().clear();
        
        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Income");
        
        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Expense");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        
        for (Map.Entry<LocalDate, Double> entry : incomeData.entrySet()) {
            incomeSeries.getData().add(new XYChart.Data<>(entry.getKey().format(formatter), entry.getValue()));
        }
        
        for (Map.Entry<LocalDate, Double> entry : expenseData.entrySet()) {
            expenseSeries.getData().add(new XYChart.Data<>(entry.getKey().format(formatter), entry.getValue()));
        }
        
        lineChart.getData().addAll(incomeSeries, expenseSeries);
    }
    
    private void updateBalanceLineChart() {
        balanceLineChart.getData().clear();
        
        XYChart.Series<String, Number> balanceSeries = new XYChart.Series<>();
        balanceSeries.setName("Balance");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (Map.Entry<LocalDate, Double> entry : balanceData.entrySet()) {
            balanceSeries.getData().add(new XYChart.Data<>(entry.getKey().format(formatter), entry.getValue()));
        }
        
        balanceLineChart.getData().add(balanceSeries);
    }

    private void updatePieCharts() {
        ObservableList<PieChart.Data> incomeData = FXCollections.observableArrayList();
        for (Map.Entry<String, Double> entry : incomeMap.entrySet()) {
            incomeData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }
        incomePieChart.setData(incomeData);
        applyDetails(incomePieChart, "Income");

        ObservableList<PieChart.Data> expenseData = FXCollections.observableArrayList();
        for (Map.Entry<String, Double> entry : expenseMap.entrySet()) {
            expenseData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }
        expensePieChart.setData(expenseData);
        applyDetails(expensePieChart, "Expense");
    }

    private void applyDetails(PieChart pieChart, String chartType) {
        double total = pieChart.getData().stream().mapToDouble(PieChart.Data::getPieValue).sum();
        for (PieChart.Data data : pieChart.getData()) {
            // Calculate percentage
            double percentage = (data.getPieValue() / total) * 100;
            percentage = Math.round(percentage * 100) / 100;
            
            data.setName(String.format("%s: %s", data.getName(), String.valueOf(percentage)));

            System.out.println(chartType + " Chart - Data: " + data.getName() + ", Value: " + data.getPieValue() + ", Percentage: " + percentage);

        }
    }


    public void deleteRow(Node node) {
        Label amountLabel = (Label) node.lookup("#amountLabel");

        boolean isExpense = amountLabel.getText().indexOf("-") != -1;
        double amountValue = Double.parseDouble(amountLabel.getText().replace("+", "").replace("-", ""));

        if (isExpense) {
            balance += Math.abs(amountValue);
        } else {
            balance -= Math.abs(amountValue);
        }

        pnItems.getChildren().remove(node);

        updateBalances();
    }

    private void updateBalances() {
        balance = 0;
        for (Node item : pnItems.getChildren()) {
            Label amountLabel = (Label) item.lookup("#amountLabel");
            double amount = Double.parseDouble(amountLabel.getText().replace("$", "").replace(",", ""));
            balance += amount;
            Label balanceLabel = (Label) item.lookup("#balanceLabel");
            balanceLabel.setText(String.format("%.2f", balance));
        }
    }

    @FXML
    private void handleQuitAction(ActionEvent event) {
        Platform.exit();
    }

    @FXML
    private void handleExport(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(((Node) event.getSource()).getScene().getWindow());

        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                // Write the header
                writer.write("Date,Detail,Amount,Balance");
                writer.newLine();

                // Write each entry
                for (Node item : pnItems.getChildren()) {
                    Label dateLabel = (Label) item.lookup("#dateLabel");
                    Label detailLabel = (Label) item.lookup("#detailLabel");
                    Label amountLabel = (Label) item.lookup("#amountLabel");
                    Label balanceLabel = (Label) item.lookup("#balanceLabel");

                    String date = dateLabel.getText();
                    String detail = detailLabel.getText();
                    String amountText = amountLabel.getText().replace("$", "").replace(",", "");
                    String balance = balanceLabel.getText();

                    double amount = Double.parseDouble(amountText);

                    // Ensure expenses are negative
                    if (amount > 0 && "Expense".equalsIgnoreCase(detail)) {
                        amount = -amount;
                    }

                    String line = String.format("%s,%s,%.2f,%s", date, detail, amount, balance);
                    writer.write(line);
                    writer.newLine();
                }
                System.out.println("Data exported to CSV file successfully.");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleImport(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());

        if (file != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                double balance = 0;
                boolean isFirstLine = true;
                pnItems.getChildren().clear();
                balance = 0; // Reset balance
                incomeMap.clear(); // Clear income map
                expenseMap.clear(); // Clear expense map
                while ((line = reader.readLine()) != null) {
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue; // Skip the header
                    }

                    String[] parts = line.split(",");
                    if (parts.length < 4) {
                        continue;
                    }

                    String date = parts[0];
                    String detail = parts[1];
                    double amount = Double.parseDouble(parts[2]);
                    balance += amount;
                    String balanceStr = String.format("%.2f", balance);

                    // Update balance
                    this.balance = balance;
                    
                    updateIncomeExpenseData(LocalDate.parse(parts[0], DateTimeFormatter.ofPattern("dd-MM-yyyy")), amount, amount>0);
                    updateBalanceData(LocalDate.parse(parts[0], DateTimeFormatter.ofPattern("dd-MM-yyyy")), amount);

                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("Item.fxml"));
                        Node node = loader.load();
                        ItemController itemController = loader.getController();
                        itemController.setData(date, detail, String.valueOf(amount), balanceStr, this);
                        pnItems.getChildren().add(node);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    
                    boolean exists = detailComboBox.getItems().stream().anyMatch(existingDetail -> existingDetail.equalsIgnoreCase(detail));
                    if (!exists) {
                    	detailComboBox.getItems().add(detail);
                    }

                    if (amount < 0) {
                        expenseMap.put(detail, expenseMap.getOrDefault(detail, 0.0) + Math.abs(amount));
                    } else {
                        incomeMap.put(detail, incomeMap.getOrDefault(detail, 0.0) + amount);
                    }
                }

                updatePieCharts();
                updateLineChart();
                updateBalanceLineChart();

                System.out.println("Data imported from CSV file successfully.");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
