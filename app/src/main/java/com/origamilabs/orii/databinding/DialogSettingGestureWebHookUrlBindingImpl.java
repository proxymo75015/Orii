package com.origamilabs.orii.databinding;

import android.util.SparseIntArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingComponent;
import androidx.databinding.ViewDataBinding;
import com.origamilabs.orii.R;

/* loaded from: classes2.dex */
public class DialogSettingGestureWebHookUrlBindingImpl extends DialogSettingGestureWebHookUrlBinding {
    private static final ViewDataBinding.IncludedLayouts sIncludes = null;
    private static final SparseIntArray sViewsWithIds = new SparseIntArray();
    private long mDirtyFlags;
    private final ConstraintLayout mboundView0;

    @Override // androidx.databinding.ViewDataBinding
    protected boolean onFieldChange(int i, Object obj, int i2) {
        return false;
    }

    @Override // androidx.databinding.ViewDataBinding
    public boolean setVariable(int i, Object obj) {
        return true;
    }

    static {
        sViewsWithIds.put(R.id.dialog_web_hook_title_text_view, 1);
        sViewsWithIds.put(R.id.dialog_web_hook_question_mark_image_view, 2);
        sViewsWithIds.put(R.id.dialog_web_hook_url_constraint_layout, 3);
        sViewsWithIds.put(R.id.dialog_web_hook_url_edit_text, 4);
        sViewsWithIds.put(R.id.dialog_web_hook_url_clear_image_view, 5);
        sViewsWithIds.put(R.id.dialog_web_hook_url_prompt_text_view, 6);
        sViewsWithIds.put(R.id.dialog_web_hook_cancel_button, 7);
        sViewsWithIds.put(R.id.dialog_web_hook_enter_button, 8);
    }

    public DialogSettingGestureWebHookUrlBindingImpl(DataBindingComponent dataBindingComponent, View view) {
        this(dataBindingComponent, view, mapBindings(dataBindingComponent, view, 9, sIncludes, sViewsWithIds));
    }

    private DialogSettingGestureWebHookUrlBindingImpl(DataBindingComponent dataBindingComponent, View view, Object[] objArr) {
        super(dataBindingComponent, view, 0, (Button) objArr[7], (Button) objArr[8], (ImageView) objArr[2], (TextView) objArr[1], (ImageView) objArr[5], (ConstraintLayout) objArr[3], (EditText) objArr[4], (TextView) objArr[6]);
        this.mDirtyFlags = -1L;
        this.mboundView0 = (ConstraintLayout) objArr[0];
        this.mboundView0.setTag(null);
        setRootTag(view);
        invalidateAll();
    }

    @Override // androidx.databinding.ViewDataBinding
    public void invalidateAll() {
        synchronized (this) {
            this.mDirtyFlags = 1L;
        }
        requestRebind();
    }

    @Override // androidx.databinding.ViewDataBinding
    public boolean hasPendingBindings() {
        synchronized (this) {
            return this.mDirtyFlags != 0;
        }
    }

    @Override // androidx.databinding.ViewDataBinding
    protected void executeBindings() {
        synchronized (this) {
            long j = this.mDirtyFlags;
            this.mDirtyFlags = 0L;
        }
    }
}
