package com.origamilabs.orii.db;

import android.database.Cursor;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.origamilabs.orii.db.PersonDao;
import com.origamilabs.orii.models.Application;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes2.dex */
public final class PersonDao_Impl implements PersonDao {
    private final RoomDatabase __db;
    private final EntityDeletionOrUpdateAdapter __deletionAdapterOfPerson;
    private final EntityInsertionAdapter __insertionAdapterOfPerson;
    private final EntityDeletionOrUpdateAdapter __updateAdapterOfPerson;

    public PersonDao_Impl(RoomDatabase roomDatabase) {
        this.__db = roomDatabase;
        this.__insertionAdapterOfPerson = new EntityInsertionAdapter<Application>(roomDatabase) { // from class: com.origamilabs.orii.db.PersonDao_Impl.1
            @Override // androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "INSERT OR ABORT INTO `Person`(`pid`,`person_name`,`led_color`,`vibration`) VALUES (?,?,?,?)";
            }

            AnonymousClass1(RoomDatabase roomDatabase2) {
                super(roomDatabase2);
            }

            @Override // androidx.room.EntityInsertionAdapter
            public void bind(SupportSQLiteStatement supportSQLiteStatement, Application application) {
                supportSQLiteStatement.bindLong(1, application.getPid());
                if (application.getPersonName() == null) {
                    supportSQLiteStatement.bindNull(2);
                } else {
                    supportSQLiteStatement.bindString(2, application.getPersonName());
                }
                supportSQLiteStatement.bindLong(3, application.getLedColor());
                supportSQLiteStatement.bindLong(4, application.getVibration());
            }
        };
        this.__deletionAdapterOfPerson = new EntityDeletionOrUpdateAdapter<Application>(roomDatabase2) { // from class: com.origamilabs.orii.db.PersonDao_Impl.2
            @Override // androidx.room.EntityDeletionOrUpdateAdapter, androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "DELETE FROM `Person` WHERE `pid` = ?";
            }

            AnonymousClass2(RoomDatabase roomDatabase2) {
                super(roomDatabase2);
            }

