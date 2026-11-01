package org.delcom.ifs18005springboottodolist.framework.view;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.SortDirection;
import org.delcom.ifs18005springboottodolist.adapter.presenter.TodoPresenter;
import org.delcom.ifs18005springboottodolist.adapter.repository.TodoRepository;
import org.delcom.ifs18005springboottodolist.adapter.storage.FileStorage;
import org.delcom.ifs18005springboottodolist.domain.entity.Role;
import org.delcom.ifs18005springboottodolist.domain.entity.Todo;
import org.delcom.ifs18005springboottodolist.domain.entity.User;
import org.delcom.ifs18005springboottodolist.domain.exception.EntityNotFoundException;
import org.delcom.ifs18005springboottodolist.domain.exception.ValidationException;
import org.delcom.ifs18005springboottodolist.framework.auth.AuthSession;
import org.delcom.ifs18005springboottodolist.usecase.TodoUseCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static com.github.mvysny.kaributesting.v10.GridKt._doubleClickItem;
import static com.github.mvysny.kaributesting.v10.GridKt._getCellComponent;
import static com.github.mvysny.kaributesting.v10.LocatorJ._click;
import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static com.github.mvysny.kaributesting.v10.LocatorJ._setValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

class TodoViewTest {

    private static final int USER_ID = 1;

    @TempDir
    Path tempDir;

