package application;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class ItemController {

    @FXML
    private HBox itemC;

    @FXML
    private Label dateLabel;

    @FXML
    private Label detailLabel;

    @FXML
    private Label amountLabel;

    @FXML
    private Label balanceLabel;

    @FXML
    private Button deleteButton;

    private Controller mainController;

    public void setData(String date, String detail, String amount, String balance, Controller mainController) {
        this.mainController = mainController;
        dateLabel.setText(date);
        detailLabel.setText(detail);
        amountLabel.setText(amount);
        balanceLabel.setText(balance);
    }

    @FXML
    private void handleDelete() {
        mainController.deleteRow(itemC);
    }
}
