package com.origamilabs.orii.databinding;

import android.graphics.drawable.Drawable;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingComponent;
import androidx.databinding.ViewDataBinding;
import androidx.databinding.adapters.ImageViewBindingAdapter;
import com.facebook.drawee.view.SimpleDraweeView;
import com.origamilabs.orii.R;
import hiennguyen.me.circleseekbar.CircleSeekBar;

/* loaded from: classes2.dex */
public class UsingOriiFragmentBindingImpl extends UsingOriiFragmentBinding {
    private static final ViewDataBinding.IncludedLayouts sIncludes = null;
    private static final SparseIntArray sViewsWithIds = new SparseIntArray();
    private long mDirtyFlags;

    @Override // androidx.databinding.ViewDataBinding
    protected boolean onFieldChange(int i, Object obj, int i2) {
        return false;
    }

    static {
        sViewsWithIds.put(R.id.gifView, 2);
        sViewsWithIds.put(R.id.progressBar, 3);
        sViewsWithIds.put(R.id.using_orii_title_text_view, 4);
        sViewsWithIds.put(R.id.turn_on_content_text_view, 5);
        sViewsWithIds.put(R.id.next_text_button, 6);
        sViewsWithIds.put(R.id.add_image_view, 7);
        sViewsWithIds.put(R.id.textView25, 8);
        sViewsWithIds.put(R.id.minus_image_view, 9);
    }

    public UsingOriiFragmentBindingImpl(DataBindingComponent dataBindingComponent, View view) {
        this(dataBindingComponent, view, mapBindings(dataBindingComponent, view, 10, sIncludes, sViewsWithIds));
    }

    private UsingOriiFragmentBindingImpl(DataBindingComponent dataBindingComponent, View view, Object[] objArr) {
        super(dataBindingComponent, view, 0, (ImageView) objArr[7], (ConstraintLayout) objArr[0], (SimpleDraweeView) objArr[2], (ImageView) objArr[9], (Button) objArr[6], (ImageView) objArr[1], (CircleSeekBar) objArr[3], (TextView) objArr[8], (TextView) objArr[5], (TextView) objArr[4]);
        this.mDirtyFlags = -1L;
        this.constraintLayout.setTag(null);
        this.playPauseImageView.setTag(null);
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
        if (1 != i) {
            return false;
        }
        setIsPlaying((Boolean) obj);
        return true;
    }

    @Override // com.origamilabs.orii.databinding.UsingOriiFragmentBinding
    public void setIsPlaying(Boolean bool) {
        this.mIsPlaying = bool;
        synchronized (this) {
            this.mDirtyFlags |= 1;
        }
        notifyPropertyChanged(1);
        super.requestRebind();
    }

    @Override // androidx.databinding.ViewDataBinding
    protected void executeBindings() {
        long j;
        ImageView imageView;
        int i;
        synchronized (this) {
            j = this.mDirtyFlags;
            this.mDirtyFlags = 0L;
        }
        Boolean bool = this.mIsPlaying;
        Drawable drawable = null;
        long j2 = j & 3;
        if (j2 != 0) {
            boolean safeUnbox = ViewDataBinding.safeUnbox(bool);
            if (j2 != 0) {
                j |= safeUnbox ? 8L : 4L;
            }
            if (safeUnbox) {
                imageView = this.playPauseImageView;
                i = R.drawable.pause;
            } else {
                imageView = this.playPauseImageView;
                i = R.drawable.play;
            }
            drawable = getDrawableFromResource(imageView, i);
        }
        if ((j & 3) != 0) {
            ImageViewBindingAdapter.setImageDrawable(this.playPauseImageView, drawable);
        }
    }
}
