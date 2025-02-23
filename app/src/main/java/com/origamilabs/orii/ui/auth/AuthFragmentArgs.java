package com.origamilabs.orii.ui.auth;

import android.os.Bundle;
import androidx.navigation.NavArgs;
import com.facebook.internal.FacebookRequestErrorClassification;
import kotlin.Metadata;
import kotlin.jvm.JvmStatic;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: AuthFragmentArgs.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\t\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0086\b\u0018\u0000 \u00152\u00020\u0001:\u0001\u0015B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003¢\u0006\u0002\u0010\u0005J\t\u0010\t\u001a\u00020\u0003HÆ\u0003J\t\u0010\n\u001a\u00020\u0003HÆ\u0003J\u001d\u0010\u000b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0003HÆ\u0001J\u0013\u0010\f\u001a\u00020\r2\b\u0010\u000e\u001a\u0004\u0018\u00010\u000fHÖ\u0003J\t\u0010\u0010\u001a\u00020\u0011HÖ\u0001J\u0006\u0010\u0012\u001a\u00020\u0013J\t\u0010\u0014\u001a\u00020\u0003HÖ\u0001R\u0011\u0010\u0002\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007R\u0011\u0010\u0004\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\b\u0010\u0007¨\u0006\u0016"}, d2 = {"Lcom/origamilabs/orii/ui/auth/AuthFragmentArgs;", "Landroidx/navigation/NavArgs;", "userId", "", "verifyCode", "(Ljava/lang/String;Ljava/lang/String;)V", "getUserId", "()Ljava/lang/String;", "getVerifyCode", "component1", "component2", "copy", "equals", "", FacebookRequestErrorClassification.KEY_OTHER, "", "hashCode", "", "toBundle", "Landroid/os/Bundle;", "toString", "Companion", "orii-app-2.2.16-202001151600-864a420_prodRelease"}, k = 1, mv = {1, 1, 15})
/* loaded from: classes2.dex */
public final /* data */ class AuthFragmentArgs implements NavArgs {

    /* renamed from: Companion, reason: from kotlin metadata */
    public static final Companion INSTANCE = new Companion(null);
    private final String userId;
    private final String verifyCode;

    public static /* synthetic */ AuthFragmentArgs copy$default(AuthFragmentArgs authFragmentArgs, String str, String str2, int i, Object obj) {
        if ((i & 1) != 0) {
            str = authFragmentArgs.userId;
        }
        if ((i & 2) != 0) {
            str2 = authFragmentArgs.verifyCode;
        }
        return authFragmentArgs.copy(str, str2);
    }

    @JvmStatic
    public static final AuthFragmentArgs fromBundle(Bundle bundle) {
        return INSTANCE.fromBundle(bundle);
    }

    /* renamed from: component1, reason: from getter */
    public final String getUserId() {
        return this.userId;
    }

    /* renamed from: component2, reason: from getter */
    public final String getVerifyCode() {
        return this.verifyCode;
    }

    public final AuthFragmentArgs copy(String userId, String verifyCode) {
        Intrinsics.checkParameterIsNotNull(userId, "userId");
        Intrinsics.checkParameterIsNotNull(verifyCode, "verifyCode");
        return new AuthFragmentArgs(userId, verifyCode);
    }

    public boolean equals(Object r3) {
        if (this == r3) {
            return true;
        }
        if (!(r3 instanceof AuthFragmentArgs)) {
            return false;
        }
        AuthFragmentArgs authFragmentArgs = (AuthFragmentArgs) r3;
        return Intrinsics.areEqual(this.userId, authFragmentArgs.userId) && Intrinsics.areEqual(this.verifyCode, authFragmentArgs.verifyCode);
    }

    public int hashCode() {
        String str = this.userId;
        int hashCode = (str != null ? str.hashCode() : 0) * 31;
        String str2 = this.verifyCode;
        return hashCode + (str2 != null ? str2.hashCode() : 0);
    }

    public String toString() {
        return "AuthFragmentArgs(userId=" + this.userId + ", verifyCode=" + this.verifyCode + ")";
    }

    public AuthFragmentArgs(String userId, String verifyCode) {
        Intrinsics.checkParameterIsNotNull(userId, "userId");
        Intrinsics.checkParameterIsNotNull(verifyCode, "verifyCode");
        this.userId = userId;
        this.verifyCode = verifyCode;
    }

    public final String getUserId() {
        return this.userId;
    }

    public final String getVerifyCode() {
        return this.verifyCode;
    }

    public final Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putString("userId", this.userId);
        bundle.putString("verifyCode", this.verifyCode);
        return bundle;
    }

    /* compiled from: AuthFragmentArgs.kt */
    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0007¨\u0006\u0007"}, d2 = {"Lcom/origamilabs/orii/ui/auth/AuthFragmentArgs$Companion;", "", "()V", "fromBundle", "Lcom/origamilabs/orii/ui/auth/AuthFragmentArgs;", "bundle", "Landroid/os/Bundle;", "orii-app-2.2.16-202001151600-864a420_prodRelease"}, k = 1, mv = {1, 1, 15})
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        @JvmStatic
        public final AuthFragmentArgs fromBundle(Bundle bundle) {
            Intrinsics.checkParameterIsNotNull(bundle, "bundle");
            bundle.setClassLoader(AuthFragmentArgs.class.getClassLoader());
            if (bundle.containsKey("userId")) {
                String string = bundle.getString("userId");
                if (string == null) {
                    throw new IllegalArgumentException("Argument \"userId\" is marked as non-null but was passed a null value.");
                }
                if (bundle.containsKey("verifyCode")) {
                    String string2 = bundle.getString("verifyCode");
                    if (string2 == null) {
                        throw new IllegalArgumentException("Argument \"verifyCode\" is marked as non-null but was passed a null value.");
                    }
                    return new AuthFragmentArgs(string, string2);
                }
                throw new IllegalArgumentException("Required argument \"verifyCode\" is missing and does not have an android:defaultValue");
            }
            throw new IllegalArgumentException("Required argument \"userId\" is missing and does not have an android:defaultValue");
        }
    }
}
