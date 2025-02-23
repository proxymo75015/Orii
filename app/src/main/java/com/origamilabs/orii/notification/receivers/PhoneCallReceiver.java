package com.origamilabs.orii.notification.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import com.facebook.internal.ServerProtocol;
import com.google.android.gms.common.internal.ServiceSpecificExtraArgs;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: PhoneCallReceiver.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u0000 \u00132\u00020\u0001:\u0002\u0013\u0014B\u0005¢\u0006\u0002\u0010\u0002J\u000e\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\u0005J\u001e\u0010\t\u001a\u00020\u00072\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000fJ\u0018\u0010\u0010\u001a\u00020\u00072\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\u0011\u001a\u00020\u0012H\u0016R\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\u0015"}, d2 = {"Lcom/origamilabs/orii/notification/receivers/PhoneCallReceiver;", "Landroid/content/BroadcastReceiver;", "()V", "listeners", "Ljava/util/ArrayList;", "Lcom/origamilabs/orii/notification/receivers/PhoneCallReceiver$OnPhoneCallStateChangeListener;", "addListener", "", ServiceSpecificExtraArgs.CastExtraArgs.LISTENER, "onCallStateChanged", "context", "Landroid/content/Context;", ServerProtocol.DIALOG_PARAM_STATE, "", "number", "", "onReceive", "intent", "Landroid/content/Intent;", "Companion", "OnPhoneCallStateChangeListener", "notification_release"}, k = 1, mv = {1, 1, 15})
/* loaded from: classes2.dex */
public final class PhoneCallReceiver extends BroadcastReceiver {
    private static Date callStartTime;
    private static boolean isIncoming;
    private static int lastState;
    private static String savedNumber;
    private final ArrayList<OnPhoneCallStateChangeListener> listeners = new ArrayList<>();
    private static final String TAG = TAG;
    private static final String TAG = TAG;

    /* compiled from: PhoneCallReceiver.kt */
    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\bf\u0018\u00002\u00020\u0001J \u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tH&J(\u0010\n\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\u000b\u001a\u00020\tH&J \u0010\f\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tH&J \u0010\r\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tH&¨\u0006\u000e"}, d2 = {"Lcom/origamilabs/orii/notification/receivers/PhoneCallReceiver$OnPhoneCallStateChangeListener;", "", "onIncomingCallAnswered", "", "ctx", "Landroid/content/Context;", "number", "", AnalyticsManager.ActionState.START, "Ljava/util/Date;", "onIncomingCallEnded", AnalyticsManager.ActionState.END, "onIncomingCallReceived", "onMissedCall", "notification_release"}, k = 1, mv = {1, 1, 15})
    public interface OnPhoneCallStateChangeListener {
        void onIncomingCallAnswered(Context ctx, String number, Date r3);

        void onIncomingCallEnded(Context ctx, String number, Date r3, Date r4);

        void onIncomingCallReceived(Context ctx, String number, Date r3);

        void onMissedCall(Context ctx, String number, Date r3);
    }

    public final void addListener(OnPhoneCallStateChangeListener r2) {
        Intrinsics.checkParameterIsNotNull(r2, "listener");
        this.listeners.add(r2);
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(intent, "intent");
        Bundle extras = intent.getExtras();
        if (extras == null) {
            Intrinsics.throwNpe();
        }
        String string = extras.getString(ServerProtocol.DIALOG_PARAM_STATE);
        Bundle extras2 = intent.getExtras();
        if (extras2 == null) {
            Intrinsics.throwNpe();
        }
        String string2 = extras2.getString("incoming_number");
        int i = 0;
        if (!Intrinsics.areEqual(string, TelephonyManager.EXTRA_STATE_IDLE)) {
            if (Intrinsics.areEqual(string, TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                i = 2;
            } else if (Intrinsics.areEqual(string, TelephonyManager.EXTRA_STATE_RINGING)) {
                i = 1;
            }
        }
        if (string2 != null) {
            onCallStateChanged(context, i, string2);
        }
    }

    public final void onCallStateChanged(Context context, int r8, String number) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(number, "number");
        int i = lastState;
        if (i == r8) {
            return;
        }
        if (r8 != 0) {
            if (r8 == 1) {
                isIncoming = true;
                callStartTime = new Date();
                savedNumber = number;
                Iterator<OnPhoneCallStateChangeListener> it = this.listeners.iterator();
                while (it.hasNext()) {
                    OnPhoneCallStateChangeListener next = it.next();
                    Date date = callStartTime;
                    if (date == null) {
                        Intrinsics.throwUninitializedPropertyAccessException("callStartTime");
                    }
                    next.onIncomingCallReceived(context, number, date);
                }
            } else if (r8 == 2) {
                if (i != 1) {
                    isIncoming = false;
                    callStartTime = new Date();
                } else {
                    isIncoming = true;
                    callStartTime = new Date();
                    Iterator<OnPhoneCallStateChangeListener> it2 = this.listeners.iterator();
                    while (it2.hasNext()) {
                        OnPhoneCallStateChangeListener next2 = it2.next();
                        String str = savedNumber;
                        if (str == null) {
                            Intrinsics.throwUninitializedPropertyAccessException("savedNumber");
                        }
                        Date date2 = callStartTime;
                        if (date2 == null) {
                            Intrinsics.throwUninitializedPropertyAccessException("callStartTime");
                        }
                        next2.onIncomingCallAnswered(context, str, date2);
                    }
                }
            }
        } else if (i == 1) {
            Iterator<OnPhoneCallStateChangeListener> it3 = this.listeners.iterator();
            while (it3.hasNext()) {
                OnPhoneCallStateChangeListener next3 = it3.next();
                String str2 = savedNumber;
                if (str2 == null) {
                    Intrinsics.throwUninitializedPropertyAccessException("savedNumber");
                }
                Date date3 = callStartTime;
                if (date3 == null) {
                    Intrinsics.throwUninitializedPropertyAccessException("callStartTime");
                }
                next3.onMissedCall(context, str2, date3);
            }
        } else if (isIncoming) {
            Iterator<OnPhoneCallStateChangeListener> it4 = this.listeners.iterator();
            while (it4.hasNext()) {
                OnPhoneCallStateChangeListener next4 = it4.next();
                String str3 = savedNumber;
                if (str3 == null) {
                    Intrinsics.throwUninitializedPropertyAccessException("savedNumber");
                }
                Date date4 = callStartTime;
                if (date4 == null) {
                    Intrinsics.throwUninitializedPropertyAccessException("callStartTime");
                }
                next4.onIncomingCallEnded(context, str3, date4, new Date());
            }
        }
        lastState = r8;
    }
}
