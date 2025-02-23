package com.origamilabs.orii.handler;

import com.origamilabs.orii.core.bluetooth.manager.CommandManager;
import com.origamilabs.orii.manager.AppManager;
import com.origamilabs.orii.models.Application;
import java.util.TimerTask;
import kotlin.Metadata;

/* compiled from: IncomingCallHandler.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u0011\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000*\u0001\u0000\b\n\u0018\u00002\u00020\u0001J\b\u0010\u0002\u001a\u00020\u0003H\u0016¨\u0006\u0004"}, d2 = {"com/origamilabs/orii/handler/IncomingCallHandler$onIncomingCallReceived$1", "Ljava/util/TimerTask;", "run", "", "orii-app-2.2.16-202001151600-864a420_prodRelease"}, k = 1, mv = {1, 1, 15})
/* loaded from: classes2.dex */
public final class IncomingCallHandler$onIncomingCallReceived$1 extends TimerTask {
    final /* synthetic */ String $sender;

    IncomingCallHandler$onIncomingCallReceived$1(String str) {
        this.$sender = str;
    }

    @Override // java.util.TimerTask, java.lang.Runnable
    public void run() {
        AppManager.INSTANCE.runQueryOnBackground(new Runnable() { // from class: com.origamilabs.orii.handler.IncomingCallHandler$onIncomingCallReceived$1$run$1
            @Override // java.lang.Runnable
            public final void run() {
                Application findByPackageName = AppManager.INSTANCE.getDatabase().applicationDao().findByPackageName("phonecall");
                com.origamilabs.orii.models.Application findByPersonName = AppManager.INSTANCE.getDatabase().personDao().findByPersonName(IncomingCallHandler$onIncomingCallReceived$1.this.$sender);
                if (findByPackageName != null) {
                    if (findByPersonName == null) {
                        findByPersonName = new com.origamilabs.orii.models.Application(-1, 0, 0, "");
                    }
                    CommandManager.getInstance().putCallMessageReceivedTask(findByPackageName.getLedColor(), findByPackageName.getVibration(), findByPersonName.getLedColor(), findByPersonName.getVibration(), false);
                }
            }
        });
    }
}
