package org.linkmessenger.request;

import androidx.annotation.NonNull;

import org.thoughtcrime.securesms.keyvalue.SignalStore;
import org.thoughtcrime.securesms.util.ProfileUtil;

import java.io.IOException;
import java.net.HttpURLConnection;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class TokenAuthInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        String token = SignalStore.account().getSocialToken();
        if(token==null || token.isEmpty()){
            token = ProfileUtil.refreshToken();
        }
        Request request = newRequestWithAccessToken(chain.request(), token);
        Response response = chain.proceed(request);

        if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            synchronized (this) {
                response.close();
                token = ProfileUtil.refreshToken();
                return chain.proceed(newRequestWithAccessToken(request, token));
            }
        }

        return response;
    }
    @NonNull
    private Request newRequestWithAccessToken(@NonNull Request request, @NonNull String accessToken) {
        return request.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .build();
    }
}
