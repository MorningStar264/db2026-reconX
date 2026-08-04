package com.dbtraining.reconx.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public class PagedResponse<T> {
    private final List<T> content;
    private final int pageNumber;
    private final int pageSize;
    private final long totalElements;
    private final int totalPages;
    private final boolean last;

    public PagedResponse(List<T> content, int pageNumber, int pageSize, long totalElements, int totalPages, boolean last) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.last = last;
    }

    public static <T> PagedResponse<T> of(Page<T> page) {
        return new PagedResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isLast()
        );
    }

    public static <T, U> PagedResponse<U> of(Page<T> page, Function<T, U> mapper) {
        Page<U> mappedPage = page.map(mapper);
        return new PagedResponse<>(
            mappedPage.getContent(),
            mappedPage.getNumber(),
            mappedPage.getSize(),
            mappedPage.getTotalElements(),
            mappedPage.getTotalPages(),
            mappedPage.isLast()
        );
    }

    // Getters
    public List<T> getContent() { return content; }
    public int getPageNumber() { return pageNumber; }
    public int getPageSize() { return pageSize; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
    public boolean isLast() { return last; }
}