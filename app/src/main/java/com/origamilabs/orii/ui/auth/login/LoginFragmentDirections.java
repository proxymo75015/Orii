package com.origamilabs.orii.ui.auth.login;

import androidx.navigation.ActionOnlyNavDirections;
import androidx.navigation.NavDirections;
import com.origamilabs.orii.R;
import kotlin.Metadata;
import kotlin.jvm.internal.DefaultConstructorMarker;

/* compiled from: LoginFragmentDirections.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\u0018\u0000 \u00032\u00020\u0001:\u0001\u0003B\u0007\b\u0002¢\u0006\u0002\u0010\u0002¨\u0006\u0004"}, d2 = {"Lcom/origamilabs/orii/ui/auth/login/LoginFragmentDirections;", "", "()V", "Companion", "orii-app-2.2.16-202001151600-864a420_prodRelease"}, k = 1, mv = {1, 1, 15})
/* loaded from: classes2.dex */
public final class LoginFragmentDirections {

    /* renamed from: Companion, reason: from kotlin metadata */
    public static final Companion INSTANCE = new Companion(null);

    /* compiled from: LoginFragmentDirections.kt */
    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\u0006\u0010\u0003\u001a\u00020\u0004¨\u0006\u0005"}, d2 = {"Lcom/origamilabs/orii/ui/auth/login/LoginFragmentDirections$Companion;", "", "()V", "actionLoginFragmentToForgotPasswordFragment", "Landroidx/navigation/NavDirections;", "orii-app-2.2.16-202001151600-864a420_prodRelease"}, k = 1, mv = {1, 1, 15})
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        public final NavDirections actionLoginFragmentToForgotPasswordFragment() {
            return new ActionOnlyNavDirections(R.id.action_loginFragment_to_forgotPasswordFragment);
        }
    }

    private LoginFragmentDirections() {
    }
}
