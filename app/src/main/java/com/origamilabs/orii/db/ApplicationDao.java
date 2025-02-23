package com.origamilabs.orii.db;

import androidx.room.Update;
import com.origamilabs.orii.models.Application;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.util.List;
import kotlin.Metadata;

/* compiled from: ApplicationDao.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0010\u0011\n\u0002\b\u0003\bg\u0018\u00002\u00020\u0001J\u0010\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H'J\u0012\u0010\u0006\u001a\u0004\u0018\u00010\u00052\u0006\u0010\u0007\u001a\u00020\bH'J\u000e\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00050\nH'J!\u0010\u000b\u001a\u00020\u00032\u0012\u0010\f\u001a\n\u0012\u0006\b\u0001\u0012\u00020\u00050\r\"\u00020\u0005H'¢\u0006\u0002\u0010\u000eJ!\u0010\u000f\u001a\u00020\u00032\u0012\u0010\f\u001a\n\u0012\u0006\b\u0001\u0012\u00020\u00050\r\"\u00020\u0005H'¢\u0006\u0002\u0010\u000e¨\u0006\u0010"}, d2 = {"Lcom/origamilabs/orii/db/ApplicationDao;", "", "delete", "", SettingsJsonConstants.APP_KEY, "Lcom/origamilabs/orii/models/Application;", "findByPackageName", "packageName", "", "getAll", "", "insert", "apps", "", "([Lcom/origamilabs/orii/models/Application;)V", "update", "orii-app-2.2.16-202001151600-864a420_prodRelease"}, k = 1, mv = {1, 1, 15})
/* loaded from: classes2.dex */
public interface ApplicationDao {
    void delete(Application r1);

    Application findByPackageName(String packageName);

    List<Application> getAll();

    void insert(Application... apps);

    @Update
    void update(Application... apps);
}
