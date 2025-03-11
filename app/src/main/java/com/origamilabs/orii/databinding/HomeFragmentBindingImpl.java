package com.origamilabs.orii.databinding;

import android.support.v4.media.session.PlaybackStateCompat;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingComponent;
import androidx.databinding.ViewDataBinding;
import androidx.databinding.adapters.TextViewBindingAdapter;
import androidx.lifecycle.MutableLiveData;
import com.origamilabs.orii.R;
import com.origamilabs.orii.ui.SharedViewModel;

/* loaded from: classes2.dex */
public class HomeFragmentBindingImpl extends HomeFragmentBinding {
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
        sViewsWithIds.put(R.id.batteryLevelImageView, 12);
        sViewsWithIds.put(R.id.soundTestButton, 13);
        sViewsWithIds.put(R.id.stop_searching_button, 14);
    }

    public HomeFragmentBindingImpl(DataBindingComponent dataBindingComponent, View view) {
        this(dataBindingComponent, view, mapBindings(dataBindingComponent, view, 15, sIncludes, sViewsWithIds));
    }

    private HomeFragmentBindingImpl(DataBindingComponent dataBindingComponent, View view, Object[] objArr) {
        super(dataBindingComponent, view, 2, (ImageView) objArr[12], (ConstraintLayout) objArr[1], (ConstraintLayout) objArr[2], (TextView) objArr[11], (ConstraintLayout) objArr[4], (ImageView) objArr[10], (TextView) objArr[7], (ConstraintLayout) objArr[0], (Button) objArr[13], (TextView) objArr[5], (Button) objArr[14], (TextView) objArr[6], (Button) objArr[8], (Button) objArr[3], (TextView) objArr[9]);
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
            if (13 != i) {
                return false;
            }
            setCanFirmwareUpdate((Boolean) obj);
        }
        return true;
    }

    @Override // com.origamilabs.orii.databinding.HomeFragmentBinding
    public void setSharedViewModel(SharedViewModel sharedViewModel) {
        this.mSharedViewModel = sharedViewModel;
        synchronized (this) {
            this.mDirtyFlags |= 4;
        }
        notifyPropertyChanged(11);
        super.requestRebind();
    }

    @Override // com.origamilabs.orii.databinding.HomeFragmentBinding
    public void setCanFirmwareUpdate(Boolean bool) {
        this.mCanFirmwareUpdate = bool;
        synchronized (this) {
            this.mDirtyFlags |= 8;
        }
        notifyPropertyChanged(13);
        super.requestRebind();
    }

    @Override // androidx.databinding.ViewDataBinding
    protected boolean onFieldChange(int i, Object obj, int i2) {
        if (i == 0) {
            return onChangeSharedViewModelCurrentState((MutableLiveData) obj, i2);
        }
        if (i != 1) {
            return false;
        }
        return onChangeSharedViewModelCountDownNumber((MutableLiveData) obj, i2);
    }

    private boolean onChangeSharedViewModelCurrentState(MutableLiveData<SharedViewModel.State> mutableLiveData, int i) {
        if (i != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 1;
        }
        return true;
    }

    private boolean onChangeSharedViewModelCountDownNumber(MutableLiveData<Integer> mutableLiveData, int i) {
        if (i != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 2;
        }
        return true;
    }

    @Override // androidx.databinding.ViewDataBinding
    protected void executeBindings() {
        long j;
        String str;
        SharedViewModel.State state;
        int i;
        int i2;
        boolean z;
        String str2;
        boolean z2;
        long j2;
        boolean z3;
        long j3;
        int i3;
        synchronized (this) {
            j = this.mDirtyFlags;
            this.mDirtyFlags = 0L;
        }
        SharedViewModel sharedViewModel = this.mSharedViewModel;
        Boolean bool = this.mCanFirmwareUpdate;
        if ((23 & j) != 0) {
            long j4 = j & 21;
            if (j4 != 0) {
                MutableLiveData<SharedViewModel.State> currentState = sharedViewModel != null ? sharedViewModel.getCurrentState() : null;
                updateLiveDataRegistration(0, currentState);
                state = currentState != null ? currentState.getValue() : null;
                z = state == SharedViewModel.State.CONNECTING;
                boolean z4 = state == SharedViewModel.State.CONNECTED;
                boolean z5 = state == SharedViewModel.State.TIMEOUT;
                if (j4 != 0) {
                    j = z ? j | PlaybackStateCompat.ACTION_PREPARE : j | PlaybackStateCompat.ACTION_PLAY_FROM_URI;
                }
                if ((j & 21) != 0) {
                    j |= z4 ? PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM : PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH;
                }
                if ((j & 21) != 0) {
                    j |= z5 ? 64L : 32L;
                }
                i = z4 ? 0 : 8;
                i2 = z5 ? 0 : 8;
            } else {
                state = null;
                i = 0;
                i2 = 0;
                z = false;
            }
            if ((j & 22) != 0) {
                MutableLiveData<Integer> countDownNumber = sharedViewModel != null ? sharedViewModel.getCountDownNumber() : null;
                updateLiveDataRegistration(1, countDownNumber);
                str = (this.statusTextView.getResources().getString(R.string.home_connection_state_connecting) + (countDownNumber != null ? countDownNumber.getValue() : null)) + 's';
            } else {
                str = null;
            }
        } else {
            str = null;
            state = null;
            i = 0;
            i2 = 0;
            z = false;
        }
        long j5 = j & 24;
        if (j5 != 0) {
            boolean safeUnbox = ViewDataBinding.safeUnbox(bool);
            if (j5 != 0) {
                j |= safeUnbox ? PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID : 512L;
            }
            z2 = safeUnbox;
            str2 = this.updateButton.getResources().getString(safeUnbox ? R.string.home_update_available : R.string.home_update_up_to_date);
        } else {
            str2 = null;
            z2 = false;
        }
        if ((PlaybackStateCompat.ACTION_PLAY_FROM_URI & j) != 0) {
            z3 = state == SharedViewModel.State.SCANNING;
            j2 = 21;
        } else {
            j2 = 21;
            z3 = false;
        }
        long j6 = j & j2;
        if (j6 != 0) {
            if (z) {
                z3 = true;
            }
            if (j6 != 0) {
                j |= z3 ? 256L : 128L;
            }
            i3 = z3 ? 0 : 8;
            j3 = 21;
        } else {
            j3 = 21;
            i3 = 0;
        }
        if ((j3 & j) != 0) {
            this.connectFailLayout.setVisibility(i2);
            this.connectedLayout.setVisibility(i);
            this.connectingLayout.setVisibility(i3);
        }
        if ((22 & j) != 0) {
            TextViewBindingAdapter.setText(this.statusTextView, str);
        }
        if ((j & 24) != 0) {
            this.updateButton.setEnabled(z2);
            TextViewBindingAdapter.setText(this.updateButton, str2);
        }
    }
}
