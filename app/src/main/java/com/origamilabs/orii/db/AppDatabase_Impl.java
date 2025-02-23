package com.origamilabs.orii.db;

import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomMasterTable;
import androidx.room.RoomOpenHelper;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.facebook.share.internal.ShareConstants;

import java.util.HashMap;
import java.util.HashSet;

/* loaded from: classes2.dex */
public final class AppDatabase_Impl extends AppDatabase {
    private volatile ApplicationDao _applicationDao;
    private volatile PersonDao _personDao;
    private volatile VoiceAssistantCounterDao _voiceAssistantCounterDao;

    /* renamed from: com.origamilabs.orii.db.AppDatabase_Impl$1 */
    class AnonymousClass1 extends RoomOpenHelper.Delegate {
        @Override // androidx.room.RoomOpenHelper.Delegate
        public void onPostMigrate(SupportSQLiteDatabase supportSQLiteDatabase) {
        }

        AnonymousClass1(int i) {
            super(i);
        }

        @Override // androidx.room.RoomOpenHelper.Delegate
        public void createAllTables(SupportSQLiteDatabase supportSQLiteDatabase) {
            supportSQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS `Person` (`pid` INTEGER NOT NULL, `person_name` TEXT NOT NULL, `led_color` INTEGER NOT NULL, `vibration` INTEGER NOT NULL, PRIMARY KEY(`pid`))");
            supportSQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS `Application` (`package_name` TEXT NOT NULL, `led_color` INTEGER NOT NULL, `vibration` INTEGER NOT NULL, PRIMARY KEY(`package_name`))");
            supportSQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS `va_counter` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `times` INTEGER NOT NULL, `date` INTEGER NOT NULL)");
            supportSQLiteDatabase.execSQL(RoomMasterTable.CREATE_QUERY);
            supportSQLiteDatabase.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'b29c667e6076c4ccd601e04418b23206')");
        }

        @Override // androidx.room.RoomOpenHelper.Delegate
        public void dropAllTables(SupportSQLiteDatabase supportSQLiteDatabase) {
            supportSQLiteDatabase.execSQL("DROP TABLE IF EXISTS `Person`");
            supportSQLiteDatabase.execSQL("DROP TABLE IF EXISTS `Application`");
            supportSQLiteDatabase.execSQL("DROP TABLE IF EXISTS `va_counter`");
        }

        @Override // androidx.room.RoomOpenHelper.Delegate
        protected void onCreate(SupportSQLiteDatabase supportSQLiteDatabase) {
            if (AppDatabase_Impl.this.mCallbacks != null) {
                int size = AppDatabase_Impl.this.mCallbacks.size();
                for (int i = 0; i < size; i++) {
                    ((RoomDatabase.Callback) AppDatabase_Impl.this.mCallbacks.get(i)).onCreate(supportSQLiteDatabase);
                }
            }
        }

        @Override // androidx.room.RoomOpenHelper.Delegate
        public void onOpen(SupportSQLiteDatabase supportSQLiteDatabase) {
            AppDatabase_Impl.this.mDatabase = supportSQLiteDatabase;
            AppDatabase_Impl.this.internalInitInvalidationTracker(supportSQLiteDatabase);
            if (AppDatabase_Impl.this.mCallbacks != null) {
                int size = AppDatabase_Impl.this.mCallbacks.size();
                for (int i = 0; i < size; i++) {
                    ((RoomDatabase.Callback) AppDatabase_Impl.this.mCallbacks.get(i)).onOpen(supportSQLiteDatabase);
                }
            }
        }

        @Override // androidx.room.RoomOpenHelper.Delegate
        public void onPreMigrate(SupportSQLiteDatabase supportSQLiteDatabase) {
            DBUtil.dropFtsSyncTriggers(supportSQLiteDatabase);
        }

        @Override // androidx.room.RoomOpenHelper.Delegate
        protected void validateMigration(SupportSQLiteDatabase supportSQLiteDatabase) {
            HashMap hashMap = new HashMap(4);
            hashMap.put("pid", new TableInfo.Column("pid", "INTEGER", true, 1));
            hashMap.put("person_name", new TableInfo.Column("person_name", "TEXT", true, 0));
            hashMap.put("led_color", new TableInfo.Column("led_color", "INTEGER", true, 0));
            hashMap.put("vibration", new TableInfo.Column("vibration", "INTEGER", true, 0));
            TableInfo tableInfo = new TableInfo("Person", hashMap, new HashSet(0), new HashSet(0));
            TableInfo read = TableInfo.read(supportSQLiteDatabase, "Person");
            if (!tableInfo.equals(read)) {
                throw new IllegalStateException("Migration didn't properly handle Person(com.origamilabs.orii.models.Person).\n Expected:\n" + tableInfo + "\n Found:\n" + read);
            }
            HashMap hashMap2 = new HashMap(3);
            hashMap2.put("package_name", new TableInfo.Column("package_name", "TEXT", true, 1));
            hashMap2.put("led_color", new TableInfo.Column("led_color", "INTEGER", true, 0));
            hashMap2.put("vibration", new TableInfo.Column("vibration", "INTEGER", true, 0));
            TableInfo tableInfo2 = new TableInfo("Application", hashMap2, new HashSet(0), new HashSet(0));
            TableInfo read2 = TableInfo.read(supportSQLiteDatabase, "Application");
            if (!tableInfo2.equals(read2)) {
                throw new IllegalStateException("Migration didn't properly handle Application(com.origamilabs.orii.models.Application).\n Expected:\n" + tableInfo2 + "\n Found:\n" + read2);
            }
            HashMap hashMap3 = new HashMap(3);
            hashMap3.put(ShareConstants.WEB_DIALOG_PARAM_ID, new TableInfo.Column(ShareConstants.WEB_DIALOG_PARAM_ID, "INTEGER", true, 1));
            hashMap3.put("times", new TableInfo.Column("times", "INTEGER", true, 0));
            hashMap3.put(AnalyticsManager.Param.DATE, new TableInfo.Column(AnalyticsManager.Param.DATE, "INTEGER", true, 0));
            TableInfo tableInfo3 = new TableInfo("va_counter", hashMap3, new HashSet(0), new HashSet(0));
            TableInfo read3 = TableInfo.read(supportSQLiteDatabase, "va_counter");
            if (tableInfo3.equals(read3)) {
                return;
            }
            throw new IllegalStateException("Migration didn't properly handle va_counter(com.origamilabs.orii.models.VoiceAssistantCounter).\n Expected:\n" + tableInfo3 + "\n Found:\n" + read3);
        }
    }

    @Override // androidx.room.RoomDatabase
    protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration databaseConfiguration) {
        return databaseConfiguration.sqliteOpenHelperFactory.create(SupportSQLiteOpenHelper.Configuration.builder(databaseConfiguration.context).name(databaseConfiguration.name).callback(new RoomOpenHelper(databaseConfiguration, new RoomOpenHelper.Delegate(2) { // from class: com.origamilabs.orii.db.AppDatabase_Impl.1
            @Override // androidx.room.RoomOpenHelper.Delegate
            public void onPostMigrate(SupportSQLiteDatabase supportSQLiteDatabase) {
            }

            AnonymousClass1(int i) {
                super(i);
            }

            @Override // androidx.room.RoomOpenHelper.Delegate
            public void createAllTables(SupportSQLiteDatabase supportSQLiteDatabase) {
                supportSQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS `Person` (`pid` INTEGER NOT NULL, `person_name` TEXT NOT NULL, `led_color` INTEGER NOT NULL, `vibration` INTEGER NOT NULL, PRIMARY KEY(`pid`))");
                supportSQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS `Application` (`package_name` TEXT NOT NULL, `led_color` INTEGER NOT NULL, `vibration` INTEGER NOT NULL, PRIMARY KEY(`package_name`))");
                supportSQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS `va_counter` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `times` INTEGER NOT NULL, `date` INTEGER NOT NULL)");
                supportSQLiteDatabase.execSQL(RoomMasterTable.CREATE_QUERY);
                supportSQLiteDatabase.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'b29c667e6076c4ccd601e04418b23206')");
            }

            @Override // androidx.room.RoomOpenHelper.Delegate
            public void dropAllTables(SupportSQLiteDatabase supportSQLiteDatabase) {
                supportSQLiteDatabase.execSQL("DROP TABLE IF EXISTS `Person`");
                supportSQLiteDatabase.execSQL("DROP TABLE IF EXISTS `Application`");
                supportSQLiteDatabase.execSQL("DROP TABLE IF EXISTS `va_counter`");
            }

            @Override // androidx.room.RoomOpenHelper.Delegate
            protected void onCreate(SupportSQLiteDatabase supportSQLiteDatabase) {
                if (AppDatabase_Impl.this.mCallbacks != null) {
                    int size = AppDatabase_Impl.this.mCallbacks.size();
                    for (int i = 0; i < size; i++) {
                        ((RoomDatabase.Callback) AppDatabase_Impl.this.mCallbacks.get(i)).onCreate(supportSQLiteDatabase);
                    }
                }
            }

            @Override // androidx.room.RoomOpenHelper.Delegate
            public void onOpen(SupportSQLiteDatabase supportSQLiteDatabase) {
                AppDatabase_Impl.this.mDatabase = supportSQLiteDatabase;
                AppDatabase_Impl.this.internalInitInvalidationTracker(supportSQLiteDatabase);
                if (AppDatabase_Impl.this.mCallbacks != null) {
                    int size = AppDatabase_Impl.this.mCallbacks.size();
                    for (int i = 0; i < size; i++) {
                        ((RoomDatabase.Callback) AppDatabase_Impl.this.mCallbacks.get(i)).onOpen(supportSQLiteDatabase);
                    }
                }
            }

            @Override // androidx.room.RoomOpenHelper.Delegate
            public void onPreMigrate(SupportSQLiteDatabase supportSQLiteDatabase) {
                DBUtil.dropFtsSyncTriggers(supportSQLiteDatabase);
            }

            @Override // androidx.room.RoomOpenHelper.Delegate
            protected void validateMigration(SupportSQLiteDatabase supportSQLiteDatabase) {
                HashMap hashMap = new HashMap(4);
                hashMap.put("pid", new TableInfo.Column("pid", "INTEGER", true, 1));
                hashMap.put("person_name", new TableInfo.Column("person_name", "TEXT", true, 0));
                hashMap.put("led_color", new TableInfo.Column("led_color", "INTEGER", true, 0));
                hashMap.put("vibration", new TableInfo.Column("vibration", "INTEGER", true, 0));
                TableInfo tableInfo = new TableInfo("Person", hashMap, new HashSet(0), new HashSet(0));
                TableInfo read = TableInfo.read(supportSQLiteDatabase, "Person");
                if (!tableInfo.equals(read)) {
                    throw new IllegalStateException("Migration didn't properly handle Person(com.origamilabs.orii.models.Person).\n Expected:\n" + tableInfo + "\n Found:\n" + read);
                }
                HashMap hashMap2 = new HashMap(3);
                hashMap2.put("package_name", new TableInfo.Column("package_name", "TEXT", true, 1));
                hashMap2.put("led_color", new TableInfo.Column("led_color", "INTEGER", true, 0));
                hashMap2.put("vibration", new TableInfo.Column("vibration", "INTEGER", true, 0));
                TableInfo tableInfo2 = new TableInfo("Application", hashMap2, new HashSet(0), new HashSet(0));
                TableInfo read2 = TableInfo.read(supportSQLiteDatabase, "Application");
                if (!tableInfo2.equals(read2)) {
                    throw new IllegalStateException("Migration didn't properly handle Application(com.origamilabs.orii.models.Application).\n Expected:\n" + tableInfo2 + "\n Found:\n" + read2);
                }
                HashMap hashMap3 = new HashMap(3);
                hashMap3.put(ShareConstants.WEB_DIALOG_PARAM_ID, new TableInfo.Column(ShareConstants.WEB_DIALOG_PARAM_ID, "INTEGER", true, 1));
                hashMap3.put("times", new TableInfo.Column("times", "INTEGER", true, 0));
                hashMap3.put(AnalyticsManager.Param.DATE, new TableInfo.Column(AnalyticsManager.Param.DATE, "INTEGER", true, 0));
                TableInfo tableInfo3 = new TableInfo("va_counter", hashMap3, new HashSet(0), new HashSet(0));
                TableInfo read3 = TableInfo.read(supportSQLiteDatabase, "va_counter");
                if (tableInfo3.equals(read3)) {
                    return;
                }
                throw new IllegalStateException("Migration didn't properly handle va_counter(com.origamilabs.orii.models.VoiceAssistantCounter).\n Expected:\n" + tableInfo3 + "\n Found:\n" + read3);
            }
        }, "b29c667e6076c4ccd601e04418b23206", "52ed0a9b396f69bae640878fd1e923b6")).build());
    }

    @Override // androidx.room.RoomDatabase
    protected InvalidationTracker createInvalidationTracker() {
        return new InvalidationTracker(this, new HashMap(0), new HashMap(0), "Person", "Application", "va_counter");
    }

    @Override // androidx.room.RoomDatabase
    public void clearAllTables() {
        super.assertNotMainThread();
        SupportSQLiteDatabase writableDatabase = super.getOpenHelper().getWritableDatabase();
        try {
            super.beginTransaction();
            writableDatabase.execSQL("DELETE FROM `Person`");
            writableDatabase.execSQL("DELETE FROM `Application`");
            writableDatabase.execSQL("DELETE FROM `va_counter`");
            super.setTransactionSuccessful();
        } finally {
            super.endTransaction();
            writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close();
            if (!writableDatabase.inTransaction()) {
                writableDatabase.execSQL("VACUUM");
            }
        }
    }

    @Override // com.origamilabs.orii.db.AppDatabase
    public PersonDao personDao() {
        PersonDao personDao;
        if (this._personDao != null) {
            return this._personDao;
        }
        synchronized (this) {
            if (this._personDao == null) {
                this._personDao = new PersonDao_Impl(this);
            }
            personDao = this._personDao;
        }
        return personDao;
    }

    @Override // com.origamilabs.orii.db.AppDatabase
    public ApplicationDao applicationDao() {
        ApplicationDao applicationDao;
        if (this._applicationDao != null) {
            return this._applicationDao;
        }
        synchronized (this) {
            if (this._applicationDao == null) {
                this._applicationDao = new ApplicationDao_Impl(this);
            }
            applicationDao = this._applicationDao;
        }
        return applicationDao;
    }

    @Override // com.origamilabs.orii.db.AppDatabase
    public VoiceAssistantCounterDao vaCounterDao() {
        VoiceAssistantCounterDao voiceAssistantCounterDao;
        if (this._voiceAssistantCounterDao != null) {
            return this._voiceAssistantCounterDao;
        }
        synchronized (this) {
            if (this._voiceAssistantCounterDao == null) {
                this._voiceAssistantCounterDao = new VoiceAssistantCounterDao_Impl(this);
            }
            voiceAssistantCounterDao = this._voiceAssistantCounterDao;
        }
        return voiceAssistantCounterDao;
    }
}
