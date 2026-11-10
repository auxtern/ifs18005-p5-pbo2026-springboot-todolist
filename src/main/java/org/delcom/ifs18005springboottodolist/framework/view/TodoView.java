package org.delcom.ifs18005springboottodolist.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.UploadHandler;
import org.delcom.ifs18005springboottodolist.adapter.presenter.TodoPresenter;
import org.delcom.ifs18005springboottodolist.adapter.storage.FileStorage;
import org.delcom.ifs18005springboottodolist.domain.entity.Todo;
import org.delcom.ifs18005springboottodolist.domain.entity.User;
import org.delcom.ifs18005springboottodolist.domain.exception.EntityNotFoundException;
import org.delcom.ifs18005springboottodolist.domain.exception.ValidationException;
import org.delcom.ifs18005springboottodolist.framework.auth.AuthSession;
import org.delcom.ifs18005springboottodolist.usecase.TodoUseCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Tampilan web Vaadin aplikasi todo milik pengguna yang sedang login.
 */
@Route(value = "", layout = MainLayout.class)
@PageTitle("Todo List")
public class TodoView extends VerticalLayout {

    private final TodoUseCase todoUseCase;
    private final TodoPresenter presenter;
    private final FileStorage fileStorage;

    private final Grid<Todo> grid = new Grid<>(Todo.class, false);
    private final TextField searchField = new TextField();
    private final Paragraph statusLabel = new Paragraph();
    private final Span emptyState = new Span("- Data todo belum tersedia!");
    private final Span totalCount = new Span("0");
    private final Span finishedCount = new Span("0");
    private final Span unfinishedCount = new Span("0");

    public TodoView(TodoUseCase todoUseCase, TodoPresenter presenter, FileStorage fileStorage) {
        this.todoUseCase = todoUseCase;
        this.presenter = presenter;
        this.fileStorage = fileStorage;

        setSizeFull();
        setPadding(true);
        setSpacing(false);
        getStyle().set("background", "#F3F4F6");

        H1 title = new H1("Todo Saya");
        title.getStyle().set("margin", "0");

        configureGrid();
        configureToolbar();

        Div card = PageCard.wrap(title, buildSummaryCards(), buildToolbar(), statusLabel, grid);
        add(card);
        expand(card);
        refreshAll();
    }

    private User requireUser() {
        return AuthSession.currentUser().orElseThrow();
    }

    private HorizontalLayout buildSummaryCards() {
        HorizontalLayout summary = new HorizontalLayout(
                createSummaryCard("Jumlah Todo", totalCount, "#DBEAFE", "#1D4ED8", "#1E40AF"),
                createSummaryCard("Sudah Selesai", finishedCount, "#DCFCE7", "#16A34A", "#166534"),
                createSummaryCard("Belum Selesai", unfinishedCount, "#FEF3C7", "#D97706", "#92400E")
        );
        summary.setWidthFull();
        summary.setSpacing(true);
        summary.getStyle().set("align-items", "stretch");
        return summary;
    }

    private Div createSummaryCard(
            String label,
            Span value,
            String background,
            String accent,
            String valueColor
    ) {
        Span caption = new Span(label);
        caption.getStyle()
                .set("display", "block")
                .set("font-size", "0.875rem")
                .set("font-weight", "500")
                .set("color", accent);

        value.getStyle()
                .set("display", "block")
                .set("margin-top", "0.35rem")
                .set("font-size", "2rem")
                .set("font-weight", "700")
                .set("line-height", "1.1")
                .set("color", valueColor);

        Div card = new Div(caption, value);
        card.getStyle()
                .set("flex", "1")
                .set("min-width", "0")
                .set("background", background)
                .set("border", "1px solid " + accent)
                .set("border-left", "6px solid " + accent)
                .set("border-radius", "12px")
                .set("padding", "1rem 1.1rem")
                .set("box-sizing", "border-box");
        return card;
    }

