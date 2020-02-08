package com.qcut.biz.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Barber {
    public static final String BARBERS = "barbers";
    private String key;
    private String name;
    private String imagePath;
}
