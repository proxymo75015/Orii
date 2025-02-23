package com.origamilabs.orii.databinding;

import android.util.SparseIntArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingComponent;
import androidx.databinding.ViewDataBinding;
import com.origamilabs.orii.R;
import com.origamilabs.orii.ui.tutorial.TutorialViewPager;

/* loaded from: classes2.dex */
public class ActivityTutorialBindingImpl extends ActivityTutorialBinding {
    private static final ViewDataBinding.IncludedLayouts sIncludes = null;
    private static final SparseIntArray sViewsWithIds = new SparseIntArray();
    private long mDirtyFlags;

    @Override // androidx.databinding.ViewDataBinding
    protected boolean onFieldChange(int i, Object obj, int i2) {
        return false;
    }

    static {
        sViewsWithIds.put(R.id.container, 22);
        sViewsWithIds.put(R.id.mask_background_image_view, 23);
        sViewsWithIds.put(R.id.tutorial_mask_menu_text_view, 24);
        sViewsWithIds.put(R.id.tutorial_mask_menu_line_view, 25);
        sViewsWithIds.put(R.id.tutorial_mask_back_text_view, 26);
        sViewsWithIds.put(R.id.tutorial_mask_back_line_view, 27);
        sViewsWithIds.put(R.id.tutorial_mask_next_text_view, 28);
        sViewsWithIds.put(R.id.tutorial_mask_next_line_view, 29);
    }

    public ActivityTutorialBindingImpl(DataBindingComponent dataBindingComponent, View view) {
        this(dataBindingComponent, view, mapBindings(dataBindingComponent, view, 30, sIncludes, sViewsWithIds));
    }

    private ActivityTutorialBindingImpl(DataBindingComponent dataBindingComponent, View view, Object[] objArr) {
        super(dataBindingComponent, view, 0, (TextView) objArr[3], (ImageView) objArr[5], (ImageView) objArr[15], (ImageView) objArr[16], (ImageView) objArr[17], (ImageView) objArr[18], (ImageView) objArr[19], (ImageView) objArr[20], (ImageView) objArr[6], (ImageView) objArr[7], (ImageView) objArr[8], (ImageView) objArr[9], (ImageView) objArr[10], (ImageView) objArr[11], (ImageView) objArr[12], (ImageView) objArr[13], (ImageView) objArr[14], (TutorialViewPager) objArr[22], (ImageView) objArr[2], (ConstraintLayout) objArr[0], (ImageView) objArr[23], (TextView) objArr[1], (TextView) objArr[4], (View) objArr[27], (TextView) objArr[26], (ConstraintLayout) objArr[21], (View) objArr[25], (TextView) objArr[24], (View) objArr[29], (TextView) objArr[28]);
        this.mDirtyFlags = -1L;
        this.backTextView.setTag(null);
        this.circle0ImageView.setTag(null);
        this.circle10ImageView.setTag(null);
        this.circle11ImageView.setTag(null);
        this.circle12ImageView.setTag(null);
        this.circle13ImageView.setTag(null);
        this.circle14ImageView.setTag(null);
        this.circle15ImageView.setTag(null);
        this.circle1ImageView.setTag(null);
        this.circle2ImageView.setTag(null);
        this.circle3ImageView.setTag(null);
        this.circle4ImageView.setTag(null);
        this.circle5ImageView.setTag(null);
        this.circle6ImageView.setTag(null);
        this.circle7ImageView.setTag(null);
        this.circle8ImageView.setTag(null);
        this.circle9ImageView.setTag(null);
        this.helpImageView.setTag(null);
        this.mainContent.setTag(null);
        this.menuTextView.setTag(null);
        this.nextTextView.setTag(null);
        this.tutorialMaskLayout.setTag(null);
        setRootTag(view);
        invalidateAll();
    }

    @Override // androidx.databinding.ViewDataBinding
    public void invalidateAll() {
        synchronized (this) {
            this.mDirtyFlags = 64L;
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
        if (14 == i) {
            setShowNextButton((Boolean) obj);
        } else if (10 == i) {
            setShowBackButton((Boolean) obj);
        } else if (15 == i) {
            setShowCircleView((Boolean) obj);
        } else if (8 == i) {
            setShowMaskView((Boolean) obj);
        } else if (3 == i) {
            setShowMenuButton((Boolean) obj);
        } else {
            if (4 != i) {
                return false;
            }
            setShowHelpButton((Boolean) obj);
        }
        return true;
    }

    @Override // com.origamilabs.orii.databinding.ActivityTutorialBinding
    public void setShowNextButton(Boolean bool) {
        this.mShowNextButton = bool;
        synchronized (this) {
            this.mDirtyFlags |= 1;
        }
        notifyPropertyChanged(14);
        super.requestRebind();
    }

    @Override // com.origamilabs.orii.databinding.ActivityTutorialBinding
    public void setShowBackButton(Boolean bool) {
        this.mShowBackButton = bool;
        synchronized (this) {
            this.mDirtyFlags |= 2;
        }
        notifyPropertyChanged(10);
        super.requestRebind();
    }

    @Override // com.origamilabs.orii.databinding.ActivityTutorialBinding
    public void setShowCircleView(Boolean bool) {
        this.mShowCircleView = bool;
        synchronized (this) {
            this.mDirtyFlags |= 4;
        }
        notifyPropertyChanged(15);
        super.requestRebind();
    }

    @Override // com.origamilabs.orii.databinding.ActivityTutorialBinding
    public void setShowMaskView(Boolean bool) {
        this.mShowMaskView = bool;
        synchronized (this) {
            this.mDirtyFlags |= 8;
        }
        notifyPropertyChanged(8);
        super.requestRebind();
    }

    @Override // com.origamilabs.orii.databinding.ActivityTutorialBinding
    public void setShowMenuButton(Boolean bool) {
        this.mShowMenuButton = bool;
        synchronized (this) {
            this.mDirtyFlags |= 16;
        }
        notifyPropertyChanged(3);
        super.requestRebind();
    }

    @Override // com.origamilabs.orii.databinding.ActivityTutorialBinding
    public void setShowHelpButton(Boolean bool) {
        this.mShowHelpButton = bool;
        synchronized (this) {
            this.mDirtyFlags |= 32;
        }
        notifyPropertyChanged(4);
        super.requestRebind();
    }

    /* JADX WARN: Removed duplicated region for block: B:18:0x003e  */
    /* JADX WARN: Removed duplicated region for block: B:28:0x005d  */
    /* JADX WARN: Removed duplicated region for block: B:38:0x007a  */
    /* JADX WARN: Removed duplicated region for block: B:48:0x0097  */
    /* JADX WARN: Removed duplicated region for block: B:58:0x00b4  */
    /* JADX WARN: Removed duplicated region for block: B:68:0x00cf  */
    /* JADX WARN: Removed duplicated region for block: B:71:0x00da  */
    /* JADX WARN: Removed duplicated region for block: B:74:0x0130  */
    /* JADX WARN: Removed duplicated region for block: B:77:0x013b  */
    /* JADX WARN: Removed duplicated region for block: B:80:0x0146  */
    /* JADX WARN: Removed duplicated region for block: B:83:0x0151  */
    /* JADX WARN: Removed duplicated region for block: B:86:? A[RETURN, SYNTHETIC] */
    @Override // androidx.databinding.ViewDataBinding
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    protected void executeBindings() {
        /*
            Method dump skipped, instructions count: 346
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.origamilabs.orii.databinding.ActivityTutorialBindingImpl.executeBindings():void");
    }
}
