package com.origamilabs.orii.db;

import android.database.Cursor;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.origamilabs.orii.models.Application;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes2.dex */
public final class ApplicationDao_Impl implements ApplicationDao {
    private final RoomDatabase __db;
    private final EntityDeletionOrUpdateAdapter __deletionAdapterOfApplication;
    private final EntityInsertionAdapter __insertionAdapterOfApplication;
    private final EntityDeletionOrUpdateAdapter __updateAdapterOfApplication;

    public ApplicationDao_Impl(RoomDatabase roomDatabase) {
        this.__db = roomDatabase;
        this.__insertionAdapterOfApplication = new EntityInsertionAdapter<Application>(roomDatabase) { // from class: com.origamilabs.orii.db.ApplicationDao_Impl.1
            @Override // androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "INSERT OR ABORT INTO `Application`(`package_name`,`led_color`,`vibration`) VALUES (?,?,?)";
            }

            AnonymousClass1(RoomDatabase roomDatabase2) {
                super(roomDatabase2);
            }

            @Override // androidx.room.EntityInsertionAdapter
            public void bind(SupportSQLiteStatement supportSQLiteStatement, Application application) {
                if (application.getPackageName() == null) {
                    supportSQLiteStatement.bindNull(1);
                } else {
                    supportSQLiteStatement.bindString(1, application.getPackageName());
                }
                supportSQLiteStatement.bindLong(2, application.getLedColor());
                supportSQLiteStatement.bindLong(3, application.getVibration());
            }
        };
        this.__deletionAdapterOfApplication = new EntityDeletionOrUpdateAdapter<Application>(roomDatabase2) { // from class: com.origamilabs.orii.db.ApplicationDao_Impl.2
            @Override // androidx.room.EntityDeletionOrUpdateAdapter, androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "DELETE FROM `Application` WHERE `package_name` = ?";
            }

            AnonymousClass2(RoomDatabase roomDatabase2) {
                super(roomDatabase2);
            }

