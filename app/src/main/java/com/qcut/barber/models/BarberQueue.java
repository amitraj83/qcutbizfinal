package com.qcut.barber.models;

import java.util.Collections;
import java.util.List;

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
public class BarberQueue {
    public static final String BARBER_WAITING_QUEUES = "barberWaitingQueues";
    private String barberKey;
    @Builder.Default
    private List<Customer> customers = Collections.emptyList();
    private Barber barber;
}
