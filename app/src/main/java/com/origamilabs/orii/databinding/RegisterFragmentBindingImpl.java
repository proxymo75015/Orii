package com.origamilabs.orii.databinding;

import android.util.SparseIntArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingComponent;
import androidx.databinding.ViewDataBinding;
import com.google.android.material.textfield.TextInputLayout;
import com.origamilabs.orii.R;

/* loaded from: classes2.dex */
public class RegisterFragmentBindingImpl extends RegisterFragmentBinding {
    private static final ViewDataBinding.IncludedLayouts sIncludes = null;
    private static final SparseIntArray sViewsWithIds = new SparseIntArray();
    private long mDirtyFlags;

    @Override // androidx.databinding.ViewDataBinding
    protected boolean onFieldChange(int i, Object obj, int i2) {
        return false;
    }

    static {
        sViewsWithIds.put(R.id.textView19, 5);
        sViewsWithIds.put(R.id.email_input_layout, 6);
        sViewsWithIds.put(R.id.email_input, 7);
        sViewsWithIds.put(R.id.password_input_layout, 8);
        sViewsWithIds.put(R.id.password_input, 9);
        sViewsWithIds.put(R.id.confirm_password_input_layout, 10);
        sViewsWithIds.put(R.id.confirm_password_input, 11);
    }

    public RegisterFragmentBindingImpl(DataBindingComponent dataBindingComponent, View view) {
        this(dataBindingComponent, view, mapBindings(dataBindingComponent, view, 12, sIncludes, sViewsWithIds));
    }

    private RegisterFragmentBindingImpl(DataBindingComponent dataBindingComponent, View view, Object[] objArr) {
        super(dataBindingComponent, view, 0, (Button) objArr[2], (EditText) objArr[11], (TextInputLayout) objArr[10], (EditText) objArr[7], (TextInputLayout) objArr[6], (ImageView) objArr[3], (ProgressBar) objArr[4], (ConstraintLayout) objArr[0], (EditText) objArr[9], (TextInputLayout) objArr[8], (Button) objArr[1], (TextView) objArr[5]);
        this.mDirtyFlags = -1L;
        this.backButton.setTag(null);
        this.emailSignUpBackgroundImageView.setTag(null);
        this.emailSignUpProgressBar.setTag(null);
        this.login.setTag(null);
        this.signUpButton.setTag(null);
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
        if (2 != i) {
            return false;
        }
        setIsSignUpProgress((Boolean) obj);
        return true;
    }

    @Override // com.origamilabs.orii.databinding.RegisterFragmentBinding
    public void setIsSignUpProgress(Boolean bool) {
        this.mIsSignUpProgress = bool;
        synchronized (this) {
            this.mDirtyFlags |= 1;
        }
        notifyPropertyChanged(2);
        super.requestRebind();
    }

    @Override // androidx.databinding.ViewDataBinding
    protected void executeBindings() {
        long j;
        boolean z;
        long j2;
        long j3;
        synchronized (this) {
            j = this.mDirtyFlags;
            this.mDirtyFlags = 0L;
        }
        Boolean bool = this.mIsSignUpProgress;
        long j4 = j & 3;
        int i = 0;
        if (j4 != 0) {
            boolean safeUnbox = ViewDataBinding.safeUnbox(bool);
            if (j4 != 0) {
                if (safeUnbox) {
                    j2 = j | 8;
                    j3 = 32;
                } else {
                    j2 = j | 4;
                    j3 = 16;
                }
                j = j2 | j3;
            }
            z = !safeUnbox;
            if (!safeUnbox) {
                i = 4;
            }
        } else {
            z = false;
        }
        if ((j & 3) != 0) {
            this.backButton.setEnabled(z);
            this.emailSignUpBackgroundImageView.setVisibility(i);
            this.emailSignUpProgressBar.setVisibility(i);
            this.signUpButton.setEnabled(z);
        }
    }
}
