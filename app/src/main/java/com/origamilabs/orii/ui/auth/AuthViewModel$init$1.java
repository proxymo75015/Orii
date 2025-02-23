package com.origamilabs.orii.ui.auth;

import android.os.Bundle;
import android.util.Log;
import com.facebook.AccessToken;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.share.internal.ShareConstants;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.origamilabs.orii.api.API;
import com.origamilabs.orii.manager.AppManager;
import com.origamilabs.orii.models.User;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import org.json.JSONObject;

/* compiled from: AuthViewModel.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u001f\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003*\u0001\u0000\b\n\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001J\b\u0010\u0003\u001a\u00020\u0004H\u0016J\u0012\u0010\u0005\u001a\u00020\u00042\b\u0010\u0006\u001a\u0004\u0018\u00010\u0007H\u0016J\u0012\u0010\b\u001a\u00020\u00042\b\u0010\t\u001a\u0004\u0018\u00010\u0002H\u0016¨\u0006\n"}, d2 = {"com/origamilabs/orii/ui/auth/AuthViewModel$init$1", "Lcom/facebook/FacebookCallback;", "Lcom/facebook/login/LoginResult;", "onCancel", "", "onError", "error", "Lcom/facebook/FacebookException;", "onSuccess", "result", "orii-app-2.2.16-202001151600-864a420_prodRelease"}, k = 1, mv = {1, 1, 15})
/* loaded from: classes2.dex */
public final class AuthViewModel$init$1 implements FacebookCallback<LoginResult> {
    final /* synthetic */ AuthFragment $fragment;

    AuthViewModel$init$1(AuthFragment authFragment) {
        this.$fragment = authFragment;
    }

    @Override // com.facebook.FacebookCallback
    public void onSuccess(final LoginResult result) {
        Log.d(AuthViewModel.TAG, "onSuccess");
        GraphRequest newMeRequest = GraphRequest.newMeRequest(result != null ? result.getAccessToken() : null, new GraphRequest.GraphJSONObjectCallback() { // from class: com.origamilabs.orii.ui.auth.AuthViewModel$init$1$onSuccess$request$1
            @Override // com.facebook.GraphRequest.GraphJSONObjectCallback
            public final void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
                AccessToken accessToken;
                Intrinsics.checkParameterIsNotNull(jsonObject, "jsonObject");
                Intrinsics.checkParameterIsNotNull(graphResponse, "graphResponse");
                Log.d(AuthViewModel.TAG, jsonObject.toString());
                String email = jsonObject.getString("email");
                jsonObject.getString("name");
                String id = jsonObject.getString(ShareConstants.WEB_DIALOG_PARAM_ID);
                API api = API.INSTANCE;
                Intrinsics.checkExpressionValueIsNotNull(email, "email");
                Intrinsics.checkExpressionValueIsNotNull(id, "id");
                LoginResult loginResult = result;
                String token = (loginResult == null || (accessToken = loginResult.getAccessToken()) == null) ? null : accessToken.getToken();
                if (token == null) {
                    Intrinsics.throwNpe();
                }
                api.facebookLogin(email, id, token, AppManager.INSTANCE.getUuid(), new API.ResponseListener() { // from class: com.origamilabs.orii.ui.auth.AuthViewModel$init$1$onSuccess$request$1.1
                    @Override // com.origamilabs.orii.api.API.ResponseListener
                    public void onSuccess(JsonObject response) {
                        Intrinsics.checkParameterIsNotNull(response, "response");
                        JsonObject asJsonObject = response.getAsJsonObject("user_info");
                        JsonElement jsonElement = asJsonObject.get("users_id");
                        Intrinsics.checkExpressionValueIsNotNull(jsonElement, "info.get(\"users_id\")");
                        String asString = jsonElement.getAsString();
                        Intrinsics.checkExpressionValueIsNotNull(asString, "info.get(\"users_id\").asString");
                        JsonElement jsonElement2 = asJsonObject.get("users_email");
                        Intrinsics.checkExpressionValueIsNotNull(jsonElement2, "info.get(\"users_email\")");
                        String asString2 = jsonElement2.getAsString();
                        Intrinsics.checkExpressionValueIsNotNull(asString2, "info.get(\"users_email\").asString");
                        JsonElement jsonElement3 = asJsonObject.get("users_name");
                        Intrinsics.checkExpressionValueIsNotNull(jsonElement3, "info.get(\"users_name\")");
                        String asString3 = jsonElement3.getAsString();
                        Intrinsics.checkExpressionValueIsNotNull(asString3, "info.get(\"users_name\").asString");
                        JsonElement jsonElement4 = asJsonObject.get("users_token");
                        Intrinsics.checkExpressionValueIsNotNull(jsonElement4, "info.get(\"users_token\")");
                        String asString4 = jsonElement4.getAsString();
                        Intrinsics.checkExpressionValueIsNotNull(asString4, "info.get(\"users_token\").asString");
                        AppManager.INSTANCE.onUserLoggedIn(new User(asString, asString2, asString3, asString4));
                        AnalyticsManager.INSTANCE.logUserLogin(AnalyticsManager.LoginWay.FACEBOOK);
                        AuthViewModel$init$1.this.$fragment.navigateToTutorial();
                    }

                    @Override // com.origamilabs.orii.api.API.ResponseListener
                    public void onError(String errorMessage) {
                        Intrinsics.checkParameterIsNotNull(errorMessage, "errorMessage");
                        AuthViewModel$init$1.this.$fragment.setFacebookLoginError();
                    }
                });
            }
        });
        Intrinsics.checkExpressionValueIsNotNull(newMeRequest, "GraphRequest.newMeReques…     })\n                }");
        Bundle bundle = new Bundle();
        bundle.putString(GraphRequest.FIELDS_PARAM, "email,name");
        newMeRequest.setParameters(bundle);
        newMeRequest.executeAsync();
    }

    @Override // com.facebook.FacebookCallback
    public void onCancel() {
        Log.d(AuthViewModel.TAG, "onCancel");
    }

    @Override // com.facebook.FacebookCallback
    public void onError(FacebookException error) {
        Log.d(AuthViewModel.TAG, "onError");
    }
}
