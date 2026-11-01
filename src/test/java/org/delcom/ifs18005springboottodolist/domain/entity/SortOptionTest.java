package org.delcom.ifs18005springboottodolist.domain.entity;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SortOptionTest {

    @Test
    void sortsByTitleAscendingAndDescending() {
        List<Todo> todos = new ArrayList<>();
        todos.add(new Todo(1, 1, "Beta"));
        todos.add(new Todo(2, 1, "Alpha"));

        todos.sort(SortOption.TITLE_ASC.comparator());
        assertEquals("Alpha", todos.get(0).getTitle());

        todos.sort(SortOption.TITLE_DESC.comparator());
        assertEquals("Beta", todos.get(0).getTitle());
    }

    @Test
    void sortsByFinishedStatus() {
        List<Todo> todos = new ArrayList<>();
        todos.add(new Todo(1, 1, "A", "", null, false));
        todos.add(new Todo(2, 1, "B", "", null, true));

        todos.sort(SortOption.FINISHED_FIRST.comparator());
        assertTrue(todos.get(0).isFinished());
        assertFalse(todos.get(1).isFinished());

        todos.sort(SortOption.UNFINISHED_FIRST.comparator());
        assertFalse(todos.get(0).isFinished());
        assertTrue(todos.get(1).isFinished());
    }
}
