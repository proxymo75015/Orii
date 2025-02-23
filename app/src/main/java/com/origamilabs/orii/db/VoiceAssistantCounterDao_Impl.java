package com.origamilabs.orii.db;

import android.database.Cursor;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.facebook.share.internal.ShareConstants;
import com.origamilabs.orii.models.VoiceAssistantCounter;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes2.dex */
public final class VoiceAssistantCounterDao_Impl implements VoiceAssistantCounterDao {
    private final RoomDatabase __db;
    private final EntityDeletionOrUpdateAdapter __deletionAdapterOfVoiceAssistantCounter;
    private final EntityInsertionAdapter __insertionAdapterOfVoiceAssistantCounter;
    private final SharedSQLiteStatement __preparedStmtOfClear;
    private final EntityDeletionOrUpdateAdapter __updateAdapterOfVoiceAssistantCounter;

    public VoiceAssistantCounterDao_Impl(RoomDatabase roomDatabase) {
        this.__db = roomDatabase;
        this.__insertionAdapterOfVoiceAssistantCounter = new EntityInsertionAdapter<VoiceAssistantCounter>(roomDatabase) { // from class: com.origamilabs.orii.db.VoiceAssistantCounterDao_Impl.1
            @Override // androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "INSERT OR ABORT INTO `va_counter`(`id`,`times`,`date`) VALUES (nullif(?, 0),?,?)";
            }

            AnonymousClass1(RoomDatabase roomDatabase2) {
                super(roomDatabase2);
            }

            @Override // androidx.room.EntityInsertionAdapter
            public void bind(SupportSQLiteStatement supportSQLiteStatement, VoiceAssistantCounter voiceAssistantCounter) {
                supportSQLiteStatement.bindLong(1, voiceAssistantCounter.getId());
                supportSQLiteStatement.bindLong(2, voiceAssistantCounter.getTimes());
                supportSQLiteStatement.bindLong(3, voiceAssistantCounter.getDate());
            }
        };
        this.__deletionAdapterOfVoiceAssistantCounter = new EntityDeletionOrUpdateAdapter<VoiceAssistantCounter>(roomDatabase2) { // from class: com.origamilabs.orii.db.VoiceAssistantCounterDao_Impl.2
            @Override // androidx.room.EntityDeletionOrUpdateAdapter, androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "DELETE FROM `va_counter` WHERE `id` = ?";
            }

            AnonymousClass2(RoomDatabase roomDatabase2) {
                super(roomDatabase2);
            }

