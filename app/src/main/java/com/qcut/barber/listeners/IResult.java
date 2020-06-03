package com.qcut.barber.listeners;

public interface IResult<T> {

    void accept(T result);
}
