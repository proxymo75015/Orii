package com.origamilabs.orii.notification;

import kotlin.Metadata;
import kotlin.jvm.internal.DefaultConstructorMarker;

/* compiled from: Constants.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\u0018\u0000 \u00032\u00020\u0001:\u0001\u0003B\u0005¢\u0006\u0002\u0010\u0002¨\u0006\u0004"}, d2 = {"Lcom/origamilabs/orii/notification/Constants;", "", "()V", "Companion", "notification_release"}, k = 1, mv = {1, 1, 15})
/* loaded from: classes2.dex */
public final class Constants {

    /* renamed from: Companion, reason: from kotlin metadata */
    public static final Companion INSTANCE = new Companion(null);
    private static final String[] SUPPORTED_APPS = {"phonecall", "sms", "com.whatsapp", "jp.naver.line.android", "com.facebook.orca", "com.tencent.mm"};

    /* compiled from: Constants.kt */
    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0011\n\u0002\u0010\u000e\n\u0002\b\u0004\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002R\u0019\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004¢\u0006\n\n\u0002\u0010\b\u001a\u0004\b\u0006\u0010\u0007¨\u0006\t"}, d2 = {"Lcom/origamilabs/orii/notification/Constants$Companion;", "", "()V", "SUPPORTED_APPS", "", "", "getSUPPORTED_APPS", "()[Ljava/lang/String;", "[Ljava/lang/String;", "notification_release"}, k = 1, mv = {1, 1, 15})
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        public final String[] getSUPPORTED_APPS() {
            return Constants.SUPPORTED_APPS;
        }
    }
}
