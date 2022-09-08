package com.rexlite.rexlitebasicnew;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

public class InstantAutoComplate extends AppCompatAutoCompleteTextView {

    public InstantAutoComplate(Context context) {
        super(context);
    }

    public InstantAutoComplate(Context arg0, AttributeSet arg1) {
        super(arg0, arg1);
    }

    public InstantAutoComplate(Context arg0, AttributeSet arg1, int arg2) {
        super(arg0, arg1, arg2);
    }

    @Override
    public boolean enoughToFilter() {
        return true;
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction,
                                  Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused && getAdapter() != null) {
            performFiltering(getText(), 0);
        }
    }

}
