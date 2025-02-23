package com.origamilabs.orii.databinding;

import android.util.SparseIntArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingComponent;
import androidx.databinding.ViewDataBinding;
import androidx.databinding.adapters.TextViewBindingAdapter;
import com.facebook.drawee.view.SimpleDraweeView;
import com.origamilabs.orii.R;
import com.origamilabs.orii.ui.tutorial.phase.one.ConnectionViewModel;

/* loaded from: classes2.dex */
public class ConnectionFragmentBindingImpl extends ConnectionFragmentBinding {
    private static final ViewDataBinding.IncludedLayouts sIncludes = null;
    private static final SparseIntArray sViewsWithIds = new SparseIntArray();
    private long mDirtyFlags;

    @Override // androidx.databinding.ViewDataBinding
    protected boolean onFieldChange(int i, Object obj, int i2) {
        return false;
    }

    static {
        sViewsWithIds.put(R.id.ring_simple_drawee_view, 2);
        sViewsWithIds.put(R.id.blink_light_image_view, 3);
        sViewsWithIds.put(R.id.textView26, 4);
    }

    public ConnectionFragmentBindingImpl(DataBindingComponent dataBindingComponent, View view) {
        this(dataBindingComponent, view, mapBindings(dataBindingComponent, view, 5, sIncludes, sViewsWithIds));
    }

    private ConnectionFragmentBindingImpl(DataBindingComponent dataBindingComponent, View view, Object[] objArr) {
        super(dataBindingComponent, view, 0, (ImageView) objArr[3], (ConstraintLayout) objArr[0], (SimpleDraweeView) objArr[2], (TextView) objArr[1], (TextView) objArr[4]);
        this.mDirtyFlags = -1L;
        this.constraintLayout.setTag(null);
        this.textView24.setTag(null);
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
        if (12 != i) {
            return false;
        }
        setViewModel((ConnectionViewModel) obj);
        return true;
    }

    @Override // com.origamilabs.orii.databinding.ConnectionFragmentBinding
    public void setViewModel(ConnectionViewModel connectionViewModel) {
        this.mViewModel = connectionViewModel;
        synchronized (this) {
            this.mDirtyFlags |= 1;
        }
        notifyPropertyChanged(12);
        super.requestRebind();
    }

    @Override // androidx.databinding.ViewDataBinding
    protected void executeBindings() {
        long j;
        synchronized (this) {
            j = this.mDirtyFlags;
            this.mDirtyFlags = 0L;
        }
        ConnectionViewModel connectionViewModel = this.mViewModel;
        String str = null;
        long j2 = j & 3;
        if (j2 != 0 && connectionViewModel != null) {
            str = connectionViewModel.getConnectionStateString();
        }
        if (j2 != 0) {
            TextViewBindingAdapter.setText(this.textView24, str);
        }
    }
}