            @Override // androidx.room.EntityDeletionOrUpdateAdapter
            public void bind(SupportSQLiteStatement supportSQLiteStatement, Application application) {
                supportSQLiteStatement.bindLong(1, application.getPid());
            }
        };
        this.__updateAdapterOfPerson = new EntityDeletionOrUpdateAdapter<Application>(roomDatabase2) { // from class: com.origamilabs.orii.db.PersonDao_Impl.3
            @Override // androidx.room.EntityDeletionOrUpdateAdapter, androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "UPDATE OR ABORT `Person` SET `pid` = ?,`person_name` = ?,`led_color` = ?,`vibration` = ? WHERE `pid` = ?";
            }

            AnonymousClass3(RoomDatabase roomDatabase2) {
                super(roomDatabase2);
            }

            @Override // androidx.room.EntityDeletionOrUpdateAdapter
            public void bind(SupportSQLiteStatement supportSQLiteStatement, Application application) {
                supportSQLiteStatement.bindLong(1, application.getPid());
                if (application.getPersonName() == null) {
                    supportSQLiteStatement.bindNull(2);
                } else {
                    supportSQLiteStatement.bindString(2, application.getPersonName());
                }
                supportSQLiteStatement.bindLong(3, application.getLedColor());
                supportSQLiteStatement.bindLong(4, application.getVibration());
                supportSQLiteStatement.bindLong(5, application.getPid());
            }
        };
    }

    /* renamed from: com.origamilabs.orii.db.PersonDao_Impl$1 */
    class AnonymousClass1 extends EntityInsertionAdapter<Application> {
        @Override // androidx.room.SharedSQLiteStatement
        public String createQuery() {
            return "INSERT OR ABORT INTO `Person`(`pid`,`person_name`,`led_color`,`vibration`) VALUES (?,?,?,?)";
        }

        AnonymousClass1(RoomDatabase roomDatabase2) {
            super(roomDatabase2);
        }

        @Override // androidx.room.EntityInsertionAdapter
        public void bind(SupportSQLiteStatement supportSQLiteStatement, Application application) {
            supportSQLiteStatement.bindLong(1, application.getPid());
            if (application.getPersonName() == null) {
                supportSQLiteStatement.bindNull(2);
            } else {
                supportSQLiteStatement.bindString(2, application.getPersonName());
            }
            supportSQLiteStatement.bindLong(3, application.getLedColor());
            supportSQLiteStatement.bindLong(4, application.getVibration());
        }
    }

    /* renamed from: com.origamilabs.orii.db.PersonDao_Impl$2 */
    class AnonymousClass2 extends EntityDeletionOrUpdateAdapter<Application> {
        @Override // androidx.room.EntityDeletionOrUpdateAdapter, androidx.room.SharedSQLiteStatement
        public String createQuery() {
            return "DELETE FROM `Person` WHERE `pid` = ?";
        }

        AnonymousClass2(RoomDatabase roomDatabase2) {
            super(roomDatabase2);
        }

        @Override // androidx.room.EntityDeletionOrUpdateAdapter
        public void bind(SupportSQLiteStatement supportSQLiteStatement, Application application) {
            supportSQLiteStatement.bindLong(1, application.getPid());
        }
    }

    /* renamed from: com.origamilabs.orii.db.PersonDao_Impl$3 */
    class AnonymousClass3 extends EntityDeletionOrUpdateAdapter<Application> {
        @Override // androidx.room.EntityDeletionOrUpdateAdapter, androidx.room.SharedSQLiteStatement
        public String createQuery() {
            return "UPDATE OR ABORT `Person` SET `pid` = ?,`person_name` = ?,`led_color` = ?,`vibration` = ? WHERE `pid` = ?";
        }

        AnonymousClass3(RoomDatabase roomDatabase2) {
            super(roomDatabase2);
        }

        @Override // androidx.room.EntityDeletionOrUpdateAdapter
        public void bind(SupportSQLiteStatement supportSQLiteStatement, Application application) {
            supportSQLiteStatement.bindLong(1, application.getPid());
            if (application.getPersonName() == null) {
                supportSQLiteStatement.bindNull(2);
            } else {
                supportSQLiteStatement.bindString(2, application.getPersonName());
            }
            supportSQLiteStatement.bindLong(3, application.getLedColor());
            supportSQLiteStatement.bindLong(4, application.getVibration());
            supportSQLiteStatement.bindLong(5, application.getPid());
        }
    }

    @Override // com.origamilabs.orii.db.PersonDao
    public void insert(Application... applicationArr) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__insertionAdapterOfPerson.insert((Object[]) applicationArr);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    @Override // com.origamilabs.orii.db.PersonDao
    public void delete(Application application) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__deletionAdapterOfPerson.handle(application);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    @Override // com.origamilabs.orii.db.PersonDao
    public void update(Application... applicationArr) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__updateAdapterOfPerson.handleMultiple(applicationArr);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    @Override // com.origamilabs.orii.db.PersonDao
    public void insertOrUpdate(Application... applicationArr) {
        this.__db.beginTransaction();
        try {
            PersonDao.DefaultImpls.insertOrUpdate(this, applicationArr);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    @Override // com.origamilabs.orii.db.PersonDao
    public List<Application> getAll() {
        RoomSQLiteQuery acquire = RoomSQLiteQuery.acquire("SELECT * FROM person", 0);
        this.__db.assertNotSuspendingTransaction();
        Cursor query = DBUtil.query(this.__db, acquire, false);
        try {
            int columnIndexOrThrow = CursorUtil.getColumnIndexOrThrow(query, "pid");
            int columnIndexOrThrow2 = CursorUtil.getColumnIndexOrThrow(query, "person_name");
            int columnIndexOrThrow3 = CursorUtil.getColumnIndexOrThrow(query, "led_color");
            int columnIndexOrThrow4 = CursorUtil.getColumnIndexOrThrow(query, "vibration");
            ArrayList arrayList = new ArrayList(query.getCount());
            while (query.moveToNext()) {
                arrayList.add(new Application(query.getInt(columnIndexOrThrow), query.getInt(columnIndexOrThrow3), query.getInt(columnIndexOrThrow4), query.getString(columnIndexOrThrow2)));
            }
            return arrayList;
        } finally {
            query.close();
            acquire.release();
        }
    }

    @Override // com.origamilabs.orii.db.PersonDao
    public Application findByPersonId(int i) {
        Application application;
        RoomSQLiteQuery acquire = RoomSQLiteQuery.acquire("SELECT * FROM person WHERE pid LIKE ? LIMIT 1", 1);
        acquire.bindLong(1, i);
        this.__db.assertNotSuspendingTransaction();
        Cursor query = DBUtil.query(this.__db, acquire, false);
        try {
            int columnIndexOrThrow = CursorUtil.getColumnIndexOrThrow(query, "pid");
            int columnIndexOrThrow2 = CursorUtil.getColumnIndexOrThrow(query, "person_name");
            int columnIndexOrThrow3 = CursorUtil.getColumnIndexOrThrow(query, "led_color");
            int columnIndexOrThrow4 = CursorUtil.getColumnIndexOrThrow(query, "vibration");
            if (query.moveToFirst()) {
                application = new Application(query.getInt(columnIndexOrThrow), query.getInt(columnIndexOrThrow3), query.getInt(columnIndexOrThrow4), query.getString(columnIndexOrThrow2));
            } else {
                application = null;
            }
            return application;
        } finally {
            query.close();
            acquire.release();
        }
    }

    @Override // com.origamilabs.orii.db.PersonDao
    public Application findByPersonName(String str) {
        Application application;
        RoomSQLiteQuery acquire = RoomSQLiteQuery.acquire("SELECT * FROM person WHERE person_name LIKE ? LIMIT 1", 1);
        if (str == null) {
            acquire.bindNull(1);
        } else {
            acquire.bindString(1, str);
        }
        this.__db.assertNotSuspendingTransaction();
        Cursor query = DBUtil.query(this.__db, acquire, false);
        try {
            int columnIndexOrThrow = CursorUtil.getColumnIndexOrThrow(query, "pid");
            int columnIndexOrThrow2 = CursorUtil.getColumnIndexOrThrow(query, "person_name");
            int columnIndexOrThrow3 = CursorUtil.getColumnIndexOrThrow(query, "led_color");
            int columnIndexOrThrow4 = CursorUtil.getColumnIndexOrThrow(query, "vibration");
            if (query.moveToFirst()) {
                application = new Application(query.getInt(columnIndexOrThrow), query.getInt(columnIndexOrThrow3), query.getInt(columnIndexOrThrow4), query.getString(columnIndexOrThrow2));
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
