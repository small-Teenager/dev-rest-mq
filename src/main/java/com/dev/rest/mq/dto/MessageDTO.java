package com.dev.rest.mq.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class MessageDTO implements Serializable {

    private static final long serialVersionUID = 218729866216560114L;
    @JsonProperty("content")
    private String content;

    @Override
    public String toString() {
        return "MessageDTO{" +
                "content='" + content + '\'' +
                '}';
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
