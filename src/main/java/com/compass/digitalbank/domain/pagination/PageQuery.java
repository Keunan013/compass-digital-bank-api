package com.compass.digitalbank.domain.pagination;

public record PageQuery(int page, int size) {

    public static final int FIRST_PAGE = 0;
    public static final int MIN_SIZE = 1;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    public PageQuery {
        if (page < FIRST_PAGE) {
            page = FIRST_PAGE;
        }
        if (size < MIN_SIZE) {
            size = DEFAULT_SIZE;
        }
        if (size > MAX_SIZE) {
            size = MAX_SIZE;
        }
    }

    public static PageQuery of(Integer page, Integer size) {
        return new PageQuery(
                page == null ? FIRST_PAGE : page,
                size == null ? DEFAULT_SIZE : size);
    }
}