            @Override // androidx.room.EntityDeletionOrUpdateAdapter
            public void bind(SupportSQLiteStatement supportSQLiteStatement, Application application) {
                if (application.getPackageName() == null) {
                    supportSQLiteStatement.bindNull(1);
                } else {
                    supportSQLiteStatement.bindString(1, application.getPackageName());
                }
            }
        };
        this.__updateAdapterOfApplication = new EntityDeletionOrUpdateAdapter<Application>(roomDatabase2) { // from class: com.origamilabs.orii.db.ApplicationDao_Impl.3
            @Override // androidx.room.EntityDeletionOrUpdateAdapter, androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "UPDATE OR ABORT `Application` SET `package_name` = ?,`led_color` = ?,`vibration` = ? WHERE `package_name` = ?";
            }

            AnonymousClass3(RoomDatabase roomDatabase2) {
                super(roomDatabase2);
            }

            @Override // androidx.room.EntityDeletionOrUpdateAdapter
            public void bind(SupportSQLiteStatement supportSQLiteStatement, Application application) {
                if (application.getPackageName() == null) {
                    supportSQLiteStatement.bindNull(1);
                } else {
                    supportSQLiteStatement.bindString(1, application.getPackageName());
                }
                supportSQLiteStatement.bindLong(2, application.getLedColor());
                supportSQLiteStatement.bindLong(3, application.getVibration());
                if (application.getPackageName() == null) {
                    supportSQLiteStatement.bindNull(4);
                } else {
                    supportSQLiteStatement.bindString(4, application.getPackageName());
                }
            }
        };
    }

    /* renamed from: com.origamilabs.orii.db.ApplicationDao_Impl$1 */
    class AnonymousClass1 extends EntityInsertionAdapter<Application> {
        @Override // androidx.room.SharedSQLiteStatement
        public String createQuery() {
            return "INSERT OR ABORT INTO `Application`(`package_name`,`led_color`,`vibration`) VALUES (?,?,?)";
        }

        AnonymousClass1(RoomDatabase roomDatabase2) {
            super(roomDatabase2);
        }

        @Override // androidx.room.EntityInsertionAdapter
        public void bind(SupportSQLiteStatement supportSQLiteStatement, Application application) {
            if (application.getPackageName() == null) {
                supportSQLiteStatement.bindNull(1);
            } else {
                supportSQLiteStatement.bindString(1, application.getPackageName());
            }
            supportSQLiteStatement.bindLong(2, application.getLedColor());
            supportSQLiteStatement.bindLong(3, application.getVibration());
        }
    }

    /* renamed from: com.origamilabs.orii.db.ApplicationDao_Impl$2 */
    class AnonymousClass2 extends EntityDeletionOrUpdateAdapter<Application> {
        @Override // androidx.room.EntityDeletionOrUpdateAdapter, androidx.room.SharedSQLiteStatement
        public String createQuery() {
            return "DELETE FROM `Application` WHERE `package_name` = ?";
        }

        AnonymousClass2(RoomDatabase roomDatabase2) {
            super(roomDatabase2);
        }

        @Override // androidx.room.EntityDeletionOrUpdateAdapter
        public void bind(SupportSQLiteStatement supportSQLiteStatement, Application application) {
            if (application.getPackageName() == null) {
                supportSQLiteStatement.bindNull(1);
            } else {
                supportSQLiteStatement.bindString(1, application.getPackageName());
            }
        }
    }

    /* renamed from: com.origamilabs.orii.db.ApplicationDao_Impl$3 */
    class AnonymousClass3 extends EntityDeletionOrUpdateAdapter<Application> {
        @Override // androidx.room.EntityDeletionOrUpdateAdapter, androidx.room.SharedSQLiteStatement
        public String createQuery() {
            return "UPDATE OR ABORT `Application` SET `package_name` = ?,`led_color` = ?,`vibration` = ? WHERE `package_name` = ?";
        }

        AnonymousClass3(RoomDatabase roomDatabase2) {
            super(roomDatabase2);
        }

        @Override // androidx.room.EntityDeletionOrUpdateAdapter
        public void bind(SupportSQLiteStatement supportSQLiteStatement, Application application) {
            if (application.getPackageName() == null) {
                supportSQLiteStatement.bindNull(1);
            } else {
                supportSQLiteStatement.bindString(1, application.getPackageName());
            }
            supportSQLiteStatement.bindLong(2, application.getLedColor());
            supportSQLiteStatement.bindLong(3, application.getVibration());
            if (application.getPackageName() == null) {
                supportSQLiteStatement.bindNull(4);
            } else {
                supportSQLiteStatement.bindString(4, application.getPackageName());
            }
        }
    }

    @Override // com.origamilabs.orii.db.ApplicationDao
    public void insert(Application... applicationArr) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__insertionAdapterOfApplication.insert((Object[]) applicationArr);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    @Override // com.origamilabs.orii.db.ApplicationDao
    public void delete(Application application) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__deletionAdapterOfApplication.handle(application);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    @Override // com.origamilabs.orii.db.ApplicationDao
    public void update(Application... applicationArr) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__updateAdapterOfApplication.handleMultiple(applicationArr);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    @Override // com.origamilabs.orii.db.ApplicationDao
    public List<Application> getAll() {
        RoomSQLiteQuery acquire = RoomSQLiteQuery.acquire("SELECT * FROM application", 0);
        this.__db.assertNotSuspendingTransaction();
        Cursor query = DBUtil.query(this.__db, acquire, false);
        try {
            int columnIndexOrThrow = CursorUtil.getColumnIndexOrThrow(query, "package_name");
            int columnIndexOrThrow2 = CursorUtil.getColumnIndexOrThrow(query, "led_color");
            int columnIndexOrThrow3 = CursorUtil.getColumnIndexOrThrow(query, "vibration");
            ArrayList arrayList = new ArrayList(query.getCount());
            while (query.moveToNext()) {
                Application application = new Application();
                application.setPackageName(query.getString(columnIndexOrThrow));
                application.setLedColor(query.getInt(columnIndexOrThrow2));
                application.setVibration(query.getInt(columnIndexOrThrow3));
                arrayList.add(application);
            }
            return arrayList;
        } finally {
            query.close();
            acquire.release();
        }
    }

    @Override // com.origamilabs.orii.db.ApplicationDao
    public Application findByPackageName(String str) {
        Application application;
        RoomSQLiteQuery acquire = RoomSQLiteQuery.acquire("SELECT * FROM application WHERE package_name LIKE ? LIMIT 1", 1);
        if (str == null) {
            acquire.bindNull(1);
        } else {
            acquire.bindString(1, str);
        }
        this.__db.assertNotSuspendingTransaction();
        Cursor query = DBUtil.query(this.__db, acquire, false);
        try {
            int columnIndexOrThrow = CursorUtil.getColumnIndexOrThrow(query, "package_name");
            int columnIndexOrThrow2 = CursorUtil.getColumnIndexOrThrow(query, "led_color");
            int columnIndexOrThrow3 = CursorUtil.getColumnIndexOrThrow(query, "vibration");
            if (query.moveToFirst()) {
                application = new Application();
                application.setPackageName(query.getString(columnIndexOrThrow));
                application.setLedColor(query.getInt(columnIndexOrThrow2));
                application.setVibration(query.getInt(columnIndexOrThrow3));
            } else {
                application = null;
            }
            return application;
        } finally {
            query.close();
            acquire.release();
        }
    }
}
