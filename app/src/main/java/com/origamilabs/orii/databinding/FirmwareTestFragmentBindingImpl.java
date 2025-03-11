package com.origamilabs.orii.databinding;

import android.util.SparseIntArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingComponent;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.MutableLiveData;
import com.origamilabs.orii.R;
import com.origamilabs.orii.ui.SharedViewModel;

/* loaded from: classes2.dex */
public class FirmwareTestFragmentBindingImpl extends FirmwareTestFragmentBinding {
    private static final ViewDataBinding.IncludedLayouts sIncludes = null;
    private static final SparseIntArray sViewsWithIds = new SparseIntArray();
    private long mDirtyFlags;

    static {
        sViewsWithIds.put(R.id.textView, 6);
        sViewsWithIds.put(R.id.failed_hint_text_view, 7);
        sViewsWithIds.put(R.id.timeOutRetryButton, 8);
        sViewsWithIds.put(R.id.welcome_text_view, 9);
        sViewsWithIds.put(R.id.croppedOriiImageView, 10);
        sViewsWithIds.put(R.id.connectedTextView, 11);
        sViewsWithIds.put(R.id.v68_button, 12);
        sViewsWithIds.put(R.id.v69_button, 13);
        sViewsWithIds.put(R.id.v70_button, 14);
        sViewsWithIds.put(R.id.v71_button, 15);
        sViewsWithIds.put(R.id.stop_searching_button, 16);
    }

    public FirmwareTestFragmentBindingImpl(DataBindingComponent dataBindingComponent, View view) {
        this(dataBindingComponent, view, mapBindings(dataBindingComponent, view, 17, sIncludes, sViewsWithIds));
    }

    private FirmwareTestFragmentBindingImpl(DataBindingComponent dataBindingComponent, View view, Object[] objArr) {
        super(dataBindingComponent, view, 2, (ConstraintLayout) objArr[1], (ConstraintLayout) objArr[2], (TextView) objArr[11], (ConstraintLayout) objArr[4], (ImageView) objArr[10], (TextView) objArr[7], (ConstraintLayout) objArr[0], (TextView) objArr[5], (Button) objArr[16], (TextView) objArr[6], (Button) objArr[8], (Button) objArr[3], (Button) objArr[12], (Button) objArr[13], (Button) objArr[14], (Button) objArr[15], (TextView) objArr[9]);
        this.mDirtyFlags = -1L;
        this.connectFailLayout.setTag(null);
        this.connectedLayout.setTag(null);
        this.connectingLayout.setTag(null);
        this.homeLayout.setTag(null);
        this.statusTextView.setTag(null);
        this.updateButton.setTag(null);
        setRootTag(view);
        invalidateAll();
    }

    @Override // androidx.databinding.ViewDataBinding
    public void invalidateAll() {
        synchronized (this) {
            this.mDirtyFlags = 16L;
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
        if (11 == i) {
            setSharedViewModel((SharedViewModel) obj);
        } else {
            if (5 != i) {
                return false;
            }
            setCanFirmwareForceUpdate((Boolean) obj);
        }
        return true;
    }

    @Override // com.origamilabs.orii.databinding.FirmwareTestFragmentBinding
    public void setSharedViewModel(SharedViewModel sharedViewModel) {
        this.mSharedViewModel = sharedViewModel;
        synchronized (this) {
            this.mDirtyFlags |= 4;
        }
        notifyPropertyChanged(11);
        super.requestRebind();
    }

    @Override // com.origamilabs.orii.databinding.FirmwareTestFragmentBinding
    public void setCanFirmwareForceUpdate(Boolean bool) {
        this.mCanFirmwareForceUpdate = bool;
        synchronized (this) {
            this.mDirtyFlags |= 8;
        }
        notifyPropertyChanged(5);
        super.requestRebind();
    }

    @Override // androidx.databinding.ViewDataBinding
    protected boolean onFieldChange(int i, Object obj, int i2) {
        if (i == 0) {
            return onChangeSharedViewModelCountDownNumber((MutableLiveData) obj, i2);
        }
        if (i != 1) {
            return false;
        }
        return onChangeSharedViewModelCurrentState((MutableLiveData) obj, i2);
    }

    private boolean onChangeSharedViewModelCountDownNumber(MutableLiveData<Integer> mutableLiveData, int i) {
        if (i != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 1;
        }
        return true;
    }

    private boolean onChangeSharedViewModelCurrentState(MutableLiveData<SharedViewModel.State> mutableLiveData, int i) {
        if (i != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 2;
        }
        return true;
    }

    /* JADX WARN: Removed duplicated region for block: B:54:0x00e4  */
    /* JADX WARN: Removed duplicated region for block: B:71:0x0123  */
    /* JADX WARN: Removed duplicated region for block: B:83:0x0142  */
    /* JADX WARN: Removed duplicated region for block: B:86:0x0158  */
    /* JADX WARN: Removed duplicated region for block: B:89:0x0163  */
    /* JADX WARN: Removed duplicated region for block: B:92:? A[RETURN, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:94:0x013a  */
    /* JADX WARN: Removed duplicated region for block: B:97:0x010c  */
    @Override // androidx.databinding.ViewDataBinding
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    protected void executeBindings() {
        /*
            Method dump skipped, instructions count: 369
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.origamilabs.orii.databinding.FirmwareTestFragmentBindingImpl.executeBindings():void");
    }
}
