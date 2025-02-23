package com.origamilabs.orii.notification.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.google.android.gms.common.internal.ServiceSpecificExtraArgs;
import com.origamilabs.orii.notification.listeners.OnNotificationReceivedListener;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: NotificationReceiver.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u0000 \f2\u00020\u0001:\u0001\fB\u0005¢\u0006\u0002\u0010\u0002J\u0018\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0016J\u000e\u0010\u000b\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bR\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e¢\u0006\u0002\n\u0000¨\u0006\r"}, d2 = {"Lcom/origamilabs/orii/notification/receivers/NotificationReceiver;", "Landroid/content/BroadcastReceiver;", "()V", ServiceSpecificExtraArgs.CastExtraArgs.LISTENER, "Lcom/origamilabs/orii/notification/listeners/OnNotificationReceivedListener;", "onReceive", "", "context", "Landroid/content/Context;", "intent", "Landroid/content/Intent;", "setOnNotificationReceivedListener", "Companion", "notification_release"}, k = 1, mv = {1, 1, 15})
/* loaded from: classes2.dex */
public final class NotificationReceiver extends BroadcastReceiver {
    private OnNotificationReceivedListener listener;
    private static final String TAG = TAG;
    private static final String TAG = TAG;

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(intent, "intent");
        Log.d(TAG, "onReceive");
        String packageName = intent.getStringExtra("packageName");
        String sender = intent.getStringExtra("sender");
        String message = intent.getStringExtra("message");
        OnNotificationReceivedListener onNotificationReceivedListener = this.listener;
        if (onNotificationReceivedListener != null) {
            if (onNotificationReceivedListener == null) {
                Intrinsics.throwNpe();
            }
            Intrinsics.checkExpressionValueIsNotNull(packageName, "packageName");
            Intrinsics.checkExpressionValueIsNotNull(sender, "sender");
            Intrinsics.checkExpressionValueIsNotNull(message, "message");
            onNotificationReceivedListener.onNotificationReceived(packageName, sender, message);
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    public final void setOnNotificationReceivedListener(Context context) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        this.listener = (OnNotificationReceivedListener) context;
    }
}
