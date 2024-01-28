package com.barikoi.cnlapp.utils;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.barikoi.cnlapp.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;


public class CollapseBehavior<V extends ViewGroup> extends CoordinatorLayout.Behavior<V>{


    public CollapseBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull V child, @NonNull View dependency) {
        return dependency.getId() != R.id.fab;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, V child, View dependency) {
        Log.d("customMapBehaviour",""+dependency.getTop());
        final int actualPeek = (int) (((parent.getHeight() * 1.0) / (16.0)) * 7.0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if(dependency.getTop()==0)
                dependency.setNestedScrollingEnabled(true);
            else dependency.setNestedScrollingEnabled(false);
        }


        if(dependency.getTop()>=actualPeek){

            float height=parent.getHeight();
            child.setTranslationY(-(height-dependency.getTop())/2);
            return true;
        }
        else return false;
    }



    private static boolean isBottomSheet(@NonNull View view) {
        final ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp instanceof CoordinatorLayout.LayoutParams) {
            return ((CoordinatorLayout.LayoutParams) lp)
                    .getBehavior() instanceof BottomSheetBehavior;
        }
        return false;
    }
}