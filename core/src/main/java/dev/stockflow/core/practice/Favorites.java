package dev.stockflow.core.practice;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class Favorites {
    private Set<String> favoritesList = new LinkedHashSet<>();

    public void add(String code){
        if (code == null) {
            throw new IllegalArgumentException("Код отустствует");
        }
        String tempCode = code.trim();
        if (tempCode.isEmpty()) {
            throw new IllegalArgumentException("Пришла пустая строка вместо кода");
        }
        favoritesList.add(tempCode);

    }

    public void delete(String code){
        if (code == null) {
            throw new IllegalArgumentException("Код отустствует");
        }
        String tempCode = code.trim();
        if (tempCode.isEmpty()) {
            throw new IllegalArgumentException("Пришла пустая строка вместо кода");
        }
        favoritesList.remove(tempCode);
    }

    public Set<String> getFavoritesList() {
        return Collections.unmodifiableSet(favoritesList);
    }

    @Override
    public String toString() {
        return "Favorites{" +
                "favoritesList=" + favoritesList +
                '}';
    }

}
