package com.qcut.barber.util;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.qcut.barber.listeners.IResult;
import com.qcut.barber.models.CustomerStatus;

import java.util.HashMap;
import java.util.Map;

public class CloudFunctionsUtils {

    public static void queueCustomer(String userId, String customerName, String channel, String preferredBarberKey, final IResult<String> callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("eventType", "ADD_CUSTOMER");
        data.put("customerName", customerName);
        data.put("customerKey", "");
        data.put("channel", channel);
        data.put("shopKey", userId);
        data.put("preferredBarberKey", preferredBarberKey);
        callQueueCustomerFunction(data, callback);
    }

    public static void updateCustomerStatus(String userId, String barberId, String customerId, CustomerStatus newStatus, final IResult<String> callback) {
        Map<String, Object> data = new HashMap<>();
        if (newStatus == CustomerStatus.PROGRESS) {
            data.put("eventType", "PROGRESS_CUSTOMER");
        } else if (newStatus == CustomerStatus.DONE) {
            data.put("eventType", "DONE_CUSTOMER");
        } else if (newStatus == CustomerStatus.REMOVED) {
            data.put("eventType", "REMOVE_CUSTOMER");
        }
        data.put("barberKey", barberId);
        data.put("customerKey", customerId);
        data.put("shopKey", userId);
        callQueueCustomerFunction(data, callback);
    }

    public static void reallocate(String userId, final IResult<String> callback) {
        LogUtils.info("Reallocation requested..");
        Map<String, Object> data = new HashMap<>();
        data.put("eventType", "REALLOCATE");
        data.put("shopKey", userId);
        callQueueCustomerFunction(data, callback);
    }
    public static void callQueueCustomerFunction(Map<String, Object> data, final IResult<String> callback) {
        FirebaseFunctions.getInstance().getHttpsCallable("queueCustomer")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        String result = (String) task.getResult().getData();
                        callback.accept(result);
                        return result;
                    }
                });
    }
}
