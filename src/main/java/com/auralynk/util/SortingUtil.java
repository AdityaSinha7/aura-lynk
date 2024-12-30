package com.auralynk.util;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SortingUtil {
    private static final Set<String> VALID_SESSION_SORT_FIELDS = new HashSet<>(
        Arrays.asList("lastMessageAt", "createdAt", "sessionName")
    );
    
    private static final Set<String> VALID_MESSAGE_SORT_FIELDS = new HashSet<>(
        Arrays.asList("timestamp", "role")
    );

    public static Sort validateAndGetSessionSort(String sortBy, String direction) {
        if (!VALID_SESSION_SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException(
                "Invalid sort field. Valid fields are: " + VALID_SESSION_SORT_FIELDS
            );
        }
        return createSort(sortBy, direction);
    }

    public static Sort validateAndGetMessageSort(String sortBy, String direction) {
        if (!VALID_MESSAGE_SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException(
                "Invalid sort field. Valid fields are: " + VALID_MESSAGE_SORT_FIELDS
            );
        }
        return createSort(sortBy, direction);
    }

    private static Sort createSort(String sortBy, String direction) {
        try {
            Direction sortDirection = Direction.fromString(direction);
            return Sort.by(sortDirection, sortBy);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Invalid sort direction. Use 'asc' or 'desc'"
            );
        }
    }
} 