package org.delcom.ifs18005springboottodolist.framework.view;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs18005springboottodolist.adapter.presenter.TodoPresenter;
import org.delcom.ifs18005springboottodolist.domain.entity.Todo;
import org.delcom.ifs18005springboottodolist.domain.entity.User;
import org.delcom.ifs18005springboottodolist.domain.exception.EntityNotFoundException;
import org.delcom.ifs18005springboottodolist.framework.auth.AuthSession;
import org.delcom.ifs18005springboottodolist.usecase.TodoUseCase;
import org.delcom.ifs18005springboottodolist.usecase.UserUseCase;

@Route(value = "user-todos", layout = MainLayout.class)
@PageTitle("Todo Pengguna")
public class UserTodosView extends VerticalLayout implements HasUrlParameter<Integer>, BeforeEnterObserver {

    private final UserUseCase userUseCase;
    private final TodoUseCase todoUseCase;
    private final TodoPresenter presenter;
    private final Grid<Todo> grid = new Grid<>(Todo.class, false);
    private final H1 title = new H1("Todo Pengguna");
    private final Paragraph empty = new Paragraph("Belum ada todo.");

    public UserTodosView(UserUseCase userUseCase, TodoUseCase todoUseCase, TodoPresenter presenter) {
        this.userUseCase = userUseCase;
        this.todoUseCase = todoUseCase;
        this.presenter = presenter;

        setSizeFull();
        setPadding(true);
        setSpacing(false);
        getStyle().set("background", "#F3F4F6");

        title.getStyle().set("margin", "0");
        empty.getStyle().set("margin", "0").set("color", "#4B5563");

        grid.addComponentColumn(todo -> {
            if (todo.getCoverPath() == null || todo.getCoverPath().isBlank()) {
                return new Span("-");
            }
            return ImagePreviewDialog.clickableThumbnail(
                    "/uploads/" + todo.getCoverPath(),
                    "Cover",
                    "48px"
            );
        }).setHeader("Cover").setAutoWidth(true);
        grid.addColumn(Todo::getId).setHeader("ID").setAutoWidth(true);
        grid.addColumn(Todo::getTitle).setHeader("Judul").setFlexGrow(1);
        grid.addColumn(todo -> {
            String description = todo.getDescription();
            if (description == null || description.isBlank()) {
                return "-";
            }
            return description;
        }).setHeader("Deskripsi").setFlexGrow(1);
        grid.addColumn(todo -> todo.isFinished() ? "Ya" : "Tidak").setHeader("Selesai").setAutoWidth(true);
        grid.addColumn(presenter::formatCreatedAt).setHeader("Dibuat").setAutoWidth(true);
        grid.setSizeFull();
        GridStyles.apply(grid);

        Div card = PageCard.wrap(title, empty, grid);
        add(card);
        expand(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User current = AuthSession.currentUser().orElse(null);
        if (current == null || !current.isAdmin()) {
            event.forwardTo(TodoView.class);
        }
    }

    @Override
    public void setParameter(com.vaadin.flow.router.BeforeEvent event, Integer userId) {
        User current = AuthSession.currentUser().orElse(null);
        if (current == null || !current.isAdmin()) {
            return;
        }
        try {
            User owner = userUseCase.getUser(userId);
            title.setText("Todo milik " + owner.getFullName() + " (@" + owner.getUsername() + ")");
            var todos = todoUseCase.getTodosByUser(userId);
            grid.setItems(todos);
            empty.setVisible(todos.isEmpty());
            grid.setVisible(!todos.isEmpty());
        } catch (EntityNotFoundException ex) {
            event.forwardTo(UsersView.class);
        }
    }
}
