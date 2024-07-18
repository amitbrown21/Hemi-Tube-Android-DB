package com.example.hemi_tube.repository;

public interface RepositoryCallback<T> {
    void onSuccess(T result);
    void onError(Exception e);
}