    private void updateSummary(List<Todo> todos) {
        long finished = todos.stream().filter(Todo::isFinished).count();
        totalCount.setText(String.valueOf(todos.size()));
        finishedCount.setText(String.valueOf(finished));
        unfinishedCount.setText(String.valueOf(todos.size() - finished));
    }

    private void configureGrid() {
        grid.addComponentColumn(this::createCoverPreview)
                .setHeader("Cover")
                .setKey("cover")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(false);

        grid.addColumn(Todo::getId)
                .setHeader("ID")
                .setKey("id")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(true)
                .setComparator(Todo::getId);

        grid.addColumn(Todo::getTitle)
                .setHeader("Judul")
                .setKey("title")
                .setFlexGrow(1)
                .setSortable(true)
                .setComparator(Comparator.comparing(Todo::getTitle, String.CASE_INSENSITIVE_ORDER));

        grid.addColumn(todo -> {
                    String description = todo.getDescription();
                    if (description == null || description.isBlank()) {
                        return "-";
                    }
                    return description.length() > 40 ? description.substring(0, 40) + "..." : description;
                })
                .setHeader("Deskripsi")
                .setKey("description")
                .setFlexGrow(1)
                .setSortable(false);

        grid.addComponentColumn(this::createFinishedCheckbox)
                .setHeader("Selesai")
                .setKey("selesai")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(true)
                .setComparator(Comparator.comparing(Todo::isFinished));

        grid.addColumn(todo -> presenter.formatCreatedAt(todo))
                .setHeader("Dibuat")
                .setKey("createdAt")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(Comparator.comparing(Todo::getCreatedAt));

        grid.addColumn(todo -> presenter.formatFinishedAt(todo))
                .setHeader("Tanggal Selesai")
                .setKey("finishedAt")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(Comparator.comparing(
                        Todo::getFinishedAt,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ));

        grid.addColumn(todo -> presenter.formatDuration(todo))
                .setHeader("Durasi")
                .setKey("duration")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(Comparator.comparing(
                        todo -> todo.duration().orElse(null),
                        Comparator.nullsLast(Comparator.naturalOrder())
                ));

        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Tindakan")
                .setKey("tindakan")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(false);

        grid.setEmptyStateComponent(emptyState);
        grid.addItemDoubleClickListener(event -> openEditDialog(event.getItem()));
        grid.setSizeFull();
        GridStyles.apply(grid);
    }

    private Component createCoverPreview(Todo todo) {
        if (todo.getCoverPath() == null || todo.getCoverPath().isBlank()) {
            return new Span("-");
        }
        return ImagePreviewDialog.clickableThumbnail(
                "/uploads/" + todo.getCoverPath(),
                "Cover",
                "48px"
        );
    }

    private Checkbox createFinishedCheckbox(Todo todo) {
        Checkbox checkbox = new Checkbox();
        checkbox.setValue(todo.isFinished());
        checkbox.addValueChangeListener(event -> toggleFinished(todo, event.getValue()));
        return checkbox;
    }

    private HorizontalLayout createActionButtons(Todo todo) {
        Button deleteButton = new Button("Hapus", VaadinIcon.TRASH.create(), event -> openDeleteDialog(todo));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
        deleteButton.getStyle().set("cursor", "pointer");
        return new HorizontalLayout(deleteButton);
    }

