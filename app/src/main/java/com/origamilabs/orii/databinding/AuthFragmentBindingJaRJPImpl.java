package com.origamilabs.orii.databinding;

import android.util.SparseIntArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingComponent;
import androidx.databinding.ViewDataBinding;
import com.origamilabs.orii.R;

/* loaded from: classes2.dex */
public class AuthFragmentBindingJaRJPImpl extends AuthFragmentBinding {
    private static final ViewDataBinding.IncludedLayouts sIncludes = null;
    private static final SparseIntArray sViewsWithIds = new SparseIntArray();
    private long mDirtyFlags;

    @Override // androidx.databinding.ViewDataBinding
    protected boolean onFieldChange(int i, Object obj, int i2) {
        return false;
    }

    static {
        sViewsWithIds.put(R.id.imageView7, 6);
        sViewsWithIds.put(R.id.textView18, 7);
        sViewsWithIds.put(R.id.separator_line, 8);
        sViewsWithIds.put(R.id.dont_have_ac_text_view, 9);
    }

    public AuthFragmentBindingJaRJPImpl(DataBindingComponent dataBindingComponent, View view) {
        this(dataBindingComponent, view, mapBindings(dataBindingComponent, view, 10, sIncludes, sViewsWithIds));
    }

    private AuthFragmentBindingJaRJPImpl(DataBindingComponent dataBindingComponent, View view, Object[] objArr) {
        super(dataBindingComponent, view, 0, (ConstraintLayout) objArr[0], (TextView) objArr[9], (Button) objArr[3], (Button) objArr[1], (Button) objArr[2], (ImageView) objArr[6], (ProgressBar) objArr[5], (View) objArr[8], (TextView) objArr[4], (TextView) objArr[7]);
        this.mDirtyFlags = -1L;
        this.authMainLayout.setTag(null);
        this.emailLoginButton.setTag(null);
        this.fbLoginButton.setTag(null);
        this.googleLoginButton.setTag(null);
        this.loginProgressBar.setTag(null);
        this.signUpTextView.setTag(null);
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

    @Override // com.origamilabs.orii.databinding.AuthFragmentBinding
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
            this.emailLoginButton.setEnabled(z);
            this.fbLoginButton.setEnabled(z);
            this.googleLoginButton.setEnabled(z);
            this.loginProgressBar.setVisibility(i);
            this.signUpTextView.setEnabled(z);
        }
    }
}
