package com.origamilabs.orii.notification.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import com.google.android.gms.common.internal.ServiceSpecificExtraArgs;
import com.origamilabs.orii.notification.listeners.OnSmsReceivedListener;
import kotlin.Metadata;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.Regex;
import kotlin.text.StringsKt;

/* compiled from: SmsReceiver.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u0000 \u000f2\u00020\u0001:\u0001\u000fB\u0005¢\u0006\u0002\u0010\u0002J\u0018\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u0006H\u0002J\u0018\u0010\n\u001a\u00020\u000b2\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\f\u001a\u00020\rH\u0016J\u000e\u0010\u000e\u001a\u00020\u000b2\u0006\u0010\u0007\u001a\u00020\bR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.¢\u0006\u0002\n\u0000¨\u0006\u0010"}, d2 = {"Lcom/origamilabs/orii/notification/receivers/SmsReceiver;", "Landroid/content/BroadcastReceiver;", "()V", ServiceSpecificExtraArgs.CastExtraArgs.LISTENER, "Lcom/origamilabs/orii/notification/listeners/OnSmsReceivedListener;", "getSenderByPhoneNumber", "", "context", "Landroid/content/Context;", "phoneNumber", "onReceive", "", "intent", "Landroid/content/Intent;", "setOnSmsReceivedListener", "Companion", "notification_release"}, k = 1, mv = {1, 1, 15})
/* loaded from: classes2.dex */
public final class SmsReceiver extends BroadcastReceiver {
    public static final String TAG = "SmsReceiver";
    private OnSmsReceivedListener listener;

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        SmsMessage createFromPdu;
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(intent, "intent");
        Bundle extras = intent.getExtras();
        if (extras != null) {
            Object obj = extras.get("pdus");
            if (obj == null) {
                throw new TypeCastException("null cannot be cast to non-null type kotlin.Array<*>");
            }
            Object[] objArr = (Object[]) obj;
            int length = objArr.length;
            for (int i = 0; i < length; i++) {
                if (Build.VERSION.SDK_INT >= 23) {
                    String string = extras.getString("format");
                    Object obj2 = objArr[i];
                    if (obj2 == null) {
                        throw new TypeCastException("null cannot be cast to non-null type kotlin.ByteArray");
                    }
                    createFromPdu = SmsMessage.createFromPdu((byte[]) obj2, string);
                    Intrinsics.checkExpressionValueIsNotNull(createFromPdu, "SmsMessage.createFromPdu…[i] as ByteArray, format)");
                } else {
                    Object obj3 = objArr[i];
                    if (obj3 == null) {
                        throw new TypeCastException("null cannot be cast to non-null type kotlin.ByteArray");
                    }
                    createFromPdu = SmsMessage.createFromPdu((byte[]) obj3);
                    Intrinsics.checkExpressionValueIsNotNull(createFromPdu, "SmsMessage.createFromPdu(pdus[i] as ByteArray)");
                }
                String str = createFromPdu.getMessageBody().toString();
                String valueOf = String.valueOf(createFromPdu.getOriginatingAddress());
                String senderByPhoneNumber = getSenderByPhoneNumber(context, valueOf);
                String str2 = "SMS from " + valueOf + " : " + senderByPhoneNumber + " :" + str + " \n";
                String str3 = senderByPhoneNumber;
                for (int i2 = 0; i2 <= 9; i2++) {
                    str3 = new Regex(String.valueOf(i2)).replace(str3, StringsKt.padEnd(StringsKt.padStart(String.valueOf(i2), 2, ' '), 3, ' '));
                }
                OnSmsReceivedListener onSmsReceivedListener = this.listener;
                if (onSmsReceivedListener == null) {
                    Intrinsics.throwUninitializedPropertyAccessException(ServiceSpecificExtraArgs.CastExtraArgs.LISTENER);
                }
                if (onSmsReceivedListener != null) {
                    OnSmsReceivedListener onSmsReceivedListener2 = this.listener;
                    if (onSmsReceivedListener2 == null) {
                        Intrinsics.throwUninitializedPropertyAccessException(ServiceSpecificExtraArgs.CastExtraArgs.LISTENER);
                    }
                    onSmsReceivedListener2.onSmsReceived(str3, str);
                }
            }
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    public final void setOnSmsReceivedListener(Context context) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        this.listener = (OnSmsReceivedListener) context;
    }

    /* JADX WARN: Removed duplicated region for block: B:6:0x004d  */
    /* JADX WARN: Removed duplicated region for block: B:9:? A[RETURN, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private final java.lang.String getSenderByPhoneNumber(android.content.Context r9, java.lang.String r10) {
        /*
            r8 = this;
            android.net.Uri r0 = android.provider.ContactsContract.PhoneLookup.CONTENT_FILTER_URI
            java.lang.String r1 = android.net.Uri.encode(r10)
            android.net.Uri r3 = android.net.Uri.withAppendedPath(r0, r1)
            android.content.ContentResolver r2 = r9.getContentResolver()
            java.lang.String r9 = "display_name"
            java.lang.String[] r4 = new java.lang.String[]{r9}
            r5 = 0
            r6 = 0
            r7 = 0
            android.database.Cursor r0 = r2.query(r3, r4, r5, r6, r7)
            java.lang.String r1 = ""
            if (r0 == 0) goto L46
            boolean r2 = r0.moveToFirst()     // Catch: java.lang.Throwable -> L38 java.lang.Exception -> L3a
            if (r2 == 0) goto L33
            int r9 = r0.getColumnIndex(r9)     // Catch: java.lang.Throwable -> L38 java.lang.Exception -> L3a
            java.lang.String r9 = r0.getString(r9)     // Catch: java.lang.Throwable -> L38 java.lang.Exception -> L3a
            java.lang.String r2 = "cursor.getString(cursor.…t.Contacts.DISPLAY_NAME))"
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r9, r2)     // Catch: java.lang.Throwable -> L38 java.lang.Exception -> L3a
            goto L34
        L33:
            r9 = r1
        L34:
            r0.close()
            goto L47
        L38:
            r9 = move-exception
            goto L42
        L3a:
            r9 = move-exception
            r9.printStackTrace()     // Catch: java.lang.Throwable -> L38
            r0.close()
            goto L46
        L42:
            r0.close()
            throw r9
        L46:
            r9 = r1
        L47:
            boolean r0 = kotlin.jvm.internal.Intrinsics.areEqual(r9, r1)
            if (r0 == 0) goto L4e
            r9 = r10
        L4e:
            return r9
        */
        throw new UnsupportedOperationException("Method not decompiled: com.origamilabs.orii.notification.receivers.SmsReceiver.getSenderByPhoneNumber(android.content.Context, java.lang.String):java.lang.String");
    }
}
