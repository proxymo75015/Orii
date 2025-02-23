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
public class LoginFragmentBindingImpl extends LoginFragmentBinding {
    private static final ViewDataBinding.IncludedLayouts sIncludes = null;
    private static final SparseIntArray sViewsWithIds = new SparseIntArray();
    private long mDirtyFlags;
    private final ConstraintLayout mboundView0;

    @Override // androidx.databinding.ViewDataBinding
    protected boolean onFieldChange(int i, Object obj, int i2) {
        return false;
    }

    static {
        sViewsWithIds.put(R.id.sign_in_with_email_text_view, 6);
        sViewsWithIds.put(R.id.email_input_layout, 7);
        sViewsWithIds.put(R.id.email_input, 8);
        sViewsWithIds.put(R.id.password_input_layout, 9);
        sViewsWithIds.put(R.id.password_input, 10);
    }

    public LoginFragmentBindingImpl(DataBindingComponent dataBindingComponent, View view) {
        this(dataBindingComponent, view, mapBindings(dataBindingComponent, view, 11, sIncludes, sViewsWithIds));
    }

    private LoginFragmentBindingImpl(DataBindingComponent dataBindingComponent, View view, Object[] objArr) {
        super(dataBindingComponent, view, 0, (Button) objArr[3], (EditText) objArr[8], (TextInputLayout) objArr[7], (ImageView) objArr[4], (ProgressBar) objArr[5], (Button) objArr[1], (EditText) objArr[10], (TextInputLayout) objArr[9], (Button) objArr[2], (TextView) objArr[6]);
        this.mDirtyFlags = -1L;
        this.backButton.setTag(null);
        this.emailLoginBackgroundImageView.setTag(null);
        this.emailLoginProgressBar.setTag(null);
        this.forgotPasswordButton.setTag(null);
        this.mboundView0 = (ConstraintLayout) objArr[0];
        this.mboundView0.setTag(null);
        this.signInButton.setTag(null);
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
        if (6 != i) {
            return false;
        }
        setIsLoginProgress((Boolean) obj);
        return true;
    }

    @Override // com.origamilabs.orii.databinding.LoginFragmentBinding
    public void setIsLoginProgress(Boolean bool) {
        this.mIsLoginProgress = bool;
        synchronized (this) {
            this.mDirtyFlags |= 1;
        }
        notifyPropertyChanged(6);
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
        Boolean bool = this.mIsLoginProgress;
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
            this.emailLoginBackgroundImageView.setVisibility(i);
            this.emailLoginProgressBar.setVisibility(i);
            this.forgotPasswordButton.setEnabled(z);
            this.signInButton.setEnabled(z);
        }
    }
}
