package org.linkmessenger.request;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.thoughtcrime.securesms.keyvalue.SignalStore;
import org.thoughtcrime.securesms.util.ProfileUtil;

import java.util.Objects;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class AccessTokenAuthenticator implements Authenticator {
    @Nullable
    @Override
    public Request authenticate(Route route, Response response) {
        synchronized (this) {
            String token = SignalStore.account().getSocialToken();
            if(token==null || token.isEmpty()){
                token = ProfileUtil.refreshToken();
            }
            return newRequestWithAccessToken(response.request(), Objects.requireNonNull(token));
        }
    }


    @NonNull
    private Request newRequestWithAccessToken(@NonNull Request request, @NonNull String accessToken) {
        return request.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .build();
    }
}
