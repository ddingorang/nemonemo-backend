package com.nemonemo.domain.memo.dto;

import lombok.Getter;

@Getter
public class MemoRequest {
    private String title;
    private String content;
    private String category;
    private boolean pinned;
}
