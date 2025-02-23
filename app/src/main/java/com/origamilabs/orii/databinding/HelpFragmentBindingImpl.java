package com.origamilabs.orii.databinding;

import android.util.SparseIntArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingComponent;
import androidx.databinding.ViewDataBinding;
import com.origamilabs.orii.R;

/* loaded from: classes2.dex */
public class HelpFragmentBindingImpl extends HelpFragmentBinding {
    private static final ViewDataBinding.IncludedLayouts sIncludes = null;
    private static final SparseIntArray sViewsWithIds = new SparseIntArray();
    private long mDirtyFlags;
    private final ConstraintLayout mboundView0;

    @Override // androidx.databinding.ViewDataBinding
    protected boolean onFieldChange(int i, Object obj, int i2) {
        return false;
    }

    static {
        sViewsWithIds.put(R.id.textView8, 2);
        sViewsWithIds.put(R.id.card_view_tutorial, 3);
        sViewsWithIds.put(R.id.textView, 4);
        sViewsWithIds.put(R.id.imageView2, 5);
        sViewsWithIds.put(R.id.card_view_support, 6);
        sViewsWithIds.put(R.id.textView3, 7);
        sViewsWithIds.put(R.id.imageView4, 8);
        sViewsWithIds.put(R.id.card_view_feedback, 9);
        sViewsWithIds.put(R.id.textView5, 10);
        sViewsWithIds.put(R.id.imageView6, 11);
        sViewsWithIds.put(R.id.card_view_privacy_policy, 12);
        sViewsWithIds.put(R.id.card_view_app_version, 13);
        sViewsWithIds.put(R.id.app_version_text_view, 14);
        sViewsWithIds.put(R.id.card_view_firmware, 15);
        sViewsWithIds.put(R.id.firmware_text_view, 16);
        sViewsWithIds.put(R.id.card_view_firmware_test_mode, 17);
    }

    public HelpFragmentBindingImpl(DataBindingComponent dataBindingComponent, View view) {
        this(dataBindingComponent, view, mapBindings(dataBindingComponent, view, 18, sIncludes, sViewsWithIds));
    }

    private HelpFragmentBindingImpl(DataBindingComponent dataBindingComponent, View view, Object[] objArr) {
        super(dataBindingComponent, view, 0, (TextView) objArr[14], (CardView) objArr[13], (CardView) objArr[9], (CardView) objArr[15], (CardView) objArr[17], (CardView) objArr[1], (CardView) objArr[12], (CardView) objArr[6], (CardView) objArr[3], (TextView) objArr[16], (ImageView) objArr[5], (ImageView) objArr[8], (ImageView) objArr[11], (TextView) objArr[4], (TextView) objArr[7], (TextView) objArr[10], (TextView) objArr[2]);
        this.mDirtyFlags = -1L;
        this.cardViewLog.setTag(null);
        this.mboundView0 = (ConstraintLayout) objArr[0];
        this.mboundView0.setTag(null);
        setRootTag(view);
        invalidateAll();
    }

    @Override // androidx.databinding.ViewDataBinding
    public void invalidateAll() {
        synchronized (this) {
            this.mDirtyFlags = 2L;
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
    public boolean setVariable(int i, Object obj) {
        if (16 != i) {
            return false;
        }
        setShowLog((Boolean) obj);
        return true;
    }

    @Override // com.origamilabs.orii.databinding.HelpFragmentBinding
    public void setShowLog(Boolean bool) {
        this.mShowLog = bool;
        synchronized (this) {
            this.mDirtyFlags |= 1;
        }
        notifyPropertyChanged(16);
        super.requestRebind();
    }

    @Override // androidx.databinding.ViewDataBinding
    protected void executeBindings() {
        long j;
        synchronized (this) {
            j = this.mDirtyFlags;
            this.mDirtyFlags = 0L;
        }
        Boolean bool = this.mShowLog;
        long j2 = j & 3;
        int i = 0;
        if (j2 != 0) {
            boolean safeUnbox = ViewDataBinding.safeUnbox(bool);
            if (j2 != 0) {
                j |= safeUnbox ? 8L : 4L;
            }
            if (!safeUnbox) {
                i = 8;
            }
        }
        if ((j & 3) != 0) {
            this.cardViewLog.setVisibility(i);
        }
    }
}
