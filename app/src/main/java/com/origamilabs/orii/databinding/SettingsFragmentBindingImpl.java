package com.origamilabs.orii.databinding;

import android.support.v4.media.session.PlaybackStateCompat;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.databinding.DataBindingComponent;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.MutableLiveData;
import com.origamilabs.orii.R;
import com.origamilabs.orii.ui.main.SharedViewModel;
import com.origamilabs.orii.ui.main.settings.SettingsViewModel;

/* loaded from: classes2.dex */
public class SettingsFragmentBindingImpl extends SettingsFragmentBinding {
    private static final ViewDataBinding.IncludedLayouts sIncludes = null;
    private static final SparseIntArray sViewsWithIds = new SparseIntArray();
    private long mDirtyFlags;
    private final NestedScrollView mboundView0;

    static {
        sViewsWithIds.put(R.id.constraintLayout, 18);
        sViewsWithIds.put(R.id.read_speed_card_view, 19);
        sViewsWithIds.put(R.id.read_speed_title_text_view, 20);
        sViewsWithIds.put(R.id.read_speed_content_text_view, 21);
        sViewsWithIds.put(R.id.imageView3, 22);
        sViewsWithIds.put(R.id.imageView4, 23);
        sViewsWithIds.put(R.id.swap_mic_card_view, 24);
        sViewsWithIds.put(R.id.swap_mic_title_text_view, 25);
        sViewsWithIds.put(R.id.swap_mic_content_text_view, 26);
        sViewsWithIds.put(R.id.imageView5, 27);
        sViewsWithIds.put(R.id.gestures_card_view, 28);
        sViewsWithIds.put(R.id.gestures_title_text_view, 29);
        sViewsWithIds.put(R.id.gestures_content_text_view, 30);
        sViewsWithIds.put(R.id.gestures_controller_text_view, 31);
        sViewsWithIds.put(R.id.gesture_up_double_tap_constraint_layout, 32);
        sViewsWithIds.put(R.id.gesture_up_double_tap_title_text_view, 33);
        sViewsWithIds.put(R.id.gesture_up_double_tap_content_1_text_view, 34);
        sViewsWithIds.put(R.id.gesture_up_double_tap_content_2_text_view, 35);
        sViewsWithIds.put(R.id.gesture_down_double_tap_constraint_layout, 36);
        sViewsWithIds.put(R.id.gesture_down_double_tap_title_text_view, 37);
        sViewsWithIds.put(R.id.gesture_down_double_tap_content_1_text_view, 38);
        sViewsWithIds.put(R.id.gesture_down_double_tap_content_2_text_view, 39);
        sViewsWithIds.put(R.id.gesture_side_double_tap_constraint_layout, 40);
        sViewsWithIds.put(R.id.gesture_side_double_tap_title_text_view, 41);
        sViewsWithIds.put(R.id.gesture_side_double_tap_content_text_view, 42);
        sViewsWithIds.put(R.id.gesture_flat_double_tap_constraint_layout, 43);
        sViewsWithIds.put(R.id.gesture_flat_double_tap_title_text_view, 44);
        sViewsWithIds.put(R.id.gesture_flat_double_tap_content_text_view, 45);
        sViewsWithIds.put(R.id.gesture_flat_triple_tap_constraint_layout, 46);
        sViewsWithIds.put(R.id.gesture_flat_triple_tap_title_text_view, 47);
        sViewsWithIds.put(R.id.gesture_flat_triple_tap_content_text_view, 48);
        sViewsWithIds.put(R.id.gesture_reverse_double_tap_constraint_layout, 49);
        sViewsWithIds.put(R.id.gesture_reverse_double_tap_title_text_view, 50);
        sViewsWithIds.put(R.id.gesture_reverse_double_tap_content_text_view, 51);
        sViewsWithIds.put(R.id.gesture_call_control_constraint_layout, 52);
        sViewsWithIds.put(R.id.gesture_call_control_title_text_view, 53);
        sViewsWithIds.put(R.id.gesture_call_control_content_text_view, 54);
        sViewsWithIds.put(R.id.gesture_reserved_constraint_layout, 55);
        sViewsWithIds.put(R.id.gesture_reserved_title_text_view, 56);
        sViewsWithIds.put(R.id.gesture_reserved_content_text_view, 57);
        sViewsWithIds.put(R.id.gesture_tap_strength_card_view, 58);
        sViewsWithIds.put(R.id.gesture_tap_strength_title_text_view, 59);
        sViewsWithIds.put(R.id.gesture_tap_strength_content_text_view, 60);
        sViewsWithIds.put(R.id.gesture_tap_strength_weak_text_view, 61);
        sViewsWithIds.put(R.id.gesture_tap_strength_strong_text_view, 62);
    }

