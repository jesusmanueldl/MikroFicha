package com.jmanuel.mikroficha;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("getSubscriptionDetails")
    Call<SubscriptionDetails> getSubscriptionDetails(
            @Query("subscriptionId") String subscriptionId,
            @Query("purchaseToken") String purchaseToken
    );
}