    private TodoUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = spy(new TodoUseCase(new TodoRepository()));
        MockVaadin.setup();
        AuthSession.login(new User(USER_ID, "andi", "hash", "Andi", Role.USER, null));
        UI.getCurrent().add(new TodoView(useCase, new TodoPresenter(), new FileStorage(tempDir.toString())));
    }

    @AfterEach
    void tearDown() {
        AuthSession.logout();
        MockVaadin.tearDown();
    }

    @Test
    void addSearchSortEditDeleteAndToggleFlow() throws Exception {
        assertTrue(_get(Paragraph.class).getText().contains("Data todo belum tersedia"));

        clickButton("Tambah");
        Dialog addDialog = _get(Dialog.class);
        _setValue(_get(addDialog, TextField.class), "Belajar Vaadin");
        _setValue(_get(addDialog, TextArea.class), "Deskripsi satu");
        clickDialogButton(addDialog, "Simpan");
        assertEquals(1, useCase.getTodosByUser(USER_ID).size());
        assertEquals("Daftar Todo", _get(Paragraph.class).getText());

        clickButton("Tambah");
        Dialog addDialog2 = _get(Dialog.class);
        _setValue(_get(addDialog2, TextField.class), "Zeta Task");
        clickDialogButton(addDialog2, "Simpan");

        TextField searchField = _get(TextField.class, spec -> spec.withPlaceholder("Cari judul..."));
        _setValue(searchField, "tidakada");
        clickButton("Cari");
        assertTrue(_get(Paragraph.class).getText().contains("Todo tidak ditemukan"));

        _setValue(searchField, "Vaadin");
        clickButton("Cari");
        assertEquals("Hasil Pencarian: \"Vaadin\"", _get(Paragraph.class).getText());

        @SuppressWarnings("unchecked")
        Grid<Todo> grid = _get(Grid.class);
        assertEquals(1, grid.getListDataView().getItemCount());

        clickButton("Muat Ulang");
        assertEquals(2, grid.getListDataView().getItemCount());
        assertEquals("Daftar Todo", _get(Paragraph.class).getText());

        grid.sort(List.of(new GridSortOrder<>(grid.getColumnByKey("title"), SortDirection.DESCENDING)));
        List<Todo> sortedDesc = grid.getListDataView().getItems().toList();
        assertEquals("Zeta Task", sortedDesc.get(0).getTitle());

        grid.sort(List.of(new GridSortOrder<>(grid.getColumnByKey("title"), SortDirection.ASCENDING)));
        List<Todo> sortedAsc = grid.getListDataView().getItems().toList();
        assertEquals("Belajar Vaadin", sortedAsc.get(0).getTitle());

        grid.sort(List.of(new GridSortOrder<>(grid.getColumnByKey("id"), SortDirection.ASCENDING)));
        grid.sort(List.of(new GridSortOrder<>(grid.getColumnByKey("selesai"), SortDirection.DESCENDING)));
        grid.sort(List.of(new GridSortOrder<>(grid.getColumnByKey("createdAt"), SortDirection.ASCENDING)));
        grid.sort(List.of(new GridSortOrder<>(grid.getColumnByKey("finishedAt"), SortDirection.ASCENDING)));
        grid.sort(List.of(new GridSortOrder<>(grid.getColumnByKey("duration"), SortDirection.ASCENDING)));

        int belajarRow = indexOfTitle(grid, "Belajar Vaadin");
        _doubleClickItem(grid, belajarRow);
        Dialog editDialog = _get(Dialog.class);
        _setValue(_get(editDialog, TextField.class), "Belajar Vaadin Updated");
        clickDialogButton(editDialog, "Simpan");

        assertTrue(useCase.getTodosByUser(USER_ID).stream()
                .anyMatch(todo -> todo.getTitle().contains("Updated")));

        int updatedRow = indexOfTitle(grid, "Belajar Vaadin Updated");
        Checkbox finishedCheckbox = (Checkbox) _getCellComponent(grid, updatedRow, "selesai");
        _setValue(finishedCheckbox, true);
        assertTrue(useCase.getTodosByUser(USER_ID).stream().anyMatch(Todo::isFinished));
        assertSummary("2", "1", "1");

        grid.sort(List.of(new GridSortOrder<>(grid.getColumnByKey("duration"), SortDirection.DESCENDING)));

        int finishedRow = indexOfTitle(grid, "Belajar Vaadin Updated");
        HorizontalLayout actions = (HorizontalLayout) _getCellComponent(grid, finishedRow, "tindakan");
        _click(_get(actions, Button.class, spec -> spec.withCaption("Hapus")));
        Dialog deleteDialog = _get(Dialog.class);
        clickDialogButton(deleteDialog, "Hapus");
        assertEquals(1, useCase.getTodosByUser(USER_ID).size());
        assertSummary("1", "0", "1");
    }

    @Test
    void handlesValidationCancelBlankSearchAndDeleteCancel() throws Exception {
        clickButton("Tambah");
        Dialog addDialog = _get(Dialog.class);
        _setValue(_get(addDialog, TextField.class), " ");
        clickDialogButton(addDialog, "Simpan");
        assertTrue(useCase.getTodosByUser(USER_ID).isEmpty());
        clickDialogButton(addDialog, "Batal");

        TextField searchField = _get(TextField.class, spec -> spec.withPlaceholder("Cari judul..."));
        _setValue(searchField, "   ");
        clickButton("Cari");
        assertTrue(_get(Paragraph.class).getText().contains("Data todo belum tersedia"));

        useCase.addTodo(USER_ID, "Ada", "Desc");
        clickButton("Muat Ulang");

        @SuppressWarnings("unchecked")
        Grid<Todo> grid = _get(Grid.class);
        _doubleClickItem(grid, 0);

        Dialog editDialog = _get(Dialog.class);
        _setValue(_get(editDialog, TextField.class), " ");
        clickDialogButton(editDialog, "Simpan");
        clickDialogButton(editDialog, "Batal");

        HorizontalLayout actions = (HorizontalLayout) _getCellComponent(grid, 0, "tindakan");
        _click(_get(actions, Button.class, spec -> spec.withCaption("Hapus")));
        Dialog deleteDialog = _get(Dialog.class);
        clickDialogButton(deleteDialog, "Batal");
        assertEquals(1, useCase.getTodosByUser(USER_ID).size());
    }

    @Test
    void handlesNotFoundOnEditDeleteAndToggle() throws Exception {
        useCase.addTodo(USER_ID, "Temp", "");
        clickButton("Muat Ulang");

        Todo selected = useCase.getTodosByUser(USER_ID).get(0);

        @SuppressWarnings("unchecked")
        Grid<Todo> grid = _get(Grid.class);

        doThrow(new EntityNotFoundException(selected.getId()))
                .when(useCase).removeTodoForUser(USER_ID, selected.getId());
        HorizontalLayout actions = (HorizontalLayout) _getCellComponent(grid, 0, "tindakan");
        _click(_get(actions, Button.class, spec -> spec.withCaption("Hapus")));
        Dialog deleteDialog = _get(Dialog.class);
        clickDialogButton(deleteDialog, "Hapus");

        doThrow(new EntityNotFoundException(selected.getId()))
                .when(useCase).updateTodoForUser(eq(USER_ID), eq(selected.getId()), anyString(), anyString(), isNull(), isNull(), eq(false));
        _doubleClickItem(grid, 0);
        Dialog editDialog = _get(Dialog.class);
        clickDialogButton(editDialog, "Simpan");
        clickDialogButton(editDialog, "Batal");

        doThrow(new EntityNotFoundException(selected.getId()))
                .when(useCase).updateTodoForUser(eq(USER_ID), eq(selected.getId()), isNull(), isNull(), anyBoolean(), isNull(), eq(false));
        Checkbox finishedCheckbox = (Checkbox) _getCellComponent(grid, 0, "selesai");
        _setValue(finishedCheckbox, true);

        doThrow(new ValidationException("judul kosong"))
                .when(useCase).updateTodoForUser(eq(USER_ID), eq(selected.getId()), isNull(), isNull(), anyBoolean(), isNull(), eq(false));
        _setValue(finishedCheckbox, false);

        doThrow(new ValidationException("bukan milik"))
                .when(useCase).removeTodoForUser(anyInt(), anyInt());
        _click(_get(actions, Button.class, spec -> spec.withCaption("Hapus")));
        Dialog deleteDialog2 = _get(Dialog.class);
        clickDialogButton(deleteDialog2, "Hapus");
    }

    private int indexOfTitle(Grid<Todo> grid, String title) {
        List<Todo> items = grid.getListDataView().getItems().toList();
        for (int i = 0; i < items.size(); i++) {
            if (title.equals(items.get(i).getTitle())) {
                return i;
            }
        }
        throw new AssertionError("Todo tidak ditemukan: " + title);
    }

    private void assertSummary(String total, String finished, String unfinished) {
        TodoView view = _get(TodoView.class);
        HorizontalLayout summary = findFirst(view, HorizontalLayout.class);
        List<String> values = summary.getChildren()
                .map(card -> ((Div) card).getChildren()
                        .skip(1)
                        .map(component -> ((Span) component).getText())
                        .findFirst()
                        .orElse(""))
                .toList();
        assertEquals(List.of(total, finished, unfinished), values);
    }

    private <T extends com.vaadin.flow.component.Component> T findFirst(
            com.vaadin.flow.component.Component root,
            Class<T> type
    ) {
        T found = findFirstOrNull(root, type);
        if (found == null) {
            throw new java.util.NoSuchElementException(type.getSimpleName() + " not found");
        }
        return found;
    }

    private <T extends com.vaadin.flow.component.Component> T findFirstOrNull(
            com.vaadin.flow.component.Component root,
            Class<T> type
    ) {
        if (type.isInstance(root)) {
            return type.cast(root);
        }
        return root.getChildren()
                .map(child -> findFirstOrNull(child, type))
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private void clickButton(String caption) {
        _click(_get(Button.class, spec -> spec.withCaption(caption)));
    }

    private void clickDialogButton(Dialog dialog, String caption) {
        _click(_get(dialog, Button.class, spec -> spec.withCaption(caption)));
    }
}