    public SettingsFragmentBindingImpl(DataBindingComponent dataBindingComponent, View view) {
        this(dataBindingComponent, view, mapBindings(dataBindingComponent, view, 63, sIncludes, sViewsWithIds));
    }

    private SettingsFragmentBindingImpl(DataBindingComponent dataBindingComponent, View view, Object[] objArr) {
        super(dataBindingComponent, view, 12, (ConstraintLayout) objArr[18], (ConstraintLayout) objArr[52], (TextView) objArr[54], (SwitchCompat) objArr[15], (TextView) objArr[53], (ConstraintLayout) objArr[36], (TextView) objArr[38], (TextView) objArr[39], (SwitchCompat) objArr[6], (TextView) objArr[37], (ConstraintLayout) objArr[43], (TextView) objArr[45], (SwitchCompat) objArr[8], (TextView) objArr[44], (ConstraintLayout) objArr[46], (TextView) objArr[48], (ImageView) objArr[9], (SwitchCompat) objArr[10], (TextView) objArr[47], (TextView) objArr[11], (ConstraintLayout) objArr[55], (TextView) objArr[57], (SwitchCompat) objArr[16], (TextView) objArr[56], (ConstraintLayout) objArr[49], (TextView) objArr[51], (ImageView) objArr[12], (SwitchCompat) objArr[13], (TextView) objArr[50], (TextView) objArr[14], (ConstraintLayout) objArr[40], (TextView) objArr[42], (SwitchCompat) objArr[7], (TextView) objArr[41], (CardView) objArr[58], (TextView) objArr[60], (SeekBar) objArr[17], (TextView) objArr[62], (TextView) objArr[59], (TextView) objArr[61], (ConstraintLayout) objArr[32], (TextView) objArr[34], (TextView) objArr[35], (SwitchCompat) objArr[5], (TextView) objArr[33], (CardView) objArr[28], (TextView) objArr[30], (AppCompatCheckBox) objArr[4], (TextView) objArr[31], (TextView) objArr[29], (TextView) objArr[22], (TextView) objArr[23], (ImageView) objArr[27], (ImageView) objArr[2], (ImageView) objArr[3], (CardView) objArr[19], (TextView) objArr[21], (TextView) objArr[20], (SeekBar) objArr[1], (CardView) objArr[24], (TextView) objArr[26], (TextView) objArr[25]);
        this.mDirtyFlags = -1L;
        this.gestureCallControlSwitch.setTag(null);
        this.gestureDownDoubleTapSwitch.setTag(null);
        this.gestureFlatDoubleTapSwitch.setTag(null);
        this.gestureFlatTripleTapQuestionMarkImageView.setTag(null);
        this.gestureFlatTripleTapSwitch.setTag(null);
        this.gestureFlatTripleTapWebHookUrlTextView.setTag(null);
        this.gestureReservedSwitch.setTag(null);
        this.gestureReverseDoubleTapQuestionMarkImageView.setTag(null);
        this.gestureReverseDoubleTapSwitch.setTag(null);
        this.gestureReverseDoubleTapWebHookUrlTextView.setTag(null);
        this.gestureSideDoubleTapSwitch.setTag(null);
        this.gestureTapStrengthSeekBar.setTag(null);
        this.gestureUpDoubleTapSwitch.setTag(null);
        this.gesturesControllerCheckBox.setTag(null);
        this.mboundView0 = (NestedScrollView) objArr[0];
        this.mboundView0.setTag(null);
        this.micModeLeftImageView.setTag(null);
        this.micModeRightImageView.setTag(null);
        this.readoutSpeedSeekBar.setTag(null);
        setRootTag(view);
        invalidateAll();
    }

