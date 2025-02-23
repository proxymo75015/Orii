package com.origamilabs.orii.databinding;

import android.support.v4.media.session.PlaybackStateCompat;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingComponent;
import androidx.databinding.ViewDataBinding;
import androidx.databinding.adapters.TextViewBindingAdapter;
import androidx.lifecycle.MutableLiveData;
import com.budiyev.android.circularprogressbar.CircularProgressBar;
import com.origamilabs.orii.R;
import com.origamilabs.orii.ui.main.home.update.updating.UpdatingViewModel;

/* loaded from: classes2.dex */
public class UpdatingFragmentBindingImpl extends UpdatingFragmentBinding {
    private static final ViewDataBinding.IncludedLayouts sIncludes = null;
    private static final SparseIntArray sViewsWithIds = new SparseIntArray();
    private long mDirtyFlags;

    static {
        sViewsWithIds.put(R.id.help_image_view, 5);
        sViewsWithIds.put(R.id.back_button, 6);
        sViewsWithIds.put(R.id.textView32, 7);
        sViewsWithIds.put(R.id.imageView15, 8);
    }

    public UpdatingFragmentBindingImpl(DataBindingComponent dataBindingComponent, View view) {
        this(dataBindingComponent, view, mapBindings(dataBindingComponent, view, 9, sIncludes, sViewsWithIds));
    }

    private UpdatingFragmentBindingImpl(DataBindingComponent dataBindingComponent, View view, Object[] objArr) {
        super(dataBindingComponent, view, 2, (ImageView) objArr[6], (ImageView) objArr[5], (TextView) objArr[4], (ImageView) objArr[8], (CircularProgressBar) objArr[2], (TextView) objArr[1], (TextView) objArr[3], (TextView) objArr[7], (ConstraintLayout) objArr[0]);
        this.mDirtyFlags = -1L;
        this.hintTextView.setTag(null);
        this.progressBar.setTag(null);
        this.returnTextView.setTag(null);
        this.statusTextView.setTag(null);
        this.updateinfo.setTag(null);
        setRootTag(view);
        invalidateAll();
    }

    @Override // androidx.databinding.ViewDataBinding
    public void invalidateAll() {
        synchronized (this) {
            this.mDirtyFlags = 8L;
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
        if (9 != i) {
            return false;
        }
        setUpdatingViewModel((UpdatingViewModel) obj);
        return true;
    }

    @Override // com.origamilabs.orii.databinding.UpdatingFragmentBinding
    public void setUpdatingViewModel(UpdatingViewModel updatingViewModel) {
        this.mUpdatingViewModel = updatingViewModel;
        synchronized (this) {
            this.mDirtyFlags |= 4;
        }
        notifyPropertyChanged(9);
        super.requestRebind();
    }

    @Override // androidx.databinding.ViewDataBinding
    protected boolean onFieldChange(int i, Object obj, int i2) {
        if (i == 0) {
            return onChangeUpdatingViewModelUpdateFinished((MutableLiveData) obj, i2);
        }
        if (i != 1) {
            return false;
        }
        return onChangeUpdatingViewModelProgress((MutableLiveData) obj, i2);
    }

    private boolean onChangeUpdatingViewModelUpdateFinished(MutableLiveData<Boolean> mutableLiveData, int i) {
        if (i != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 1;
        }
        return true;
    }

    private boolean onChangeUpdatingViewModelProgress(MutableLiveData<Integer> mutableLiveData, int i) {
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
        int i;
        boolean z;
        int i2;
        int i3;
        MutableLiveData<Integer> mutableLiveData;
        Integer num;
        String str;
        boolean z2;
        String str2;
        String str3;
        long j2;
        long j3;
        synchronized (this) {
            j = this.mDirtyFlags;
            this.mDirtyFlags = 0L;
        }
        UpdatingViewModel updatingViewModel = this.mUpdatingViewModel;
        String str4 = null;
        if ((j & 15) != 0) {
            MutableLiveData<Boolean> updateFinished = updatingViewModel != null ? updatingViewModel.getUpdateFinished() : null;
            updateLiveDataRegistration(0, updateFinished);
            z = ViewDataBinding.safeUnbox(updateFinished != null ? updateFinished.getValue() : null);
            if ((j & 13) != 0) {
                if (z) {
                    j2 = j | 32 | 128;
                    j3 = PlaybackStateCompat.ACTION_PLAY_FROM_URI;
                } else {
                    j2 = j | 16 | 64;
                    j3 = PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM;
                }
                j = j2 | j3;
            }
            if ((j & 15) != 0) {
                j = z ? j | PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH : j | PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID;
            }
            if ((j & 13) != 0) {
                str = this.hintTextView.getResources().getString(z ? R.string.update_updating_hint_up_to_date : R.string.update_updating_hint_do_not_touch);
                i3 = z ? 0 : 8;
                i = getColorFromResource(this.progressBar, z ? R.color.colorUpdateSuccess : android.R.color.black);
            } else {
                i = 0;
                i3 = 0;
                str = null;
            }
            if ((j & 14) != 0) {
                mutableLiveData = updatingViewModel != null ? updatingViewModel.getProgress() : null;
                updateLiveDataRegistration(1, mutableLiveData);
                num = mutableLiveData != null ? mutableLiveData.getValue() : null;
                i2 = ViewDataBinding.safeUnbox(num);
            } else {
                i2 = 0;
                mutableLiveData = null;
                num = null;
            }
        } else {
            i = 0;
            z = false;
            i2 = 0;
            i3 = 0;
            mutableLiveData = null;
            num = null;
            str = null;
        }
        long j4 = j & PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID;
        if (j4 != 0) {
            if (updatingViewModel != null) {
                mutableLiveData = updatingViewModel.getProgress();
            }
            updateLiveDataRegistration(1, mutableLiveData);
            if (mutableLiveData != null) {
                num = mutableLiveData.getValue();
            }
            i2 = ViewDataBinding.safeUnbox(num);
            z2 = i2 <= 99;
            if (j4 != 0) {
                j = z2 ? j | 512 : j | 256;
            }
        } else {
            z2 = false;
        }
        if ((j & 512) != 0) {
            str2 = String.format(this.statusTextView.getResources().getString(R.string.update_updating_status_progress), num + "%");
        } else {
            str2 = null;
        }
        if ((PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID & j) != 0) {
            str3 = z2 ? str2 : this.statusTextView.getResources().getString(R.string.update_updating_status_restarting);
        } else {
            str3 = null;
        }
        long j5 = 15 & j;
        if (j5 != 0) {
            if (z) {
                str3 = this.statusTextView.getResources().getString(R.string.update_updating_status_success);
            }
            str4 = str3;
        }
        String str5 = str4;
        if ((13 & j) != 0) {
            TextViewBindingAdapter.setText(this.hintTextView, str);
            this.progressBar.setForegroundStrokeColor(i);
            this.returnTextView.setVisibility(i3);
        }
        if ((j & 14) != 0) {
            this.progressBar.setProgress(i2);
        }
        if (j5 != 0) {
            TextViewBindingAdapter.setText(this.statusTextView, str5);
        }
    }
}
