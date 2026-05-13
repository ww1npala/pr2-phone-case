package com.phonecase.ui.controller;

import com.phonecase.dto.DesignDTO;
import com.phonecase.dto.OrderDTO;
import com.phonecase.model.Category;
import com.phonecase.model.Order;
import com.phonecase.model.PhoneModel;
import com.phonecase.repository.PhoneModelRepository;
import com.phonecase.service.DesignService;
import com.phonecase.service.OrderService;
import com.phonecase.service.ReportService;
import com.phonecase.util.FXMLNavigator;
import com.phonecase.util.SessionManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.util.List;

/**
 * Головний контролер застосунку.
 * Реалізує MVVM: обробка подій, біндінг даних до таблиць, асинхронне завантаження.
 * Патерн Facade — делегує бізнес-логіку сервісному шару.
 */
public class MainController {

  private static final Logger logger = LoggerFactory.getLogger(MainController.class);
  @FXML private Label welcomeLabel;
  @FXML private Label roleLabel;
  @FXML private TabPane mainTabPane;

  @FXML private TableView<DesignDTO> designsTable;
  @FXML private TableColumn<DesignDTO, Integer> colDesignId;
  @FXML private TableColumn<DesignDTO, String>  colDesignName;
  @FXML private TableColumn<DesignDTO, String>  colDesignCategory;
  @FXML private TableColumn<DesignDTO, Double>  colDesignPrice;
  @FXML private TableColumn<DesignDTO, String>  colDesignAvailable;

  @FXML private TextField searchField;
  @FXML private ComboBox<Category> categoryFilter;
  @FXML private Button btnAddDesign;
  @FXML private Button btnEditDesign;
  @FXML private Button btnDeleteDesign;
  @FXML private Button btnExportDesigns;

  @FXML private TableView<OrderDTO> ordersTable;
  @FXML private TableColumn<OrderDTO, Integer> colOrderId;
  @FXML private TableColumn<OrderDTO, String>  colOrderUser;
  @FXML private TableColumn<OrderDTO, String>  colOrderDesign;
  @FXML private TableColumn<OrderDTO, String>  colOrderPhone;
  @FXML private TableColumn<OrderDTO, Integer> colOrderQty;
  @FXML private TableColumn<OrderDTO, Double>  colOrderPrice;
  @FXML private TableColumn<OrderDTO, String>  colOrderStatus;

  @FXML private Button btnExportOrders;
  @FXML private Button btnUpdateStatus;
  @FXML private ComboBox<Order.Status> statusFilter;
  @FXML private Label lblTotalDesigns;
  @FXML private Label lblTotalOrders;
  @FXML private Label lblTotalRevenue;
  @FXML private Label lblPendingOrders;
  private final DesignService designService;
  private final OrderService orderService;
  private final ReportService reportService;
  private final PhoneModelRepository phoneModelRepository;
  private final ObservableList<DesignDTO> designsList = FXCollections.observableArrayList();
  private final ObservableList<OrderDTO> ordersList   = FXCollections.observableArrayList();

  @Inject
  public MainController(DesignService designService, OrderService orderService,
      ReportService reportService, PhoneModelRepository phoneModelRepository) {
    this.designService = designService;
    this.orderService  = orderService;
    this.reportService = reportService;
    this.phoneModelRepository = phoneModelRepository;
  }

  @FXML
  public void initialize() {
    setupUserInfo();
    setupDesignsTable();
    setupOrdersTable();
    setupFilters();
    loadDataAsync();
    setupAdminAccess();
  }

  private void setupUserInfo() {
    if (SessionManager.getCurrentUser() != null) {
      welcomeLabel.setText("Вітаємо, " + SessionManager.getCurrentUser().getUsername() + "!");
      roleLabel.setText("Роль: " + SessionManager.getCurrentUser().getRole());
    }
  }

