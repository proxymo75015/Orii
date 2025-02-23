package com.origamilabs.orii.db;

import androidx.room.Update;
import com.origamilabs.orii.models.Application;
import java.util.List;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: PersonDao.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0010\u0011\n\u0002\b\u0004\bg\u0018\u00002\u00020\u0001J\u0010\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H'J\u0012\u0010\u0006\u001a\u0004\u0018\u00010\u00052\u0006\u0010\u0007\u001a\u00020\bH'J\u0012\u0010\t\u001a\u0004\u0018\u00010\u00052\u0006\u0010\n\u001a\u00020\u000bH'J\u000e\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00050\rH'J!\u0010\u000e\u001a\u00020\u00032\u0012\u0010\u000f\u001a\n\u0012\u0006\b\u0001\u0012\u00020\u00050\u0010\"\u00020\u0005H'¢\u0006\u0002\u0010\u0011J!\u0010\u0012\u001a\u00020\u00032\u0012\u0010\u000f\u001a\n\u0012\u0006\b\u0001\u0012\u00020\u00050\u0010\"\u00020\u0005H\u0017¢\u0006\u0002\u0010\u0011J!\u0010\u0013\u001a\u00020\u00032\u0012\u0010\u000f\u001a\n\u0012\u0006\b\u0001\u0012\u00020\u00050\u0010\"\u00020\u0005H'¢\u0006\u0002\u0010\u0011¨\u0006\u0014"}, d2 = {"Lcom/origamilabs/orii/db/PersonDao;", "", "delete", "", "person", "Lcom/origamilabs/orii/models/Person;", "findByPersonId", "pid", "", "findByPersonName", "name", "", "getAll", "", "insert", "people", "", "([Lcom/origamilabs/orii/models/Person;)V", "insertOrUpdate", "update", "orii-app-2.2.16-202001151600-864a420_prodRelease"}, k = 1, mv = {1, 1, 15})
/* loaded from: classes2.dex */
public interface PersonDao {
    void delete(Application person);

    Application findByPersonId(int pid);

    Application findByPersonName(String name);

    List<Application> getAll();

    void insert(Application... people);

    void insertOrUpdate(Application... people);

    @Update
    void update(Application... people);

    /* compiled from: PersonDao.kt */
    @Metadata(bv = {1, 0, 3}, k = 3, mv = {1, 1, 15})
    public static final class DefaultImpls {
        public static void insertOrUpdate(PersonDao personDao, Application... people) {
            Intrinsics.checkParameterIsNotNull(people, "people");
            for (Application application : people) {
                if (personDao.findByPersonId(application.getPid()) != null) {
                    personDao.update(application);
                } else {
                    personDao.insert(application);
                }
            }
        }
    }
}
