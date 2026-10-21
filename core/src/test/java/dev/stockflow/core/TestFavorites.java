package dev.stockflow.core;

import dev.stockflow.core.practice.Favorites;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TestFavorites {
    @Test
    public void createAndDelete_normal_successful() {
        Favorites favorites = new Favorites();
        List<String> listCkeck = List.of("B  2", "A", "a");
        List<String> list = new ArrayList<>();
        assertTrue(favorites.getFavoritesList().isEmpty());

        favorites.add(" B  2 ");
        favorites.add("A");
        favorites.add("a");

        Set<String> favoritesList = favorites.getFavoritesList();
        for (String item : favoritesList) {
            list.add(item);
        }
        assertEquals(listCkeck, list);

        List<String> listCkeckDel = List.of("B  2", "a");
        List<String> listDel = new ArrayList<>();
        favorites.delete("A");

        Set<String> favoritesListDel = favorites.getFavoritesList();
        for (String item : favoritesListDel) {
            listDel.add(item);
        }
        assertEquals(listCkeckDel, listDel);
    }

    @Test
    public void create_existingCodeDoesNotModifyCollection_successful() {
        Favorites favorites = new Favorites();
        favorites.add("B");
        favorites.add("A");
        favorites.add("C");

        List<String> listCkeck = List.of("B", "A", "C");
        List<String> list = new ArrayList<>();

        Set<String> favoritesList = favorites.getFavoritesList();
        for (String item : favoritesList) {
            list.add(item);
        }
        assertEquals(listCkeck, list);
        assertEquals(3, list.size());

        favorites.add(" A ");

        Set<String> favoritesListAdd = favorites.getFavoritesList();
        List<String> listAdd = new ArrayList<>();

        for (String item : favoritesListAdd) {
            listAdd.add(item);
        }
        assertEquals(listCkeck, listAdd);
    }

    @Test
    public void create_repeatingDeletionDoesNotBreakCollection_successful() {
        Favorites favorites = new Favorites();
        favorites.add("B");
        favorites.add("A");
        favorites.add("C");

        List<String> listCkeck = List.of("B", "A", "C");
        List<String> list = new ArrayList<>();

        Set<String> favoritesList = favorites.getFavoritesList();
        for (String item : favoritesList) {
            list.add(item);
        }
        assertEquals(listCkeck, list);


        favorites.delete(" A ");
        List<String> listCkeckDel = List.of("B", "C");
        List<String> listDel = new ArrayList<>();

        Set<String> favoritesListDel = favorites.getFavoritesList();
        for (String item : favoritesListDel) {
            listDel.add(item);
        }
        assertEquals(listCkeckDel, listDel);

        favorites.delete(" A ");
        List<String> listCkeckDelAgain = List.of("B", "C");
        List<String> listDelAgain = new ArrayList<>();

        Set<String> favoritesListDelAgain = favorites.getFavoritesList();
        for (String item : favoritesListDelAgain) {
            listDelAgain.add(item);
        }
        assertEquals(listCkeckDelAgain, listDelAgain);


        favorites.add(" A ");
        List<String> listCkeckAdd = List.of("B", "C", "A");
        List<String> listAdd = new ArrayList<>();

        Set<String> favoritesListAdd = favorites.getFavoritesList();
        for (String item : favoritesListAdd) {
            listAdd.add(item);
        }
        assertEquals(listCkeckAdd, listAdd);
    }

    @Test
    public void create_deletingLastEntryAndHandlingEmptyFavoritesList_successful() {
        Favorites favorites = new Favorites();
        favorites.add("A");
        List<String> listCkeck = List.of("A");
        List<String> list = new ArrayList<>();

        Set<String> favoritesList = favorites.getFavoritesList();
        for (String item : favoritesList) {
            list.add(item);
        }
        assertEquals(listCkeck, list);

        favorites.delete("A");
        assertTrue(favorites.getFavoritesList().isEmpty());

        favorites.delete("X");
        assertTrue(favorites.getFavoritesList().isEmpty());
    }

    @Test
    public void create_InvalidDataForSelectedItem_throwException() {
        Favorites favorites = new Favorites();
        List<String> listCkeck = List.of("A", "B");
        List<String> list = new ArrayList<>();
        assertTrue(favorites.getFavoritesList().isEmpty());

        favorites.add("A");
        favorites.add("B");

        Set<String> favoritesList = favorites.getFavoritesList();
        for (String item : favoritesList) {
            list.add(item);
        }
        assertEquals(listCkeck, list);

        Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
            favorites.add("");
        });
        assertEquals("Пришла пустая строка вместо кода", exception.getMessage());

        List<String> listCkeckBefore = List.of("A", "B");
        List<String> listBefore = new ArrayList<>();

        Set<String> favoritesListBefore = favorites.getFavoritesList();
        for (String item : favoritesListBefore) {
            listBefore.add(item);
        }
        assertEquals(listCkeckBefore, listBefore);

        Throwable exception2 = assertThrows(IllegalArgumentException.class, () -> {
            favorites.add(" ");
        });
        assertEquals("Пришла пустая строка вместо кода", exception2.getMessage());
        List<String> listCkeckBefore1 = List.of("A", "B");
        List<String> listBefore1 = new ArrayList<>();

        Set<String> favoritesListBefore1 = favorites.getFavoritesList();
        for (String item : favoritesListBefore1) {
            listBefore1.add(item);
        }
        assertEquals(listCkeckBefore1, listBefore1);

        Throwable exception3 = assertThrows(IllegalArgumentException.class, () -> {
            favorites.add(null);
        });
        assertEquals("Код отустствует", exception3.getMessage());
        List<String> listCkeckBefore2 = List.of("A", "B");
        List<String> listBefore2 = new ArrayList<>();

        Set<String> favoritesListBefore2 = favorites.getFavoritesList();
        for (String item : favoritesListBefore2) {
            listBefore2.add(item);
        }
        assertEquals(listCkeckBefore2, listBefore2);

        Throwable exception4 = assertThrows(IllegalArgumentException.class, () -> {
            favorites.delete(null);
        });
        assertEquals("Код отустствует", exception4.getMessage());
        assertIterableEquals(List.of("A", "B"), favorites.getFavoritesList());

        Throwable exception5 = assertThrows(IllegalArgumentException.class, () -> {
            favorites.delete("");
        });
        assertEquals("Пришла пустая строка вместо кода", exception5.getMessage());
        assertIterableEquals(List.of("A", "B"), favorites.getFavoritesList());

        Throwable exception6 = assertThrows(IllegalArgumentException.class, () -> {
            favorites.delete("  ");
        });
        assertEquals("Пришла пустая строка вместо кода", exception6.getMessage());
        assertIterableEquals(List.of("A", "B"), favorites.getFavoritesList());

    }

    @Test
    public void create_ObjectIndependenceDuringAdditionAndRemoval_successful() {
        Favorites favorites_1 = new Favorites();
        Favorites favorites_2 = new Favorites();

        favorites_1.add("A");
        assertEquals(null, favorites_2.getFavoritesList().stream().findFirst().orElse(null));

        favorites_2.add("A");
        favorites_1.delete("A");
        assertEquals("A", favorites_2.getFavoritesList().stream().findFirst().orElse(null));
        assertEquals(null, favorites_1.getFavoritesList().stream().findFirst().orElse(null));
        // сравнивает количество элементов, значения и порядок обхода.
        assertIterableEquals(List.of("A"), favorites_2.getFavoritesList());
    }

    @Test
    public void create_protectionAgainstModificationBypassingObjectOperations_successful() {
        Favorites favorites = new Favorites();
        favorites.add("A");
        favorites.add("B");

        Throwable exception = assertThrows(UnsupportedOperationException.class, () -> {
            favorites.getFavoritesList().remove("A");
        });
        assertEquals(null, exception.getMessage());

        List<String> listCkeck = List.of("A", "B");
        List<String> list = new ArrayList<>();
        Set<String> favoritesList = favorites.getFavoritesList();
        for (String item : favoritesList) {
            list.add(item);
        }
        assertEquals(listCkeck, list);
    }
}