  private void setupDesignsTable() {
    colDesignId.setCellValueFactory(new PropertyValueFactory<>("id"));
    colDesignName.setCellValueFactory(new PropertyValueFactory<>("name"));
    colDesignCategory.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
    colDesignPrice.setCellValueFactory(data ->
        new SimpleDoubleProperty(data.getValue().getPrice()).asObject());
    colDesignAvailable.setCellValueFactory(data ->
        new SimpleStringProperty(data.getValue().isAvailable() ? "✓ Так" : "✗ Ні"));
    designsTable.setItems(designsList);
    designsTable.setPlaceholder(new Label("Дизайни не знайдено"));
    designsTable.setRowFactory(tv -> new TableRow<>() {
      @Override
      protected void updateItem(DesignDTO item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || empty) {
          setStyle("");
        } else if (!item.isAvailable()) {
          setStyle("-fx-background-color: #ffeeee;");
        } else {
          setStyle("");
        }
      }
    });
  }

  private void setupOrdersTable() {
    colOrderId.setCellValueFactory(new PropertyValueFactory<>("id"));
    colOrderUser.setCellValueFactory(new PropertyValueFactory<>("username"));
    colOrderDesign.setCellValueFactory(new PropertyValueFactory<>("designName"));
    colOrderPhone.setCellValueFactory(new PropertyValueFactory<>("phoneModelName"));
    colOrderQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
    colOrderPrice.setCellValueFactory(data ->
        new SimpleDoubleProperty(data.getValue().getTotalPrice()).asObject());
    colOrderStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

    ordersTable.setItems(ordersList);
    ordersTable.setPlaceholder(new Label("Замовлень поки немає"));
    ordersTable.setRowFactory(tv -> new TableRow<>() {
      @Override
      protected void updateItem(OrderDTO item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || empty) { setStyle(""); return; }
        switch (item.getStatusRaw() != null ? item.getStatusRaw() : "") {
          case "PENDING"   -> setStyle("-fx-background-color: #fff9e6;");
          case "CONFIRMED" -> setStyle("-fx-background-color: #e6f3ff;");
          case "SHIPPED"   -> setStyle("-fx-background-color: #e6ffe6;");
          case "DELIVERED" -> setStyle("-fx-background-color: #d4edda;");
          case "CANCELLED" -> setStyle("-fx-background-color: #ffeeee;");
          default          -> setStyle("");
        }
      }
    });
  }

  private void setupFilters() {
    if (categoryFilter != null) {
      List<Category> categories = designService.getAllCategories();
      Category allCat = new Category();
      allCat.setId(0);
      allCat.setName("Всі категорії");
      ObservableList<Category> catList = FXCollections.observableArrayList();
      catList.add(allCat);
      catList.addAll(categories);
      categoryFilter.setItems(catList);
      categoryFilter.getSelectionModel().selectFirst();
      categoryFilter.setOnAction(e -> filterDesigns());
    }

    if (searchField != null) {
      searchField.textProperty().addListener((obs, old, val) -> filterDesigns());
    }

    if (statusFilter != null) {
      ObservableList<Order.Status> statuses = FXCollections.observableArrayList();
      statuses.add(null);
      statuses.addAll(Order.Status.values());
      statusFilter.setItems(statuses);
      statusFilter.setPromptText("Всі статуси");
      statusFilter.setOnAction(e -> filterOrders());
    }
  }

  private void setupAdminAccess() {
    boolean isAdmin   = SessionManager.isAdmin();
    boolean isManager = "MANAGER".equals(SessionManager.getCurrentUser() != null
        ? SessionManager.getCurrentUser().getRole() : "");
    boolean canManage = isAdmin || isManager;
    if (btnAddDesign    != null) btnAddDesign.setVisible(isAdmin);
    if (btnEditDesign   != null) btnEditDesign.setVisible(isAdmin);
    if (btnDeleteDesign != null) btnDeleteDesign.setVisible(isAdmin);
    if (btnUpdateStatus != null) btnUpdateStatus.setVisible(canManage);
  }

  private void loadDataAsync() {
    Task<Void> task = new Task<>() {
      @Override
      protected Void call() {
        List<DesignDTO> designs = designService.getAllDesigns();
        List<OrderDTO>  orders  = orderService.getAllOrders();
        Platform.runLater(() -> {
          designsList.setAll(designs);
          ordersList.setAll(orders);
          updateStats(designs, orders);
        });
        return null;
      }
    };
    task.setOnFailed(e -> logger.error("Помилка завантаження даних: {}", task.getException().getMessage()));
    new Thread(task).start();
  }

  private void updateStats(List<DesignDTO> designs, List<OrderDTO> orders) {
    if (lblTotalDesigns != null)
      lblTotalDesigns.setText(String.valueOf(designs.size()));
    if (lblTotalOrders != null)
      lblTotalOrders.setText(String.valueOf(orders.size()));
    if (lblTotalRevenue != null)
      lblTotalRevenue.setText(String.format("%.2f грн", orderService.getTotalRevenue()));
    if (lblPendingOrders != null)
      lblPendingOrders.setText(String.valueOf(orderService.countByStatus(Order.Status.PENDING)));
  }


  @FXML
  public void handleSearch() {
    filterDesigns();
  }

  private void filterDesigns() {
    String query = searchField != null ? searchField.getText().trim() : "";
    Category selectedCat = categoryFilter != null ? categoryFilter.getValue() : null;

    Task<List<DesignDTO>> task = new Task<>() {
      @Override
      protected List<DesignDTO> call() {
        if (!query.isBlank()) {
          return designService.searchDesigns(query);
        } else if (selectedCat != null && selectedCat.getId() != 0) {
          return designService.getDesignsByCategory(selectedCat.getId());
        } else {
          return designService.getAllDesigns();
        }
      }
    };
    task.setOnSucceeded(e -> Platform.runLater(() -> designsList.setAll(task.getValue())));
    new Thread(task).start();
  }

  private void filterOrders() {
    Order.Status status = statusFilter != null ? statusFilter.getValue() : null;
    Task<List<OrderDTO>> task = new Task<>() {
      @Override
      protected List<OrderDTO> call() {
        if (status != null) {
          return orderService.getAllOrders().stream()
              .filter(o -> o.getStatus().equals(status.getDisplayName()))
              .toList();
        }
        return orderService.getAllOrders();
      }
    };
    task.setOnSucceeded(e -> Platform.runLater(() -> ordersList.setAll(task.getValue())));
    new Thread(task).start();
  }

  @FXML
  public void handleAddDesign() {
    showDesignDialog(null);
  }

  @FXML
  public void handleEditDesign() {
    DesignDTO selected = designsTable.getSelectionModel().getSelectedItem();
    if (selected == null) {
      showAlert("Оберіть дизайн для редагування.", Alert.AlertType.WARNING);
      return;
    }
    showDesignDialog(selected);
  }

  @FXML
  public void handleDeleteDesign() {
    DesignDTO selected = designsTable.getSelectionModel().getSelectedItem();
    if (selected == null) {
      showAlert("Оберіть дизайн для видалення.", Alert.AlertType.WARNING);
      return;
    }
    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
        "Видалити дизайн \"" + selected.getName() + "\"?",
        ButtonType.YES, ButtonType.NO);
    confirm.setTitle("Підтвердження");
    confirm.showAndWait().ifPresent(bt -> {
      if (bt == ButtonType.YES) {
        boolean deleted = designService.deleteDesign(selected.getId());
        if (deleted) {
          designsList.remove(selected);
          showAlert("Дизайн видалено.", Alert.AlertType.INFORMATION);
        } else {
          showAlert("Не вдалось видалити дизайн.", Alert.AlertType.ERROR);
        }
      }
    });
  }

  @FXML
  public void handleCreateOrder() {
    List<DesignDTO> available = designService.getAvailableDesigns();
    if (available.isEmpty()) {
      showAlert("Немає доступних дизайнів для замовлення.", Alert.AlertType.WARNING);
      return;
    }

    Dialog<Void> dialog = new Dialog<>();
    dialog.setTitle("Нове замовлення");
    dialog.setHeaderText("Оформлення замовлення чохла");

    ButtonType orderBtn = new ButtonType("Замовити", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(orderBtn, ButtonType.CANCEL);

    ComboBox<DesignDTO> designBox = new ComboBox<>(FXCollections.observableArrayList(available));
    designBox.setPromptText("Оберіть дизайн");
    designBox.setConverter(new javafx.util.StringConverter<>() {
      public String toString(DesignDTO d) { return d == null ? "" : d.getName() + " — " + d.getPrice() + " грн"; }
      public DesignDTO fromString(String s) { return null; }
    });

    ComboBox<com.phonecase.model.PhoneModel> modelBox = new ComboBox<>(
        FXCollections.observableArrayList(phoneModelRepository.findAll()));
    modelBox.setPromptText("Оберіть модель телефону");

    designBox.setOnAction(e -> {
      DesignDTO selected = designBox.getValue();
      if (selected != null) {
        List<com.phonecase.model.PhoneModel> compatible =
            designService.getDesignsByPhoneModel(selected.getId()).isEmpty()
                ? phoneModelRepository.findAll()
                : phoneModelRepository.findAll().stream()
                    .filter(pm -> designService.getDesignsByPhoneModel(pm.getId()).stream()
                        .anyMatch(d -> d.getId().equals(selected.getId())))
                    .toList();
        modelBox.setItems(FXCollections.observableArrayList(compatible.isEmpty()
            ? phoneModelRepository.findAll() : compatible));
      }
    });

    Spinner<Integer> qtySpinner = new Spinner<>(1, 99, 1);
    qtySpinner.setEditable(true);

    TextField phoneField = new TextField();
    phoneField.setPromptText("+380XXXXXXXXX");

    TextField addressField = new TextField();
    addressField.setPromptText("Місто, вулиця, будинок");

    Label priceLabel = new Label("Сума: —");
    priceLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2980b9;");

    javafx.beans.value.ChangeListener<Object> priceCalc = (obs, o, n) -> {
      if (designBox.getValue() != null) {
        double total = designBox.getValue().getPrice() * qtySpinner.getValue();
        priceLabel.setText(String.format("Сума: %.2f грн", total));
      }
    };
    designBox.valueProperty().addListener(priceCalc);
    qtySpinner.valueProperty().addListener(priceCalc);

    javafx.scene.layout.VBox box = new javafx.scene.layout.VBox(8,
        new Label("Дизайн:"), designBox,
        new Label("Модель телефону:"), modelBox,
        new Label("Кількість:"), qtySpinner,
        new Label("Контактний номер:"), phoneField,
        new Label("Адреса доставки:"), addressField,
        new javafx.scene.control.Separator(),
        priceLabel);
    box.setPadding(new javafx.geometry.Insets(15));
    box.setPrefWidth(380);
    dialog.getDialogPane().setContent(box);

    dialog.setResultConverter(btn -> {
      if (btn == orderBtn) {
        if (designBox.getValue() == null) { showAlert("Оберіть дизайн.", Alert.AlertType.WARNING); return null; }
        if (modelBox.getValue() == null) { showAlert("Оберіть модель телефону.", Alert.AlertType.WARNING); return null; }
        if (phoneField.getText().isBlank()) { showAlert("Введіть номер телефону.", Alert.AlertType.WARNING); return null; }
        if (addressField.getText().isBlank()) { showAlert("Введіть адресу доставки.", Alert.AlertType.WARNING); return null; }
        try {
          orderService.createOrder(
              SessionManager.getCurrentUser().getId(),
              designBox.getValue().getId(),
              modelBox.getValue().getId(),
              qtySpinner.getValue(),
              phoneField.getText().trim(),
              addressField.getText().trim());
          loadDataAsync();
          showAlert("✅ Замовлення успішно оформлено!\nМи зв'яжемось з вами найближчим часом.", Alert.AlertType.INFORMATION);
        }
        catch (Exception ex) {
          ex.printStackTrace();

          Throwable cause = ex.getCause();
          if (cause != null) {
            cause.printStackTrace();
          }

          showAlert(
              "Помилка:\n" + ex.getMessage() +
                  (cause != null ? "\nПричина: " + cause.getMessage() : ""),
              Alert.AlertType.ERROR
          );
        }
      }
      return null;
    });
    dialog.showAndWait();
  }

  @FXML
  public void handleUpdateStatus() {
    OrderDTO selected = ordersTable.getSelectionModel().getSelectedItem();
    if (selected == null) {
      showAlert("Оберіть замовлення зі списку.", Alert.AlertType.WARNING);
      return;
    }
    if ("CANCELLED".equals(selected.getStatusRaw()) || "DELIVERED".equals(selected.getStatusRaw())) {
      showAlert("Цей статус змінити неможливо.", Alert.AlertType.WARNING);
      return;
    }

    final Order.Status current = switch (selected.getStatusRaw()) {
      case "PENDING" -> Order.Status.PENDING;
      case "CONFIRMED" -> Order.Status.CONFIRMED;
      case "SHIPPED" -> Order.Status.SHIPPED;
      case "DELIVERED" -> Order.Status.DELIVERED;
      case "CANCELLED" -> Order.Status.CANCELLED;
      default -> Order.Status.PENDING;
    };

    Order.Status next = orderService.nextStatus(current);

    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
    confirm.setTitle("Змінити статус");
    confirm.setHeaderText("Замовлення #" + selected.getId() + " — " + selected.getDesignName());
    confirm.setContentText("Поточний статус: " + selected.getStatus() +
        "\n\nОберіть дію:");

    ButtonType btnNext    = new ButtonType("➡ " + next.getDisplayName());
    ButtonType btnCancel  = new ButtonType("❌ Скасувати замовлення");
    ButtonType btnCustom  = new ButtonType("⚙ Обрати статус...");
    ButtonType btnClose   = new ButtonType("Закрити", ButtonBar.ButtonData.CANCEL_CLOSE);

    confirm.getButtonTypes().setAll(btnNext, btnCancel, btnCustom, btnClose);
    confirm.showAndWait().ifPresent(bt -> {
      if (bt == btnNext) {
        orderService.updateStatus(selected.getId(), next);
        loadDataAsync();
      } else if (bt == btnCancel) {
        orderService.updateStatus(selected.getId(), Order.Status.CANCELLED);
        loadDataAsync();
      } else if (bt == btnCustom) {
        ChoiceDialog<Order.Status> choice = new ChoiceDialog<>(current, Order.Status.values());
        choice.setTitle("Обрати статус");
        choice.setHeaderText("Замовлення #" + selected.getId());
        choice.setContentText("Статус:");
        choice.showAndWait().ifPresent(s -> {
          orderService.updateStatus(selected.getId(), s);
          loadDataAsync();
        });
      }
    });
  }

  @FXML
  public void handleExportDesigns() {
    FileChooser fc = new FileChooser();
    fc.setTitle("Зберегти звіт дизайнів");
    fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel файл", "*.xlsx"));
    fc.setInitialFileName("designs_report.xlsx");
    File file = fc.showSaveDialog(btnExportDesigns.getScene().getWindow());
    if (file != null) {
      Task<Void> task = new Task<>() {
        @Override protected Void call() {
          reportService.exportDesignsToExcel(file);
          return null;
        }
      };
      task.setOnSucceeded(e -> Platform.runLater(() ->
          showAlert("Звіт збережено:\n" + file.getAbsolutePath(), Alert.AlertType.INFORMATION)));
      task.setOnFailed(e -> Platform.runLater(() ->
          showAlert("Помилка генерації звіту.", Alert.AlertType.ERROR)));
      new Thread(task).start();
    }
  }

  @FXML
  public void handleExportOrders() {
    FileChooser fc = new FileChooser();
    fc.setTitle("Зберегти звіт замовлень");
    fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel файл", "*.xlsx"));
    fc.setInitialFileName("orders_report.xlsx");
    File file = fc.showSaveDialog(btnExportOrders.getScene().getWindow());
    if (file != null) {
      Task<Void> task = new Task<>() {
        @Override protected Void call() {
          reportService.exportOrdersToExcel(file);
          return null;
        }
      };
      task.setOnSucceeded(e -> Platform.runLater(() ->
          showAlert("Звіт збережено:\n" + file.getAbsolutePath(), Alert.AlertType.INFORMATION)));
      new Thread(task).start();
    }
  }

  @FXML
  public void handleRefresh() {
    loadDataAsync();
  }

  @FXML
  public void handleLogout() {
    SessionManager.logout();
    Stage stage = (Stage) welcomeLabel.getScene().getWindow();
    FXMLNavigator.navigate(stage, "login.fxml", "PhoneCase Manager — Вхід", 400, 350);
  }

  private void showDesignDialog(DesignDTO existing) {
    Dialog<DesignDTO> dialog = new Dialog<>();
    dialog.setTitle(existing == null ? "Новий дизайн" : "Редагувати дизайн");
    dialog.setHeaderText(existing == null ? "Додати новий дизайн чохла" : "Редагування: " + existing.getName());

    ButtonType saveButtonType = new ButtonType("Зберегти", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

    TextField nameField = new TextField(existing != null ? existing.getName() : "");
    nameField.setPromptText("Назва дизайну");
    TextField descField = new TextField(existing != null ? existing.getDescription() : "");
    descField.setPromptText("Опис");
    TextField priceField = new TextField(existing != null ? String.valueOf(existing.getPrice()) : "");
    priceField.setPromptText("Ціна (грн)");

    ComboBox<Category> catBox = new ComboBox<>();
    catBox.setItems(FXCollections.observableArrayList(designService.getAllCategories()));
    catBox.setPromptText("Оберіть категорію");
    if (existing != null) {
      catBox.getItems().stream()
          .filter(c -> c.getId() == existing.getCategoryId())
          .findFirst().ifPresent(catBox::setValue);
    }

    CheckBox availableCheck = new CheckBox("Доступний");
    availableCheck.setSelected(existing == null || existing.isAvailable());

    javafx.scene.layout.VBox box = new javafx.scene.layout.VBox(10,
        new Label("Назва:"), nameField,
        new Label("Опис:"), descField,
        new Label("Ціна:"), priceField,
        new Label("Категорія:"), catBox,
        availableCheck);
    box.setPadding(new javafx.geometry.Insets(10));
    dialog.getDialogPane().setContent(box);

    dialog.setResultConverter(btn -> {
      if (btn == saveButtonType) {
        DesignDTO dto = existing != null ? existing : new DesignDTO();
        dto.setName(nameField.getText().trim());
        dto.setDescription(descField.getText().trim());
        try { dto.setPrice(Double.parseDouble(priceField.getText().trim())); }
        catch (NumberFormatException e) { dto.setPrice(0); }
        if (catBox.getValue() != null) {
          dto.setCategoryId(catBox.getValue().getId());
          dto.setCategoryName(catBox.getValue().getName());
        }
        dto.setAvailable(availableCheck.isSelected());
        dto.setCreatedBy(SessionManager.getCurrentUser().getId());
        return dto;
      }
      return null;
    });

    dialog.showAndWait().ifPresent(dto -> {
      try {
        if (existing == null) {
          DesignDTO created = designService.createDesign(dto);
          designsList.add(0, created);
        } else {
          DesignDTO updated = designService.updateDesign(dto);
          int idx = designsList.indexOf(existing);
          if (idx >= 0) designsList.set(idx, updated);
        }
        showAlert("Дизайн збережено успішно.", Alert.AlertType.INFORMATION);
      } catch (Exception e) {
        showAlert("Помилка: " + e.getMessage(), Alert.AlertType.ERROR);
      }
    });
  }

  private void showAlert(String message, Alert.AlertType type) {
    Alert alert = new Alert(type, message, ButtonType.OK);
    alert.setTitle("PhoneCase Manager");
    alert.setHeaderText(null);
    alert.showAndWait();
  }
}