    @Override // androidx.databinding.ViewDataBinding
    public void invalidateAll() {
        synchronized (this) {
            this.mDirtyFlags = PlaybackStateCompat.ACTION_PREPARE;
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
        if (7 == i) {
            setSettingsViewModel((SettingsViewModel) obj);
        } else {
            if (11 != i) {
                return false;
            }
            setSharedViewModel((SharedViewModel) obj);
        }
        return true;
    }

    @Override // com.origamilabs.orii.databinding.SettingsFragmentBinding
    public void setSettingsViewModel(SettingsViewModel settingsViewModel) {
        this.mSettingsViewModel = settingsViewModel;
        synchronized (this) {
            this.mDirtyFlags |= PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM;
        }
        notifyPropertyChanged(7);
        super.requestRebind();
    }

    @Override // com.origamilabs.orii.databinding.SettingsFragmentBinding
    public void setSharedViewModel(SharedViewModel sharedViewModel) {
        this.mSharedViewModel = sharedViewModel;
        synchronized (this) {
            this.mDirtyFlags |= PlaybackStateCompat.ACTION_PLAY_FROM_URI;
        }
        notifyPropertyChanged(11);
        super.requestRebind();
    }

    @Override // androidx.databinding.ViewDataBinding
    protected boolean onFieldChange(int i, Object obj, int i2) {
        switch (i) {
            case 0:
                return onChangeSettingsViewModelCallControlMode((MutableLiveData) obj, i2);
            case 1:
                return onChangeSettingsViewModelUpDoubleTapMode((MutableLiveData) obj, i2);
            case 2:
                return onChangeSettingsViewModelDownDoubleTapMode((MutableLiveData) obj, i2);
            case 3:
                return onChangeSettingsViewModelReverseDoubleTapMode((MutableLiveData) obj, i2);
            case 4:
                return onChangeSettingsViewModelSensitivity((MutableLiveData) obj, i2);
            case 5:
                return onChangeSettingsViewModelShowFlatTripleTapWebHook((MutableLiveData) obj, i2);
            case 6:
                return onChangeSharedViewModelCurrentState((MutableLiveData) obj, i2);
            case 7:
                return onChangeSettingsViewModelFlatDoubleTapMode((MutableLiveData) obj, i2);
            case 8:
                return onChangeSettingsViewModelSideDoubleTapMode((MutableLiveData) obj, i2);
            case 9:
                return onChangeSettingsViewModelFlatThreeTapMode((MutableLiveData) obj, i2);
            case 10:
                return onChangeSettingsViewModelShowReverseDoubleTapWebHook((MutableLiveData) obj, i2);
            case 11:
                return onChangeSettingsViewModelMicMode((MutableLiveData) obj, i2);
            default:
                return false;
        }
    }

    private boolean onChangeSettingsViewModelCallControlMode(MutableLiveData<Boolean> mutableLiveData, int i) {
        if (i != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 1;
        }
        return true;
    }

    private boolean onChangeSettingsViewModelUpDoubleTapMode(MutableLiveData<Boolean> mutableLiveData, int i) {
        if (i != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 2;
        }
        return true;
    }

    private boolean onChangeSettingsViewModelDownDoubleTapMode(MutableLiveData<Boolean> mutableLiveData, int i) {
        if (i != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 4;
        }
        return true;
    }

    private boolean onChangeSettingsViewModelReverseDoubleTapMode(MutableLiveData<Boolean> mutableLiveData, int i) {
        if (i != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 8;
        }
        return true;
    }

    private boolean onChangeSettingsViewModelSensitivity(MutableLiveData<Integer> mutableLiveData, int i) {
        if (i != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 16;
        }
        return true;
    }

    private boolean onChangeSettingsViewModelShowFlatTripleTapWebHook(MutableLiveData<Boolean> mutableLiveData, int i) {
        if (i != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 32;
        }
        return true;
    }

    private boolean onChangeSharedViewModelCurrentState(MutableLiveData<SharedViewModel.State> mutableLiveData, int i) {
        if (i != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 64;
        }
        return true;
    }

    private boolean onChangeSettingsViewModelFlatDoubleTapMode(MutableLiveData<Boolean> mutableLiveData, int i) {
        if (i != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 128;
        }
        return true;
    }

    private boolean onChangeSettingsViewModelSideDoubleTapMode(MutableLiveData<Boolean> mutableLiveData, int i) {
        if (i != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 256;
        }
        return true;
    }

    private boolean onChangeSettingsViewModelFlatThreeTapMode(MutableLiveData<Boolean> mutableLiveData, int i) {
        if (i != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 512;
        }
        return true;
    }

    private boolean onChangeSettingsViewModelShowReverseDoubleTapWebHook(MutableLiveData<Boolean> mutableLiveData, int i) {
        if (i != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID;
        }
        return true;
    }

    private boolean onChangeSettingsViewModelMicMode(MutableLiveData<Boolean> mutableLiveData, int i) {
        if (i != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH;
        }
        return true;
    }

    /* JADX WARN: Removed duplicated region for block: B:108:0x01df  */
    /* JADX WARN: Removed duplicated region for block: B:189:0x0233  */
    /* JADX WARN: Removed duplicated region for block: B:194:0x01d1  */
    /* JADX WARN: Removed duplicated region for block: B:197:0x018c  */
    /* JADX WARN: Removed duplicated region for block: B:200:0x0163  */
    /* JADX WARN: Removed duplicated region for block: B:203:0x013e  */
    /* JADX WARN: Removed duplicated region for block: B:67:0x0121  */
    /* JADX WARN: Removed duplicated region for block: B:75:0x0147  */
    /* JADX WARN: Removed duplicated region for block: B:83:0x016c  */
    /* JADX WARN: Removed duplicated region for block: B:91:0x0197  */
    @Override // androidx.databinding.ViewDataBinding
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    protected void executeBindings() {
        /*
            Method dump skipped, instructions count: 869
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.origamilabs.orii.databinding.SettingsFragmentBindingImpl.executeBindings():void");
    }
}
