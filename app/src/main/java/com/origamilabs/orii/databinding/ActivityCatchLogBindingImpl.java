package com.origamilabs.orii.databinding;

import android.util.SparseIntArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingComponent;
import androidx.databinding.ViewDataBinding;
import androidx.databinding.adapters.TextViewBindingAdapter;
import androidx.lifecycle.MutableLiveData;
import com.origamilabs.orii.generated.callback.OnClickListener;
import com.origamilabs.orii.ui.main.help.CatchLogViewModel;

/* loaded from: classes2.dex */
public class ActivityCatchLogBindingImpl extends ActivityCatchLogBinding implements OnClickListener.Listener {
    private static final ViewDataBinding.IncludedLayouts sIncludes = null;
    private static final SparseIntArray sViewsWithIds = null;
    private final View.OnClickListener mCallback1;
    private final View.OnClickListener mCallback2;
    private final View.OnClickListener mCallback3;
    private long mDirtyFlags;
    private final ConstraintLayout mboundView0;

    public ActivityCatchLogBindingImpl(DataBindingComponent dataBindingComponent, View view) {
        this(dataBindingComponent, view, mapBindings(dataBindingComponent, view, 5, sIncludes, sViewsWithIds));
    }

    private ActivityCatchLogBindingImpl(DataBindingComponent dataBindingComponent, View view, Object[] objArr) {
        super(dataBindingComponent, view, 1, (Button) objArr[2], (Button) objArr[4], (EditText) objArr[1], (Button) objArr[3]);
        this.mDirtyFlags = -1L;
        this.catchLogButton.setTag(null);
        this.clearLogButton.setTag(null);
        this.logEditText.setTag(null);
        this.mboundView0 = (ConstraintLayout) objArr[0];
        this.mboundView0.setTag(null);
        this.saveLogButton.setTag(null);
        setRootTag(view);
        this.mCallback3 = new OnClickListener(this, 3);
        this.mCallback1 = new OnClickListener(this, 1);
        this.mCallback2 = new OnClickListener(this, 2);
        invalidateAll();
    }

    @Override // androidx.databinding.ViewDataBinding
    public void invalidateAll() {
        synchronized (this) {
            this.mDirtyFlags = 4L;
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
        setViewModel((CatchLogViewModel) obj);
        return true;
    }

    @Override // com.origamilabs.orii.databinding.ActivityCatchLogBinding
    public void setViewModel(CatchLogViewModel catchLogViewModel) {
        this.mViewModel = catchLogViewModel;
        synchronized (this) {
            this.mDirtyFlags |= 2;
        }
        notifyPropertyChanged(12);
        super.requestRebind();
    }

    @Override // androidx.databinding.ViewDataBinding
    protected boolean onFieldChange(int i, Object obj, int i2) {
        if (i != 0) {
            return false;
        }
        return onChangeViewModelOriiLog((MutableLiveData) obj, i2);
    }

    private boolean onChangeViewModelOriiLog(MutableLiveData<String> mutableLiveData, int i) {
        if (i != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 1;
        }
        return true;
    }

    @Override // androidx.databinding.ViewDataBinding
    protected void executeBindings() {
        long j;
        synchronized (this) {
            j = this.mDirtyFlags;
            this.mDirtyFlags = 0L;
        }
        CatchLogViewModel catchLogViewModel = this.mViewModel;
        long j2 = 7 & j;
        String str = null;
        if (j2 != 0) {
            MutableLiveData<String> oriiLog = catchLogViewModel != null ? catchLogViewModel.getOriiLog() : null;
            updateLiveDataRegistration(0, oriiLog);
            if (oriiLog != null) {
                str = oriiLog.getValue();
            }
        }
        if ((j & 4) != 0) {
            this.catchLogButton.setOnClickListener(this.mCallback1);
            this.clearLogButton.setOnClickListener(this.mCallback3);
            this.saveLogButton.setOnClickListener(this.mCallback2);
        }
        if (j2 != 0) {
            TextViewBindingAdapter.setText(this.logEditText, str);
        }
    }

    @Override // com.origamilabs.orii.generated.callback.OnClickListener.Listener
    public final void _internalCallbackOnClick(int i, View view) {
        if (i == 1) {
            CatchLogViewModel catchLogViewModel = this.mViewModel;
            if (catchLogViewModel != null) {
                catchLogViewModel.catchLog();
                return;
            }
            return;
        }
        if (i == 2) {
            CatchLogViewModel catchLogViewModel2 = this.mViewModel;
            if (catchLogViewModel2 != null) {
                catchLogViewModel2.saveLog();
                return;
            }
            return;
        }
        if (i != 3) {
            return;
        }
        CatchLogViewModel catchLogViewModel3 = this.mViewModel;
        if (catchLogViewModel3 != null) {
            catchLogViewModel3.clearLog();
        }
    }
}