            @Override // androidx.room.EntityDeletionOrUpdateAdapter
            public void bind(SupportSQLiteStatement supportSQLiteStatement, VoiceAssistantCounter voiceAssistantCounter) {
                supportSQLiteStatement.bindLong(1, voiceAssistantCounter.getId());
            }
        };
        this.__updateAdapterOfVoiceAssistantCounter = new EntityDeletionOrUpdateAdapter<VoiceAssistantCounter>(roomDatabase2) { // from class: com.origamilabs.orii.db.VoiceAssistantCounterDao_Impl.3
            @Override // androidx.room.EntityDeletionOrUpdateAdapter, androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "UPDATE OR ABORT `va_counter` SET `id` = ?,`times` = ?,`date` = ? WHERE `id` = ?";
            }

            AnonymousClass3(RoomDatabase roomDatabase2) {
                super(roomDatabase2);
            }

            @Override // androidx.room.EntityDeletionOrUpdateAdapter
            public void bind(SupportSQLiteStatement supportSQLiteStatement, VoiceAssistantCounter voiceAssistantCounter) {
                supportSQLiteStatement.bindLong(1, voiceAssistantCounter.getId());
                supportSQLiteStatement.bindLong(2, voiceAssistantCounter.getTimes());
                supportSQLiteStatement.bindLong(3, voiceAssistantCounter.getDate());
                supportSQLiteStatement.bindLong(4, voiceAssistantCounter.getId());
            }
        };
        this.__preparedStmtOfClear = new SharedSQLiteStatement(roomDatabase2) { // from class: com.origamilabs.orii.db.VoiceAssistantCounterDao_Impl.4
            @Override // androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "DELETE FROM va_counter";
            }

            AnonymousClass4(RoomDatabase roomDatabase2) {
                super(roomDatabase2);
            }
        };
    }

    /* renamed from: com.origamilabs.orii.db.VoiceAssistantCounterDao_Impl$1 */
    class AnonymousClass1 extends EntityInsertionAdapter<VoiceAssistantCounter> {
        @Override // androidx.room.SharedSQLiteStatement
        public String createQuery() {
            return "INSERT OR ABORT INTO `va_counter`(`id`,`times`,`date`) VALUES (nullif(?, 0),?,?)";
        }

        AnonymousClass1(RoomDatabase roomDatabase2) {
            super(roomDatabase2);
        }

        @Override // androidx.room.EntityInsertionAdapter
        public void bind(SupportSQLiteStatement supportSQLiteStatement, VoiceAssistantCounter voiceAssistantCounter) {
            supportSQLiteStatement.bindLong(1, voiceAssistantCounter.getId());
            supportSQLiteStatement.bindLong(2, voiceAssistantCounter.getTimes());
            supportSQLiteStatement.bindLong(3, voiceAssistantCounter.getDate());
        }
    }

    /* renamed from: com.origamilabs.orii.db.VoiceAssistantCounterDao_Impl$2 */
    class AnonymousClass2 extends EntityDeletionOrUpdateAdapter<VoiceAssistantCounter> {
        @Override // androidx.room.EntityDeletionOrUpdateAdapter, androidx.room.SharedSQLiteStatement
        public String createQuery() {
            return "DELETE FROM `va_counter` WHERE `id` = ?";
        }

        AnonymousClass2(RoomDatabase roomDatabase2) {
            super(roomDatabase2);
        }

        @Override // androidx.room.EntityDeletionOrUpdateAdapter
        public void bind(SupportSQLiteStatement supportSQLiteStatement, VoiceAssistantCounter voiceAssistantCounter) {
            supportSQLiteStatement.bindLong(1, voiceAssistantCounter.getId());
        }
    }

    /* renamed from: com.origamilabs.orii.db.VoiceAssistantCounterDao_Impl$3 */
    class AnonymousClass3 extends EntityDeletionOrUpdateAdapter<VoiceAssistantCounter> {
        @Override // androidx.room.EntityDeletionOrUpdateAdapter, androidx.room.SharedSQLiteStatement
        public String createQuery() {
            return "UPDATE OR ABORT `va_counter` SET `id` = ?,`times` = ?,`date` = ? WHERE `id` = ?";
        }

        AnonymousClass3(RoomDatabase roomDatabase2) {
            super(roomDatabase2);
        }

        @Override // androidx.room.EntityDeletionOrUpdateAdapter
        public void bind(SupportSQLiteStatement supportSQLiteStatement, VoiceAssistantCounter voiceAssistantCounter) {
            supportSQLiteStatement.bindLong(1, voiceAssistantCounter.getId());
            supportSQLiteStatement.bindLong(2, voiceAssistantCounter.getTimes());
            supportSQLiteStatement.bindLong(3, voiceAssistantCounter.getDate());
            supportSQLiteStatement.bindLong(4, voiceAssistantCounter.getId());
        }
    }

    /* renamed from: com.origamilabs.orii.db.VoiceAssistantCounterDao_Impl$4 */
    class AnonymousClass4 extends SharedSQLiteStatement {
        @Override // androidx.room.SharedSQLiteStatement
        public String createQuery() {
            return "DELETE FROM va_counter";
        }

        AnonymousClass4(RoomDatabase roomDatabase2) {
            super(roomDatabase2);
        }
    }

    @Override // com.origamilabs.orii.db.VoiceAssistantCounterDao
    public void insert(VoiceAssistantCounter... voiceAssistantCounterArr) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__insertionAdapterOfVoiceAssistantCounter.insert((Object[]) voiceAssistantCounterArr);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    @Override // com.origamilabs.orii.db.VoiceAssistantCounterDao
    public void delete(VoiceAssistantCounter voiceAssistantCounter) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__deletionAdapterOfVoiceAssistantCounter.handle(voiceAssistantCounter);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    @Override // com.origamilabs.orii.db.VoiceAssistantCounterDao
    public void update(VoiceAssistantCounter... voiceAssistantCounterArr) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__updateAdapterOfVoiceAssistantCounter.handleMultiple(voiceAssistantCounterArr);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    @Override // com.origamilabs.orii.db.VoiceAssistantCounterDao
    public void clear() {
        this.__db.assertNotSuspendingTransaction();
        SupportSQLiteStatement acquire = this.__preparedStmtOfClear.acquire();
        this.__db.beginTransaction();
        try {
            acquire.executeUpdateDelete();
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
            this.__preparedStmtOfClear.release(acquire);
        }
    }

    @Override // com.origamilabs.orii.db.VoiceAssistantCounterDao
    public List<VoiceAssistantCounter> getAll() {
        RoomSQLiteQuery acquire = RoomSQLiteQuery.acquire("SELECT * FROM va_counter", 0);
        this.__db.assertNotSuspendingTransaction();
        Cursor query = DBUtil.query(this.__db, acquire, false);
        try {
            int columnIndexOrThrow = CursorUtil.getColumnIndexOrThrow(query, ShareConstants.WEB_DIALOG_PARAM_ID);
            int columnIndexOrThrow2 = CursorUtil.getColumnIndexOrThrow(query, "times");
            int columnIndexOrThrow3 = CursorUtil.getColumnIndexOrThrow(query, AnalyticsManager.Param.DATE);
            ArrayList arrayList = new ArrayList(query.getCount());
            while (query.moveToNext()) {
                VoiceAssistantCounter voiceAssistantCounter = new VoiceAssistantCounter(query.getInt(columnIndexOrThrow2), query.getInt(columnIndexOrThrow3));
                voiceAssistantCounter.setId(query.getInt(columnIndexOrThrow));
                arrayList.add(voiceAssistantCounter);
            }
            return arrayList;
        } finally {
            query.close();
            acquire.release();
        }
    }
}