    private void configureToolbar() {
        searchField.setPlaceholder("Cari judul...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
    }

    private HorizontalLayout buildToolbar() {
        Button addButton = new Button("Tambah", VaadinIcon.PLUS.create(), event -> openAddDialog());
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.getStyle().set("cursor", "pointer");

        Button searchButton = new Button("Cari", VaadinIcon.SEARCH.create(), event -> searchTodos());
        searchButton.getStyle().set("cursor", "pointer");

        Button refreshButton = new Button("Muat Ulang", VaadinIcon.REFRESH.create(), event -> refreshAll());
        refreshButton.getStyle().set("cursor", "pointer");

        HorizontalLayout toolbar = new HorizontalLayout(
                searchField,
                searchButton,
                refreshButton,
                addButton
        );
        toolbar.setWidthFull();
        toolbar.setFlexGrow(1, searchField);
        return toolbar;
    }

    private void openAddDialog() {
        TextField titleField = new TextField("Judul");
        TextArea descriptionField = new TextArea("Deskripsi");
        titleField.setWidthFull();
        descriptionField.setWidthFull();

        AtomicReference<String> pendingCoverName = new AtomicReference<>();
        AtomicReference<byte[]> pendingCoverData = new AtomicReference<>();
        Upload upload = imageUpload(pendingCoverName, pendingCoverData);
        Span uploadHint = new Span("Cover (opsional)");

        Dialog dialog = new Dialog();
        ProgressBar loadingBar = new ProgressBar();
        loadingBar.setVisible(false);

        Button saveButton = new Button("Simpan", VaadinIcon.CHECK.create());
        saveButton.addClickListener(event -> {
            loadingBar.setVisible(true);
            saveButton.setEnabled(false);
            try {
                User user = requireUser();
                Todo created = todoUseCase.addTodo(user.getId(), titleField.getValue(), descriptionField.getValue());
                String filename = pendingCoverName.get();
                byte[] data = pendingCoverData.get();
                if (filename != null && !filename.isBlank() && data != null) {
                    String path = fileStorage.store("covers", filename, new ByteArrayInputStream(data));
                    todoUseCase.updateTodoForUser(user.getId(), created.getId(), null, null, null, path, false);
                }
                showSuccess(presenter.showAddSuccess(created));
                dialog.close();
                refreshAll();
            } catch (ValidationException ex) {
                loadingBar.setVisible(false);
                saveButton.setEnabled(true);
                showError(presenter.showValidationError(ex.getMessage()));
            } catch (EntityNotFoundException | IOException ex) {
                loadingBar.setVisible(false);
                saveButton.setEnabled(true);
                showError(presenter.showValidationError(ex.getMessage()));
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());
        VerticalLayout body = new VerticalLayout(titleField, descriptionField, uploadHint, upload);
        body.setPadding(false);
        body.setSpacing(true);
        buildDialog(dialog, "Menambah Todo", DialogTone.PRIMARY, loadingBar, body, cancelButton, saveButton);
        dialog.open();
    }

    private void openEditDialog(Todo todo) {
        TextField titleField = new TextField("Judul");
        titleField.setValue(todo.getTitle());
        TextArea descriptionField = new TextArea("Deskripsi");
        descriptionField.setValue(todo.getDescription() == null ? "" : todo.getDescription());
        titleField.setWidthFull();
        descriptionField.setWidthFull();

        AtomicReference<String> pendingCoverName = new AtomicReference<>();
        AtomicReference<byte[]> pendingCoverData = new AtomicReference<>();
        Upload upload = imageUpload(pendingCoverName, pendingCoverData);
        Span uploadHint = new Span("Ganti cover (opsional)");

        Dialog dialog = new Dialog();
        ProgressBar loadingBar = new ProgressBar();
        loadingBar.setVisible(false);

        Button saveButton = new Button("Simpan", VaadinIcon.CHECK.create());
        saveButton.addClickListener(event -> {
            loadingBar.setVisible(true);
            saveButton.setEnabled(false);
            try {
                User user = requireUser();
                String coverPath = null;
                boolean clearCover = false;
                String filename = pendingCoverName.get();
                byte[] data = pendingCoverData.get();
                if (filename != null && !filename.isBlank() && data != null) {
                    coverPath = fileStorage.store("covers", filename, new ByteArrayInputStream(data));
                }
                todoUseCase.updateTodoForUser(
                        user.getId(),
                        todo.getId(),
                        titleField.getValue(),
                        descriptionField.getValue(),
                        null,
                        coverPath,
                        clearCover
                );
                showSuccess(presenter.showUpdateSuccess());
                dialog.close();
                refreshAll();
            } catch (EntityNotFoundException ex) {
                loadingBar.setVisible(false);
                saveButton.setEnabled(true);
                showError(presenter.showUpdateFailed(ex.getId()));
            } catch (ValidationException | IOException ex) {
                loadingBar.setVisible(false);
                saveButton.setEnabled(true);
                showError(presenter.showValidationError(ex.getMessage()));
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());
        VerticalLayout body = new VerticalLayout(titleField, descriptionField, uploadHint, upload);
        body.setPadding(false);
        body.setSpacing(true);
        buildDialog(dialog, "Mengubah Todo #" + todo.getId(), DialogTone.PRIMARY, loadingBar, body, cancelButton, saveButton);
        dialog.open();
    }

    private void openDeleteDialog(Todo todo) {
        Paragraph message = new Paragraph(
                "Yakin ingin menghapus todo \"" + todo.getTitle() + "\" (ID " + todo.getId() + ")?"
        );
        message.getStyle().set("margin", "0");

        Dialog dialog = new Dialog();
        ProgressBar loadingBar = new ProgressBar();
        loadingBar.setVisible(false);

        Button confirmButton = new Button("Hapus", VaadinIcon.TRASH.create());
        confirmButton.addClickListener(event -> {
            loadingBar.setVisible(true);
            confirmButton.setEnabled(false);
            deleteTodo(todo);
            dialog.close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());
        buildDialog(dialog, "Menghapus Todo", DialogTone.DANGER, loadingBar, message, cancelButton, confirmButton);
        dialog.open();
    }

    private enum DialogTone {
        PRIMARY,
        DANGER
    }

    private void buildDialog(
            Dialog dialog,
            String title,
            DialogTone tone,
            ProgressBar loadingBar,
            Component bodyContent,
            Button... footerButtons
    ) {
        dialog.setWidth("500px");
        dialog.setMaxWidth("95vw");
        dialog.setResizable(false);
        dialog.addClassName("todo-form-dialog");

        boolean danger = tone == DialogTone.DANGER;
        String headerBackground = danger ? "#DC2626" : "#2563EB";

        Span headerTitle = new Span(title);
        headerTitle.getStyle()
                .set("margin", "0")
                .set("color", "#FFFFFF")
                .set("font-size", "1.125rem")
                .set("font-weight", "600")
                .set("line-height", "1.4");

        Div header = new Div(headerTitle);
        header.getStyle()
                .set("background", headerBackground)
                .set("padding", "1rem 1.5rem")
                .set("box-sizing", "border-box")
                .set("width", "100%");

        loadingBar.setIndeterminate(true);
        loadingBar.getStyle().set("margin", "0").set("height", "4px");

        Div body = new Div(bodyContent);
        body.getStyle()
                .set("background", "#FFFFFF")
                .set("padding", "1.25rem 1.5rem")
                .set("overflow", "hidden")
                .set("box-sizing", "border-box")
                .set("width", "100%");

        Div footer = new Div();
        footer.getStyle()
                .set("display", "flex")
                .set("justify-content", "flex-end")
                .set("align-items", "center")
                .set("gap", "0.75rem")
                .set("background", "#F3F4F6")
                .set("border-top", "1px solid #E5E7EB")
                .set("padding", "0.85rem 1.5rem 1.1rem")
                .set("box-sizing", "border-box")
                .set("width", "100%");
        for (Button btn : footerButtons) {
            btn.getStyle().set("cursor", "pointer");
        }
        footer.add(footerButtons);

        Div shell = new Div(header, loadingBar, body, footer);
        shell.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("width", "100%")
                .set("overflow", "hidden")
                .set("border-radius", "12px")
                .set("background", "#FFFFFF");

        dialog.removeAll();
        dialog.add(shell);
        removeOverlayPadding(dialog);
    }

    private void removeOverlayPadding(Dialog dialog) {
        dialog.addOpenedChangeListener(event -> {
            if (event.isOpened()) {
                dialog.getElement().executeJs("""
                        const overlay = this.$.overlay;
                        if (!overlay || !overlay.shadowRoot) {
                          return;
                        }
                        const content = overlay.shadowRoot.querySelector('[part="content"]');
                        if (content) {
                          content.style.padding = '0';
                          content.style.overflow = 'visible';
                          content.style.maxHeight = 'none';
                        }
                        const overlayPart = overlay.shadowRoot.querySelector('[part="overlay"]');
                        if (overlayPart) {
                          overlayPart.style.padding = '0';
                          overlayPart.style.overflow = 'hidden';
                        }
                        """);
            }
        });
    }

    private void toggleFinished(Todo todo, boolean finished) {
        try {
            todoUseCase.updateTodoForUser(requireUser().getId(), todo.getId(), null, null, finished, null, false);
            showSuccess(presenter.showUpdateSuccess());
            refreshGrid(todoUseCase.getTodosByUser(requireUser().getId()), "Daftar Todo");
        } catch (EntityNotFoundException ex) {
            showError(presenter.showUpdateFailed(ex.getId()));
            refreshGrid(todoUseCase.getTodosByUser(requireUser().getId()), "Daftar Todo");
        } catch (ValidationException ex) {
            showError(presenter.showValidationError(ex.getMessage()));
            refreshGrid(todoUseCase.getTodosByUser(requireUser().getId()), "Daftar Todo");
        }
    }

    private void deleteTodo(Todo todo) {
        try {
            todoUseCase.removeTodoForUser(requireUser().getId(), todo.getId());
            showSuccess(presenter.showRemoveSuccess());
            refreshAll();
        } catch (EntityNotFoundException ex) {
            showError(presenter.showRemoveFailed(ex.getId()));
        } catch (ValidationException ex) {
            showError(presenter.showValidationError(ex.getMessage()));
        }
    }

    private void searchTodos() {
        String keyword = searchField.getValue();
        if (keyword.isBlank()) {
            refreshAll();
            return;
        }
        List<Todo> results = todoUseCase.searchTodos(requireUser().getId(), keyword);
        if (results.isEmpty()) {
            emptyState.setText("- Todo tidak ditemukan!");
            refreshGrid(results, "Hasil Pencarian: \"" + keyword + "\"");
        } else {
            emptyState.setText("- Data todo belum tersedia!");
            refreshGrid(results, "Hasil Pencarian: \"" + keyword + "\"");
        }
    }

    private void refreshAll() {
        emptyState.setText("- Data todo belum tersedia!");
        refreshGrid(todoUseCase.getTodosByUser(requireUser().getId()), "Daftar Todo");
        searchField.clear();
    }

    private void refreshGrid(List<Todo> todos, String title) {
        grid.setItems(todos);
        updateSummary(todoUseCase.getTodosByUser(requireUser().getId()));
        if (todos.isEmpty()) {
            statusLabel.setText(title + " — " + emptyState.getText().replaceFirst("^- ", ""));
        } else {
            statusLabel.setText(title);
        }
    }

    private Upload imageUpload(AtomicReference<String> pendingName, AtomicReference<byte[]> pendingData) {
        Upload upload = new Upload(UploadHandler.inMemory((metadata, data) -> {
            pendingName.set(metadata.fileName());
            pendingData.set(data);
        }));
        upload.setAcceptedMimeTypes("image/png", "image/jpeg", "image/webp");
        upload.setAcceptedFileExtensions(".png", ".jpg", ".jpeg", ".webp");
        upload.setMaxFiles(1);
        return upload;
    }

    private void showSuccess(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 4000, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
