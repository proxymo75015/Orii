package com.origamilabs.orii.db;

import androidx.room.Update;
import com.origamilabs.orii.models.VoiceAssistantCounter;
import java.util.List;
import kotlin.Metadata;

/* compiled from: VoiceAssistantCounterDao.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0010\u0011\n\u0002\b\u0003\bg\u0018\u00002\u00020\u0001J\b\u0010\u0002\u001a\u00020\u0003H'J\u0010\u0010\u0004\u001a\u00020\u00032\u0006\u0010\u0005\u001a\u00020\u0006H'J\u000e\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00060\bH'J!\u0010\t\u001a\u00020\u00032\u0012\u0010\n\u001a\n\u0012\u0006\b\u0001\u0012\u00020\u00060\u000b\"\u00020\u0006H'¢\u0006\u0002\u0010\fJ!\u0010\r\u001a\u00020\u00032\u0012\u0010\n\u001a\n\u0012\u0006\b\u0001\u0012\u00020\u00060\u000b\"\u00020\u0006H'¢\u0006\u0002\u0010\f¨\u0006\u000e"}, d2 = {"Lcom/origamilabs/orii/db/VoiceAssistantCounterDao;", "", "clear", "", "delete", "counter", "Lcom/origamilabs/orii/models/VoiceAssistantCounter;", "getAll", "", "insert", "counters", "", "([Lcom/origamilabs/orii/models/VoiceAssistantCounter;)V", "update", "orii-app-2.2.16-202001151600-864a420_prodRelease"}, k = 1, mv = {1, 1, 15})
/* loaded from: classes2.dex */
public interface VoiceAssistantCounterDao {
    void clear();

    void delete(VoiceAssistantCounter counter);

    List<VoiceAssistantCounter> getAll();

    void insert(VoiceAssistantCounter... counters);

    @Update
    void update(VoiceAssistantCounter... counters);
}
