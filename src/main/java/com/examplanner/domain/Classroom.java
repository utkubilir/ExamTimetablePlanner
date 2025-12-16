package com.examplanner.domain;

public class Classroom {
    private String id;
    private String name;
    private int capacity;

    public Classroom(String id, String name, int capacity) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Classroom ID cannot be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Classroom name cannot be null or empty");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Classroom capacity must be positive, got: " + capacity);
        }
        this.id = id.trim();
        this.name = name.trim();
        this.capacity = capacity;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    @Override
    public String toString() {
        return name + " (" + capacity + ")";
    }
}
